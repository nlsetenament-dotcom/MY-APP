package com.nls.chat.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nls.chat.databinding.ItemMessageReceivedBinding
import com.nls.chat.databinding.ItemMessageSentBinding
import com.nls.chat.model.Message
import com.nls.chat.model.MessageSender
import com.nls.chat.model.MessageStatus

/**
 * MessageAdapter
 * Adapter para RecyclerView del chat.
 * Maneja dos tipos de vistas: mensajes enviados (derecha) y recibidos (izquierda).
 * Usa ListAdapter + DiffUtil para animaciones eficientes.
 */
class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_SENT     = 1
        private const val VIEW_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).sender == MessageSender.USER) VIEW_SENT else VIEW_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_SENT) {
            SentViewHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
        } else {
            ReceivedViewHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is SentViewHolder     -> holder.bind(msg)
            is ReceivedViewHolder -> holder.bind(msg)
        }
    }

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    inner class SentViewHolder(private val b: ItemMessageSentBinding)
        : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvMessage.text = msg.text
            b.tvTime.text    = msg.getFormattedTime()
            // Icono de estado
            b.ivStatus.setImageResource(
                if (msg.status == MessageStatus.ERROR)
                    android.R.drawable.ic_delete
                else
                    android.R.drawable.checkbox_on_background
            )
        }
    }

    inner class ReceivedViewHolder(private val b: ItemMessageReceivedBinding)
        : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvMessage.text = msg.text
            b.tvTime.text    = msg.getFormattedTime()
        }
    }

    // ─── DiffUtil ─────────────────────────────────────────────────────────────

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(old: Message, new: Message) = old.id == new.id
        override fun areContentsTheSame(old: Message, new: Message) = old == new
    }
}
