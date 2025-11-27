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
     * - "972542199006" → "542199006"
     * - "+972-54-219-9006" → "542199006"
     * - "054-219-9006" → "542199006"
     */
    fun normalize(phoneNumber: String): String {
        // Remove all non-digit characters (spaces, dashes, parentheses, +)
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")
        
        return when {
            // Israeli international format: +972XXXXXXXXX → XXXXXXXXX
            digitsOnly.startsWith("972") && digitsOnly.length >= 12 -> {
                digitsOnly.substring(3) // Remove "972" prefix
            }
            
            // Israeli local format: 0XXXXXXXXX → XXXXXXXXX
            digitsOnly.startsWith("0") && digitsOnly.length == 10 -> {
                digitsOnly.substring(1) // Remove leading "0"
            }
            
            // Already normalized or different format
            else -> digitsOnly
        }
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

