package com.bootreceiver.app.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

/**
 * Classe utilitária para abrir aplicativos pelo package name ou URLs
 * 
 * Verifica se o app está instalado e tenta abri-lo, ou abre URL no navegador
 */
class AppLauncher(private val context: Context) {
    
    /**
     * Tenta abrir um aplicativo pelo seu package name
     * 
     * @param packageName Package name do app (ex: "com.example.app")
     * @return true se o app foi aberto com sucesso, false caso contrário
     */
    fun launchApp(packageName: String): Boolean {
        return try {
            // Verifica se o app está instalado
            if (!isAppInstalled(packageName)) {
                Log.e(TAG, "App não está instalado: $packageName")
                return false
            }
            
            // Obtém o intent para abrir o app
            val packageManager = context.packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            
            if (launchIntent == null) {
                Log.e(TAG, "Não foi possível obter intent para: $packageName")
                return false
            }
            
            // Adiciona flags necessárias para abrir o app
            // FLAG_ACTIVITY_NEW_TASK é essencial para abrir de um contexto não-Activity
            // FLAG_ACTIVITY_CLEAR_TOP garante que não haja múltiplas instâncias
            // FLAG_ACTIVITY_SINGLE_TOP evita recriação se já estiver no topo
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            
            // Abre o app
            context.startActivity(launchIntent)
            Log.d(TAG, "App aberto com sucesso: $packageName")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir app: $packageName", e)
            false
        }
    }
    
    /**
     * Fecha e reabre um aplicativo (reinicia o app)
     * 
     * @param packageName Package name do app
     * @return true se o app foi reiniciado com sucesso
     */
    fun restartApp(packageName: String): Boolean {
        return try {
            Log.d(TAG, "🔄 ========== REINICIANDO APP ==========")
            Log.d(TAG, "Package: $packageName")
            
            // Verifica se o app está instalado
            if (!isAppInstalled(packageName)) {
                Log.e(TAG, "❌ App não está instalado: $packageName")
                return false
            }
            
            // Método 1: Usar ActivityManager para fechar processos
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                
                // Fecha processos em background (pode não fechar se estiver em foreground)
                activityManager.killBackgroundProcesses(packageName)
                Log.d(TAG, "Processos em background finalizados")
                
                // Aguarda um pouco
                Thread.sleep(1000)
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao finalizar processos: ${e.message}")
            }
            
            // Método 2: Reabrir o app com flags que forçam recriação
            try {
                val packageManager = context.packageManager
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                
                if (launchIntent == null) {
                    Log.e(TAG, "❌ Não foi possível obter intent para: $packageName")
                    return false
                }
                
                // Flags para forçar reinício completo
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                
                // Abre o app
                context.startActivity(launchIntent)
                Log.d(TAG, "✅ App reiniciado com sucesso: $packageName")
                return true
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao reabrir app: ${e.message}", e)
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao reiniciar app: $packageName", e)
            false
        }
    }
    
    /**
     * Abre uma URL no navegador padrão do dispositivo
     * Otimizado para players de vídeo - evita travamentos
     * 
     * @param url URL para abrir (deve começar com http:// ou https://)
     * @return true se a URL foi aberta com sucesso, false caso contrário
     */
    fun launchUrl(url: String): Boolean {
        return try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Log.e(TAG, "URL inválida: $url (deve começar com http:// ou https://)")
                return false
            }
            
            // Valida a URL antes de tentar abrir
            val uri = Uri.parse(url)
            if (uri == null || uri.host.isNullOrEmpty()) {
                Log.e(TAG, "URL inválida ou malformada: $url")
                return false
            }
            
            Log.d(TAG, "🌐 Abrindo URL no navegador: $url")
            
            // Cria intent com flags otimizadas para evitar travamentos
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                // Flags essenciais para abrir de contexto não-Activity
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                // Limpa a pilha de atividades para evitar conflitos
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                
                // Evita recriação desnecessária
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                
                // Reseta a task se necessário (útil para players)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                
                // Traz para frente se já estiver aberto
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                
                // Flag adicional para evitar travamentos em players
                // Garante que o navegador seja aberto de forma limpa
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            }
            
            // Verifica se há algum app que pode abrir a URL
            val packageManager = context.packageManager
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            
            if (resolveInfo == null) {
                Log.e(TAG, "❌ Nenhum navegador encontrado para abrir a URL: $url")
                return false
            }
            
            // Abre a URL de forma segura
            context.startActivity(intent)
            
            Log.d(TAG, "✅ URL aberta com sucesso no navegador: $url")
            true
            
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "❌ Navegador não encontrado para abrir URL: $url", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "❌ URL com sintaxe inválida: $url", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao abrir URL: $url", e)
            false
        }
    }
    
    /**
     * Abre um app ou URL dependendo do que foi passado
     * Se for uma URL (começa com http:// ou https://), abre no navegador
     * Caso contrário, tenta abrir como package name
     * 
     * @param packageNameOrUrl Package name do app ou URL
     * @return true se foi aberto com sucesso, false caso contrário
     */
    fun launchAppOrUrl(packageNameOrUrl: String): Boolean {
        return if (packageNameOrUrl.startsWith("http://") || packageNameOrUrl.startsWith("https://")) {
            launchUrl(packageNameOrUrl)
        } else {
            launchApp(packageNameOrUrl)
        }
    }
    
    /**
     * Verifica se um app está instalado no dispositivo
     */
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    companion object {
        private const val TAG = "AppLauncher"
    }
}
