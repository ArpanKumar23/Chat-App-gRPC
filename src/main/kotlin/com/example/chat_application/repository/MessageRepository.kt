package com.example.chat_application.repository

import com.example.chat_application.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<MessageEntity, Int> {
    fun findByRoomId(roomId: String): List<MessageEntity>
}