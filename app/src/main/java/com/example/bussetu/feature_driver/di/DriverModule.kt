package com.example.bussetu.di

import com.example.bussetu.feature_driver.data.remote.DriverApi
import com.example.bussetu.feature_driver.data.repository.DriverRepositoryImpl
import com.example.bussetu.feature_driver.domain.repository.DriverRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DriverModule {

    @Provides
    @Singleton
    fun provideDriverApi(retrofit: Retrofit): DriverApi {
        return retrofit.create(DriverApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDriverRepository(api: DriverApi): DriverRepository {
        return DriverRepositoryImpl(api)
    }
}