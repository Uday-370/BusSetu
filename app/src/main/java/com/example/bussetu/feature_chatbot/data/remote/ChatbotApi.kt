package com.example.bussetu.feature_chatbot.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApi {
    @POST("/api/chatbot")
    suspend fun sendMessage(@Body request: ChatbotRequestDto): ChatbotResponseDto
}
