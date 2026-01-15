# 🔒 Fullscreen e Background - Funcionalidades Implementadas

## ✅ Funcionalidades Implementadas

### 1. Serviço em Background Persistente

O `KioskModeService` agora funciona **mesmo em background**, garantindo que:

- ✅ **Continua rodando** mesmo se o app for fechado
- ✅ **Verifica kiosk_mode** a cada 3 segundos quando inativo, 500ms quando ativo
- ✅ **Reabre automaticamente** se `kiosk_mode = true` no banco
- ✅ **Reinicia automaticamente** se o serviço for morto pelo sistema
- ✅ **Funciona mesmo se navegador fechar** - detecta e reabre

### 2. Fullscreen Helper

Criado utilitário `FullscreenHelper` para forçar fullscreen:

- ✅ Suporta Android 11+ (API 30+) e versões anteriores
- ✅ Oculta barra de navegação e status bar
- ✅ Modo sticky (permanece oculto)
- ✅ Mantém tela ligada

## 🔄 Como Funciona

### Serviço em Background

1. **Inicia automaticamente** quando o app abre
2. **Verifica banco** periodicamente:
   - A cada **3 segundos** quando kiosk inativo
   - A cada **500ms** quando kiosk ativo
3. **Se `kiosk_mode = true`**:
   - Abre URL/app automaticamente
   - Monitora constantemente
   - Reabre se fechar
4. **Se serviço for morto**:
   - Reinicia automaticamente (START_STICKY)
   - Verifica se kiosk ainda está ativo
   - Continua funcionando

### Fullscreen

O fullscreen pode ser aplicado de duas formas:

#### Opção 1: Via ADB (Recomendado)

```bash
# Forçar fullscreen no navegador Chrome
adb shell settings put global policy_control immersive.full=com.android.chrome

# Forçar fullscreen em todos os apps
adb shell settings put global policy_control immersive.full=*

# Remover fullscreen
adb shell settings put global policy_control null
```

#### Opção 2: Via Código (Limitado)

O código tenta identificar o navegador e sugere o comando ADB, mas não pode forçar diretamente sem root.

## 📋 Configuração

### Ativar Kiosk Mode no Banco

```sql
-- Ativar kiosk mode para um dispositivo
UPDATE lista_sticktv 
SET kiosk_mode = true,
    ultima_atualizacao = NOW()
WHERE codigo_dispositivo = 'SEU_DEVICE_ID';
```

### Verificar se Está Funcionando

```bash
# Ver logs do serviço
adb logcat | grep KioskModeService

# Deve mostrar:
# 🔒 MODO KIOSK ATIVADO!
# 📱 Serviço funcionará em BACKGROUND - sempre verificará e reabrirá se necessário
# 🔄 Reabrindo URL no navegador: https://...
```

## ⚙️ Comportamento Detalhado

### Quando `kiosk_mode = true`:

1. **Serviço detecta** mudança no banco (até 3 segundos)
2. **Abre URL/app** automaticamente
3. **Monitora a cada 500ms** se está rodando
4. **Se fechar/minimizar**:
   - Detecta imediatamente
   - Aguarda 1 segundo (para evitar múltiplas aberturas)
   - Reabre automaticamente
5. **Continua em background** mesmo se app fechar

### Quando `kiosk_mode = false`:

1. **Serviço detecta** mudança no banco
2. **Remove overlay** de kiosk
3. **Para monitoramento agressivo**
4. **Continua verificando** a cada 3 segundos (aguardando ativação)

## 🔧 Forçar Fullscreen no Navegador

### Método 1: Via ADB (Mais Confiável)

```bash
# 1. Identificar o navegador padrão
adb shell pm list packages | grep -i browser

# 2. Forçar fullscreen (exemplo com Chrome)
adb shell settings put global policy_control immersive.full=com.android.chrome

# 3. Verificar se funcionou
adb shell settings get global policy_control
```

### Método 2: Via Script Automático

Crie um script que executa após o boot:

```bash
#!/system/bin/sh
# Aguarda sistema inicializar
sleep 10

# Força fullscreen no Chrome
settings put global policy_control immersive.full=com.android.chrome
```

### Método 3: Via App (Requer Root)

O código tenta usar `FullscreenHelper.forceFullscreenViaADB()`, mas requer root.

## ⚠️ Limitações

1. **Fullscreen em apps de terceiros**:
   - Requer root ou ADB habilitado
   - Não pode ser forçado diretamente sem essas permissões
   - Solução: Use ADB ou configure no dispositivo

2. **Bateria**:
   - Serviço em background consome bateria
   - Recomendado manter dispositivo conectado à energia

3. **Otimizações de bateria**:
   - Desabilite otimizações de bateria para o app
   - Vá em: Configurações > Apps > MRIT Control > Bateria > Sem otimização

## 💡 Dicas

### Para Garantir Funcionamento Contínuo

1. **Desabilite otimizações de bateria**:
   ```bash
   adb shell dumpsys deviceidle whitelist +com.bootreceiver.app
   ```

2. **Mantenha ADB conectado** (para fullscreen):
   ```bash
   adb tcpip 5555
   adb connect IP_DO_DISPOSITIVO:5555
   ```

3. **Configure fullscreen no boot**:
   - Crie script que executa após boot
   - Ou configure manualmente uma vez

### Verificar Status

```sql
-- Ver status de kiosk de todos os dispositivos
SELECT codigo_dispositivo, nome, kiosk_mode, ultima_atualizacao
FROM lista_sticktv
WHERE kiosk_mode = true;
```

## 🐛 Troubleshooting

### Serviço não funciona em background

1. Verifique otimizações de bateria
2. Verifique logs: `adb logcat | grep KioskModeService`
3. Reinicie o app manualmente

### Fullscreen não funciona

1. Verifique se ADB está conectado
2. Verifique se o comando foi executado: `adb shell settings get global policy_control`
3. Tente reiniciar o dispositivo

### Navegador não reabre

1. Verifique se `kiosk_mode = true` no banco
2. Verifique logs: `adb logcat | grep KioskModeService`
3. Aguarde até 3 segundos (tempo de verificação)

---

**Versão**: 2.0  
**Data**: 2025  
**Funcionalidades**: Background persistente + Fullscreen
