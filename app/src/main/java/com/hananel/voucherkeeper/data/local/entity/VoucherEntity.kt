package com.hananel.voucherkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a voucher (approved or pending).
 * 
 * @property id Unique identifier
 * @property status Voucher status: "approved" or "pending"
 * @property merchantName Extracted merchant/provider name
 * @property amount Voucher amount (if detected)
 * @property voucherUrl Redemption URL (if available)
 * @property redeemCode Redemption code (if available)
 * @property senderPhone SMS sender phone number
 * @property senderName SMS sender name (if available)
 * @property rawMessage Original SMS message content
 * @property timestamp Message reception timestamp (epoch millis)
 */
@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val status: String, // "approved" or "pending"
    val merchantName: String? = null,
    val amount: String? = null,
    val voucherUrl: String? = null,
    val redeemCode: String? = null,
    val senderPhone: String,
    val senderName: String? = null,
    val rawMessage: String,
    val timestamp: Long
)

