Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\edge\medbook-bff'
Write-Host 'In attesa di 90 secondi prima di avviare medbook-bff...' -ForegroundColor Yellow
Start-Sleep -Seconds 90
Write-Host 'Avvio medbook-bff...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
