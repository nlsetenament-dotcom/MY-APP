package com.nls.chat.data.local

import androidx.room.*
import com.nls.chat.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * MessageDao
 * Data Access Object para operaciones de mensajes en Room (SQLite).
 */
@Dao
interface MessageDao {

    /** Observa todos los mensajes ordenados por timestamp (Flow reactivo) */
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<Message>>

    /** Retorna todos los mensajes como lista (para cargar historial) */
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<Message>

    /** Inserta un mensaje y retorna su ID generado */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    /** Actualiza un mensaje existente (ej: cambiar estado SENDING → SENT) */
    @Update
    suspend fun updateMessage(message: Message)

    /** Elimina todos los mensajes (borrar chat) */
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    /** Elimina un mensaje específico */
    @Delete
    suspend fun deleteMessage(message: Message)

    /** Cuenta el total de mensajes */
    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getMessageCount(): Int
}
