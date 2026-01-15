# 🔄 Alternativas para Reboot no Android 15

## ⚠️ Problema Identificado

No **Android 15**, o método `DevicePolicyManager.reboot()` requer que o app seja **Device Owner**, não apenas **Device Admin**.

### Diferença Crítica:

- **Device Admin**: Permissões limitadas, não pode reiniciar no Android 15
- **Device Owner**: Permissões completas, pode reiniciar

## 🔍 Por Que Não Funciona?

### Android 15 - Mudanças de Segurança

1. **DevicePolicyManager.reboot()** agora requer **Device Owner**
2. **Soft Restart** foi deprecado
3. Restrições de segurança mais rigorosas

### Limitação do Fabricante

Alguns fabricantes bloqueiam reboot remoto mesmo com Device Owner:
- Xiaomi
- Huawei  
- Samsung (alguns modelos)
- Outros

## 🚀 Alternativas Implementadas

O código já tenta múltiplos métodos automaticamente:

1. ✅ **DevicePolicyManager.reboot()** (requer Device Owner no Android 15)
2. ✅ **PowerManager.reboot()** (requer app de sistema)
3. ✅ **Runtime.exec com su** (requer root)
4. ✅ **Runtime.exec com reboot** (pode funcionar em alguns dispositivos)
5. ✅ **Intent ACTION_REBOOT** (novo - pode funcionar)
6. ✅ **am broadcast** (novo - workaround)

## 💡 Soluções Práticas

### Opção 1: Tornar o App Device Owner (Recomendado para Enterprise)

**Device Owner** só pode ser configurado durante a configuração inicial do dispositivo ou via ADB em dispositivos não configurados.

**Via ADB (requer dispositivo não configurado):**
```bash
adb shell dpm set-device-owner com.bootreceiver.app/.receiver.DeviceAdminReceiver
```

**Limitações:**
- Só funciona em dispositivos que ainda não foram configurados
- Ou dispositivos resetados para fábrica
- Não funciona em dispositivos já em uso

### Opção 2: Usar Root (Se Disponível)

Se o dispositivo tiver root, o método 3 (su -c reboot) deve funcionar.

**Verificar se tem root:**
```bash
adb shell su -c "id"
```

Se retornar `uid=0(root)`, o dispositivo tem root.

### Opção 3: Usar App de Sistema

Instalar o app como app de sistema permite usar `PowerManager.reboot()`. Isso requer:
- Root
- Ou instalação via recovery
- Ou firmware customizado

### Opção 4: Workaround - Reiniciar Manualmente

Se nenhum método funcionar, você pode:

1. **Notificar o usuário** para reiniciar manualmente
2. **Usar notificação** com instruções
3. **Abrir configurações** de reiniciar

**Implementação:**
```kotlin
// Abrir tela de opções de energia (pode ter opção de reiniciar)
val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
context.startActivity(intent)
```

### Opção 5: Usar ADB Remoto (Se Disponível)

Se o dispositivo tiver ADB habilitado e acessível remotamente:

```bash
adb -s <device_id> reboot
```

**Habilitar ADB via rede:**
```bash
adb tcpip 5555
adb connect <ip_do_dispositivo>:5555
```

## 🔧 Implementação de Notificação para Reinício Manual

Se nenhum método automático funcionar, podemos implementar uma notificação que orienta o usuário:

```kotlin
private fun showRebootNotification() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
    val pendingIntent = PendingIntent.getActivity(
        this, 0, intent, 
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("⚠️ Reiniciar Dispositivo")
        .setContentText("Toque para abrir opções de energia e reiniciar manualmente")
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(false)
        .build()
    
    notificationManager.notify(REBOOT_NOTIFICATION_ID, notification)
}
```

## 📋 Checklist de Alternativas

- [ ] Tentar tornar app Device Owner (via ADB, se dispositivo não configurado)
- [ ] Verificar se dispositivo tem root (su -c reboot)
- [ ] Instalar como app de sistema (requer root/recovery)
- [ ] Usar notificação para reinício manual
- [ ] Habilitar ADB via rede e usar reboot remoto
- [ ] Verificar se fabricante permite reboot remoto

## 🎯 Recomendação

Para **Digital Signage** em produção:

1. **Configure dispositivos como Device Owner** durante setup inicial
2. **Ou use dispositivos com root** (se permitido)
3. **Ou implemente notificação** para reinício manual quando necessário
4. **Ou use ADB remoto** se acessível

## 📝 Nota Importante

**Android 15** tornou o reboot remoto muito mais restritivo por segurança. Isso é intencional do Google para prevenir apps maliciosos de reiniciar dispositivos.

Para apps legítimos de gerenciamento de dispositivos (MDM), a solução é usar **Device Owner** ou **root**.

---

**Se nenhuma alternativa funcionar, considere implementar notificação para reinício manual ou usar ADB remoto.**
