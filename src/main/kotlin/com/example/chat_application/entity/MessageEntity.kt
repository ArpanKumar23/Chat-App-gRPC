package com.example.chat_application.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "messages")
data class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var messageId: Int = 0, // 0 is important otherwise sql might try update instead of creating a new row.
    val username: String,
    val roomId: String,
    val content: String,
    val timestamp: Long
)
