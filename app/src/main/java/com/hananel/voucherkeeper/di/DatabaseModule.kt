package com.hananel.voucherkeeper.di

import android.content.Context
import androidx.room.Room
import com.hananel.voucherkeeper.data.local.VoucherDatabase
import com.hananel.voucherkeeper.data.local.dao.ApprovedSenderDao
import com.hananel.voucherkeeper.data.local.dao.TrustedDomainDao
import com.hananel.voucherkeeper.data.local.dao.VoucherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideVoucherDatabase(
        @ApplicationContext context: Context
    ): VoucherDatabase {
        return Room.databaseBuilder(
            context,
            VoucherDatabase::class.java,
            "voucher_keeper_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideVoucherDao(database: VoucherDatabase): VoucherDao {
        return database.voucherDao()
    }
    
    @Provides
    fun provideApprovedSenderDao(database: VoucherDatabase): ApprovedSenderDao {
        return database.approvedSenderDao()
    }
    
    @Provides
    fun provideTrustedDomainDao(database: VoucherDatabase): TrustedDomainDao {
        return database.trustedDomainDao()
    }
    
    @Provides
    @Singleton
    fun provideSeedDataModule(
        @ApplicationContext context: Context,
        database: VoucherDatabase
    ): SeedDataModule {
        return SeedDataModule(context, database)
    }
}

