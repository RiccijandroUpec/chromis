@echo off
set JAVAC="C:\Program Files\Eclipse Adoptium\jdk-8.0.472.8-hotspot\bin\javac.exe"
set JAR="C:\Program Files\Eclipse Adoptium\jdk-8.0.472.8-hotspot\bin\jar.exe"
set BUILD_DIR=build\classes

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

%JAVAC% -d "%BUILD_DIR%" -sourcepath "src-pos;src-data;src-beans" -encoding UTF-8 -source 1.8 -target 1.8 -cp "chromispos.jar;lib/*" src-pos\uk\chromis\globals\SystemProperty.java 2>&1

if %ERRORLEVEL% EQU 0 (
    %JAR% uf chromispos.jar -C "%BUILD_DIR%" .
    echo DONE
) else (
    echo ERROR
)
