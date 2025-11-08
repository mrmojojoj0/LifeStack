dir /s /b src\*.java > sources.txt
mkdir build\classes
javac -cp "lib/*" -d build\classes @sources.txt
java -cp "build\classes;lib/*" MyApp.Main

