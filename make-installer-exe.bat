mkdir app-input
copy LifeStack-all.jar app-input
copy icon.ico app-input

jpackage --type exe ^
  --name LifeStack-installer ^
  --app-version 1.0 ^
  --input app-input ^
  --main-jar LifeStack-all.jar ^
  --main-class MyApp.Main ^
  --dest dist ^
  --icon app-input\icon.ico ^
  --win-shortcut ^
  --win-menu