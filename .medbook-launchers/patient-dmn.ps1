Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\dmn\patient-dmn'
Write-Host 'In attesa di 35 secondi prima di avviare patient-dmn...' -ForegroundColor Yellow
Start-Sleep -Seconds 35
Write-Host 'Avvio patient-dmn...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
