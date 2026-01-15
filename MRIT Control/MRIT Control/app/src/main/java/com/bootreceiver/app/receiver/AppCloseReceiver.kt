package com.bootreceiver.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.bootreceiver.app.service.KioskModeService
import com.bootreceiver.app.utils.DeviceIdManager
import com.bootreceiver.app.utils.SupabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver que escuta quando o app é fechado
 * e reinicia o KioskModeService se o modo kiosk estiver ativo
 * 
 * Isso garante que o serviço continue funcionando mesmo quando
 * o app é completamente fechado pelo usuário ou pelo sistema
 */
class AppCloseReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_RESTARTED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "App foi reiniciado/reinstalado - verificando se precisa reiniciar serviço")
                restartServiceIfNeeded(context)
            }
            else -> {
                // Para outros eventos, também verifica se precisa reiniciar
                Log.d(TAG, "Evento recebido: ${intent.action} - verificando se precisa reiniciar serviço")
                restartServiceIfNeeded(context)
            }
        }
    }
    
    /**
     * Verifica se o modo kiosk está ativo e reinicia o serviço se necessário
     */
    private fun restartServiceIfNeeded(context: Context) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            try {
                // Aguarda um pouco para garantir que o app foi realmente fechado
                delay(2000)
                
                val deviceId = DeviceIdManager.getDeviceId(context)
                val supabaseManager = SupabaseManager()
                val kioskMode = supabaseManager.getKioskMode(deviceId)
                
                if (kioskMode == true) {
                    Log.d(TAG, "🔒 Modo kiosk está ativo - reiniciando KioskModeService...")
                    
                    val serviceIntent = Intent(context, KioskModeService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    
                    Log.d(TAG, "✅ KioskModeService reiniciado com sucesso")
                } else {
                    Log.d(TAG, "ℹ️ Modo kiosk não está ativo - não é necessário reiniciar serviço")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar e reiniciar serviço: ${e.message}", e)
            }
        }
    }
    
    companion object {
        private const val TAG = "AppCloseReceiver"
    }
}
