# 🔍 Diagnóstico Rápido - App Não Abre no Boot

## ⚡ Verificação Rápida (5 minutos)

### 1. Verificar se o Receiver Está Registrado

```bash
adb shell dumpsys package com.bootreceiver.app | grep -A 10 "receiver"
```

**Deve mostrar:**
```
Receiver #0: com.bootreceiver.app/.receiver.BootReceiver
```

**Se não mostrar nada**: O app não foi aberto manualmente após instalação.

### 2. Verificar se o App Está Configurado

```bash
adb shell run-as com.bootreceiver.app cat /data/data/com.bootreceiver.app/shared_prefs/BootReceiverPrefs.xml
```

**Deve mostrar algo como:**
```xml
<string name="target_package_name">com.exemplo.app</string>
```

### 3. Verificar Logs do Boot

```bash
# Limpar logs antigos
adb logcat -c

# Ver logs em tempo real (aguarde alguns segundos)
adb logcat | grep -E "BootReceiver|BootService"
```

**O que procurar:**
- `BootReceiver: Boot detectado!` - Receiver recebeu o evento
- `BootService: BootService iniciado` - Serviço iniciou
- `BootService: Internet disponível!` - Internet detectada
- `AppLauncher: App aberto com sucesso` - App foi aberto

### 4. Testar Manualmente (Sem Reiniciar)

```bash
# Simular boot
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED

# Verificar logs imediatamente
adb logcat | grep -E "BootReceiver|BootService"
```

## 🐛 Problemas Comuns e Soluções

### Problema 1: Receiver Não Está Registrado

**Sintoma**: `dumpsys` não mostra o receiver

**Solução**:
1. Abra o app manualmente pelo menos uma vez
2. Verifique novamente com `dumpsys`
3. Se ainda não aparecer, reinstale o app

### Problema 2: App Não Está Configurado

**Sintoma**: `BootReceiverPrefs.xml` está vazio ou não existe

**Solução**:
1. Abra o app manualmente
2. Selecione um app na lista
3. Verifique novamente o arquivo de preferências

### Problema 3: BootReceiver Não Recebe o Evento

**Sintoma**: Logs não mostram "Boot detectado!"

**Soluções**:
1. Verifique se o app foi aberto manualmente após instalação
2. Verifique permissões: `adb shell dumpsys package com.bootreceiver.app | grep permission`
3. Tente reinstalar o app

### Problema 4: BootService Não Inicia

**Sintoma**: Logs mostram "Boot detectado!" mas não mostram "BootService iniciado"

**Solução**: Pode ser problema de permissões ou Android 10+. Verifique logs de erro.

### Problema 5: Internet Não Detectada

**Sintoma**: Logs mostram "Internet não disponível"

**Solução**:
1. Verifique se WiFi está conectado
2. Aguarde - o app tenta a cada 10 segundos
3. Verifique: `adb shell ping 8.8.8.8`

## ✅ Checklist Completo

Execute estes comandos em ordem:

```bash
# 1. Verificar receiver
adb shell dumpsys package com.bootreceiver.app | grep -A 10 "receiver"

# 2. Verificar configuração
adb shell run-as com.bootreceiver.app cat /data/data/com.bootreceiver.app/shared_prefs/BootReceiverPrefs.xml

# 3. Limpar logs
adb logcat -c

# 4. Testar manualmente
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED

# 5. Ver logs (aguarde 10 segundos)
adb logcat | grep -E "BootReceiver|BootService|AppLauncher"
```

## 🔧 Solução Rápida

Se nada funcionar, tente:

```bash
# 1. Limpar dados do app
adb shell pm clear com.bootreceiver.app

# 2. Reinstalar o app
adb install -r app-debug.apk

# 3. Abrir manualmente
adb shell am start -n com.bootreceiver.app/.ui.AppSelectionActivity

# 4. Configurar app alvo (via interface)

# 5. Testar
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

## 📱 Para Smartphones Específicos

Alguns fabricantes (Xiaomi, Huawei, Samsung) podem bloquear apps em background.

**Soluções**:
1. Desabilite otimização de bateria para o app
2. Adicione o app à lista de apps permitidos em background
3. Desabilite modo de economia de energia

---

**Execute os comandos acima e me envie os resultados para diagnóstico preciso!**
