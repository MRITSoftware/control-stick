# 📋 Como Verificar Logs do Reboot

## 🔍 Método 1: Via Android Studio

1. Conecte o dispositivo via USB
2. Abra o Android Studio
3. Vá em **View → Tool Windows → Logcat**
4. Filtre por: `RebootMonitorService` ou `RebootManager`
5. Procure por mensagens de erro (vermelho) ou avisos (amarelo)

## 🔍 Método 2: Via ADB (se estiver instalado)

### Instalar ADB (se não tiver)

**Windows:**
1. Baixe o Android Platform Tools: https://developer.android.com/studio/releases/platform-tools
2. Extraia e adicione ao PATH do sistema
3. Ou use o caminho completo: `C:\Users\SeuUsuario\AppData\Local\Android\Sdk\platform-tools\adb.exe`

### Comandos Úteis

```bash
# Ver logs em tempo real (filtrado)
adb logcat | grep -E "RebootMonitorService|RebootManager|DeviceAdmin"

# Ver logs completos e salvar em arquivo
adb logcat > logs.txt

# Limpar logs antigos
adb logcat -c

# Ver logs apenas de erro
adb logcat *:E

# Ver logs do app específico
adb logcat | grep "com.bootreceiver.app"
```

## 🔍 Método 3: Via App (StatusActivity)

O app tem uma tela de status que mostra informações importantes:

1. Abra o app **MRIT Control**
2. Procure pela opção de **Status** ou **Diagnóstico**
3. Verifique:
   - Device Admin está ativo?
   - Device ID correto?
   - Serviço rodando?

## 📝 O Que Procurar nos Logs

### ✅ Logs de Sucesso

```
RebootMonitorService: COMANDO DE REINICIAR ENCONTRADO!
RebootManager: ✅ Comando de reiniciar enviado via DevicePolicyManager.reboot()
RebootMonitorService: ✅✅✅ COMANDO DE REINICIAR ENVIADO! ✅✅✅
```

### ❌ Logs de Erro Comuns

#### Erro 1: Device Admin Não Está Ativo
```
RebootManager: ⚠️ Device Admin não está ativo - método 1 não disponível
RebootMonitorService: ❌❌❌ Device Admin NÃO está ativo! ❌❌❌
```

**Solução:**
- Vá em: **Configurações → Segurança → Administradores do dispositivo**
- Ative **MRIT Control**
- **IMPORTANTE**: Reinstale o app após ativar

#### Erro 2: SecurityException
```
RebootManager: ❌ DevicePolicyManager.reboot() falhou por segurança: ...
```

**Possíveis causas:**
- Device Admin não está realmente ativo
- Política `<reboot />` não foi aplicada (precisa reinstalar app)
- Fabricante bloqueou reboot remoto

**Solução:**
1. Desative Device Admin
2. Reinstale o app
3. Ative Device Admin novamente
4. Teste novamente

#### Erro 3: UnsupportedOperationException
```
RebootManager: ❌ DevicePolicyManager.reboot() não suportado: ...
```

**Causa:** Dispositivo/fabricante não suporta reboot via DevicePolicyManager

**Soluções:**
- Alguns dispositivos Android TV/Stick não suportam
- Pode ser necessário root
- Verifique se o dispositivo suporta Device Admin reboot

#### Erro 4: API Level Muito Antigo
```
RebootManager: ⚠️ API level X é muito antigo para DevicePolicyManager.reboot()
```

**Causa:** Android versão muito antiga (precisa Android 7.0+)

**Solução:** Atualize o Android ou use método alternativo (root)

#### Erro 5: Todos os Métodos Falharam
```
RebootManager: ❌ Todos os métodos de reiniciar falharam.
```

**Causa:** Nenhum método funcionou (Device Admin, PowerManager, su, reboot)

**Soluções:**
1. Verifique Device Admin está ativo
2. Verifique se app foi reinstalado após adicionar `<reboot />`
3. Alguns dispositivos simplesmente não suportam reboot remoto

## 🔧 Checklist de Diagnóstico

Execute estes passos em ordem:

### 1. Verificar Device Admin

**Via ADB:**
```bash
adb shell dumpsys device_policy | grep -A 10 "com.bootreceiver.app"
```

**Deve mostrar:**
```
Admin #0: ComponentInfo{com.bootreceiver.app/com.bootreceiver.app.receiver.DeviceAdminReceiver}
```

**Se não mostrar:** Device Admin não está ativo

### 2. Verificar Política de Reboot

**Via ADB:**
```bash
adb shell dumpsys device_policy | grep -A 20 "com.bootreceiver.app" | grep -i reboot
```

**Deve mostrar políticas ativas** (pode não mostrar explicitamente, mas se Device Admin está ativo e app foi reinstalado, deve funcionar)

### 3. Verificar Logs Durante Tentativa

1. Limpe os logs: `adb logcat -c`
2. Crie um comando de reboot no Supabase
3. Aguarde 30-60 segundos
4. Veja os logs: `adb logcat | grep -E "RebootMonitorService|RebootManager"`

### 4. Verificar Versão do Android

**Via ADB:**
```bash
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
```

**Precisa:** Android 7.0+ (API 24+)

## 📱 Dispositivos Conhecidos com Problemas

### Android TV/Stick
- Alguns modelos não suportam `DevicePolicyManager.reboot()`
- Pode funcionar apenas com root

### Xiaomi/Huawei/Samsung
- Podem bloquear reboot remoto por segurança
- Verifique configurações de segurança do fabricante

## 🚀 Solução Rápida

Se nada funcionar, tente:

1. **Desinstalar completamente o app**
2. **Reinstalar o app** (versão mais recente com `<reboot />`)
3. **Ativar Device Admin** imediatamente após instalar
4. **Reiniciar o dispositivo** manualmente uma vez
5. **Testar novamente** o reboot remoto

## 📞 Informações para Suporte

Se precisar de ajuda, forneça:

1. **Logs completos** (últimos 100 linhas)
2. **Marca e modelo do dispositivo**
3. **Versão do Android** (`adb shell getprop ro.build.version.release`)
4. **API Level** (`adb shell getprop ro.build.version.sdk`)
5. **Status do Device Admin** (`adb shell dumpsys device_policy | grep bootreceiver`)

---

**Dica:** Salve os logs em um arquivo para análise:
```bash
adb logcat > logs_reboot.txt
```
