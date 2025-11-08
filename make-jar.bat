(
  echo Main-Class: MyApp.Main
  echo Class-Path: lib/jasypt-1.9.3.jar lib/jfreechart-1.5.4.jar lib/jcalendar-1.4.jar lib/pdfbox-app-3.0.6.jar
  echo.
) > manifest.txt

jar cfm LifeStack.jar manifest.txt -C build\classes .