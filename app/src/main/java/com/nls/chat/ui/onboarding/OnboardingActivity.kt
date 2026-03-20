package com.nls.chat.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.nls.chat.R
import com.nls.chat.data.local.PreferencesManager
import com.nls.chat.databinding.ActivityOnboardingBinding
import com.nls.chat.model.*
import com.nls.chat.ui.home.HomeActivity
import java.io.File
import java.io.FileOutputStream

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefs: PreferencesManager

    private var currentStep = 0
    private val totalSteps = 5

    private var companionName = ""
    private var selectedGender = Gender.FEMALE
    private var selectedPersonality = Personality.SWEET
    private var selectedSpeechStyle = SpeechStyle.CASUAL
    private var selectedEmotionalLevel = EmotionalLevel.MEDIUM
    private var selectedInterests = mutableListOf<String>()
    private var avatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            avatarUri = it
            Glide.with(this).load(it).circleCrop().into(binding.ivAvatarPreview)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pickImageLauncher.launch("image/*")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

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

        val layouts = listOf(binding.step0Layout, binding.step1Layout, binding.step2Layout, binding.step3Layout, binding.step4Layout)
        layouts.forEachIndexed { index, view -> view.visibility = if (index == step) View.VISIBLE else View.GONE }
        
        binding.btnBack.visibility = if (step == 0) View.GONE else View.VISIBLE
        binding.btnNext.text = if (step == 4) "¡Crear!" else "Siguiente"
    }

    private fun handleNext() {
        when (currentStep) {
            0 -> {
                companionName = binding.etCompanionName.text.toString().trim()
                if (companionName.isBlank()) {
                    binding.etCompanionName.error = "Nombre requerido"
                    return
                }
                val checked = binding.rgGender.checkedRadioButtonId
                selectedGender = when (checked) {
                    R.id.rbMale -> Gender.MALE
                    R.id.rbNonBinary -> Gender.NON_BINARY
                    else -> Gender.FEMALE
                }
                showStep(1)
            }
            1 -> {
                val pId = binding.rgPersonality.checkedRadioButtonId
                selectedPersonality = when (pId) {
                    R.id.rbFunny -> Personality.FUNNY
                    R.id.rbSerious -> Personality.SERIOUS
                    R.id.rbFlirty -> Personality.FLIRTY
                    R.id.rbAdventurous -> Personality.ADVENTUROUS
                    else -> Personality.SWEET
                }
                val sId = binding.rgSpeech.checkedRadioButtonId
                selectedSpeechStyle = when (sId) {
                    R.id.rbFormal -> SpeechStyle.FORMAL
                    R.id.rbYouth -> SpeechStyle.YOUTH
                    else -> SpeechStyle.CASUAL
                }
                showStep(2)
            }
            2 -> {
                val eId = binding.rgEmotional.checkedRadioButtonId
                selectedEmotionalLevel = when (eId) {
                    R.id.rbLow -> EmotionalLevel.LOW
                    R.id.rbHigh -> EmotionalLevel.HIGH
                    else -> EmotionalLevel.MEDIUM
                }
                showStep(3)
            }
            3 -> {
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

    private fun handleBack() { if (currentStep > 0) showStep(currentStep - 1) }

    private fun pickAvatar() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) pickImageLauncher.launch("image/*")
        else requestPermissionLauncher.launch(permission)
    }

    private fun finishOnboarding() {
        val path = avatarUri?.let { copyAvatarToInternal(it) }
        val config = CompanionConfig(companionName, selectedGender, selectedPersonality, selectedSpeechStyle, selectedEmotionalLevel, selectedInterests.toList(), path)
        prefs.companionConfig = config
        prefs.isOnboardingComplete = true
        goToHome()
    }

    private fun copyAvatarToInternal(uri: Uri): String? {
        return try {
            val file = File(File(filesDir, "avatars").apply { mkdirs() }, "avatar.jpg")
            contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { input.copyTo(it) } }
            file.absolutePath
        } catch (e: Exception) { null }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
