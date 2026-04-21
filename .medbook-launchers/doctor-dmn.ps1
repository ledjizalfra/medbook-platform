Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\dmn\doctor-dmn'
Write-Host 'In attesa di 38 secondi prima di avviare doctor-dmn...' -ForegroundColor Yellow
Start-Sleep -Seconds 38
Write-Host 'Avvio doctor-dmn...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
