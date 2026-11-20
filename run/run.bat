@echo off
title Smart Parking Management System

echo ========================================
echo   SMART PARKING MANAGEMENT SYSTEM
echo ========================================
echo.

REM Java
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"

REM Maven
set "MAVEN_HOME=C:\Users\yashu\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16"

REM Add Java and Maven to PATH
set "PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%"

echo Java:
java -version

echo.
echo Maven:
mvn -version

echo.
echo Building Java Backend...

cd /d "C:\Users\yashu\Desktop\par\backend"

call mvn clean package

if errorlevel 1 (
    echo.
    echo ========================================
    echo BUILD FAILED
    echo ========================================
    pause
    exit /b
)

echo.
echo ========================================
echo Copying WAR file to Tomcat
echo ========================================

copy /Y "target\smart-parking.war" "C:\Users\yashu\Downloads\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\webapps\smart-parking.war"

echo.
echo ========================================
echo Starting Tomcat
echo ========================================

cd /d "C:\Users\yashu\Downloads\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\bin"

call startup.bat

echo.
echo ========================================
echo BACKEND STARTED SUCCESSFULLY
echo ========================================
echo.
echo Backend API:
echo http://localhost:8080/smart-parking/api/slots
echo.

pause
