package com.bootreceiver.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import java.util.UUID

/**
 * Gerenciador para obter um ID único do dispositivo
 * 
 * Garante que o ID seja sempre o mesmo, mesmo após reinstalação do app.
 * Usa uma combinação de Android ID + ID salvo em SharedPreferences para garantir persistência.
 */
object DeviceIdManager {
    
    private const val PREFS_NAME = "device_id_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_ANDROID_ID = "android_id_backup"
    
    /**
     * Obtém um ID único do dispositivo que persiste mesmo após reinstalação
     * 
     * Estratégia:
     * 1. Tenta usar Android ID (único por dispositivo)
     * 2. Se Android ID mudou ou não existe, usa ID salvo em SharedPreferences
     * 3. Se não existe ID salvo, gera um novo UUID e salva
     * 
     * Isso garante que o mesmo dispositivo sempre tenha o mesmo ID,
     * mesmo após reinstalar o app ou fazer factory reset (em alguns casos)
     */
    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentAndroidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""
        
        // Verifica se já existe um ID salvo
        val savedDeviceId = prefs.getString(KEY_DEVICE_ID, null)
        val savedAndroidId = prefs.getString(KEY_ANDROID_ID, null)
        
        return when {
            // Caso 1: Já existe ID salvo e Android ID não mudou
            savedDeviceId != null && savedAndroidId == currentAndroidId && currentAndroidId.isNotEmpty() -> {
                Log.d(TAG, "Device ID recuperado do cache: $savedDeviceId")
                savedDeviceId
            }
            
            // Caso 2: Android ID existe e é válido, mas não temos ID salvo
            currentAndroidId.isNotEmpty() && currentAndroidId != "9774d56d682e549c" -> {
                // Android ID válido (não é o ID padrão de emuladores antigos)
                Log.d(TAG, "Usando Android ID como Device ID: $currentAndroidId")
                
                // Salva para uso futuro
                prefs.edit()
                    .putString(KEY_DEVICE_ID, currentAndroidId)
                    .putString(KEY_ANDROID_ID, currentAndroidId)
                    .apply()
                
                currentAndroidId
            }
            
            // Caso 3: Android ID mudou ou não existe, mas temos ID salvo
            savedDeviceId != null -> {
                Log.w(TAG, "Android ID mudou (era: $savedAndroidId, agora: $currentAndroidId). Mantendo ID salvo: $savedDeviceId")
                // Mantém o ID salvo mesmo se Android ID mudou
                savedDeviceId
            }
            
            // Caso 4: Primeira vez - gera novo UUID
            else -> {
                val newDeviceId = generateUniqueId(context)
                Log.d(TAG, "Novo Device ID gerado: $newDeviceId")
                
                // Salva o novo ID
                prefs.edit()
                    .putString(KEY_DEVICE_ID, newDeviceId)
                    .putString(KEY_ANDROID_ID, currentAndroidId)
                    .apply()
                
                newDeviceId
            }
        }
    }
    
    /**
     * Gera um ID único baseado em características do dispositivo
     */
    private fun generateUniqueId(context: Context): String {
        // Tenta usar Android ID primeiro
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        
        if (androidId != null && androidId.isNotEmpty() && androidId != "9774d56d682e549c") {
            return androidId
        }
        
        // Se Android ID não está disponível, gera UUID baseado em características do dispositivo
        val deviceInfo = buildString {
            append(android.os.Build.MANUFACTURER)
            append(android.os.Build.MODEL)
            append(android.os.Build.SERIAL)
            append(android.os.Build.FINGERPRINT)
        }
        
        // Gera hash do device info
        val hash = deviceInfo.hashCode().toString(16)
        val uuid = UUID.nameUUIDFromBytes(deviceInfo.toByteArray()).toString().replace("-", "")
        
        // Combina hash + UUID para garantir unicidade
        return "${hash}_${uuid}".take(32) // Limita a 32 caracteres
    }
    
    /**
     * Força a regeneração do Device ID (útil para testes)
     */
    fun resetDeviceId(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_DEVICE_ID)
            .remove(KEY_ANDROID_ID)
            .apply()
        Log.d(TAG, "Device ID resetado")
    }
    
    private const val TAG = "DeviceIdManager"
}
