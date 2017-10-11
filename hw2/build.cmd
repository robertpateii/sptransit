@ECHO OFF
ECHO Warning: this is just a backup build script. Ideally use ant build since this is a pain to maintain.
IF EXIST build rmdir /s /q build
SET compile=javac
IF NOT "%JAVA_HOME%"=="" SET compile=%JAVA_HOME%\bin\javac.exe
mkdir build
%compile% -d build Client.java
%compile% -d build Server.java