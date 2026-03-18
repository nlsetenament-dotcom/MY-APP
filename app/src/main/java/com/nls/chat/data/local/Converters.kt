package com.nls.chat.data.local

import androidx.room.TypeConverter
import com.nls.chat.model.MessageSender
import com.nls.chat.model.MessageStatus

/**
 * Converters
 * Convierte los enums a String para que Room pueda guardarlos en SQLite.
 */
class Converters {

    @TypeConverter
    fun fromSender(sender: MessageSender): String = sender.name

    @TypeConverter
    fun toSender(value: String): MessageSender = MessageSender.valueOf(value)

    @TypeConverter
    fun fromStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): MessageStatus = MessageStatus.valueOf(value)
}
