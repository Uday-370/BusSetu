package com.example.bussetu.feature_chatbot.domain.model

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String, // "user" or "bot"
    val text: String,
    val icon: String? = null,
    val isTyping: Boolean = false
)
