package com.christianrincon.taxit.di

import com.christianrincon.taxit.BuildConfig
import com.christianrincon.taxit.data.network.TaxApiService
import com.christianrincon.taxit.data.network.ZipLookupApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZiptaxRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZipLookupRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideZiptaxOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-API-Key", BuildConfig.ZIPTAX_API_KEY)
                    .build()
                chain.proceed(request)
            }
            .build()

    @Provides
    @Singleton
    @ZiptaxRetrofit
    fun provideZiptaxRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.zip-tax.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @ZipLookupRetrofit
    fun provideZipLookupRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.zippopotam.us/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTaxApiService(@ZiptaxRetrofit retrofit: Retrofit): TaxApiService =
        retrofit.create(TaxApiService::class.java)

    @Provides
    @Singleton
    fun provideZipLookupApiService(@ZipLookupRetrofit retrofit: Retrofit): ZipLookupApiService =
        retrofit.create(ZipLookupApiService::class.java)
}
