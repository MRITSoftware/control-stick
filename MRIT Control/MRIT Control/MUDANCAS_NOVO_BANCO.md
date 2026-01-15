# 🔄 Mudanças para Novo Banco de Dados

## 📋 Resumo das Alterações

O sistema foi atualizado para usar um novo banco de dados Supabase com uma estrutura diferente. Agora o sistema suporta:

1. ✅ **Nova tabela `lista_sticktv`** - Substitui a tabela `devices`
2. ✅ **Suporte para URLs/PWA** - Pode abrir URLs diretamente no navegador
3. ✅ **Novas credenciais do Supabase** - URL e chave atualizadas

## 🔧 Configuração do Banco de Dados

### 1. Executar Script SQL

Execute o script `SETUP_LISTA_STICKTV.sql` no SQL Editor do Supabase. Este script cria **DUAS tabelas**:

1. **`lista_sticktv`** - Para registrar dispositivos e suas configurações
2. **`device_commands`** - Para comandos remotos (ex: reiniciar app)

### 2. Estrutura das Tabelas

#### Tabela `lista_sticktv`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | UUID | ID único do registro |
| `codigo_dispositivo` | TEXT | Código único do dispositivo (Android ID) |
| `nome` | TEXT | Nome da unidade/localização |
| `url_pwa` | TEXT | URL do PWA para abrir no dispositivo |
| `ativo` | BOOLEAN | Se o dispositivo está ativo |
| `kiosk_mode` | BOOLEAN | Modo kiosk ativo (bloqueia minimização) |
| `criado_em` | TIMESTAMP | Data de criação |
| `ultima_atualizacao` | TIMESTAMP | Última atualização |

#### Tabela `device_commands`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | UUID | ID único do comando |
| `device_id` | TEXT | Código do dispositivo (deve corresponder a `codigo_dispositivo`) |
| `command` | TEXT | Tipo de comando (ex: `restart_app`) |
| `executed` | BOOLEAN | Se o comando foi executado |
| `created_at` | TIMESTAMP | Data de criação do comando |
| `executed_at` | TIMESTAMP | Data de execução do comando |

## 🚀 Como Usar

### Opção 1: Configurar URL Manualmente no App

1. Abra o app **MRIT Control**
2. Clique em **"Ou inserir URL do PWA"**
3. Digite a URL (ex: `https://app.muraltv.com.br`)
4. Confirme

### Opção 2: Configurar URL no Banco de Dados

Execute no Supabase SQL Editor:

```sql
-- Inserir novo dispositivo com URL
INSERT INTO lista_sticktv (codigo_dispositivo, nome, url_pwa, ativo, kiosk_mode)
VALUES ('SEU_DEVICE_ID', 'Nome da Unidade', 'https://app.muraltv.com.br', true, true);

-- Ou atualizar URL existente
UPDATE lista_sticktv 
SET url_pwa = 'https://app.muraltv.com.br', 
    ultima_atualizacao = NOW()
WHERE codigo_dispositivo = 'SEU_DEVICE_ID';
```

### Obter Device ID

O Device ID é o Android ID do dispositivo. Você pode obtê-lo:

1. **Via App**: Abra o app e veja na tela de Status
2. **Via ADB**: `adb shell settings get secure android_id`
3. **Via Logs**: Procure por `DeviceIdManager` nos logs

## 🔄 Como Funciona

### No Boot do Dispositivo

1. Dispositivo liga/reinicia
2. App detecta o boot automaticamente
3. Verifica se há URL configurada:
   - **Primeiro**: Busca na configuração local (SharedPreferences)
   - **Se não encontrar**: Busca no banco de dados Supabase
4. Aguarda conexão com internet
5. Abre a URL no navegador padrão

### Modo Kiosk

Quando `kiosk_mode = true` no banco:

- ✅ Navegador não pode ser minimizado
- ✅ Se fechar, reabre automaticamente
- ✅ Monitoramento a cada 500ms para garantir que está aberto

### Reiniciar App/URL Remotamente

O app monitora comandos na tabela `device_commands` a cada 30 segundos. Para reiniciar:

```sql
-- Criar comando de reiniciar
INSERT INTO device_commands (device_id, command, executed)
VALUES ('SEU_DEVICE_ID', 'restart_app', false);
```

**⚠️ IMPORTANTE**: O `device_id` na tabela `device_commands` deve corresponder ao `codigo_dispositivo` da tabela `lista_sticktv`.

## 📝 Exemplos de Uso

### Exemplo 1: Registrar Novo Dispositivo

```sql
INSERT INTO lista_sticktv (codigo_dispositivo, nome, url_pwa, ativo, kiosk_mode)
VALUES ('a2674df4a688c7d7', 'Sala 01', 'https://app.muraltv.com.br', true, true);
```

### Exemplo 2: Atualizar URL

```sql
UPDATE lista_sticktv 
SET url_pwa = 'https://novoapp.muraltv.com.br',
    ultima_atualizacao = NOW()
WHERE codigo_dispositivo = 'a2674df4a688c7d7';
```

### Exemplo 3: Ativar Modo Kiosk

```sql
UPDATE lista_sticktv 
SET kiosk_mode = true,
    ultima_atualizacao = NOW()
WHERE codigo_dispositivo = 'a2674df4a688c7d7';
```

### Exemplo 4: Ver Todos os Dispositivos

```sql
SELECT codigo_dispositivo, nome, url_pwa, ativo, kiosk_mode, ultima_atualizacao
FROM lista_sticktv
ORDER BY ultima_atualizacao DESC;
```

### Exemplo 5: Criar Comando para Reiniciar App/URL

```sql
-- Reiniciar app/URL de um dispositivo específico
INSERT INTO device_commands (device_id, command, executed)
VALUES ('a2674df4a688c7d7', 'restart_app', false);
```

### Exemplo 6: Ver Comandos Pendentes

```sql
-- Ver comandos pendentes de um dispositivo
SELECT * FROM device_commands 
WHERE device_id = 'a2674df4a688c7d7' 
  AND command = 'restart_app' 
  AND executed = false
ORDER BY created_at DESC;
```

## ⚠️ Notas Importantes

1. **URL deve começar com `http://` ou `https://`**
2. **O app busca URL do banco se não tiver configuração local**
3. **Modo kiosk funciona tanto para apps quanto para URLs**
4. **Navegador padrão do dispositivo será usado para abrir URLs**
5. **Para garantir que não abra outros apps, use modo kiosk ativo**

## 🔍 Troubleshooting

### URL não abre

1. Verifique se a URL está correta no banco
2. Verifique se há internet disponível
3. Verifique logs: `adb logcat | grep BootService`

### Modo kiosk não funciona

1. Verifique se `kiosk_mode = true` no banco
2. Verifique logs: `adb logcat | grep KioskModeService`
3. Aguarde até 10 segundos para o serviço detectar mudanças

### Dispositivo não registra

1. Verifique conexão com internet
2. Verifique credenciais do Supabase no código
3. Verifique logs: `adb logcat | grep SupabaseManager`

---

**Versão**: 2.0  
**Data**: 2025  
**Banco**: base.muraltv.com.br
