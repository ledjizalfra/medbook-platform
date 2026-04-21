# =============================================================================
# MedBook Platform - Script di avvio completo v7
#
# Struttura cartelle:
#   $root\infra\      -> eureka-server, config-server
#   $root\dmn\        -> tutti i domain microservices
#   $root\edge\       -> medbook-bff, api-gateway
#   $root\frontend\   -> medbook-fe
#
# PRE-REQUISITI (avviare manualmente prima di questo script):
#   - Keycloak    : http://localhost:8082
#   - Zipkin      : http://localhost:9411
#   - Kafka       : localhost:9092
#   - PostgreSQL  : localhost:5432
#
# USO:
#   powershell -ExecutionPolicy Bypass -File .\start-medbook-v7.ps1
# =============================================================================

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$tmp  = "$root\.medbook-launchers"

# -----------------------------------------------------------------------------
# Genera uno script .ps1 per ogni servizio con il ritardo configurato.
# Ogni scheda esegue il proprio script - nessun problema di escape inline.
# -----------------------------------------------------------------------------
if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
New-Item -ItemType Directory -Path $tmp | Out-Null

function New-Launcher {
    param(
        [string]$Name,
        [string]$ServicePath,
        [int]$DelaySeconds,
        [string]$Command = "mvn spring-boot:run `"-Dspring.profiles.active=dev`""
    )
    $content = "Set-Location '$ServicePath'"
    if ($DelaySeconds -gt 0) {
        $content += "`nWrite-Host 'In attesa di $DelaySeconds secondi prima di avviare $Name...' -ForegroundColor Yellow"
        $content += "`nStart-Sleep -Seconds $DelaySeconds"
    }
    $content += "`nWrite-Host 'Avvio $Name...' -ForegroundColor Green"
    $content += "`n$Command"
    Set-Content -Path "$tmp\$Name.ps1" -Value $content -Encoding UTF8
}

# Eureka prima di config-server
New-Launcher -Name "eureka-server"    -ServicePath "$root\infra\eureka-server"    -DelaySeconds 0
New-Launcher -Name "config-server"    -ServicePath "$root\infra\config-server"    -DelaySeconds 15
New-Launcher -Name "patient-dmn"      -ServicePath "$root\dmn\patient-dmn"        -DelaySeconds 35
New-Launcher -Name "doctor-dmn"       -ServicePath "$root\dmn\doctor-dmn"         -DelaySeconds 38
New-Launcher -Name "clinic-dmn"       -ServicePath "$root\dmn\clinic-dmn"         -DelaySeconds 41
New-Launcher -Name "appointment-dmn"  -ServicePath "$root\dmn\appointment-dmn"    -DelaySeconds 44
New-Launcher -Name "notification-dmn" -ServicePath "$root\dmn\notification-dmn"   -DelaySeconds 47
New-Launcher -Name "medbook-bff"      -ServicePath "$root\edge\medbook-bff"       -DelaySeconds 90
New-Launcher -Name "api-gateway"      -ServicePath "$root\edge\api-gateway"       -DelaySeconds 115
New-Launcher -Name "medbook-fe"       -ServicePath "$root\frontend\medbook-fe"    -DelaySeconds 130 -Command "ng serve"

Clear-Host
Write-Host "=============================================" -ForegroundColor White
Write-Host "  MedBook Platform - Avvio servizi v7"       -ForegroundColor White
Write-Host "=============================================" -ForegroundColor White
Write-Host ""
Write-Host "  Apertura Windows Terminal..." -ForegroundColor Cyan
Write-Host ""

# -----------------------------------------------------------------------------
# --window 0 va specificato UNA SOLA VOLTA all'inizio del comando wt.
# Le schede successive usano solo "new-tab" senza --window.
# -----------------------------------------------------------------------------
wt --window 0 `
    new-tab --title "eureka-server"    -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\eureka-server.ps1" `;  `
    new-tab --title "config-server"    -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\config-server.ps1" `;  `
    new-tab --title "patient-dmn"      -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\patient-dmn.ps1" `;    `
    new-tab --title "doctor-dmn"       -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\doctor-dmn.ps1" `;     `
    new-tab --title "clinic-dmn"       -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\clinic-dmn.ps1" `;     `
    new-tab --title "appointment-dmn"  -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\appointment-dmn.ps1" `; `
    new-tab --title "notification-dmn" -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\notification-dmn.ps1" `; `
    new-tab --title "medbook-bff"      -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\medbook-bff.ps1" `;    `
    new-tab --title "api-gateway"      -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\api-gateway.ps1" `;    `
    new-tab --title "medbook-fe"       -- pwsh -NoExit -ExecutionPolicy Bypass -File "$tmp\medbook-fe.ps1"

Write-Host "  Schede aperte. Avvio in corso (attendi ~2 minuti)..." -ForegroundColor Yellow
Write-Host ""
Write-Host "  eureka-server    http://localhost:8070  (subito)"  -ForegroundColor Gray
Write-Host "  config-server    http://localhost:8071  (15s)"     -ForegroundColor Gray
Write-Host "  DMN              http://localhost:809x  (35-47s)"  -ForegroundColor Gray
Write-Host "  medbook-bff      http://localhost:8081  (90s)"     -ForegroundColor Gray
Write-Host "  api-gateway      http://localhost:8080  (115s)"    -ForegroundColor Gray
Write-Host "  medbook-fe       http://localhost:4200  (130s)"    -ForegroundColor Gray
Write-Host ""
Write-Host "  Keycloak (manuale)  http://localhost:8082"         -ForegroundColor DarkGray
Write-Host "  Zipkin   (manuale)  http://localhost:9411"         -ForegroundColor DarkGray
Write-Host "  Kafka    (manuale)  localhost:9092"                -ForegroundColor DarkGray
Write-Host "  Mailtrap (cloud)    https://mailtrap.io"           -ForegroundColor DarkGray
Write-Host ""
