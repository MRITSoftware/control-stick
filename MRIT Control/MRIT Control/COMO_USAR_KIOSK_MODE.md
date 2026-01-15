# 🔒 Como Usar o Modo Kiosk

## 📱 O que é o Modo Kiosk?

O **Modo Kiosk** é uma funcionalidade que **bloqueia a minimização** do app configurado, mantendo-o sempre fixo na tela. É ideal para Digital Signage onde você não quer que o app seja minimizado acidentalmente.

## 🚀 Configuração Inicial

### Passo 1: Adicionar Coluna no Supabase

Execute o script SQL no Supabase SQL Editor:

```sql
-- Adiciona coluna kiosk_mode na tabela devices
ALTER TABLE devices 
ADD COLUMN IF NOT EXISTS kiosk_mode BOOLEAN DEFAULT false;

-- Atualiza dispositivos existentes
UPDATE devices 
SET kiosk_mode = false 
WHERE kiosk_mode IS NULL;
```

**Ou use o arquivo:** `KIOSK_MODE_SETUP.sql`

### Passo 2: Ativar Modo Kiosk

No Supabase SQL Editor, execute:

```sql
-- Ativar modo kiosk para um dispositivo específico
UPDATE devices 
SET kiosk_mode = true 
WHERE device_id = 'SEU_DEVICE_ID_AQUI';
```

**Substitua `SEU_DEVICE_ID_AQUI`** pelo Device ID do dispositivo.

## 🔄 Como Funciona

### Quando `kiosk_mode = true`:

1. **App não pode ser minimizado** - O app configurado fica fixo na tela
2. **Monitoramento automático** - O serviço verifica a cada 10 segundos
3. **Se minimizado, reabre automaticamente** - Se o app for minimizado, é trazido de volta
4. **Mantém em foreground** - Usa flags do Android para manter o app sempre visível

### Quando `kiosk_mode = false`:

1. **App pode ser minimizado normalmente** - Comportamento padrão
2. **Sem restrições** - O app funciona normalmente

## 📋 Exemplos de Uso

### Ativar Modo Kiosk

```sql
-- Para um dispositivo específico
UPDATE devices 
SET kiosk_mode = true 
WHERE device_id = 'a2674df4a688c7d7';
```

### Desativar Modo Kiosk

```sql
-- Para um dispositivo específico
UPDATE devices 
SET kiosk_mode = false 
WHERE device_id = 'a2674df4a688c7d7';
```

### Ativar para Todos os Dispositivos

```sql
UPDATE devices 
SET kiosk_mode = true;
```

### Verificar Status

```sql
-- Ver todos os dispositivos e seu status de kiosk
SELECT device_id, unit_name, kiosk_mode, last_seen 
FROM devices 
ORDER BY last_seen DESC;
```

### Ver Apenas Dispositivos com Kiosk Ativo

```sql
SELECT device_id, unit_name, last_seen 
FROM devices 
WHERE kiosk_mode = true;
```

## ⚡ Mudanças em Tempo Real

O app monitora mudanças no `kiosk_mode` a cada **10 segundos**. Isso significa:

- ✅ **Ativou kiosk?** → App é trazido para frente e fixado
- ✅ **Desativou kiosk?** → App pode ser minimizado normalmente
- ✅ **App minimizado com kiosk ativo?** → App é reaberto automaticamente

## 🔍 Verificar se Está Funcionando

### Via Logs (ADB)

```bash
adb logcat | grep -E "KioskModeService|KioskModeManager"
```

**Procure por:**
- `🔒 MODO KIOSK ATIVADO!`
- `📱 App não está rodando. Abrindo...`
- `⚠️ App minimizado/fechado com kiosk ativo! Reabrindo...`

### Via Supabase

```sql
-- Verificar se kiosk_mode está ativo
SELECT device_id, kiosk_mode 
FROM devices 
WHERE device_id = 'SEU_DEVICE_ID';
```

## ⚠️ Limitações

1. **Não funciona com todos os apps**: Alguns apps podem ter proteções que impedem o modo kiosk
2. **Requer app configurado**: Precisa ter um app configurado no MRIT Control
3. **Android 11+**: Algumas funcionalidades podem ter limitações em versões mais recentes
4. **Não bloqueia botão Home**: O usuário ainda pode pressionar o botão Home (mas o app será reaberto)

## 💡 Dicas

- **Teste primeiro**: Ative o modo kiosk e teste se funciona com seu app específico
- **Use com cuidado**: Modo kiosk pode ser frustrante se ativado acidentalmente
- **Desative quando necessário**: Sempre tenha uma forma de desativar (via Supabase)
- **Monitore logs**: Use os logs para verificar se está funcionando corretamente

## 🛠️ Solução de Problemas

### App não fica fixo

1. Verifique se `kiosk_mode = true` no banco
2. Verifique logs: `adb logcat | grep KioskModeService`
3. Verifique se o app configurado está correto
4. Reinicie o dispositivo

### App continua minimizando

1. Alguns apps têm proteções que impedem modo kiosk
2. Verifique se o serviço está rodando
3. Verifique permissões do app

### Como desativar se ficar travado

Execute no Supabase:

```sql
UPDATE devices 
SET kiosk_mode = false 
WHERE device_id = 'SEU_DEVICE_ID';
```

Aguarde até 10 segundos e o modo kiosk será desativado.

---

**Simples e eficaz!** Use o modo kiosk para manter seu app sempre visível em Digital Signage! 🚀
