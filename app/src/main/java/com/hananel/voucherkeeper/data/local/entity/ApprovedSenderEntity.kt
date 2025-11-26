package com.hananel.voucherkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an approved SMS sender.
 * Messages from approved senders are automatically classified as vouchers
 * if they contain strong voucher terms.
 * 
 * @property phone Phone number (primary key)
 * @property name Optional friendly name for the sender
 */
@Entity(tableName = "approved_senders")
data class ApprovedSenderEntity(
    @PrimaryKey
    val phone: String,
    val name: String? = null
)

