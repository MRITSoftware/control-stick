package com.bootreceiver.app.utils

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager

/**
 * Helper para forçar modo fullscreen e ocultar barras do sistema
 * 
 * Usa diferentes métodos dependendo da versão do Android
 */
object FullscreenHelper {
    
    private const val TAG = "FullscreenHelper"
    
    /**
     * Aplica fullscreen em uma Activity
     * Oculta barra de navegação e status bar
     */
    fun applyFullscreen(activity: Activity) {
        try {
            val window = activity.window
            val decorView = window.decorView
            
            // Para Android 11+ (API 30+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let { controller ->
                    // Oculta barras do sistema
                    controller.hide(android.view.WindowInsets.Type.statusBars())
                    controller.hide(android.view.WindowInsets.Type.navigationBars())
                    // Configura comportamento sticky (permanece oculto)
                    controller.systemBarsBehavior = 
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Para Android 10 e anteriores
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }
            
            // Mantém tela ligada
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
            // Previne que apareça sobre outras apps
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            
            Log.d(TAG, "✅ Fullscreen aplicado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao aplicar fullscreen: ${e.message}", e)
        }
    }
    
    /**
     * Aplica fullscreen usando WindowManager (para serviços)
     * Isso pode ser usado para forçar fullscreen em outros apps
     */
    fun applyFullscreenToWindow(windowManager: WindowManager, view: View) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.windowInsetsController?.let { controller ->
                    controller.hide(android.view.WindowInsets.Type.statusBars())
                    controller.hide(android.view.WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = 
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                view.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }
            Log.d(TAG, "✅ Fullscreen aplicado à view")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao aplicar fullscreen à view: ${e.message}", e)
        }
    }
    
    /**
     * Força fullscreen via ADB (requer root ou ADB)
     * Útil para forçar fullscreen em apps de terceiros como navegadores
     */
    fun forceFullscreenViaADB(packageName: String): Boolean {
        return try {
            // Comando ADB para forçar fullscreen
            // Isso requer que o dispositivo tenha ADB habilitado ou root
            val command = "settings put global policy_control immersive.full=$packageName"
            
            val process = Runtime.getRuntime().exec("su -c '$command'")
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.d(TAG, "✅ Fullscreen forçado via ADB para: $packageName")
                true
            } else {
                Log.w(TAG, "⚠️ Não foi possível forçar fullscreen via ADB (pode precisar de root)")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao forçar fullscreen via ADB: ${e.message}", e)
            false
        }
    }
}
