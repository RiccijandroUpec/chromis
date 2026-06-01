@echo off
setlocal
pushd %~dp0

echo ==========================================
echo    Iniciando ChromisPOS Ecuador - v1.5.5
echo    Autor: Riccijandro ^| github.com/riccijandro
echo ==========================================
echo.

set "J11=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot\bin\javaw.exe"
set "APPDIR=%~dp0"
if "%APPDIR:~-1%"=="\" set "APPDIR=%APPDIR:~0,-1%"

start "ChromisPOS Ecuador" /D "%APPDIR%" "%J11%" -cp "build\classes;chromispos.jar;lib\*" -Duser.country=EC -Duser.language=es -Duser.timezone=America/Guayaquil -Dfile.encoding=UTF-8 uk.chromis.pos.forms.StartPOS

popd
