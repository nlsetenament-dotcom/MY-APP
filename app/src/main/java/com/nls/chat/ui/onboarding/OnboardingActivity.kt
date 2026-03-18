package com.nls.chat.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.nls.chat.data.local.PreferencesManager
import com.nls.chat.databinding.ActivityOnboardingBinding
import com.nls.chat.model.*
import com.nls.chat.ui.home.HomeActivity
import com.nls.chat.utils.Constants
import java.io.File
import java.io.FileOutputStream

/**
 * OnboardingActivity
 * Wizard de 5 pasos para configurar el compañero/a virtual.
 * Al completarlo, guarda CompanionConfig y abre HomeActivity.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefs: PreferencesManager

    private var currentStep = 0
    private val totalSteps = 5

    // Valores del formulario
    private var companionName = ""
    private var selectedGender = Gender.FEMALE
    private var selectedPersonality = Personality.SWEET
    private var selectedSpeechStyle = SpeechStyle.CASUAL
    private var selectedEmotionalLevel = EmotionalLevel.MEDIUM
    private var selectedInterests = mutableListOf<String>()
    private var avatarUri: Uri? = null

    // Launcher para elegir imagen de galería
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            Glide.with(this).load(it).circleCrop().into(binding.ivAvatarPreview)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImageLauncher.launch("image/*")
        else Toast.makeText(this, "Permiso necesario para elegir foto", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        // Si ya terminó el onboarding, ir directo al Home
        if (prefs.isOnboardingComplete) {
            goToHome()
            return
        }

        setupUI()
        showStep(0)
    }

    private fun setupUI() {
        binding.btnNext.setOnClickListener { handleNext() }
        binding.btnBack.setOnClickListener { handleBack() }
        binding.ivAvatarPreview.setOnClickListener { pickAvatar() }
        binding.btnPickAvatar.setOnClickListener { pickAvatar() }
    }

    private fun showStep(step: Int) {
        currentStep = step
        val progress = ((step + 1).toFloat() / totalSteps * 100).toInt()
        binding.progressBar.progress = progress
        binding.tvStepIndicator.text = "Paso ${step + 1} de $totalSteps"

        // Ocultar todos los pasos
        binding.step0Layout.hide()
        binding.step1Layout.hide()
        binding.step2Layout.hide()
        binding.step3Layout.hide()
        binding.step4Layout.hide()

        // Mostrar paso actual
        when (step) {
            0 -> { binding.step0Layout.show(); binding.btnBack.hide() }
            1 -> { binding.step1Layout.show(); binding.btnBack.show() }
            2 -> { binding.step2Layout.show() }
            3 -> { binding.step3Layout.show() }
            4 -> { binding.step4Layout.show(); binding.btnNext.text = "¡Crear compañero/a!" }
        }
    }

    private fun handleNext() {
        when (currentStep) {
            0 -> {
                companionName = binding.etCompanionName.text.toString().trim()
                if (companionName.isBlank()) {
                    binding.etCompanionName.error = "Ingresa un nombre"
                    return
                }
                selectedGender = when (binding.rgGender.checkedRadioButtonId) {
                    binding.rbMale.id   -> Gender.MALE
                    binding.rbNonBinary.id -> Gender.NON_BINARY
                    else               -> Gender.FEMALE
                }
                showStep(1)
            }
            1 -> {
                selectedPersonality = when (binding.rgPersonality.checkedRadioButtonId) {
                    binding.rbFunny.id       -> Personality.FUNNY
                    binding.rbSerious.id     -> Personality.SERIOUS
                    binding.rbFlirty.id      -> Personality.FLIRTY
                    binding.rbAdventurous.id -> Personality.ADVENTUROUS
                    else                     -> Personality.SWEET
                }
                selectedSpeechStyle = when (binding.rgSpeech.checkedRadioButtonId) {
                    binding.rbFormal.id -> SpeechStyle.FORMAL
                    binding.rbYouth.id  -> SpeechStyle.YOUTH
                    else               -> SpeechStyle.CASUAL
                }
                showStep(2)
            }
            2 -> {
                selectedEmotionalLevel = when (binding.rgEmotional.checkedRadioButtonId) {
                    binding.rbLow.id  -> EmotionalLevel.LOW
                    binding.rbHigh.id -> EmotionalLevel.HIGH
                    else             -> EmotionalLevel.MEDIUM
                }
                showStep(3)
            }
            3 -> {
                // Recoger intereses seleccionados
                selectedInterests.clear()
                for (i in 0 until binding.chipGroupInterests.childCount) {
                    val chip = binding.chipGroupInterests.getChildAt(i) as? Chip
                    if (chip?.isChecked == true) selectedInterests.add(chip.text.toString())
                }
                showStep(4)
            }
            4 -> finishOnboarding()
        }
    }

    private fun handleBack() {
        if (currentStep > 0) showStep(currentStep - 1)
    }

    private fun pickAvatar() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun finishOnboarding() {
        // Copiar avatar a directorio interno
        val savedAvatarPath = avatarUri?.let { copyAvatarToInternal(it) }

        val config = CompanionConfig(
            name          = companionName,
            gender        = selectedGender,
            personality   = selectedPersonality,
            speechStyle   = selectedSpeechStyle,
            emotionalLevel = selectedEmotionalLevel,
            interests     = selectedInterests.toList(),
            avatarPath    = savedAvatarPath
        )

        prefs.companionConfig = config
        prefs.isOnboardingComplete = true

        goToHome()
    }

    private fun copyAvatarToInternal(uri: Uri): String? {
        return try {
            val dir = File(filesDir, "avatars").apply { mkdirs() }
            val file = File(dir, "avatar.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) { null }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    // ─── Extension helpers ────────────────────────────────────────────────────
    private fun android.view.View.show() { visibility = android.view.View.VISIBLE }
    private fun android.view.View.hide() { visibility = android.view.View.GONE }
}
