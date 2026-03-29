# Requiere: PowerShell "Ejecutar como administrador"
# Uso: clic derecho -> Ejecutar con PowerShell (como administrador)
# Objetivo: WSL2 + plataformas de hipervisor que Docker Desktop necesita en Windows 11 Home.

$ErrorActionPreference = 'Stop'

function Test-Admin {
    $current = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($current)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

if (-not (Test-Admin)) {
    Write-Host "Este script debe ejecutarse como ADMINISTRADOR." -ForegroundColor Red
    Write-Host "Clic derecho en PowerShell -> Ejecutar como administrador, luego:" -ForegroundColor Yellow
    Write-Host "  Set-Location '$PSScriptRoot'" -ForegroundColor Cyan
    Write-Host "  .\enable-docker-prereqs-windows.ps1" -ForegroundColor Cyan
    exit 1
}

Write-Host "Habilitando caracteristicas opcionales (puede tardar)..." -ForegroundColor Green

dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
dism.exe /online /enable-feature /featurename:HypervisorPlatform /all /norestart

Write-Host "Instalando WSL (kernel + componentes). Si pide reinicio, reinicia y vuelve a abrir Docker Desktop." -ForegroundColor Green
wsl.exe --install --no-distribution

Write-Host ""
Write-Host "Listo. REINICIA el PC si Windows lo solicita." -ForegroundColor Yellow
Write-Host "Despues: abre Docker Desktop; en Ajustes usa el motor basado en WSL 2." -ForegroundColor Yellow
