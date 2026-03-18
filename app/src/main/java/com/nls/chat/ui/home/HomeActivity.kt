package com.nls.chat.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nls.chat.R
import com.nls.chat.data.local.PreferencesManager
import com.nls.chat.databinding.ActivityHomeBinding
import com.nls.chat.ui.chat.ChatActivity
import com.nls.chat.ui.onboarding.OnboardingActivity
import com.nls.chat.ui.settings.SettingsActivity
import java.io.File

/**
 * HomeActivity
 * Pantalla principal de la app. Muestra el perfil del compañero
 * y botones para ir al chat o a configuración.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        // Recargar config por si cambió en Settings
        loadCompanionProfile()
    }

    private fun setupUI() {
        binding.btnStartChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnResetOnboarding.setOnClickListener {
            prefs.isOnboardingComplete = false
            prefs.companionConfig = null
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }

    private fun loadCompanionProfile() {
        val config = prefs.companionConfig ?: return

        binding.tvCompanionName.text = config.name
        binding.tvPersonality.text   = "✨ ${config.personality.displayName} · ${config.speechStyle.displayName}"
        binding.tvInterests.text = if (config.interests.isNotEmpty())
            config.interests.joinToString(" · ")
        else "Sin intereses especificados"

        // Cargar avatar
        val avatarPath = config.avatarPath
        if (!avatarPath.isNullOrBlank() && File(avatarPath).exists()) {
            Glide.with(this).load(File(avatarPath)).circleCrop()
                .placeholder(R.drawable.ic_avatar_default)
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_avatar_default)
        }
    }
}
