# Levanta los 3 servicios en puertos separados:
#   Backend  -> 8080
#   WhatsApp -> 3001
#   Frontend -> 4200

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$JavaHome = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"

Write-Host "=== Colegio Gestión de Acceso ===" -ForegroundColor Cyan

# 1. Liberar puertos
& "$Root\stop.ps1"
Write-Host ""

# 2. Backend (puerto 8080)
Start-Process powershell -ArgumentList @(
    '-NoExit', '-Command',
    @"
`$env:JAVA_HOME = '$JavaHome'
cd '$Root\gestion-acceso-backend'
Write-Host '=== BACKEND - puerto 8080 ===' -ForegroundColor Green
.\mvnw.cmd spring-boot:run
"@
)

Start-Sleep -Seconds 3

# 3. WhatsApp Bridge (puerto 3001)
Start-Process powershell -ArgumentList @(
    '-NoExit', '-Command',
    @"
cd '$Root\whatsapp-bridge'
if (-not (Test-Path node_modules)) { npm install }
Write-Host '=== WHATSAPP BRIDGE - puerto 3001 ===' -ForegroundColor Yellow
Write-Host 'Escanee el QR aqui abajo con WhatsApp > Dispositivos vinculados' -ForegroundColor Yellow
`$env:PORT = '3001'
node index.js
"@
)

Start-Sleep -Seconds 2

# 4. Frontend Angular (puerto 4200)
Start-Process powershell -ArgumentList @(
    '-NoExit', '-Command',
    @"
cd '$Root\colegio-gestion-acceso'
if (-not (Test-Path node_modules)) { npm install }
Write-Host '=== FRONTEND - puerto 4200 ===' -ForegroundColor Blue
npx ng serve --host 127.0.0.1 --port 4200
"@
)

Write-Host ""
Write-Host "Servicios iniciados en 3 ventanas separadas:" -ForegroundColor Green
Write-Host "  Frontend:  http://127.0.0.1:4200"
Write-Host "  Backend:   http://localhost:8080"
Write-Host "  Swagger:   http://localhost:8080/swagger-ui/index.html"
Write-Host "  WhatsApp:  http://localhost:3001/status"
Write-Host ""
Write-Host "Login: admin@colegio.edu / admin123" -ForegroundColor Cyan
Write-Host "Para detener todo: doble clic en 'Detener Servicios.bat' o .\stop.ps1" -ForegroundColor Gray
