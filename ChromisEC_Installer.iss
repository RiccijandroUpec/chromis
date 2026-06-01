[Setup]
; Información básica de la aplicación
AppName=ChromisEC
AppVersion=1.5.5
AppPublisher=Riccijandro - Open Source Contributor
AppPublisherURL=https://github.com/riccijandro/chromisposec
AppSupportURL=https://www.chromis.co.uk
AppUpdatesURL=https://www.chromis.co.uk

; Directorios por defecto
DefaultDirName={pf}\ChromisEC
DefaultGroupName=ChromisEC
DisableProgramGroupPage=yes

; Iconos y diseño
; SetupIconFile=images\favicon.ico (Descomentar si tienes un icono .ico)
WizardStyle=modern
Compression=lzma2
SolidCompression=yes
OutputDir=.\InstaladorFinal
OutputBaseFilename=Setup_ChromisEC

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Ejecutable principal
Source: "chromispos.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "run_chromispos.bat"; DestDir: "{app}"; Flags: ignoreversion
; Archivo de configuración maestro
Source: "chromisposconfig.properties"; DestDir: "{app}"; Flags: ignoreversion

; Carpetas completas necesarias para que funcione
Source: "lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "locales\*"; DestDir: "{app}\locales"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "images\*"; DestDir: "{app}\images"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "fonts\*"; DestDir: "{app}\fonts"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "cssStyles\*"; DestDir: "{app}\cssStyles"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "licensing\*"; DestDir: "{app}\licensing"; Flags: ignoreversion recursesubdirs createallsubdirs

; Si existe la carpeta JRE, la empaquetamos para que sea 100% portable y no requiera instalar Java
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist

[Icons]
; Acceso directo en el menú de inicio
Name: "{group}\ChromisEC"; Filename: "javaw.exe"; Parameters: "-jar ""{app}\chromispos.jar""" ; WorkingDir: "{app}"
; Acceso directo de desinstalación
Name: "{group}\{cm:UninstallProgram,ChromisEC}"; Filename: "{uninstallexe}"
; Acceso directo en el escritorio (si el usuario lo marca en la instalación)
Name: "{commondesktop}\ChromisEC"; Filename: "javaw.exe"; Parameters: "-jar ""{app}\chromispos.jar"""; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
; Ejecutar automáticamente al terminar la instalación
Filename: "javaw.exe"; Parameters: "-jar ""{app}\chromispos.jar"""; WorkingDir: "{app}"; Description: "Lanzar ChromisEC ahora"; Flags: nowait postinstall skipifsilent
