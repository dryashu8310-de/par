@echo off
title Smart Parking Management System

set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
set "MAVEN_HOME=C:\Users\yashu\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo ========================================
echo SMART PARKING MANAGEMENT SYSTEM
echo ========================================
echo.

echo Checking Java...
java -version

echo.
echo Checking Maven...
"%MAVEN_HOME%\bin\mvn.cmd" -version

echo.
echo Building Backend...
cd /d "C:\Users\yashu\Desktop\par\backend"

call "%MAVEN_HOME%\bin\mvn.cmd" clean package

if errorlevel 1 (
    echo.
    echo BUILD FAILED
    pause
    exit /b 1
)

echo.
echo Copying WAR to Tomcat...
copy /Y "target\smart-parking.war" "C:\Users\yashu\Downloads\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\webapps\smart-parking.war"

echo.
echo Starting Tomcat...
cd /d "C:\Users\yashu\Downloads\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\bin"
call startup.bat

echo.
echo ========================================
echo BACKEND STARTED
echo ========================================
echo http://localhost:8080/smart-parking/api/slots
echo ========================================
pause