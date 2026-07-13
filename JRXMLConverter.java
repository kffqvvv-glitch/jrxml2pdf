import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JRXMLConverter {

    /**
     * Imageエレメントが存在するか判定
     */
    public static boolean hasImageElement(File jrxml) {

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.parse(jrxml);

            NodeList imageList =
                    document.getElementsByTagNameNS("*", "image");

            if (imageList.getLength() > 0) {

                System.out.println(
                        "Imageエレメントを "
                                + imageList.getLength()
                                + " 件検出しました。"
                );

                return true;

            }

            return false;

        } catch (Exception ex) {

            throw new RuntimeException(ex);

        }

    }

    /**
     * QR版JRXML生成
     */
    public static File convert(File originalFile) {

        try {

            //---------------------------------
            // bkup
            //---------------------------------

            File backupDir =
                    new File(
                            originalFile.getParentFile(),
                            "bkup_sourcejrxml"
                    );

            if (!backupDir.exists()) {

                backupDir.mkdirs();

            }

            //---------------------------------
            // 元JRXML退避
            //---------------------------------

            File backupFile =
                    new File(
                            backupDir,
                            originalFile.getName()
                    );

            Files.move(
                    originalFile.toPath(),
                    backupFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            //---------------------------------
            // QR版ファイル名
            //---------------------------------

            String name =
                    originalFile.getName();

            int pos =
                    name.lastIndexOf(".");

            String qrName;

            if (pos >= 0) {

                qrName =
                        name.substring(0, pos)
                                + "_imageConvertToQR"
                                + name.substring(pos);

            } else {

                qrName =
                        name + "_imageConvertToQR";

            }

            File qrFile =
                    new File(
                            originalFile.getParentFile(),
                            qrName
                    );

            //---------------------------------
            // XML読込
            //---------------------------------

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.parse(backupFile);

            //---------------------------------
            // Field変更
            //---------------------------------

            replaceImageField(document);

            //---------------------------------
            // Image→QRCode
            //---------------------------------

            replaceImageElement(document);

            //---------------------------------
            // 保存
            //---------------------------------

            saveDocument(
                    document,
                    qrFile
            );

            System.out.println(
                    "QR版JRXML作成 : "
                            + qrFile.getName()
            );

            return qrFile;

        } catch (Exception ex) {

            throw new RuntimeException(ex);

        }

    }
    /**
     * java.awt.Image のFieldを
     * java.lang.Stringへ変更する
     */
    private static void replaceImageField(Document document) {

        NodeList fieldList =
                document.getElementsByTagNameNS(
                        "*",
                        "field"
                );

        int count = 0;

        for (int i = 0; i < fieldList.getLength(); i++) {

            Element field =
                    (Element) fieldList.item(i);

            String clazz =
                    field.getAttribute("class");

            if ("java.awt.Image".equals(clazz)) {

                field.setAttribute(
                        "class",
                        "java.lang.String"
                );

                count++;

                System.out.println(
                        "Field変更 : "
                                + field.getAttribute("name")
                                + " (java.awt.Image → java.lang.String)"
                );

            }

        }

        if (count == 0) {

            System.out.println(
                    "java.awt.Image型Fieldはありませんでした。"
            );

        } else {

            System.out.println(
                    count
                            + " 件のFieldを変更しました。"
            );

        }

    }

    /**
     * reportElementをコピーする
     */
    private static Element copyReportElement(
            Document document,
            Element reportElement
    ) {

        Element copy =
                (Element) document.importNode(
                        reportElement,
                        true
                );

        return copy;

    }
    /**
     * QRCodeコンポーネント生成
     */
    private static Element createQRCode(
            Document document,
            Element reportElement,
            String expression
    ) {

        //----------------------------------
        // componentElement
        //----------------------------------

        Element componentElement =
                document.createElement("componentElement");

        //----------------------------------
        // reportElementコピー
        //----------------------------------

        Element reportCopy =
                copyReportElement(
                        document,
                        reportElement
                );

        componentElement.appendChild(
                reportCopy
        );

        //----------------------------------
        // jr:QRCode
        //----------------------------------

        Element qrCode =
                document.createElementNS(
                        "http://jasperreports.sourceforge.net/jasperreports/components",
                        "jr:QRCode"
                );

        //----------------------------------
        // namespace
        //----------------------------------

        qrCode.setAttribute(
                "xmlns:jr",
                "http://jasperreports.sourceforge.net/jasperreports/components"
        );

        qrCode.setAttribute(
                "xmlns:xsi",
                "http://www.w3.org/2001/XMLSchema-instance"
        );

        qrCode.setAttribute(
                "xsi:schemaLocation",
                "http://jasperreports.sourceforge.net/jasperreports/components "
              + "http://jasperreports.sourceforge.net/xsd/components.xsd"
        );

        //----------------------------------
        // QR設定
        //----------------------------------

        qrCode.setAttribute(
                "margin",
                "6"
        );

        qrCode.setAttribute(
                "errorCorrectionLevel",
                "M"
        );

        //----------------------------------
        // codeExpression
        //----------------------------------

        Element codeExpression =
                document.createElementNS(
                        "http://jasperreports.sourceforge.net/jasperreports/components",
                        "jr:codeExpression"
                );

        // ★CDATAではなく通常テキスト
        codeExpression.setTextContent(
                expression
        );

        qrCode.appendChild(
                codeExpression
        );

        //----------------------------------
        // componentElementへ追加
        //----------------------------------

        componentElement.appendChild(
                qrCode
        );

        return componentElement;

    }
    /**
     * ImageをQRCodeへ置換
     */
    private static void replaceImageElement(
            Document document
    ) {

        NodeList imageList =
                document.getElementsByTagNameNS(
                        "*",
                        "image"
                );

        int count = imageList.getLength();

        if (count == 0) {

            System.out.println(
                    "Imageエレメントはありませんでした。"
            );

            return;

        }

        System.out.println(
                "Image→QRCode変換開始（"
                        + count
                        + "件）"
        );

        /*
         * NodeListはライブなので
         * 後ろから処理する
         */
        for (int i = count - 1; i >= 0; i--) {

            Element image =
                    (Element) imageList.item(i);

            //----------------------------------
            // reportElement取得
            //----------------------------------

            Element reportElement =
                    (Element) image
                            .getElementsByTagNameNS(
                                    "*",
                                    "reportElement"
                            )
                            .item(0);

            //----------------------------------
            // imageExpression取得
            //----------------------------------

            Element imageExpression =
                    (Element) image
                            .getElementsByTagNameNS(
                                    "*",
                                    "imageExpression"
                            )
                            .item(0);

            String expression = "";

            if (imageExpression != null) {

                expression =
                        imageExpression
                                .getTextContent()
                                .trim();

            }

            //----------------------------------
            // QRCode生成
            //----------------------------------

            Element qrComponent =
                    createQRCode(
                            document,
                            reportElement,
                            expression
                    );

            //----------------------------------
            // imageの親取得
            //----------------------------------

            Element parent =
                    (Element) image.getParentNode();

            //----------------------------------
            // imageの前へ挿入
            //----------------------------------

            parent.insertBefore(
                    qrComponent,
                    image
            );

            //----------------------------------
            // image削除
            //----------------------------------

            parent.removeChild(
                    image
            );

            System.out.println(
                    "QRCodeへ変換 : "
                            + expression
            );

        }

        System.out.println(
                "Image→QRCode変換完了"
        );

    }
    /**
     * XML保存
     */
    private static void saveDocument(
            Document document,
            File outputFile
    ) {

        try {

            TransformerFactory factory =
                    TransformerFactory.newInstance();

            Transformer transformer =
                    factory.newTransformer();

            //----------------------------------
            // XML出力設定
            //----------------------------------

            transformer.setOutputProperty(
                    OutputKeys.INDENT,
                    "yes"
            );

            transformer.setOutputProperty(
                    OutputKeys.ENCODING,
                    "UTF-8"
            );

            transformer.setOutputProperty(
                    OutputKeys.METHOD,
                    "xml"
            );

            transformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION,
                    "no"
            );

            //----------------------------------
            // 保存
            //----------------------------------

            DOMSource source =
                    new DOMSource(document);

            StreamResult result =
                    new StreamResult(outputFile);

            transformer.transform(
                    source,
                    result
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "JRXML保存失敗",
                    ex
            );

        }

    }

}