package com.hananel.voucherkeeper.domain.parser

/**
 * Represents an incoming SMS message to be parsed.
 * 
 * @property senderPhone Actual phone number (originatingAddress) - ALWAYS the real number
 * @property senderName Display name (displayOriginatingAddress) if different from phone -
 *                      e.g., "Shufersal", "Terminal X" for saved contact names in system
 * @property bodyText SMS message body
 * @property timestamp Message reception time (epoch millis)
 */
data class SMSMessage(
    val senderPhone: String,        // Real phone number (e.g., "+972501234567")
    val senderName: String? = null, // Saved contact name (e.g., "Shufersal") or null
    val bodyText: String,
    val timestamp: Long
)

/**
 * Extracted voucher data from SMS message.
 * 
 * @property merchantName Detected merchant/provider name
 * @property amount Detected voucher amount (e.g., "100 ₪")
 * @property voucherUrl Redemption URL
 * @property redeemCode Redemption/activation code
 * @property rawMessage Original SMS content (always preserved)
 */
data class ExtractedData(
    val merchantName: String? = null,
    val amount: String? = null,
    val voucherUrl: String? = null,
    val redeemCode: String? = null,
    val rawMessage: String
)

/**
 * Classification decision result from the parser engine.
 */
sealed class VoucherDecision {
    /**
     * Message approved as a real voucher (auto-approved).
     */
    data class Approved(val extractedData: ExtractedData) : VoucherDecision()
    
    /**
     * Message looks like a voucher but from unknown sender (manual review needed).
     */
    data class Pending(val extractedData: ExtractedData) : VoucherDecision()
    
    /**
     * Message rejected as promotional/marketing content.
     */
    data object Discard : VoucherDecision()
}

