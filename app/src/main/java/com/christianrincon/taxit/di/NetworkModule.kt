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

// Qualifier keeps the Ziptax Retrofit separate from other Retrofit instances.
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZiptaxRetrofit

// Qualifier keeps the backup ZIP lookup Retrofit separate from Ziptax.
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZipLookupRetrofit

// Provides network objects that Hilt can inject into the repository.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideZiptaxOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Adds the Ziptax API key to tax requests only.
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
        // Main Retrofit client for sales tax data.
        Retrofit.Builder()
            .baseUrl("https://api.zip-tax.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @ZipLookupRetrofit
    fun provideZipLookupRetrofit(): Retrofit =
        // Backup Retrofit client for city/state ZIP lookup without an API key.
        Retrofit.Builder()
            .baseUrl("https://api.zippopotam.us/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTaxApiService(@ZiptaxRetrofit retrofit: Retrofit): TaxApiService =
        // Creates the typed Ziptax API interface.
        retrofit.create(TaxApiService::class.java)

    @Provides
    @Singleton
    fun provideZipLookupApiService(@ZipLookupRetrofit retrofit: Retrofit): ZipLookupApiService =
        // Creates the typed backup ZIP lookup API interface.
        retrofit.create(ZipLookupApiService::class.java)
}
