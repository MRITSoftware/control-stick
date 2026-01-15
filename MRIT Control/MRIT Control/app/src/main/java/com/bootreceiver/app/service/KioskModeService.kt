package com.bootreceiver.app.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bootreceiver.app.R
import com.bootreceiver.app.ui.AppSelectionActivity
import com.bootreceiver.app.utils.AppLauncher
import com.bootreceiver.app.utils.DeviceIdManager
import com.bootreceiver.app.utils.PreferenceManager
import com.bootreceiver.app.utils.SupabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Serviço que monitora o modo kiosk do dispositivo
 * 
 * Este serviço:
 * 1. Verifica periodicamente se kiosk_mode está ativo no Supabase
 * 2. Se ativo, previne que o app configurado seja minimizado
 * 3. Se o app estiver minimizado e kiosk_mode for ativado, traz de volta
 */
class KioskModeService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isRunning = false
    private val supabaseManager = SupabaseManager()
    private lateinit var deviceId: String
    private var lastKioskMode: Boolean? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "KioskModeService criado")
        deviceId = DeviceIdManager.getDeviceId(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) {
            Log.d(TAG, "Serviço já está rodando")
            return START_STICKY
        }
        
        try {
            isRunning = true
            Log.d(TAG, "KioskModeService iniciado para dispositivo: $deviceId")
            
            createNotificationChannel()
            
            // Inicia como Foreground Service
            try {
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
                Log.d(TAG, "Foreground Service iniciado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao iniciar Foreground Service: ${e.message}", e)
            }
            
            // Inicia o monitoramento
            serviceScope.launch {
                startMonitoring()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro crítico ao iniciar serviço: ${e.message}", e)
            isRunning = false
        }
        
        return START_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Modo Kiosk",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitora modo kiosk do dispositivo"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, AppSelectionActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            pendingIntentFlags
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MRIT Control - Modo Kiosk")
            .setContentText("Monitorando modo kiosk...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(false)
            .build()
    }
    
    private suspend fun startMonitoring() {
        var consecutiveErrors = 0
        val maxConsecutiveErrors = 5
        
        while (isRunning) {
            try {
                Log.d(TAG, "🔍 Verificando modo kiosk...")
                
                val kioskMode = supabaseManager.getKioskMode(deviceId)
                
                // Reset contador de erros se sucesso
                consecutiveErrors = 0
                
                // Se mudou o estado, aplica as mudanças
                if (lastKioskMode != kioskMode) {
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    if (kioskMode == true) {
                        Log.d(TAG, "🔒 MODO KIOSK ATIVADO!")
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        applyKioskMode()
                    } else {
                        Log.d(TAG, "🔓 MODO KIOSK DESATIVADO")
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        removeKioskMode()
                    }
                    lastKioskMode = kioskMode
                } else if (kioskMode == true) {
                    // Se kiosk está ativo, verifica constantemente se o app está rodando
                    // Verifica muito mais frequentemente para prevenir minimização
                    ensureAppIsRunning()
                }
                
                // Usa intervalo menor quando kiosk está ativo, maior quando inativo
                delay(if (kioskMode == true) CHECK_INTERVAL_MS else BACKGROUND_CHECK_INTERVAL_MS)
                
            } catch (e: Exception) {
                consecutiveErrors++
                Log.e(TAG, "Erro no monitoramento (tentativa $consecutiveErrors/$maxConsecutiveErrors): ${e.message}", e)
                
                // Se muitos erros consecutivos, aguarda mais tempo
                if (consecutiveErrors >= maxConsecutiveErrors) {
                    Log.w(TAG, "⚠️ Muitos erros consecutivos. Aguardando mais tempo antes de tentar novamente...")
                    delay(ERROR_RETRY_DELAY_MS * 3) // Aguarda 6 segundos
                    consecutiveErrors = 0 // Reset após espera longa
                } else {
                    delay(ERROR_RETRY_DELAY_MS)
                }
            }
        }
    }
    
    /**
     * Aplica o modo kiosk: garante que o app/URL configurado esteja rodando
     * e inicia monitoramento agressivo para prevenir minimização
     * Funciona mesmo em background - sempre verifica e reabre se necessário
     */
    private fun applyKioskMode() {
        val preferenceManager = PreferenceManager(this)
        val pwaUrl = preferenceManager.getPWAUrl()
        val targetPackage = preferenceManager.getTargetPackageName()
        val targetToOpen = pwaUrl ?: targetPackage
        
        if (targetToOpen.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ Nenhum app ou URL configurado. Não é possível aplicar modo kiosk.")
            return
        }
        
        Log.d(TAG, "🔒 Aplicando modo kiosk para: $targetToOpen")
        Log.d(TAG, "📱 Serviço funcionará em BACKGROUND - sempre verificará e reabrirá se necessário")
        
        val appLauncher = AppLauncher(this)
        
        // Se for URL, sempre abre (navegador)
        if (pwaUrl != null) {
            Log.d(TAG, "📱 Abrindo URL no navegador: $pwaUrl")
            appLauncher.launchUrl(pwaUrl)
            
            // Tenta forçar fullscreen no navegador (se possível)
            // Nota: Forçar fullscreen em apps de terceiros requer root ou ADB
            // Mas o serviço continuará monitorando e reabrindo se necessário
            try {
                val browserPackage = getDefaultBrowserPackage()
                if (browserPackage != null) {
                    Log.d(TAG, "🔍 Navegador identificado: $browserPackage")
                    Log.d(TAG, "ℹ️ Para forçar fullscreen, use: adb shell settings put global policy_control immersive.full=$browserPackage")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Não foi possível identificar navegador: ${e.message}")
            }
        } else if (targetPackage != null) {
            // Se for package name, verifica se está rodando
            if (!isAppRunning(targetPackage)) {
                Log.d(TAG, "📱 App não está rodando. Abrindo...")
                appLauncher.launchApp(targetPackage)
            } else {
                Log.d(TAG, "✅ App já está rodando")
            }
        }
        
        // Inicia overlay para interceptar gestos (requer permissão SYSTEM_ALERT_WINDOW)
        try {
            val overlayIntent = Intent(this, com.bootreceiver.app.service.KioskOverlayService::class.java).apply {
                putExtra("kiosk_enabled", true)
            }
            startService(overlayIntent)
            Log.d(TAG, "📡 Overlay de kiosk iniciado")
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível iniciar overlay (pode precisar de permissão): ${e.message}")
        }
        
        // Inicia monitoramento agressivo em uma coroutine separada
        // Este monitoramento funciona mesmo em background
        serviceScope.launch {
            aggressiveKioskMonitoring(targetToOpen)
        }
    }
    
    /**
     * Tenta identificar o package do navegador padrão
     */
    private fun getDefaultBrowserPackage(): String? {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://www.example.com"))
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível identificar navegador padrão: ${e.message}")
            null
        }
    }
    
    /**
     * Monitoramento agressivo do app/URL quando kiosk está ativo
     * Verifica constantemente e reabre imediatamente se minimizado ou fechado
     */
    private suspend fun aggressiveKioskMonitoring(targetPackageOrUrl: String) {
        var consecutiveFailures = 0
        val preferenceManager = PreferenceManager(this@KioskModeService)
        val isUrl = targetPackageOrUrl.startsWith("http://") || targetPackageOrUrl.startsWith("https://")
        
        while (isRunning) {
            try {
                val kioskMode = supabaseManager.getKioskMode(deviceId)
                if (kioskMode == true) {
                    val appLauncher = AppLauncher(this@KioskModeService)
                    
                    // Se for URL, sempre reabre (navegador pode ter sido fechado)
                    if (isUrl) {
                        // Pequeno delay para evitar abrir múltiplas vezes rapidamente
                        // Isso previne travamentos em players
                        delay(1000)
                        Log.d(TAG, "🔄 Reabrindo URL no navegador: $targetPackageOrUrl")
                        val success = appLauncher.launchUrl(targetPackageOrUrl)
                        if (success) {
                            consecutiveFailures = 0
                        } else {
                            consecutiveFailures++
                            Log.w(TAG, "⚠️ Falha ao reabrir URL (tentativa $consecutiveFailures)")
                        }
                    } else {
                        // Se for package name, verifica se está rodando
                        if (!isAppRunning(targetPackageOrUrl)) {
                            consecutiveFailures++
                            Log.d(TAG, "🚨 APP FECHADO/MINIMIZADO! REABRINDO IMEDIATAMENTE... (tentativa $consecutiveFailures)")
                            
                            // Tenta abrir o app múltiplas vezes rapidamente
                            appLauncher.launchApp(targetPackageOrUrl)
                            delay(300) // Aguarda 300ms
                            
                            // Se ainda não está rodando, tenta novamente
                            if (!isAppRunning(targetPackageOrUrl)) {
                                Log.d(TAG, "⚠️ Tentativa 2: Reabrindo app...")
                                appLauncher.launchApp(targetPackageOrUrl)
                                delay(500)
                            }
                            
                            // Se ainda não está rodando, tenta mais uma vez
                            if (!isAppRunning(targetPackageOrUrl)) {
                                Log.d(TAG, "⚠️ Tentativa 3: Reabrindo app...")
                                appLauncher.launchApp(targetPackageOrUrl)
                            }
                        } else {
                            // App está rodando, reseta contador de falhas
                            if (consecutiveFailures > 0) {
                                Log.d(TAG, "✅ App reaberto com sucesso após $consecutiveFailures tentativas")
                                consecutiveFailures = 0
                            }
                        }
                    }
                    delay(CHECK_INTERVAL_MS) // Verifica muito frequentemente
                } else {
                    // Se kiosk foi desativado, para o monitoramento agressivo
                    Log.d(TAG, "🔓 Kiosk desativado - parando monitoramento agressivo")
                    break
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro no monitoramento agressivo: ${e.message}", e)
                delay(ERROR_RETRY_DELAY_MS)
            }
        }
    }
    
    /**
     * Remove o modo kiosk (permite minimizar normalmente)
     */
    private fun removeKioskMode() {
        Log.d(TAG, "🔓 Modo kiosk removido. App pode ser minimizado normalmente.")
        
        // Remove overlay
        try {
            val overlayIntent = Intent(this, com.bootreceiver.app.service.KioskOverlayService::class.java).apply {
                putExtra("kiosk_enabled", false)
            }
            startService(overlayIntent)
            Log.d(TAG, "📡 Overlay de kiosk removido")
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao remover overlay: ${e.message}")
        }
    }
    
    /**
     * Garante que o app/URL configurado esteja rodando (se kiosk estiver ativo)
     * Verifica mais frequentemente quando kiosk está ativo
     */
    private fun ensureAppIsRunning() {
        val preferenceManager = PreferenceManager(this)
        val pwaUrl = preferenceManager.getPWAUrl()
        val targetPackage = preferenceManager.getTargetPackageName()
        val targetToOpen = pwaUrl ?: targetPackage
        
        if (targetToOpen.isNullOrEmpty()) {
            return
        }
        
        val appLauncher = AppLauncher(this)
        
        // Se for URL, sempre reabre
        if (pwaUrl != null) {
            Log.d(TAG, "⚠️⚠️⚠️ REABRINDO URL COM KIOSK ATIVO! REABRINDO IMEDIATAMENTE...")
            // Pequeno delay para evitar travamentos
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                appLauncher.launchUrl(pwaUrl)
            }, 300)
        } else if (targetPackage != null) {
            // Se for package name, verifica se está rodando
            if (!isAppRunning(targetPackage)) {
                Log.d(TAG, "⚠️⚠️⚠️ APP MINIMIZADO COM KIOSK ATIVO! REABRINDO IMEDIATAMENTE...")
                
                // Tenta múltiplas vezes rapidamente
                appLauncher.launchApp(targetPackage)
                
                // Aguarda 200ms e tenta novamente (muito mais rápido)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (!isAppRunning(targetPackage)) {
                        Log.d(TAG, "⚠️ Tentativa 2: Reabrindo app...")
                        appLauncher.launchApp(targetPackage)
                    }
                }, 200)
                
                // Aguarda mais 500ms e tenta novamente
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (!isAppRunning(targetPackage)) {
                        Log.d(TAG, "⚠️ Tentativa 3: Reabrindo app...")
                        appLauncher.launchApp(targetPackage)
                    }
                }, 700)
            }
        }
    }
    
    /**
     * Verifica se um app está rodando em foreground
     * Melhorado para detectar se o app foi fechado (não apenas minimizado)
     */
    private fun isAppRunning(packageName: String): Boolean {
        try {
            val activityManager = getSystemService(ActivityManager::class.java)
            
            // Método 1: Verifica processos em foreground
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val runningProcesses = activityManager.runningAppProcesses
                val isForeground = runningProcesses?.any { 
                    it.processName == packageName && 
                    (it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                     it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE)
                } == true
                
                if (isForeground) {
                    return true
                }
            }
            
            // Método 2: Verifica a activity no topo (mais confiável)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val runningTasks = activityManager.getAppTasks()
                if (runningTasks != null && runningTasks.isNotEmpty()) {
                    for (task in runningTasks) {
                        val taskInfo = task.taskInfo
                        if (taskInfo != null && taskInfo.topActivity != null) {
                            if (taskInfo.topActivity!!.packageName == packageName) {
                                return true
                            }
                        }
                    }
                }
            } else {
                // Método alternativo para versões antigas
                @Suppress("DEPRECATION")
                val runningTasks = activityManager.getRunningTasks(1)
                if (runningTasks.isNotEmpty()) {
                    val topActivity = runningTasks[0].topActivity
                    if (topActivity != null && topActivity.packageName == packageName) {
                        return true
                    }
                }
            }
            
            // Método 3: Verifica se o processo existe (mesmo em background)
            // Se o processo não existe, o app foi fechado
            try {
                val packageManager = packageManager
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val pid = android.os.Process.getUidForName(packageName)
                
                // Se chegou aqui, o app está instalado, mas verifica se está rodando
                val runningProcesses = activityManager.runningAppProcesses
                val processExists = runningProcesses?.any { 
                    it.processName == packageName
                } == true
                
                // Se o processo não existe, o app foi fechado
                if (!processExists) {
                    Log.d(TAG, "📱 Processo do app não existe - app foi fechado")
                    return false
                }
            } catch (e: Exception) {
                // Se não conseguiu obter info do app, assume que não está rodando
                Log.d(TAG, "📱 Não foi possível verificar processo: ${e.message}")
            }
            
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar se app está rodando: ${e.message}", e)
            return false
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d(TAG, "⚠️ KioskModeService destruído - tentando reiniciar...")
        
        // Se o serviço foi destruído mas kiosk está ativo, tenta reiniciar
        // Isso garante que o serviço continue funcionando mesmo se for morto pelo sistema
        serviceScope.launch {
            try {
                delay(2000) // Aguarda 2 segundos
                val kioskMode = supabaseManager.getKioskMode(deviceId)
                if (kioskMode == true) {
                    Log.d(TAG, "🔄 Kiosk ainda ativo - reiniciando serviço em background...")
                    val restartIntent = Intent(this@KioskModeService, KioskModeService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(restartIntent)
                    } else {
                        startService(restartIntent)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao tentar reiniciar serviço: ${e.message}", e)
            }
        }
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "⚠️ App removido da lista de tarefas - mas serviço continua rodando")
        // O serviço continua rodando mesmo se o app for fechado
        // START_STICKY garante que será reiniciado se necessário
        // Isso permite que o kiosk continue funcionando mesmo se o app for fechado
    }
    
    companion object {
        private const val TAG = "KioskModeService"
        private const val CHANNEL_ID = "kiosk_mode_channel"
        private const val NOTIFICATION_ID = 2
        private const val CHECK_INTERVAL_MS = 500L // Verifica a cada 500ms quando kiosk ativo (muito rápido para prevenir minimização)
        private const val ERROR_RETRY_DELAY_MS = 2000L // Em caso de erro, aguarda 2 segundos
        private const val BACKGROUND_CHECK_INTERVAL_MS = 3000L // Verifica a cada 3 segundos em background
    }
}
