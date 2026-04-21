Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\dmn\clinic-dmn'
Write-Host 'In attesa di 41 secondi prima di avviare clinic-dmn...' -ForegroundColor Yellow
Start-Sleep -Seconds 41
Write-Host 'Avvio clinic-dmn...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
