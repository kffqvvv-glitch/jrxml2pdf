import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class runReport {

    public static void main(String[] args) throws Exception {

        /* ===== カレントフォルダ ===== */
        File currentDir = new File(".");

        /* ===== jrxml を自動検出 ===== */
        File[] jrxmlFiles = currentDir.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".jrxml")
        );
        if (jrxmlFiles == null || jrxmlFiles.length != 1) {
            throw new IllegalStateException(
                    "jrxml ファイルはフォルダ内に 1 つだけ配置してください。"
            );
        }
        File jrxmlFile = jrxmlFiles[0];

        /* ===== csv を自動検出 ===== */
        File[] csvFiles = currentDir.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".csv")
        );
        if (csvFiles == null || csvFiles.length != 1) {
            throw new IllegalStateException(
                    "csv ファイルはフォルダ内に 1 つだけ配置してください。"
            );
        }
        File csvFile = csvFiles[0];

        /* ===== 出力 PDF 名（csv に倣う） ===== */
        String csvName = csvFile.getName();
        String baseName = csvName.replaceFirst("\\.csv$", "");
        String outputPdf = baseName + ".pdf";

        System.out.println("JRXML : " + jrxmlFile.getName());
        System.out.println("CSV   : " + csvFile.getName());
        System.out.println("PDF   : " + outputPdf);

        /* ===== Imageエレメントの確認 ===== */
        boolean useQRCode = false;

        if (JRXMLConverter.hasImageElement(jrxmlFile)) {

            Scanner scanner = new Scanner(System.in);

            System.out.println();
            System.out.println("Imageエレメントを検出しました。");
            System.out.print("QRコードとして使用しますか？ (Y/N)：");

            String answer = scanner.nextLine().trim();

            if (answer.equalsIgnoreCase("Y")) {
                useQRCode = true;
                System.out.println("QRコード変換を実行します。");
            } else {
                System.out.println("通常のImageとして処理します。");
            }

            System.out.println();
        }

        // ★Step2でここに変換処理を追加します
        if (useQRCode) {
            jrxmlFile = JRXMLConverter.convert(jrxmlFile);
        }

        /* ===== CSVデータソース（UTF-8） ===== */
        InputStreamReader reader =
                new InputStreamReader(
                        new FileInputStream(csvFile),
                        StandardCharsets.UTF_8
                );

        JRCsvDataSource dataSource = new JRCsvDataSource(reader);
        dataSource.setUseFirstRowAsHeader(true);
        dataSource.setFieldDelimiter(',');

        /* ===== パラメータ ===== */
        Map<String, Object> parameters = new HashMap<>();

        /* ===== jrxml → Jasper コンパイル ===== */
        JasperReport jasperReport =
                JasperCompileManager.compileReport(jrxmlFile.getPath());

        /* ===== データ投入 ===== */
        JasperPrint jasperPrint =
                JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        dataSource
                );

        /* ===== PDF出力 ===== */
        JasperExportManager.exportReportToPdfFile(
                jasperPrint,
                outputPdf
        );

        System.out.println(
                "PDF output completed: " +
                new File(outputPdf).getAbsolutePath()
        );
    }
}