# Limpia el volumen ANTIGUO de RabbitMQ (rabbitmq_data) tras el cambio a hospedaje.events / hospedaje_rabbitmq_data.
# Requisito: Docker Desktop (o daemon) en ejecución.
# Uso (desde la raíz del repo):
#   powershell -ExecutionPolicy Bypass -File docker/reset-rabbitmq-volume.ps1

$ErrorActionPreference = "Continue"
Set-Location (Split-Path -Parent $PSScriptRoot)

Write-Host "Deteniendo stack (si existe)..." -ForegroundColor Cyan
docker compose down 2>$null

# Nombre típico del volumen con prefijo de proyecto = nombre de la carpeta del repo
$projectName = (Get-Item .).Name -replace '[^a-zA-Z0-9]', ''
$oldVolume = "${projectName}_rabbitmq_data"

Write-Host "Intentando eliminar volumen legacy: $oldVolume" -ForegroundColor Cyan
docker volume rm $oldVolume 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "OK: volumen eliminado." -ForegroundColor Green
} else {
    Write-Host "Info: el volumen no existía, ya estaba borrado, o el nombre difiere. Lista de volúmenes 'rabbit':" -ForegroundColor Yellow
    docker volume ls | Select-String -Pattern "rabbit"
}

Write-Host ""
Write-Host "Siguiente paso: docker compose up -d" -ForegroundColor Green
Write-Host "RabbitMQ usará el volumen 'hospedaje_rabbitmq_data' (nuevo exchange hospedaje.events)." -ForegroundColor Green
