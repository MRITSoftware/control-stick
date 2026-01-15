# Script para fazer commit e push do projeto
$ErrorActionPreference = "Stop"

# Define o diretório do projeto
$projectDir = Join-Path $PSScriptRoot "MRIT Control"

if (-not (Test-Path $projectDir)) {
    Write-Host "Erro: Diretório do projeto não encontrado: $projectDir"
    exit 1
}

Set-Location $projectDir
Write-Host "Diretório atual: $(Get-Location)"

# Verifica se já existe repositório Git
if (-not (Test-Path .git)) {
    Write-Host "Inicializando repositório Git..."
    git init
    git remote add origin https://github.com/MRITSoftware/control-stick.git
}

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

# Faz commit
Write-Host "Fazendo commit..."
git commit -m "feat: Atualização para novo banco Supabase e suporte a PWA URLs"

# Configura branch e faz push
Write-Host "Fazendo push..."
git branch -M main
git push -u origin main

Write-Host "Concluído!"
