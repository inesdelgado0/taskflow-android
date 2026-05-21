package com.taskflow.app.di

import com.taskflow.app.BuildConfig
import com.taskflow.app.data.remote.api.AuthApi
import com.taskflow.app.data.remote.api.EvaluationApi
import com.taskflow.app.data.remote.api.ObservationApi
import com.taskflow.app.data.remote.api.ProjectApi
import com.taskflow.app.data.remote.api.TaskApi
import com.taskflow.app.data.remote.interceptor.AuthInterceptor
import com.taskflow.app.data.remote.interceptor.NetworkErrorInterceptor
import com.taskflow.app.data.remote.interceptor.TokenRefreshInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL        = "http://10.0.2.2:3000/v1/"
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT    = 30L
    private const val WRITE_TIMEOUT   = 30L

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor:      HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor,
        networkErrorInterceptor: NetworkErrorInterceptor
    ): OkHttpClient = OkHttpClient.Builder()

        .addInterceptor(networkErrorInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(tokenRefreshInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProjectApi(retrofit: Retrofit): ProjectApi =
        retrofit.create(ProjectApi::class.java)

    @Provides
    @Singleton
    fun provideTaskApi(retrofit: Retrofit): TaskApi =
        retrofit.create(TaskApi::class.java)

    @Provides
    @Singleton
    fun provideObservationApi(retrofit: Retrofit): ObservationApi =
        retrofit.create(ObservationApi::class.java)

    @Provides
    @Singleton
    fun provideEvaluationApi(retrofit: Retrofit): EvaluationApi =
        retrofit.create(EvaluationApi::class.java)

}
