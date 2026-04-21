Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\infra\config-server'
Write-Host 'In attesa di 15 secondi prima di avviare config-server...' -ForegroundColor Yellow
Start-Sleep -Seconds 15
Write-Host 'Avvio config-server...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
