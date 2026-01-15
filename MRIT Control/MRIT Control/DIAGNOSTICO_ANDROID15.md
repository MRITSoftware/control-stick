# 🔍 Diagnóstico Específico - Android 15 (API 35)

## 📱 Informações do Dispositivo

- **Android Version**: 15
- **API Level**: 35
- **Device Admin**: ✅ Ativo
- **Serviço**: ✅ Rodando

## ⚠️ Possíveis Problemas com Android 15

### 1. Mudanças no DevicePolicyManager no Android 15

Android 15 introduziu mudanças de segurança que podem afetar `DevicePolicyManager.reboot()`:

- **Políticas mais restritivas**: Alguns métodos podem requerer permissões adicionais
- **Verificação de políticas**: O sistema pode verificar se a política `<reboot />` está realmente aplicada
- **Apps de sistema**: Alguns fabricantes podem restringir reboot remoto apenas para apps de sistema

### 2. Verificar se Política Foi Aplicada

Mesmo com Device Admin ativo, a política `<reboot />` pode não ter sido aplicada se:

- O app foi instalado **antes** de adicionar a política
- Device Admin foi ativado **antes** de reinstalar o app
- O sistema não recarregou as políticas

### 3. Solução para Android 15

#### Passo 1: Verificar Políticas Aplicadas

**Via ADB:**
```bash
adb shell dumpsys device_policy | grep -A 20 "com.bootreceiver.app"
```

**Procure por:**
- Lista de políticas ativas
- Verificação se `<reboot />` está listada

#### Passo 2: Reinstalar com Política Correta

1. **Desative Device Admin**
   - Configurações → Segurança → Administradores do dispositivo → MRIT Control → Desativar

2. **Desinstale o app completamente**
   ```bash
   adb uninstall com.bootreceiver.app
   ```

3. **Reinstale o app** (versão mais recente com `<reboot />`)

4. **Ative Device Admin novamente**
   - Configurações → Segurança → Administradores do dispositivo → MRIT Control → Ativar

5. **Reinicie o dispositivo manualmente uma vez**
   - Isso força o sistema a recarregar todas as políticas

6. **Teste o reboot novamente**

#### Passo 3: Verificar Logs Específicos

Quando tentar o reboot, verifique os logs para ver o erro específico:

```bash
adb logcat | grep -E "RebootManager|DevicePolicyManager|SecurityException"
```

**Erros comuns no Android 15:**

1. **SecurityException**: Política não aplicada ou permissão negada
2. **UnsupportedOperationException**: Método não suportado neste dispositivo
3. **IllegalStateException**: Device Admin não está realmente ativo

### 4. Alternativas para Android 15

Se `DevicePolicyManager.reboot()` não funcionar:

#### Opção A: Usar PowerManager (pode não funcionar)
- Requer app de sistema ou permissão especial
- Geralmente não funciona em apps normais

#### Opção B: Root (se disponível)
- `su -c reboot` pode funcionar
- Requer dispositivo com root

#### Opção C: Verificar se Fabricante Bloqueou

Alguns fabricantes bloqueiam reboot remoto mesmo com Device Admin:
- Xiaomi: Pode bloquear por segurança
- Samsung: Pode requerer configuração adicional
- Outros: Verifique documentação do fabricante

### 5. Teste de Diagnóstico

Execute este teste para verificar o que está acontecendo:

1. **Na tela de Status do app**, clique em **"TESTAR REBOOT AGORA"**

2. **Imediatamente após clicar**, execute:
   ```bash
   adb logcat -c  # Limpa logs
   adb logcat | grep -E "RebootManager|DevicePolicyManager|ERROR|Exception"
   ```

3. **Procure por:**
   - `RebootManager: 🔄 ========== INICIANDO TENTATIVA DE REBOOT ==========`
   - `RebootManager: Device Admin ativo: true`
   - `RebootManager: 🔧 Método 1: Tentando reiniciar via DevicePolicyManager.reboot()...`
   - Qualquer mensagem de erro (SecurityException, UnsupportedOperationException, etc.)

4. **Envie os logs** para análise

### 6. Checklist Específico para Android 15

- [ ] Device Admin está ativo? ✅ (confirmado na tela)
- [ ] API Level é suficiente? ✅ (35 > 24)
- [ ] App foi reinstalado após adicionar `<reboot />`?
- [ ] Dispositivo foi reiniciado após ativar Device Admin?
- [ ] Logs mostram algum erro específico?
- [ ] Fabricante permite reboot remoto?

### 7. Comandos de Diagnóstico

```bash
# 1. Verificar Device Admin
adb shell dumpsys device_policy | grep -A 10 "com.bootreceiver.app"

# 2. Verificar políticas ativas
adb shell dumpsys device_policy | grep -i reboot

# 3. Verificar versão do Android
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk

# 4. Verificar fabricante
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model

# 5. Testar reboot manual (se tiver root)
adb shell su -c reboot
```

### 8. Próximos Passos

1. **Verifique os logs** quando tentar o reboot
2. **Reinstale o app** seguindo os passos acima
3. **Reinicie o dispositivo** manualmente após reinstalar
4. **Teste novamente** e verifique os logs

---

**Se ainda não funcionar após seguir todos os passos, envie os logs completos para análise detalhada.**
