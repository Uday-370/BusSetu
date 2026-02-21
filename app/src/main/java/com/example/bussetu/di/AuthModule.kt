package com.example.bussetu.di

import com.example.bussetu.core.utils.SessionManager
import com.example.bussetu.feature_auth.data.remote.AuthApi
import com.example.bussetu.feature_auth.data.repository.AuthRepositoryImpl
import com.example.bussetu.feature_auth.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    // 1. Tells Hilt how to create the AuthApi using Retrofit
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    // 2. Tells Hilt to provide AuthRepositoryImpl whenever AuthRepository is requested
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepositoryImpl(api, sessionManager)
    }
}