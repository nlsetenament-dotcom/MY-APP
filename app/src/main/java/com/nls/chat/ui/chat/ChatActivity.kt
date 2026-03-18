package com.nls.chat.ui.chat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.nls.chat.R
import com.nls.chat.databinding.ActivityChatBinding
import com.nls.chat.utils.hide
import com.nls.chat.utils.show
import java.io.File

/**
 * ChatActivity
 * Pantalla principal del chat.
 * Observa el ChatViewModel y actualiza la UI reactivamente.
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupInputArea()
        observeViewModel()

        viewModel.showWelcomeIfNeeded()
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val config = viewModel.config
        binding.tvToolbarName.text   = config.name
        binding.tvToolbarStatus.text = "En línea"

        val avatarPath = config.avatarPath
        if (!avatarPath.isNullOrBlank() && File(avatarPath).exists()) {
            Glide.with(this).load(File(avatarPath)).circleCrop()
                .placeholder(R.drawable.ic_avatar_default)
                .into(binding.ivToolbarAvatar)
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        val lm = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.rvMessages.layoutManager = lm
        binding.rvMessages.adapter = adapter
        binding.rvMessages.itemAnimator = null
    }

    private fun setupInputArea() {
        binding.btnSend.setOnClickListener { sendMessage() }

        binding.etMessage.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrBlank()
                binding.btnSend.isEnabled = hasText
                binding.btnSend.alpha = if (hasText) 1f else 0.45f
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.btnSend.isEnabled = false
        binding.btnSend.alpha = 0.45f
    }

    // ─── ViewModel observers ──────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.isTyping.observe(this) { typing ->
            if (typing) {
                binding.layoutTyping.show()
                val anim = AnimationUtils.loadAnimation(this, R.anim.typing_dots)
                binding.tvTypingDot1.startAnimation(anim)
                binding.tvTypingDot2.startAnimation(anim)
                binding.tvTypingDot3.startAnimation(anim)
                binding.rvMessages.post {
                    binding.rvMessages.smoothScrollToPosition(adapter.itemCount)
                }
            } else {
                binding.layoutTyping.hide()
            }
        }

        viewModel.errorEvent.observe(this) { err ->
            err?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isBlank()) return
        binding.etMessage.text?.clear()
        viewModel.sendMessage(text)
    }

    // ─── Menu ─────────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_chat -> {
                AlertDialog.Builder(this)
                    .setTitle("Borrar conversación")
                    .setMessage("¿Seguro que quieres borrar todo el historial?")
                    .setPositiveButton("Borrar") { _, _ -> viewModel.clearChat() }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
