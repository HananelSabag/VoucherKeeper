package com.hananel.voucherkeeper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hananel.voucherkeeper.data.local.dao.ApprovedSenderDao
import com.hananel.voucherkeeper.data.local.dao.TrustedDomainDao
import com.hananel.voucherkeeper.data.local.dao.VoucherDao
import com.hananel.voucherkeeper.data.local.entity.ApprovedSenderEntity
import com.hananel.voucherkeeper.data.local.entity.TrustedDomainEntity
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity

/**
 * Main Room database for Voucher Keeper.
 * Contains vouchers, approved senders, and trusted domains.
 */
@Database(
    entities = [
        VoucherEntity::class,
        ApprovedSenderEntity::class,
        TrustedDomainEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VoucherDatabase : RoomDatabase() {
    abstract fun voucherDao(): VoucherDao
    abstract fun approvedSenderDao(): ApprovedSenderDao
    abstract fun trustedDomainDao(): TrustedDomainDao
    
    companion object {
        /**
         * Migration from version 1 to 2.
         * Adds isRedeemed and redeemedAt columns to vouchers table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE vouchers ADD COLUMN isRedeemed INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE vouchers ADD COLUMN redeemedAt INTEGER")
            }
        }
    }
}

