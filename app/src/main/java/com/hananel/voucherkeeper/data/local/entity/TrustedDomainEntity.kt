package com.hananel.voucherkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a trusted voucher domain.
 * URLs containing these domains are considered valid voucher access points.
 * 
 * @property domain Domain name (e.g., "pluxee.co.il")
 */
@Entity(tableName = "trusted_domains")
data class TrustedDomainEntity(
    @PrimaryKey
    val domain: String
)

