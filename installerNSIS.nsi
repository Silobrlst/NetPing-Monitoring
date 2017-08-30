# define the name of the installer
Outfile "NetPing-Monitoring.exe"
 
# define the directory to install to, the desktop in this case as specified  
# by the predefined $DESKTOP variable
InstallDir "$PROGRAMFILES\NetPing-Monitoring"

;--------------------------------

; Pages

Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------
 
# default section
Section

# create a shortcut named "new shortcut" in the start menu programs directory
# presently, the new shortcut doesn't call anything (the second field is blank)
createShortCut "$DESKTOP\NetPing-Monitoring.lnk" "$INSTDIR\NetPing-Monitoring.bat" "" "$INSTDIR\appIcon.ico"

# to delete shortcut, go to start menu directory and manually delete it

# define what to install and place it in the output path
SetOutPath "$INSTDIR\libs"
File libs\json-20160810.jar
File libs\log4j-api-2.8.2.jar
File libs\log4j-core-2.8.2.jar
File libs\snmp4j-2.5.6.jar

SetOutPath $INSTDIR
File appIcon.png
File appIcon.ico
File NetPing-Monitoring.jar
File NetPing-Monitoring.bat

# define uninstaller name
WriteUninstaller $INSTDIR\uninstaller.exe
 
SectionEnd

# create a section to define what the uninstaller does.
# the section will always be named "Uninstall"
Section "Uninstall"
 
# Always delete uninstaller first
Delete $INSTDIR\uninstaller.exe

Delete $INSTDIR\libs\json-20160810.jar
Delete $INSTDIR\libs\log4j-api-2.8.2.jar
Delete $INSTDIR\libs\log4j-core-2.8.2.jar
Delete $INSTDIR\libs\snmp4j-2.5.6.jar
Delete $INSTDIR\libs
Delete $INSTDIR\appIcon.png
Delete $INSTDIR\appIcon.ico
Delete $INSTDIR\NetPing-Monitoring.jar
Delete $INSTDIR\NetPing-Monitoring.bat
Delete $INSTDIR\guiSettings.json

SectionEnd