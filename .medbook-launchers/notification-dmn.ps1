Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\dmn\notification-dmn'
Write-Host 'In attesa di 47 secondi prima di avviare notification-dmn...' -ForegroundColor Yellow
Start-Sleep -Seconds 47
Write-Host 'Avvio notification-dmn...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
