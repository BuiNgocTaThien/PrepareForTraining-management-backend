$body = '{"email":"admin@example.com","password":"123456"}'
$resp = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/login" -Method Post -Body $body -ContentType "application/json"
$token = $resp.data.accessToken
Write-Host "Token: $token"
try {
  $resp2 = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/projects?page=0&size=20" -Method Get -Headers @{ Authorization = "Bearer $token" }
  $resp2 | ConvertTo-Json
} catch {
  Write-Host "Error:" $_.Exception.Response.StatusCode.value__
}
