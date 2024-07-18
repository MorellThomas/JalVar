@ECHO OFF
SET CURRENTDIR=%cd%
SET COMMAND=java -jar %CURRENTDIR%\build\libs\jalview-all-2.11.3.0-j11.jar
ECHO @ECHO OFF > JalviewVar.bat
ECHO %COMMAND% >> JalviewVar.bat
MOVE JalviewVar.bat %USERPROFILE%\Desktop

