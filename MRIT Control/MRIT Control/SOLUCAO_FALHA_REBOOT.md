# 🔧 Solução para Falha ao Reiniciar

## ⚠️ Problema Comum

Ao tentar reiniciar o dispositivo, você recebe a mensagem: **"Falha ao reiniciar dispositivo"**

## 🔍 Diagnóstico Rápido

### 1. Verificar Device Admin

**No app:**
1. Abra o app **MRIT Control**
2. Vá na tela de **Status** (se disponível)
3. Verifique se mostra: **"✅ Device Admin ATIVO"**

**Via ADB (se disponível):**
```bash
adb shell dumpsys device_policy | grep -A 10 "com.bootreceiver.app"
```

**Deve mostrar:**
```
Admin #0: ComponentInfo{com.bootreceiver.app/com.bootreceiver.app.receiver.DeviceAdminReceiver}
```

### 2. Verificar se App Foi Reinstalado

**⚠️ CRÍTICO:** Se você instalou o app ANTES de adicionar a política `<reboot />`, você PRECISA:

1. **Desinstalar completamente o app**
2. **Reinstalar o app** (versão mais recente)
3. **Ativar Device Admin** imediatamente após instalar
4. **Testar novamente**

**Por quê?** A política `<reboot />` só é aplicada quando o Device Admin é ativado. Se você ativou o Device Admin ANTES de ter a política no código, ela não foi aplicada.

### 3. Verificar Versão do Android

**Precisa:** Android 7.0+ (API 24+)

**Verificar:**
- No app, tela de Status mostra a versão
- Ou: **Configurações → Sobre o dispositivo → Versão do Android**

## 🚀 Solução Passo a Passo

### Passo 1: Desativar Device Admin

1. Vá em: **Configurações → Segurança → Administradores do dispositivo**
2. Encontre **MRIT Control**
3. **Desative** (desmarque)

### Passo 2: Desinstalar App

1. **Configurações → Apps → MRIT Control → Desinstalar**
2. Ou via ADB: `adb uninstall com.bootreceiver.app`

### Passo 3: Reinstalar App

1. Instale a versão mais recente do APK
2. **NÃO abra o app ainda**

### Passo 4: Ativar Device Admin

1. Vá em: **Configurações → Segurança → Administradores do dispositivo**
2. Encontre **MRIT Control**
3. **Ative** (marque)
4. Aceite a confirmação

### Passo 5: Abrir App e Testar

1. Abra o app **MRIT Control**
2. Vá na tela de **Status**
3. Verifique se mostra: **"✅ Device Admin ATIVO"**
4. Clique em **"Testar Reboot Agora"**
5. Confirme o reboot

## 📋 Verificar Logs

### Via Android Studio

1. Conecte dispositivo via USB
2. Abra Android Studio
3. **View → Tool Windows → Logcat**
4. Filtre por: `RebootManager` ou `RebootMonitorService`
5. Procure por mensagens de erro (vermelho)

### Via ADB

```bash
# Ver logs em tempo real
adb logcat | grep -E "RebootManager|RebootMonitorService"

# Ver apenas erros
adb logcat *:E | grep -E "RebootManager|RebootMonitorService"

# Salvar logs em arquivo
adb logcat > logs.txt
```

### O Que Procurar nos Logs

#### ✅ Sucesso
```
RebootManager: ✅ Comando de reiniciar enviado via DevicePolicyManager.reboot()
```

#### ❌ Erro: Device Admin Não Ativo
```
RebootManager: ⚠️ Device Admin não está ativo - método 1 não disponível
```

**Solução:** Ative Device Admin e reinstale o app

#### ❌ Erro: SecurityException
```
RebootManager: ❌ DevicePolicyManager.reboot() falhou por segurança
```

**Solução:** 
- Reinstale o app após ativar Device Admin
- Verifique se `device_admin.xml` tem `<reboot />`

#### ❌ Erro: UnsupportedOperationException
```
RebootManager: ❌ DevicePolicyManager.reboot() não suportado
```

**Causa:** Dispositivo/fabricante não suporta reboot remoto

**Soluções:**
- Alguns Android TV/Stick não suportam
- Pode precisar de root
- Verifique documentação do fabricante

#### ❌ Erro: API Level Muito Antigo
```
RebootManager: ⚠️ API level X é muito antigo
```

**Solução:** Atualize o Android (precisa Android 7.0+)

## 🔧 Checklist Completo

Execute em ordem:

- [ ] Device Admin está ativo?
- [ ] App foi reinstalado após adicionar `<reboot />`?
- [ ] Android versão 7.0+ (API 24+)?
- [ ] `device_admin.xml` tem `<reboot />`?
- [ ] Logs mostram algum erro específico?
- [ ] Fabricante permite reboot remoto?

## 📱 Dispositivos com Problemas Conhecidos

### Android TV/Stick
- Alguns modelos não suportam `DevicePolicyManager.reboot()`
- Pode funcionar apenas com root

### Xiaomi/Huawei/Samsung
- Podem bloquear reboot remoto por segurança
- Verifique configurações de segurança do fabricante

## 💡 Dica

**Use a tela de Status do app** para verificar rapidamente:
- Device Admin está ativo?
- API Level é suficiente?
- Serviço está rodando?
- Há comandos pendentes?

A tela de Status agora mostra informações mais detalhadas sobre o problema!

---

**Se ainda não funcionar após seguir todos os passos, verifique os logs e me envie as mensagens de erro específicas.**
