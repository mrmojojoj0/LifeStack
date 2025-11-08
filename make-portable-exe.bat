mkdir app-input
copy LifeStack-all.jar app-input
jpackage --type app-image ^
  --name LifeStack ^
  --app-version 1.0 ^
  --input app-input ^
  --main-jar LifeStack-all.jar ^
  --main-class MyApp.Main ^
  --dest dist ^
  --icon icon.ico