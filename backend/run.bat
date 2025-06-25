@echo off
echo Starting FoodieExpress Backend...
echo.

if not exist "out" (
    echo Compiled classes not found. Please run compile.bat first.
    pause
    exit /b 1
)

java -cp out com.foodieexpress.FoodieExpressApp

pause
