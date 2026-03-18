package com.nls.chat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Message
 * Entidad Room que representa un mensaje del chat.
 * Cada mensaje tiene sender (USER o AI), texto, timestamp y estado.
 */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val text: String,

    val sender: MessageSender,

    val timestamp: Long = System.currentTimeMillis(),

    val status: MessageStatus = MessageStatus.SENT
) {
    /** Devuelve la hora formateada HH:mm */
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /** Devuelve la fecha formateada (para separadores de día) */
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("d 'de' MMMM", Locale("es"))
        return sdf.format(Date(timestamp))
    }

    val isFromUser: Boolean get() = sender == MessageSender.USER
}

enum class MessageSender { USER, AI }

enum class MessageStatus { SENDING, SENT, ERROR }
