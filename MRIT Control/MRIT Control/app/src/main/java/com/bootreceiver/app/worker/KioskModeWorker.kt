package com.bootreceiver.app.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import com.bootreceiver.app.service.KioskModeService
import com.bootreceiver.app.utils.DeviceIdManager
import com.bootreceiver.app.utils.SupabaseManager
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Worker que verifica o modo kiosk periodicamente mesmo quando o app está fechado
 * 
 * Este worker é executado pelo WorkManager, que garante execução mesmo quando
 * o app está completamente fechado ou o processo foi morto
 */
class KioskModeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🔍 Worker verificando modo kiosk (app pode estar fechado)...")
            
            val deviceId = DeviceIdManager.getDeviceId(applicationContext)
            val supabaseManager = SupabaseManager()
            val kioskMode = supabaseManager.getKioskMode(deviceId)
            
            if (kioskMode == true) {
                Log.d(TAG, "🔒 Modo kiosk está ativo - iniciando serviço...")
                
                // Inicia o serviço mesmo se o app estiver fechado
                val serviceIntent = Intent(applicationContext, KioskModeService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    applicationContext.startForegroundService(serviceIntent)
                } else {
                    applicationContext.startService(serviceIntent)
                }
                
                Log.d(TAG, "✅ Serviço iniciado com sucesso")
            } else {
                Log.d(TAG, "ℹ️ Modo kiosk não está ativo")
            }
            
            // Agenda próxima verificação em 10 segundos
            scheduleNextCheck()
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erro no worker: ${e.message}", e)
            // Agenda próxima verificação mesmo em caso de erro
            scheduleNextCheck()
            Result.retry()
        }
    }
    
    /**
     * Agenda a próxima verificação
     */
    private fun scheduleNextCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<KioskModeWorker>()
            .setConstraints(constraints)
            .setInitialDelay(10, TimeUnit.SECONDS) // Verifica a cada 10 segundos
            .build()
        
        WorkManager.getInstance(applicationContext)
            .enqueue(workRequest)
        
        Log.d(TAG, "⏰ Próxima verificação agendada em 10 segundos")
    }
    
    companion object {
        private const val TAG = "KioskModeWorker"
        
        /**
         * Inicia o worker para verificar modo kiosk periodicamente
         */
        fun start(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val workRequest = OneTimeWorkRequestBuilder<KioskModeWorker>()
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.SECONDS) // Primeira verificação em 5 segundos
                .build()
            
            WorkManager.getInstance(context)
                .enqueue(workRequest)
            
            Log.d(TAG, "🚀 KioskModeWorker iniciado")
        }
    }
}
