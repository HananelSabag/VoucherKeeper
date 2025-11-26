package com.hananel.voucherkeeper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false
)
abstract class VoucherDatabase : RoomDatabase() {
    abstract fun voucherDao(): VoucherDao
    abstract fun approvedSenderDao(): ApprovedSenderDao
    abstract fun trustedDomainDao(): TrustedDomainDao
}

