package com.example.bussetu.di

import com.example.bussetu.feature_map.data.remote.PassengerApi
import com.example.bussetu.feature_map.data.repository.PassengerRepositoryImpl
import com.example.bussetu.feature_map.domain.repository.PassengerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PassengerModule {

    // 1. Tells Hilt how to create the PassengerApi using Retrofit
    @Provides
    @Singleton
    fun providePassengerApi(retrofit: Retrofit): PassengerApi {
        return retrofit.create(PassengerApi::class.java)
    }

    // 2. Tells Hilt to provide PassengerRepositoryImpl whenever PassengerRepository is requested
    @Provides
    @Singleton
    fun providePassengerRepository(
        api: PassengerApi
    ): PassengerRepository {
        // We pass the API into the implementation just like your AuthModule!
        return PassengerRepositoryImpl(api)
    }
}