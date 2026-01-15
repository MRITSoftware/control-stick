# Instruções para Commit e Push

## Status Atual

- ✅ Remote configurado: https://github.com/MRITSoftware/control-stick.git
- ✅ GitHub Actions workflow criado: `.github/workflows/build.yml`
- ✅ `.gitignore` atualizado para ignorar arquivos desnecessários
- ⚠️ O repositório Git está no diretório home do usuário, não no diretório do projeto

## Como fazer o commit manualmente

⚠️ **IMPORTANTE**: O repositório Git está no diretório home do usuário. Você precisa inicializar o Git no diretório correto do projeto.

1. Abra o PowerShell ou CMD no diretório do projeto:
   ```powershell
   cd "D:\VISION\Atualizações\Control MRIT\MRIT Control\MRIT Control"
   ```

2. Verifique se já existe um repositório Git:
   ```powershell
   if (Test-Path .git) { Write-Host "Git repo encontrado" } else { Write-Host "Git repo nao encontrado - precisa inicializar" }
   ```

3. Se não existir, inicialize o Git:
   ```powershell
   git init
   git remote add origin https://github.com/MRITSoftware/control-stick.git
   ```

4. Adicione os arquivos do projeto:
   ```powershell
   git add app/ build.gradle.kts settings.gradle.kts gradle.properties gradlew gradlew.bat gradle/ .gitignore .github/ *.md SETUP_*.sql
   ```

5. Faça o commit:
   ```powershell
   git commit -m "feat: Atualização para novo banco Supabase e suporte a PWA URLs"
   ```

6. Configure a branch main e faça o push:
   ```powershell
   git branch -M main
   git push -u origin main
   ```

**Nota**: Se o repositório remoto já tiver conteúdo, você pode precisar usar `git push -u origin main --force` (use com cuidado).

## GitHub Actions

O workflow de build do APK está configurado em `.github/workflows/build.yml`. Ele irá:
- Buildar o APK automaticamente em cada push para `main`, `master` ou `develop`
- Gerar tanto o APK de release quanto o de debug
- Disponibilizar os APKs como artifacts para download

## Arquivos incluídos no commit

- Código fonte do app Android (`app/`)
- Arquivos de configuração do Gradle
- Documentação (arquivos `.md`)
- Scripts SQL de setup (`SETUP_*.sql`)
- Workflow do GitHub Actions
- `.gitignore` atualizado

## Arquivos ignorados

- `.gradle/` (cache do Gradle)
- `build/` (arquivos compilados)
- `AppData/`, `Downloads/`, `Desktop/` (diretórios do sistema)
- `mrit/`, `mrit_functions/`, `functions/`, `myenv/` (venvs Python)
- Arquivos temporários e de sistema
