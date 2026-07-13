@echo off
setlocal

REM --- classpath（lib配下のjarをすべて読み込む） ---
set CP=.;lib\*

REM --- ① Javaソースをコンパイル ---
javac -encoding UTF-8 -cp "%CP%" runReport.java
if errorlevel 1 (
    echo コンパイルに失敗しました
    pause
    exit /b
)

REM --- ② 実行（AWTフォントチェック無効化） ---
java ^
  -Dnet.sf.jasperreports.awt.ignore.missing.font=true ^
  -cp "%CP%" ^
  runReport

pause
