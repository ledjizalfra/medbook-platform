Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\dmn\appointment-dmn'
Write-Host 'In attesa di 44 secondi prima di avviare appointment-dmn...' -ForegroundColor Yellow
Start-Sleep -Seconds 44
Write-Host 'Avvio appointment-dmn...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
