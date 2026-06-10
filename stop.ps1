# Detiene backend (8080), whatsapp-bridge (3001) y frontend (4200)
$Ports = @(8080, 3001, 4200)

Write-Host "=== Deteniendo servicios ===" -ForegroundColor Yellow

foreach ($port in $Ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($conn in $connections) {
        $processId = $conn.OwningProcess
        if ($processId -and $processId -ne 0) {
            try {
                $proc = Get-Process -Id $processId -ErrorAction SilentlyContinue
                Write-Host "Puerto $port -> PID $processId ($($proc.ProcessName))" -ForegroundColor Gray
                taskkill /F /T /PID $processId 2>$null | Out-Null
                Write-Host "  Detenido (proceso y dependencias)." -ForegroundColor Green
            } catch {
                Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
                Write-Host "  Detenido." -ForegroundColor Green
            }
        }
    }
}

# Cerrar ventanas PowerShell abiertas por start.ps1
$serviceMarkers = @(
    'gestion-acceso-backend',
    'whatsapp-bridge',
    'colegio-gestion-acceso'
)
Get-CimInstance Win32_Process -Filter "Name = 'powershell.exe'" -ErrorAction SilentlyContinue |
    Where-Object {
        $cmd = $_.CommandLine
        $cmd -and ($serviceMarkers | Where-Object { $cmd -like "*$_*" })
    } |
    ForEach-Object {
        Write-Host "Cerrando ventana de servicio PID $($_.ProcessId)" -ForegroundColor Gray
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }

Start-Sleep -Seconds 2

Write-Host ""
Write-Host "Estado de puertos:" -ForegroundColor Cyan
foreach ($port in $Ports) {
    $busy = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($busy) {
        Write-Host "  Puerto $port -> OCUPADO (PID $($busy[0].OwningProcess))" -ForegroundColor Red
    } else {
        Write-Host "  Puerto $port -> LIBRE" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Servicios detenidos." -ForegroundColor Green
