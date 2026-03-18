package com.nls.chat.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.nls.chat.model.CompanionConfig
import com.nls.chat.utils.Constants

/**
 * PreferencesManager
 * Wrapper para SharedPreferences. Gestiona:
 * - Configuración del compañero (CompanionConfig)
 * - Preferencia de tema (claro/oscuro/b&w)
 * - Flag de onboarding completado
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // ─── Companion Config ─────────────────────────────────────────────────────

    var companionConfig: CompanionConfig?
        get() {
            val json = prefs.getString(Constants.KEY_COMPANION_CONFIG, null) ?: return null
            return runCatching { gson.fromJson(json, CompanionConfig::class.java) }.getOrNull()
        }
        set(value) {
            val json = if (value != null) gson.toJson(value) else null
            prefs.edit().putString(Constants.KEY_COMPANION_CONFIG, json).apply()
        }

    // ─── Onboarding ───────────────────────────────────────────────────────────

    var isOnboardingComplete: Boolean
        get() = prefs.getBoolean(Constants.KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(Constants.KEY_ONBOARDING_DONE, value).apply()

    // ─── Tema ─────────────────────────────────────────────────────────────────

    var appTheme: String
        get() = prefs.getString(Constants.KEY_THEME, Constants.THEME_LIGHT) ?: Constants.THEME_LIGHT
        set(value) = prefs.edit().putString(Constants.KEY_THEME, value).apply()

    // ─── Avatar path ──────────────────────────────────────────────────────────

    var avatarPath: String?
        get() = prefs.getString(Constants.KEY_AVATAR_PATH, null)
        set(value) = prefs.edit().putString(Constants.KEY_AVATAR_PATH, value).apply()

    // ─── Reset ────────────────────────────────────────────────────────────────

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
