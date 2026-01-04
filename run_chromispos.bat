@echo off
setlocal
pushd %~dp0

echo ==========================================
echo    Iniciando ChromisPOS con Java 11
echo ==========================================
echo.

set "J11=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot\bin\java.exe"
set "CLASSPATH=build\classes;chromispos.jar;lib/*"

"%J11%" ^
	-cp "%CLASSPATH%" ^
	-Duser.dir=%CD% ^
	-Duser.country=EC ^
	-Duser.language=es ^
	-Duser.timezone=America/Guayaquil ^
	-Dfile.encoding=UTF-8 ^
	uk.chromis.pos.forms.StartPOS

popd
pause
