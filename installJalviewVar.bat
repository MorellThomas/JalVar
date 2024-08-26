@ECHO OFF
SET CURRENTDIR=%cd%
SET COMMAND=java -jar %CURRENTDIR%\build\libs\jalview-all-2.11.3.0-j11.jar
ECHO @ECHO OFF > JalVar.bat
ECHO %COMMAND% >> JalVar.bat
MOVE JalVar.bat %USERPROFILE%\Desktop

