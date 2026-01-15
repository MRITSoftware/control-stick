# Script para executar comandos Git no diretório do projeto
$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Obtém o diretório do script (onde está o projeto)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = $scriptDir

Write-Host "Diretório do projeto: $projectDir"
Write-Host "Verificando se existe: $(Test-Path $projectDir)"

if (-not (Test-Path $projectDir)) {
    Write-Host "Erro: Diretório do projeto não encontrado: $projectDir"
    exit 1
}

Set-Location $projectDir
Write-Host "Diretório atual: $(Get-Location)"
Write-Host ""

# Verifica se já existe repositório Git
if (-not (Test-Path .git)) {
    Write-Host "Inicializando repositório Git..."
    git init
    git remote add origin https://github.com/MRITSoftware/control-stick.git
} else {
    Write-Host "Repositório Git já existe. Configurando remote..."
    git remote set-url origin https://github.com/MRITSoftware/control-stick.git
}

Write-Host ""
Write-Host "Remote configurado:"
git remote -v
Write-Host ""

# Adiciona arquivos
Write-Host "Adicionando arquivos..."
git add .gitignore
git add .github/workflows/build.yml
git add gradlew
git add gradlew.bat
git add app/
git add build.gradle.kts
git add settings.gradle.kts
git add gradle.properties
git add gradle/
git add *.md
git add SETUP_*.sql

Write-Host ""
Write-Host "Status dos arquivos:"
git status --short | Select-Object -First 20
Write-Host ""

# Faz commit
Write-Host "Fazendo commit..."
git commit -m "feat: Atualizacao para novo banco Supabase e suporte a PWA URLs"

Write-Host ""
Write-Host "Configurando branch e fazendo push..."
git branch -M main
git push -u origin main

Write-Host ""
Write-Host "Concluído!"
