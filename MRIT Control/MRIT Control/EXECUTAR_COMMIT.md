# Como Fazer o Commit e Push

## ⚠️ IMPORTANTE
O repositório Git precisa estar no diretório do projeto, não no diretório home. Execute os comandos abaixo no diretório correto.

## Solução Rápida

### Passo 1: Abra o PowerShell e navegue até o diretório do projeto

```powershell
cd "D:\VISION\Atualizações\Control MRIT\MRIT Control\MRIT Control"
```

**OU** se estiver no diretório do workspace (`d:\VISION\Atualizações\Control MRIT\MRIT Control`):

```powershell
cd "MRIT Control"
```

### Passo 2: Execute os comandos Git

```powershell
# Inicializa o Git (se ainda não foi feito)
if (-not (Test-Path .git)) { 
    git init
    git remote add origin https://github.com/MRITSoftware/control-stick.git 
} else {
    git remote set-url origin https://github.com/MRITSoftware/control-stick.git
}

# Verifica o remote
git remote -v

# Adiciona os arquivos
git add .gitignore .github/workflows/build.yml gradlew gradlew.bat app/ build.gradle.kts settings.gradle.kts gradle.properties gradle/ *.md SETUP_*.sql

# Faz o commit
git commit -m "feat: Atualizacao para novo banco Supabase e suporte a PWA URLs"

# Configura a branch e faz o push
git branch -M main
git push -u origin main
```

## Repositório Remoto
- **URL**: https://github.com/MRITSoftware/control-stick.git
- **Branch**: main

## Arquivos incluídos no commit
- `.gitignore`
- `.github/workflows/build.yml`
- `gradlew` e `gradlew.bat`
- `app/` (código fonte)
- `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
- `gradle/`
- Todos os arquivos `.md` (documentação)
- Todos os arquivos `SETUP_*.sql` (scripts SQL)

## Verificação

Após o push, verifique no GitHub:
- https://github.com/MRITSoftware/control-stick

O GitHub Actions irá gerar o APK automaticamente após o push.
