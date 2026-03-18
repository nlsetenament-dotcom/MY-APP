package com.nls.chat.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.nls.chat.data.local.PreferencesManager

object ThemeManager {

    fun applyTheme(context: Context) {
        val prefs = PreferencesManager(context)
        when (prefs.appTheme) {
            Constants.THEME_DARK  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Constants.THEME_BW    -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else                  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun setTheme(context: Context, theme: String) {
        PreferencesManager(context).appTheme = theme
        applyTheme(context)
    }
}
