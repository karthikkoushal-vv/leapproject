@echo off
title FoodShare Backend Server
echo ===================================================
echo    Starting FoodShare Spring Boot Platform...
echo ===================================================
cd /d "%~dp0"
echo Opening browser to http://localhost:8080 ...
timeout /t 5 >nul
start http://localhost:8080
.\mvnw.cmd spring-boot:run
pause
