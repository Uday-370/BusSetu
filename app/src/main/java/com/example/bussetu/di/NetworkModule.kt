package com.example.bussetu.di

import com.example.bussetu.core.presentation.components.AppConstants
import com.example.bussetu.core.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ─── 1. Logging Interceptor ───────────────────────────────────────────────────
    // Prints every API request + response body to Logcat.
    // Tag: "OkHttp" — Filter in Logcat to see all traffic.
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // ─── 2. Auth Interceptor ──────────────────────────────────────────────────────
    // Automatically attaches "Authorization: Bearer <token>" to every request.
    // Reads from SessionManager.cachedToken (in-memory, no coroutines needed here).
    // Also adds the ngrok bypass header so the tunnel doesn't show a browser warning.
    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Attach JWT token if we have one (logged-in requests)
            val token = sessionManager.cachedToken
            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            // ⚠️ ngrok free tier shows a browser warning page unless we add this header.
            // This tells ngrok to skip the warning and return the actual API response.
            requestBuilder.addHeader("ngrok-skip-browser-warning", "true")

            chain.proceed(requestBuilder.build())
        }
    }

    // ─── 3. OkHttpClient ─────────────────────────────────────────────────────────
    // Combines both interceptors + sets timeouts.
    // Timeouts prevent the app from hanging forever on a bad/slow connection.
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)          // Auth runs first (adds headers)
            .addInterceptor(loggingInterceptor)       // Logging runs last (logs final request)
            .connectTimeout(AppConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AppConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(AppConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // ─── 4. Retrofit ─────────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}