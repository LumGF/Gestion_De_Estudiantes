@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Colegio - Iniciar Servicios

echo.
echo  ========================================
echo   Colegio - Gestion de Acceso
echo   Iniciando servicios...
echo  ========================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"

echo.
echo  Los servicios corren en ventanas separadas.
echo  Para detener todo: doble clic en "Detener Servicios.bat"
echo.
pause
