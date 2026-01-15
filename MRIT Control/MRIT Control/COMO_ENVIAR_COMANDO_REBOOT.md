# Como Enviar Comando de Reiniciar Dispositivo

Este guia explica como enviar comandos de reiniciar para dispositivos através do Supabase.

## 📋 Pré-requisitos

1. **Device ID do dispositivo**: Você precisa do Android ID do dispositivo
2. **Acesso ao Supabase**: Painel do Supabase ou SQL Editor
3. **Tabela criada**: A tabela `device_commands` deve estar criada (veja `SUPABASE_SETUP.sql`)

## 🔍 Como Obter o Device ID

### Opção 1: Via Logs do App
1. Conecte o dispositivo via USB
2. Execute: `adb logcat | grep DeviceIdManager`
3. O Device ID será exibido nos logs quando o app iniciar

### Opção 2: Via App (se tiver interface)
O app pode exibir o Device ID na tela inicial (se implementado)

### Opção 3: Via ADB direto
```bash
adb shell settings get secure android_id
```

## 📝 Método 1: Via SQL Editor do Supabase

1. Acesse o **Supabase Dashboard**: https://supabase.com/dashboard
2. Selecione seu projeto
3. Vá em **SQL Editor**
4. Execute o seguinte SQL:

```sql
-- Enviar comando de reiniciar para um dispositivo específico
INSERT INTO device_commands (device_id, command, executed)
VALUES ('SEU_DEVICE_ID_AQUI', 'reboot', false);
```

**Exemplo:**
```sql
INSERT INTO device_commands (device_id, command, executed)
VALUES ('abc123def456789', 'reboot', false);
```

## 📝 Método 2: Via Table Editor do Supabase

1. Acesse o **Supabase Dashboard**
2. Vá em **Table Editor**
3. Selecione a tabela `device_commands`
4. Clique em **Insert row**
5. Preencha:
   - `device_id`: ID do dispositivo
   - `command`: `reboot`
   - `executed`: `false`
6. Clique em **Save**

## 📝 Método 3: Via Script Python

Crie um arquivo `enviar_comando_reboot.py`:

```python
import requests
import json

# Configurações do Supabase
SUPABASE_URL = "https://kihyhoqbrkwbfudttevo.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpaHlob3Ficmt3YmZ1ZHR0ZXZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTU1NTUwMjcsImV4cCI6MjAzMTEzMTAyN30.XtBTlSiqhsuUIKmhAMEyxofV-dRst7240n912m4O4Us"

def enviar_comando_reboot(device_id):
    """Envia comando de reiniciar para um dispositivo"""
    url = f"{SUPABASE_URL}/rest/v1/reboot_commands"
    headers = {
        "apikey": SUPABASE_KEY,
        "Authorization": f"Bearer {SUPABASE_KEY}",
        "Content-Type": "application/json",
        "Prefer": "return=representation"
    }
    
    data = {
        "device_id": device_id,
        "command": "reboot",
        "executed": False
    }
    
    try:
        response = requests.post(url, headers=headers, json=data)
        response.raise_for_status()
        print(f"✅ Comando enviado com sucesso para dispositivo: {device_id}")
        print(f"Resposta: {response.json()}")
        return True
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro ao enviar comando: {e}")
        if hasattr(e.response, 'text'):
            print(f"Detalhes: {e.response.text}")
        return False

def listar_comandos_pendentes(device_id=None):
    """Lista comandos pendentes (não executados)"""
    url = f"{SUPABASE_URL}/rest/v1/reboot_commands"
    headers = {
        "apikey": SUPABASE_KEY,
        "Authorization": f"Bearer {SUPABASE_KEY}"
    }
    
    params = {
        "should_reboot": "eq.true",
        "executed": "eq.false",
        "order": "created_at.desc"
    }
    
    if device_id:
        params["device_id"] = f"eq.{device_id}"
    
    try:
        response = requests.get(url, headers=headers, params=params)
        response.raise_for_status()
        comandos = response.json()
        
        if comandos:
            print(f"\n📋 Comandos pendentes encontrados: {len(comandos)}")
            for cmd in comandos:
                print(f"  - ID: {cmd['id']}, Device: {cmd['device_id']}, Criado: {cmd.get('created_at')}")
        else:
            print("✅ Nenhum comando pendente")
        
        return comandos
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro ao listar comandos: {e}")
        return []

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) < 2:
        print("Uso: python enviar_comando_reboot.py <device_id>")
        print("\nExemplo:")
        print("  python enviar_comando_reboot.py abc123def456789")
        sys.exit(1)
    
    device_id = sys.argv[1]
    enviar_comando_reboot(device_id)
    
    # Lista comandos pendentes após enviar
    print("\n" + "="*50)
    listar_comandos_pendentes(device_id)
```

**Uso:**
```bash
python enviar_comando_reboot.py abc123def456789
```

## 📝 Método 4: Via cURL (Terminal)

```bash
curl -X POST "https://kihyhoqbrkwbfudttevo.supabase.co/rest/v1/device_commands" \
  -H "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpaHlob3Ficmt3YmZ1ZHR0ZXZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTU1NTUwMjcsImV4cCI6MjAzMTEzMTAyN30.XtBTlSiqhsuUIKmhAMEyxofV-dRst7240n912m4O4Us" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpaHlob3Ficmt3YmZ1ZHR0ZXZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTU1NTUwMjcsImV4cCI6MjAzMTEzMTAyN30.XtBTlSiqhsuUIKmhAMEyxofV-dRst7240n912m4O4Us" \
  -H "Content-Type: application/json" \
  -H "Prefer: return=representation" \
  -d '{
    "device_id": "SEU_DEVICE_ID_AQUI",
    "command": "reboot",
    "executed": false
  }'
```

## ✅ Verificar Status do Comando

### Ver comandos pendentes:
```sql
SELECT * FROM device_commands 
WHERE device_id = 'SEU_DEVICE_ID_AQUI' 
  AND command = 'reboot' 
  AND executed = false
ORDER BY created_at DESC;
```

### Ver todos os comandos (executados e pendentes):
```sql
SELECT * FROM device_commands 
WHERE device_id = 'SEU_DEVICE_ID_AQUI' 
ORDER BY created_at DESC;
```

### Ver comandos executados:
```sql
SELECT * FROM device_commands 
WHERE device_id = 'SEU_DEVICE_ID_AQUI' 
  AND executed = true
ORDER BY executed_at DESC;
```

## ⚠️ Importante

1. **Device Admin**: O dispositivo precisa ter o app configurado como Device Admin para poder reiniciar
2. **Monitoramento**: O app verifica comandos a cada 30 segundos (configurável)
3. **Execução única**: Cada comando é marcado como executado após ser processado
4. **Múltiplos dispositivos**: Você pode enviar comandos para diferentes dispositivos usando seus respectivos Device IDs

## 🔄 Fluxo Completo

1. **Enviar comando** → Insere registro na tabela `device_commands`
2. **App detecta** → `RebootMonitorService` verifica periodicamente
3. **Comando encontrado** → App marca como executado
4. **Reiniciar** → App tenta reiniciar o dispositivo
5. **Verificar** → Confirme que `executed = true` na tabela

## 📱 Teste Rápido

1. Obtenha o Device ID do dispositivo
2. Envie um comando via SQL ou script
3. Aguarde até 30 segundos (tempo de verificação do app)
4. O dispositivo deve reiniciar (se Device Admin estiver ativo)
