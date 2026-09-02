package com.example.bussetu.feature_chatbot.data.remote

data class ChatbotRequestDto(
    val message: String
)

data class ChatbotResponseDto(
    val text: String,
    val icon: String?
)
