@echo off
chcp 65001 >nul
cd /d "%~dp0MRIT Control"
if not exist .git (
    echo Inicializando repositorio Git...
    git init
    git remote add origin https://github.com/MRITSoftware/control-stick.git
) else (
    git remote set-url origin https://github.com/MRITSoftware/control-stick.git
)
echo Adicionando arquivos...
git add .gitignore .github/workflows/build.yml gradlew gradlew.bat app/ build.gradle.kts settings.gradle.kts gradle.properties gradle/ *.md SETUP_*.sql
echo Fazendo commit...
git commit -m "feat: Atualizacao para novo banco Supabase e suporte a PWA URLs"
echo Fazendo push...
git branch -M main
git push -u origin main
echo Concluido!
pause
