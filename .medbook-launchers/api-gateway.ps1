Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\edge\api-gateway'
Write-Host 'In attesa di 115 secondi prima di avviare api-gateway...' -ForegroundColor Yellow
Start-Sleep -Seconds 115
Write-Host 'Avvio api-gateway...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
