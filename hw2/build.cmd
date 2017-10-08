@ECHO OFF
ECHO Rebuilding...
IF EXIST "*.class" del *.class
SET compile=javac
IF NOT "%JAVA_HOME%"=="" SET compile=%JAVA_HOME%\bin\javac.exe
%compile% Client.java
%compile% Server.java