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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaxItDatabase(@ApplicationContext context: Context): TaxItDatabase =
        Room.databaseBuilder(context, TaxItDatabase::class.java, "taxit_database")
            .build()

    @Provides
    @Singleton
    fun provideTaxCalculationDao(db: TaxItDatabase): TaxCalculationDao =
        db.taxCalculationDao()

    @Provides
    @Singleton
    fun provideZipRateCacheDao(db: TaxItDatabase): ZipRateCacheDao =
        db.zipRateCacheDao()
}
