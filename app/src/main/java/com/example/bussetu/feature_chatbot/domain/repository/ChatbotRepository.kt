package com.example.bussetu.feature_chatbot.domain.repository

import com.example.bussetu.feature_chatbot.domain.model.ChatMessage

interface ChatbotRepository {
    suspend fun sendMessage(text: String): Result<ChatMessage>
}
