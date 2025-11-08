@echo off
setlocal

echo Compiling sources into build\classes...
mkdir build\classes 2>nul

rem Prefer JAVAC from JAVA_HOME if set, otherwise fall back to javac on PATH
if defined JAVA_HOME (
  set "JAVAC=%JAVA_HOME%\bin\javac"
) else (
  set "JAVAC=javac"
)

%JAVAC% -cp "lib/*" -d build\classes @sources.txt
if errorlevel 1 (
  echo Compilation failed. Fix the errors above before creating the fat jar.
  exit /b 1
)

echo Preparing temporary workspace tmp_jar...

rmdir /S /Q tmp_jar 2>nul
mkdir tmp_jar
pushd tmp_jar

rem Unpack every jar in lib into tmp_jar. In a batch file use double percent (%%I).
for %%I in ("..\lib\*.jar") do (
  echo Unpacking %%I
  jar xf "%%I"
)

popd

echo Copying compiled classes into tmp_jar...
xcopy /E /I build\classes\* tmp_jar\ >nul

echo Creating manifest...
(echo Main-Class: MyApp.Main & echo.) > manifest.txt

echo Building LifeStack-all.jar (fat jar)...
jar cfm LifeStack-all.jar manifest.txt -C tmp_jar .

echo Cleaning up...
rmdir /S /Q tmp_jar
del manifest.txt

echo Done. Created LifeStack-all.jar
endlocal
