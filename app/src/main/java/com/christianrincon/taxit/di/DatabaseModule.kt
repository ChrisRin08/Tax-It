package com.christianrincon.taxit.di

import android.content.Context
import androidx.room.Room
import com.christianrincon.taxit.data.db.TaxCalculationDao
import com.christianrincon.taxit.data.db.TaxItDatabase
import com.christianrincon.taxit.data.db.ZipRateCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Provides Room database objects that Hilt can inject.
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaxItDatabase(@ApplicationContext context: Context): TaxItDatabase =
        // Creates the local database for history and ZIP rate cache data.
        Room.databaseBuilder(context, TaxItDatabase::class.java, "taxit_database")
            .build()

    @Provides
    @Singleton
    fun provideTaxCalculationDao(db: TaxItDatabase): TaxCalculationDao =
        // Gives the repository access to saved calculation history.
        db.taxCalculationDao()

    @Provides
    @Singleton
    fun provideZipRateCacheDao(db: TaxItDatabase): ZipRateCacheDao =
        // Gives the repository access to cached ZIP tax rates.
        db.zipRateCacheDao()
}
