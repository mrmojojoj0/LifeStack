# Set project paths
$projectRoot = "C:\Users\sakin\OneDrive\Desktop\LifeStack"
$src = Join-Path $projectRoot "src"
$buildClasses = Join-Path $projectRoot "build\classes"
$build = Join-Path $projectRoot "build"
$jarFile = Join-Path $build "MyApp.jar"
$mainClass = "MyApp.MyAppMain"  # <-- replace with your main class
$iconPath = Join-Path $projectRoot "icon.ico"  # optional, replace with your icon

# 1️⃣ Create build folder
if (-Not (Test-Path $buildClasses)) {
    New-Item -ItemType Directory -Path $buildClasses | Out-Null
}

# 2️⃣ Compile all Java files recursively
Get-ChildItem -Recurse -Filter *.java $src | ForEach-Object {
    javac -d $buildClasses -cp "$projectRoot\lib\*" $_.FullName
}

# 3️⃣ Create manifest file for jar
$manifestFile = Join-Path $build "manifest.txt"
@"
Main-Class: $mainClass
Class-Path: lib/iText-Core-9.3.0-only-jars/* lib/jasypt-1.9.3/* lib/jcalendar-1.4/* lib/org/jfree/*
"@ | Set-Content -Path $manifestFile -Encoding ASCII

# 4️⃣ Create runnable jar
jar --create --file $jarFile -C $buildClasses . -m $manifestFile

# 5️⃣ Convert jar to .exe using jpackage
jpackage --name MyApp --input $build --main-jar "MyApp.jar" --main-class $mainClass --type exe --icon $iconPath

Write-Host "✅ Compilation and packaging complete. MyApp.exe created!"
