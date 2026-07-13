# jrxml2pdf
Generate PDF from JRXML and CSV with QR code conversion support.
---

# セットアップ
> [!IMPORTANT]
> GitHubのアップロード容量制限のため、ライブラリは **lib1** と **lib2** に分割しています。
> 実行前に **lib1** と **lib2** のすべてのJARファイルを、1つの **lib** フォルダへまとめてください。

### フォルダ構成（GitHub）
```
jrxml2pdf
│
├─ lib1
├─ lib2
├─ src
├─ README.md
└─ 1jrxmlAnd1CsvTo1Pdf.bat
```

### 実行前

```
jrxml2pdf
│
├─ lib
│   ├─ *.jar
│   ├─ *.jar
│   └─ ...
├─ src
├─ README.md
└─ 1jrxmlAnd1CsvTo1Pdf.bat
```

※ **lib1** と **lib2** の中にあるすべてのJARファイルを、新しく作成した **lib** フォルダへコピーしてください。

---

# 使用方法

1. lib1 と lib2 のJARファイルを **lib** フォルダへまとめる
2. JRXMLファイルを1つ配置する
3. CSVファイルを1つ配置する
4. `1jrxmlAnd1CsvTo1Pdf.bat` を実行する

---

# 動作確認環境

- Java 17
- Jaspersoft Studio 6.21.5

---

# 主な機能

- JRXML自動検出
- CSV自動検出
- PDF生成
- ImageエレメントをQRCodeへ変換
- java.awt.Imageをjava.lang.Stringへ自動変換
- 元JRXMLのバックアップ作成（bkupフォルダ）
