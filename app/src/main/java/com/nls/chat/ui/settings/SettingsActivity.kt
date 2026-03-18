package com.nls.chat.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.nls.chat.R
import com.nls.chat.data.local.PreferencesManager
import com.nls.chat.databinding.ActivitySettingsBinding
import com.nls.chat.utils.Constants
import com.nls.chat.utils.ThemeManager
import java.io.File
import java.io.FileOutputStream

/**
 * SettingsActivity
 * Configuración de la app:
 * - Cambio de tema (claro/oscuro/b&w)
 * - Cambiar foto de perfil
 * - Nombre del compañero editable
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PreferencesManager

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { saveAvatar(it) }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImageLauncher.launch("image/*")
        else Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        setupToolbar()
        loadCurrentSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Configuración"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadCurrentSettings() {
        val config = prefs.companionConfig ?: return

        binding.etCompanionName.setText(config.name)

        // Avatar
        val path = config.avatarPath
        if (!path.isNullOrBlank() && File(path).exists()) {
            Glide.with(this).load(File(path)).circleCrop()
                .placeholder(R.drawable.ic_avatar_default)
                .into(binding.ivAvatar)
        }

        // Tema actual
        when (prefs.appTheme) {
            Constants.THEME_DARK -> binding.rgTheme.check(binding.rbDark.id)
            Constants.THEME_BW   -> binding.rgTheme.check(binding.rbBw.id)
            else                 -> binding.rgTheme.check(binding.rbLight.id)
        }
    }

    private fun setupListeners() {
        binding.ivAvatar.setOnClickListener { pickAvatar() }
        binding.btnChangeAvatar.setOnClickListener { pickAvatar() }

        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                binding.rbDark.id -> Constants.THEME_DARK
                binding.rbBw.id   -> Constants.THEME_BW
                else              -> Constants.THEME_LIGHT
            }
            ThemeManager.setTheme(this, theme)
            recreate()
        }

        binding.btnSaveSettings.setOnClickListener {
            val newName = binding.etCompanionName.text.toString().trim()
            if (newName.isBlank()) {
                binding.etCompanionName.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            }
            val config = prefs.companionConfig ?: return@setOnClickListener
            prefs.companionConfig = config.copy(name = newName)
            Toast.makeText(this, "Guardado ✓", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pickAvatar() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED)
            pickImageLauncher.launch("image/*")
        else
            requestPermissionLauncher.launch(perm)
    }

    private fun saveAvatar(uri: Uri) {
        try {
            val dir = File(filesDir, "avatars").apply { mkdirs() }
            val file = File(dir, "avatar.jpg")
            contentResolver.openInputStream(uri)?.use { inp ->
                FileOutputStream(file).use { out -> inp.copyTo(out) }
            }
            Glide.with(this).load(file).circleCrop().into(binding.ivAvatar)
            val config = prefs.companionConfig ?: return
            prefs.companionConfig = config.copy(avatarPath = file.absolutePath)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show()
        }
    }
}
