package com.hananel.voucherkeeper.util

/**
 * Helper for normalizing and comparing phone numbers.
 * Handles Israeli phone number formats (972/+972/0).
 */
object PhoneNumberHelper {
    
    /**
     * Normalize a phone number to a standard format for comparison.
     * Removes all non-digits, then standardizes Israeli numbers.
     * 
     * Examples:
     * - "0542199006" → "542199006"
     * - "+972542199006" → "542199006"
     * - "+9720542199006" → "542199006" (handles redundant 0)
     * - "972542199006" → "542199006"
     * - "+972-54-219-9006" → "542199006"
     * - "054-219-9006" → "542199006"
     * - "02-1234567" → "21234567"
     * - "+97221234567" → "21234567"
     * 
     * Israeli phone formats:
     * - Cellular: 972-50-xxx-xxxx (9 digits after 972)
     * - Landline: 972-2-xxx-xxxx (8 digits after 972)
     * - Local with 0: 0-50-xxx-xxxx or 0-2-xxx-xxxx
     * - IMPORTANT: The "0" is only for local dialing, redundant in international format
     */
    fun normalize(phoneNumber: String): String {
        // Remove all non-digit characters (spaces, dashes, parentheses, +)
        var digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")
        
        // Step 1: Remove Israeli country code if present
        if (digitsOnly.startsWith("972") && digitsOnly.length >= 11) {
            digitsOnly = digitsOnly.substring(3) // Remove "972"
        }
        
        // Step 2: Remove leading "0" (local format or redundant after country code)
        // The "0" is only used for local dialing in Israel, never in international format
        if (digitsOnly.startsWith("0") && digitsOnly.length >= 9) {
            digitsOnly = digitsOnly.substring(1) // Remove the "0"
        }
        
        return digitsOnly
    }
    
    /**
     * Check if two phone numbers match after normalization.
     */
    fun areEqual(phone1: String, phone2: String): Boolean {
        val normalized1 = normalize(phone1)
        val normalized2 = normalize(phone2)
        return normalized1.isNotEmpty() && normalized1 == normalized2
    }
}

