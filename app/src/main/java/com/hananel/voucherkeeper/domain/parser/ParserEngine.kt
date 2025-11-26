package com.hananel.voucherkeeper.domain.parser

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser Engine - The brain of Voucher Keeper.
 * 
 * Implements the decision tree logic to classify SMS messages as:
 * - APPROVED: Real voucher from approved sender
 * - PENDING: Looks like voucher but unknown sender
 * - DISCARD: Promotional/marketing content
 * 
 * @author Hananel Sabag
 */
@Singleton
class ParserEngine @Inject constructor() {
    
    /**
     * Process an incoming SMS message and classify it.
     * 
     * @param message SMS message to process
     * @param isApprovedSender Whether the sender is in the approved list
     * @param customTrustedDomains Additional trusted domains from user settings
     * @return Classification decision (Approved, Pending, or Discard)
     */
    fun process(
        message: SMSMessage,
        isApprovedSender: Boolean,
        customTrustedDomains: List<String> = emptyList()
    ): VoucherDecision {
        val bodyText = message.bodyText
        
        // Extract data from message
        val extractedData = extractVoucherData(message, customTrustedDomains)
        
        // Calculate boolean flags for decision tree
        val hasUrl = extractedData.voucherUrl != null
        val hasTrustedVoucherDomain = extractedData.voucherUrl?.let { url ->
            WordBanks.containsTrustedDomain(url, customTrustedDomains)
        } ?: false
        val hasRedeemCode = extractedData.redeemCode != null
        val hasStrongVoucherWord = WordBanks.containsAnyTerm(bodyText, WordBanks.STRONG_VOUCHER_TERMS)
        val hasCouponPromoWord = WordBanks.containsAnyTerm(bodyText, WordBanks.COUPON_PROMO_TERMS)
        val hasAccessPoint = hasTrustedVoucherDomain || hasRedeemCode
        
        // ==========================================
        // DECISION TREE (Exact spec implementation)
        // ==========================================
        
        // PRE-FILTER: Remove marketing content
        if (hasCouponPromoWord && !hasStrongVoucherWord) {
            return VoucherDecision.Discard
        }
        
        // APPROVED: Known sender + strong terms + access point
        if (isApprovedSender && hasStrongVoucherWord && hasAccessPoint) {
            return VoucherDecision.Approved(extractedData)
        }
        
        // PENDING: Unknown sender + strong terms + access point
        if (!isApprovedSender && hasStrongVoucherWord && hasAccessPoint) {
            return VoucherDecision.Pending(extractedData)
        }
        
        // DISCARD: Everything else
        return VoucherDecision.Discard
    }
    
    /**
     * Extract voucher data from SMS message.
     * Attempts to find merchant name, amount, URL, and redemption code.
     */
    private fun extractVoucherData(
        message: SMSMessage,
        customTrustedDomains: List<String>
    ): ExtractedData {
        val bodyText = message.bodyText
        
        // Extract URL
        val url = extractUrl(bodyText)
        
        // Extract redemption code (alphanumeric sequences)
        val redeemCode = extractRedeemCode(bodyText)
        
        // Extract amount (currency patterns)
        val amount = extractAmount(bodyText)
        
        // Extract merchant name (basic heuristic)
        val merchantName = extractMerchantName(bodyText, message.senderName)
        
        return ExtractedData(
            merchantName = merchantName,
            amount = amount,
            voucherUrl = url,
            redeemCode = redeemCode,
            rawMessage = bodyText
        )
    }
    
    /**
     * Extract URL from text using regex.
     */
    private fun extractUrl(text: String): String? {
        val urlRegex = Regex(
            pattern = """https?://[^\s]+""",
            option = RegexOption.IGNORE_CASE
        )
        return urlRegex.find(text)?.value
    }
    
    /**
     * Extract redemption code.
     * Looks for patterns like "CODE: ABC123" or standalone alphanumeric sequences.
     */
    private fun extractRedeemCode(text: String): String? {
        // Pattern 1: "code:" or "קוד:" followed by alphanumeric
        val codePatternRegex = Regex(
            pattern = """(?:code|קוד)[\s:]*([A-Z0-9]{4,})""",
            options = setOf(RegexOption.IGNORE_CASE)
        )
        codePatternRegex.find(text)?.groupValues?.getOrNull(1)?.let { return it }
        
        // Pattern 2: Standalone uppercase alphanumeric (6+ chars)
        val standaloneCodeRegex = Regex("""[A-Z0-9]{6,}""")
        return standaloneCodeRegex.find(text)?.value
    }
    
    /**
     * Extract amount from text.
     * Supports: "100 ₪", "100 NIS", "$50", etc.
     */
    private fun extractAmount(text: String): String? {
        val amountRegex = Regex(
            pattern = """(\d+(?:[.,]\d{1,2})?)\s*(?:₪|NIS|ILS|\$|USD|EUR|€)""",
            option = RegexOption.IGNORE_CASE
        )
        return amountRegex.find(text)?.value
    }
    
    /**
     * Extract merchant name.
     * Uses sender name as fallback or attempts to find brand names in text.
     */
    private fun extractMerchantName(text: String, senderName: String?): String? {
        // Use sender name if available
        senderName?.let { return it }
        
        // Look for known merchants in text
        val knownMerchants = listOf(
            "Pluxee", "Cibus", "Edenred", "Shufersal", "שופרסל",
            "MultiPass", "מולטיפאס"
        )
        
        knownMerchants.forEach { merchant ->
            if (text.contains(merchant, ignoreCase = true)) {
                return merchant
            }
        }
        
        return null
    }
}

