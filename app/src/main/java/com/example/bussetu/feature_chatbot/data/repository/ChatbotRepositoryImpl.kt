package com.example.bussetu.feature_chatbot.data.repository

import com.example.bussetu.feature_chatbot.data.remote.ChatbotApi
import com.example.bussetu.feature_chatbot.data.remote.ChatbotRequestDto
import com.example.bussetu.feature_chatbot.domain.model.ChatMessage
import com.example.bussetu.feature_chatbot.domain.repository.ChatbotRepository
import javax.inject.Inject

class ChatbotRepositoryImpl @Inject constructor(
    private val api: ChatbotApi
) : ChatbotRepository {
    override suspend fun sendMessage(text: String): Result<ChatMessage> {
        return try {
            val response = api.sendMessage(ChatbotRequestDto(text))
            Result.success(
                ChatMessage(
                    sender = "bot",
                    text = response.text,
                    icon = response.icon
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
