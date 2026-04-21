Set-Location 'D:\DOCUMENTI PERSONALI\UNIVERSITA\PEGASO\Project-Work\GitHub\medbook-platform\infra\eureka-server'
Write-Host 'Avvio eureka-server...' -ForegroundColor Green
mvn spring-boot:run "-Dspring.profiles.active=dev"
