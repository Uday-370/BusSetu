package com.example.bussetu.di

import com.example.bussetu.feature_chatbot.data.remote.ChatbotApi
import com.example.bussetu.feature_chatbot.data.repository.ChatbotRepositoryImpl
import com.example.bussetu.feature_chatbot.domain.repository.ChatbotRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatbotModule {

    @Provides
    @Singleton
    fun provideChatbotApi(retrofit: Retrofit): ChatbotApi {
        return retrofit.create(ChatbotApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatbotRepository(api: ChatbotApi): ChatbotRepository {
        return ChatbotRepositoryImpl(api)
    }
}
