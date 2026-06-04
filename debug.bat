@echo off
set "J11=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot\bin\java.exe"
"%J11%" -cp "build\classes;chromispos.jar;lib\*" uk.chromis.pos.forms.StartPOS > debug.log 2>&1
