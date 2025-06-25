@echo off
echo Compiling FoodieExpress Java Backend...

:: Create output directory
if not exist "out" mkdir out

:: Compile all Java files
javac -d out -cp src src/main/java/com/foodieexpress/model/*.java
javac -d out -cp src src/main/java/com/foodieexpress/service/*.java
javac -d out -cp src src/main/java/com/foodieexpress/*.java

if %errorlevel% == 0 (
    echo Compilation successful!
    echo.
    echo To run the application:
    echo java -cp out com.foodieexpress.FoodieExpressApp
) else (
    echo Compilation failed!
)

pause
