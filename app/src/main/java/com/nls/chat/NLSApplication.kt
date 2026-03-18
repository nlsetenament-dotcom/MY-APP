package com.nls.chat

import android.app.Application
import com.nls.chat.utils.ThemeManager

/**
 * NLSApplication
 * Clase Application global. Se ejecuta antes que cualquier Activity.
 * Se encarga de inicializar el tema guardado por el usuario.
 */
class NLSApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Aplicar tema guardado (claro/oscuro/b&w) al arrancar la app
        ThemeManager.applyTheme(this)
    }
}
