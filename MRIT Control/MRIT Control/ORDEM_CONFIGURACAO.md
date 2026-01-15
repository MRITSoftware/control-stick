# 📋 Ordem Correta de Configuração

## ✅ Ordem Ideal (Recomendada)

### 1. **Instalar o App**
- Instale o APK no dispositivo
- **NÃO abra o app ainda**

### 2. **Ativar Device Admin PRIMEIRO** ⚠️ **CRÍTICO**
- Vá em: **Configurações → Segurança → Administradores do dispositivo**
- Encontre **MRIT Control**
- **Ative** (marque a caixa)
- Aceite a confirmação
- **Por quê primeiro?** A política `<reboot />` só é aplicada quando o Device Admin é ativado. Se você ativar depois, pode não funcionar corretamente.

### 3. **Abrir o App**
- Agora abra o app **MRIT Control**
- O app vai solicitar outras permissões automaticamente (se necessário)

### 4. **Registrar Dispositivo**
- O app vai pedir o **email da unidade** (ex: sala01@empresa.com)
- Informe o email e confirme
- Isso registra o dispositivo no Supabase

### 5. **Escolher App para Abrir Automaticamente**
- O app vai mostrar uma lista de apps instalados
- Escolha qual app deve abrir automaticamente no boot
- Confirme a seleção

### 6. **Configurar Outras Permissões (se solicitado)**
- O app pode solicitar:
  - Desativar otimização de bateria
  - Permissão de sobreposição (SYSTEM_ALERT_WINDOW)
- Configure conforme solicitado

### 7. **Testar Reboot**
- Vá na tela de **Status** (se disponível)
- Clique em **"Testar Reboot Agora"**
- Ou crie um comando no Supabase e aguarde

---

## ⚠️ Por Que Esta Ordem?

### Device Admin PRIMEIRO é Crítico

Quando você ativa o Device Admin, o Android lê o arquivo `device_admin.xml` e aplica as políticas definidas. Se você:

1. ❌ **Abrir o app primeiro** → App pode funcionar, mas reboot não vai funcionar
2. ✅ **Ativar Device Admin primeiro** → Política `<reboot />` é aplicada corretamente

### O Que Acontece em Cada Ordem

#### ❌ Ordem Errada:
```
1. Instalar app
2. Abrir app
3. Escolher app para abrir
4. Ativar Device Admin depois
```
**Problema:** A política `<reboot />` pode não ser aplicada corretamente porque o app já estava rodando.

#### ✅ Ordem Correta:
```
1. Instalar app
2. Ativar Device Admin PRIMEIRO
3. Abrir app
4. Configurar resto
```
**Vantagem:** Quando o app abre, o Device Admin já está ativo com todas as políticas aplicadas.

---

## 🔄 Se Você Já Configurou Errado

Se você já instalou e configurou o app na ordem errada:

### Solução Rápida:

1. **Desative Device Admin**
   - Configurações → Segurança → Administradores do dispositivo → MRIT Control → Desativar

2. **Desinstale o app**
   - Configurações → Apps → MRIT Control → Desinstalar

3. **Reinstale o app**

4. **Siga a ordem correta acima** (Device Admin primeiro!)

---

## 📱 Fluxo Automático do App

O app também pode solicitar Device Admin automaticamente:

### Quando o App Solicita Automaticamente:

1. **Ao iniciar RebootMonitorService** (se Device Admin não estiver ativo)
2. **Na tela de seleção de app** (se Device Admin não estiver ativo)
3. **Na tela de Status** (botão "Ativar Device Admin")

### Mas é Melhor Fazer Manualmente Primeiro!

Por quê?
- Você garante que a política `<reboot />` é aplicada desde o início
- Evita problemas de timing
- Mais confiável

---

## ✅ Checklist de Configuração

Execute em ordem:

- [ ] 1. Instalar APK
- [ ] 2. **Ativar Device Admin** (Configurações → Segurança → Administradores do dispositivo)
- [ ] 3. Abrir app MRIT Control
- [ ] 4. Registrar dispositivo (informar email)
- [ ] 5. Escolher app para abrir automaticamente
- [ ] 6. Configurar outras permissões (se solicitado)
- [ ] 7. Testar reboot (tela de Status ou comando no Supabase)

---

## 🎯 Resumo

**A ordem mais importante é:**

1. **Instalar**
2. **Device Admin PRIMEIRO** ⚠️
3. **Depois abrir e configurar o resto**

Isso garante que a política `<reboot />` seja aplicada corretamente desde o início!

---

## 💡 Dica

Se você não tem certeza se configurou corretamente:

1. Vá na tela de **Status** do app
2. Verifique se mostra: **"✅ Device Admin ATIVO"**
3. Se mostrar, está correto!
4. Se mostrar **"❌ Device Admin INATIVO"**, siga os passos de correção acima.
