@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Colegio - Detener Servicios

echo.
echo  ========================================
echo   Colegio - Gestion de Acceso
echo   Deteniendo servicios...
echo  ========================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop.ps1"

echo.
pause
