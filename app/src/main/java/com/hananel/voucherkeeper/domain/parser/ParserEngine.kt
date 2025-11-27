package com.hananel.voucherkeeper.domain.parser

import android.util.Log
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
    
    companion object {
        private const val TAG = "VoucherKeeper_Parser"
    }
    
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
        
        Log.d(TAG, "=== PARSER ENGINE - Analyzing Message ===")
        Log.d(TAG, "Message length: ${bodyText.length} chars")
        
        // Extract data from message
        val extractedData = extractVoucherData(message, customTrustedDomains)
        
        // Extract ALL URLs from the message
        val allUrls = extractAllUrls(bodyText)
        Log.d(TAG, "All URLs in message: ${allUrls.joinToString(", ")}")
        
        // Calculate boolean flags for decision tree
        val hasUrl = extractedData.voucherUrl != null
        
        // Check if ANY of the URLs contains a trusted domain
        val hasTrustedVoucherDomain = allUrls.any { url ->
            WordBanks.containsTrustedDomain(url, customTrustedDomains)
        }
        
        Log.d(TAG, "Trusted domain check result: $hasTrustedVoucherDomain")
        val hasRedeemCode = extractedData.redeemCode != null
        val hasStrongVoucherWord = WordBanks.containsAnyTerm(bodyText, WordBanks.STRONG_VOUCHER_TERMS)
        val hasCouponPromoWord = WordBanks.containsAnyTerm(bodyText, WordBanks.COUPON_PROMO_TERMS)
        val hasHardSpamIndicator = WordBanks.containsAnyTerm(bodyText, WordBanks.HARD_SPAM_INDICATORS)
        val hasAccessPoint = hasTrustedVoucherDomain || hasRedeemCode
        
        Log.d(TAG, "Decision Flags:")
        Log.d(TAG, "  - isApprovedSender: $isApprovedSender")
        Log.d(TAG, "  - hasUrl: $hasUrl")
        Log.d(TAG, "  - hasTrustedVoucherDomain: $hasTrustedVoucherDomain")
        Log.d(TAG, "  - hasRedeemCode: $hasRedeemCode")
        Log.d(TAG, "  - hasStrongVoucherWord: $hasStrongVoucherWord")
        Log.d(TAG, "  - hasCouponPromoWord: $hasCouponPromoWord")
        Log.d(TAG, "  - hasHardSpamIndicator: $hasHardSpamIndicator")
        Log.d(TAG, "  - hasAccessPoint: $hasAccessPoint")
        
        Log.d(TAG, "Extracted Data:")
        Log.d(TAG, "  - merchantName: ${extractedData.merchantName}")
        Log.d(TAG, "  - amount: ${extractedData.amount}")
        Log.d(TAG, "  - voucherUrl: ${extractedData.voucherUrl}")
        Log.d(TAG, "  - redeemCode: ${extractedData.redeemCode}")
        
        // ==========================================
        // DECISION TREE (Enhanced with ChatGPT refinements)
        // ==========================================
        
        // SUPER PRE-FILTER: Hard spam indicators ALWAYS discard
        // These phrases ALWAYS mean spam, even with voucher words
        if (hasHardSpamIndicator) {
            Log.d(TAG, "→ DISCARD: Hard spam indicators detected (marketing newsletter)")
            return VoucherDecision.Discard
        }
        
        // PRE-FILTER: Hard anti-spam wall
        // Block ALL promo/marketing messages that don't have strong voucher words
        if (hasCouponPromoWord && !hasStrongVoucherWord) {
            Log.d(TAG, "→ DISCARD: Marketing/promo content (no strong voucher words)")
            return VoucherDecision.Discard
        }
        
        // APPROVED: Trusted sender + voucher words + (trusted domain OR redeem code)
        // Real vouchers from known senders with verified access points
        if (isApprovedSender && hasStrongVoucherWord && (hasRedeemCode || hasTrustedVoucherDomain)) {
            Log.d(TAG, "→ APPROVED: Trusted sender + verified access point")
            return VoucherDecision.Approved(extractedData)
        }
        
        // PENDING: Trusted sender + voucher words BUT unknown domain
        // Even trusted senders need review if domain is not recognized
        if (isApprovedSender && hasStrongVoucherWord && !hasTrustedVoucherDomain && hasUrl) {
            Log.d(TAG, "→ PENDING: Trusted sender but unknown domain - needs review")
            return VoucherDecision.Pending(extractedData)
        }
        
        // PENDING: Unknown sender + voucher words + access point
        // Looks like a real voucher but sender not verified yet
        if (!isApprovedSender && hasStrongVoucherWord && hasAccessPoint) {
            Log.d(TAG, "→ PENDING: Unknown sender but looks like voucher")
            return VoucherDecision.Pending(extractedData)
        }
        
        // DISCARD: Everything else
        Log.d(TAG, "→ DISCARD: Did not meet criteria")
        Log.d(TAG, "  Reasons:")
        if (!hasStrongVoucherWord) Log.d(TAG, "    - No strong voucher words")
        if (!hasAccessPoint) Log.d(TAG, "    - No access point (URL or code)")
        
        return VoucherDecision.Discard
    }
    
    /**
     * Public method to extract voucher data from plain text.
     * Used for manual entry when user pastes SMS message.
     * 
     * @param messageText Raw SMS text pasted by user
     * @return ExtractedData with parsed fields (URL, code, amount, merchant)
     */
    fun extractFromText(messageText: String): ExtractedData {
        val mockMessage = SMSMessage(
            bodyText = messageText,
            senderPhone = "Manual Entry",
            senderName = null,
            timestamp = System.currentTimeMillis()
        )
        return extractVoucherData(mockMessage, emptyList())
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
     * Returns the first URL found.
     */
    private fun extractUrl(text: String): String? {
        // Match URLs - stops at whitespace or common delimiters
        val urlRegex = Regex(
            pattern = """https?://[^\s]+""",
            option = RegexOption.IGNORE_CASE
        )
        
        // Find all URLs
        val urls = urlRegex.findAll(text).map { it.value.trim() }.toList()
        
        if (urls.isEmpty()) {
            return null
        }
        
        // Log all found URLs for debugging
        Log.d(TAG, "extractUrl found ${urls.size} URLs")
        urls.forEachIndexed { index, url ->
            Log.d(TAG, "  URL $index: $url")
        }
        
        // If only one URL, return it
        if (urls.size == 1) {
            return urls.first()
        }
        
        // Multiple URLs - smart filtering to find the voucher URL (not terms/regulations)
        val termsKeywords = listOf(
            // Hebrew - תקנונים ותנאים
            "תקנון", "תנאים", "תנאי", "שימוש", "תקנון-שימוש", 
            "תנאי-שימוש", "מדיניות", "פרטיות", "מידע", "עזרה",
            
            // English - Full words
            "terms", "conditions", "regulations", "policy", "privacy",
            "rules", "legal", "agreement", "disclaimer", "guidelines",
            "faq", "help", "support", "about", "info", "details",
            
            // English - Common variations in URLs
            "terms-and-conditions", "termsandconditions", "terms_conditions",
            "terms-of-use", "termsofuse", "terms_of_use",
            "t-and-c", "tandc", "t&c", "tnc",
            "privacy-policy", "privacypolicy", "privacy_policy",
            "user-agreement", "useragreement",
            "learn-more", "learnmore", "more-info", "moreinfo"
        )
        
        // Filter out URLs that are likely terms/regulations
        val voucherUrls = urls.filter { url ->
            val urlLower = url.lowercase()
            val textBeforeUrl = text.substringBefore(url, "").takeLast(50).lowercase()
            
            // Check if URL or text before it contains terms keywords
            val isTermsUrl = termsKeywords.any { keyword ->
                urlLower.contains(keyword) || textBeforeUrl.contains(keyword)
            }
            
            if (isTermsUrl) {
                Log.d(TAG, "  ⚠️ Filtered out terms URL: $url")
            }
            
            !isTermsUrl
        }
        
        // Return the first non-terms URL, or if all filtered, return shortest URL
        val selectedUrl = voucherUrls.firstOrNull() ?: urls.minByOrNull { it.length }
        
        Log.d(TAG, "  ✅ Selected voucher URL: $selectedUrl")
        return selectedUrl
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
     * Supports both: "100 ₪", "₪100", "100 NIS", "$50", etc.
     */
    private fun extractAmount(text: String): String? {
        // Pattern 1: Currency symbol BEFORE number (₪100.00)
        val beforePattern = Regex(
            pattern = """(?:₪|NIS|ILS|\$|USD|EUR|€)\s*(\d+(?:[.,]\d{1,2})?)""",
            option = RegexOption.IGNORE_CASE
        )
        beforePattern.find(text)?.let { match ->
            val amount = match.value
            Log.d(TAG, "Found amount (currency before): $amount")
            return amount
        }
        
        // Pattern 2: Currency symbol AFTER number (100 ₪)
        val afterPattern = Regex(
            pattern = """(\d+(?:[.,]\d{1,2})?)\s*(?:₪|NIS|ILS|\$|USD|EUR|€)""",
            option = RegexOption.IGNORE_CASE
        )
        afterPattern.find(text)?.let { match ->
            val amount = match.value
            Log.d(TAG, "Found amount (currency after): $amount")
            return amount
        }
        
        Log.d(TAG, "No amount found in text")
        return null
    }
    
    /**
     * Extract ALL URLs from text (for checking trusted domains).
     */
    private fun extractAllUrls(text: String): List<String> {
        val urlRegex = Regex(
            pattern = """https?://[^\s]+""",
            option = RegexOption.IGNORE_CASE
        )
        val urls = urlRegex.findAll(text).map { it.value.trim() }.toList()
        
        Log.d(TAG, "extractAllUrls found ${urls.size} URLs")
        urls.forEachIndexed { index, url ->
            Log.d(TAG, "  All URL $index: $url")
        }
        
        return urls
    }
    
    /**
     * Extract merchant name.
     * Returns sender name ONLY - we don't guess from URL or text.
     * The sender is the most reliable source.
     */
    private fun extractMerchantName(text: String, senderName: String?): String? {
        // Only return sender name if available
        // Don't try to extract from URL or text - it's unreliable
        return senderName
    }
}

