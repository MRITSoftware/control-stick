package com.bootreceiver.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
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
 * Serviço que verifica conexão com internet e abre o app configurado
 * 
 * Este serviço:
 * 1. Aguarda alguns segundos após o boot (para garantir que o sistema está pronto)
 * 2. Verifica se há conexão com internet
 * 3. Se houver internet, abre o app configurado
 * 4. Se não houver, aguarda e tenta novamente em intervalos
 */
class BootService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isRunning = false
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BootService criado")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) {
            Log.d(TAG, "Serviço já está rodando")
            return START_STICKY
        }
        
        isRunning = true
        Log.d(TAG, "BootService iniciado")
        
        // Inicia o processo de verificação em uma coroutine
        serviceScope.launch {
            processBootSequence()
        }
        
        // Retorna START_STICKY para que o serviço seja reiniciado se for morto
        return START_STICKY
    }
    
    /**
     * Processa a sequência de boot:
     * 1. Aguarda delay inicial
     * 2. Verifica internet e abre app ou URL
     */
    private suspend fun processBootSequence() {
        val preferenceManager = PreferenceManager(this)
        
        // Delay inicial após boot (15 segundos)
        // Isso garante que o sistema Android está completamente inicializado
        // e que o WiFi tenha tempo de conectar
        Log.d(TAG, "Aguardando ${DELAY_AFTER_BOOT_MS}ms (${DELAY_AFTER_BOOT_MS / 1000} segundos) após boot...")
        delay(DELAY_AFTER_BOOT_MS)
        Log.d(TAG, "Delay concluído. Iniciando verificação de internet...")
        
        // Primeiro tenta usar configuração local
        var targetPackageOrUrl = preferenceManager.getTargetPackageName()
        var pwaUrl = preferenceManager.getPWAUrl()
        
        // Se não tiver configuração local, busca do banco de dados
        if (targetPackageOrUrl.isNullOrEmpty() && pwaUrl.isNullOrEmpty()) {
            Log.d(TAG, "Nenhuma configuração local encontrada. Buscando do banco de dados...")
            val deviceId = DeviceIdManager.getDeviceId(this)
            val supabaseManager = SupabaseManager()
            
            try {
                val urlFromDb = supabaseManager.getPWAUrl(deviceId)
                if (!urlFromDb.isNullOrEmpty()) {
                    pwaUrl = urlFromDb
                    preferenceManager.savePWAUrl(urlFromDb)
                    Log.d(TAG, "URL do PWA obtida do banco: $urlFromDb")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar URL do banco: ${e.message}", e)
            }
        }
        
        // Determina o que abrir
        val targetToOpen = targetPackageOrUrl ?: pwaUrl
        
        if (targetToOpen.isNullOrEmpty()) {
            Log.w(TAG, "Nenhum app ou URL configurado. Parando serviço.")
            stopSelf()
            return
        }
        
        // Tenta verificar internet e abrir o app/URL
        tryOpenAppWithInternetCheck(targetToOpen)
    }
    
    /**
     * Verifica internet e tenta abrir o app ou URL
     * Se não houver internet, agenda nova tentativa
     */
    private suspend fun tryOpenAppWithInternetCheck(packageNameOrUrl: String) {
        var attempts = 0
        val maxAttempts = MAX_RETRY_ATTEMPTS
        
        while (attempts < maxAttempts && isRunning) {
            attempts++
            Log.d(TAG, "Tentativa $attempts/$maxAttempts: Verificando conexão com internet...")
            
            if (isInternetAvailable()) {
                Log.d(TAG, "Internet disponível! Tentando abrir: $packageNameOrUrl")
                
                // Pequeno delay antes de abrir para garantir que o sistema está pronto
                // Isso ajuda a evitar travamentos, especialmente em players
                delay(500)
                
                val appLauncher = AppLauncher(this)
                val success = appLauncher.launchAppOrUrl(packageNameOrUrl)
                
                if (success) {
                    Log.d(TAG, "✅ App/URL aberto com sucesso!")
                    // Aguarda um pouco para garantir que o navegador/player iniciou corretamente
                    delay(2000)
                    stopSelf()
                    return
                } else {
                    Log.w(TAG, "⚠️ Falha ao abrir app/URL. Aguardando antes de tentar novamente...")
                    // Aguarda um pouco mais antes de tentar novamente
                    delay(RETRY_DELAY_MS)
                }
            } else {
                Log.w(TAG, "Internet não disponível. Aguardando ${RETRY_DELAY_MS}ms antes de tentar novamente...")
                delay(RETRY_DELAY_MS)
            }
        }
        
        if (attempts >= maxAttempts) {
            Log.e(TAG, "Número máximo de tentativas atingido. Parando serviço.")
        }
        
        stopSelf()
    }
    
    /**
     * Verifica se há conexão ativa com internet
     * 
     * @return true se houver internet, false caso contrário
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d(TAG, "BootService destruído")
    }
    
    companion object {
        private const val TAG = "BootService"
        private const val DELAY_AFTER_BOOT_MS = 15000L // 15 segundos após boot
        private const val RETRY_DELAY_MS = 10000L // 10 segundos entre tentativas
        private const val MAX_RETRY_ATTEMPTS = 120 // Máximo de 120 tentativas (20 minutos)
    }
}
