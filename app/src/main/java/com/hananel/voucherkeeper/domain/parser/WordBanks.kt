package com.hananel.voucherkeeper.domain.parser

/**
 * Word banks for voucher classification.
 * Contains strong voucher terms, promo/coupon terms, and trusted domains.
 * 
 * Based on extensive analysis of real SMS messages (Hebrew + English).
 */
object WordBanks {
    
    /**
     * Strong voucher terms indicating a real monetary voucher.
     * Messages containing these terms are likely to be actual vouchers.
     */
    val STRONG_VOUCHER_TERMS = setOf(
        // Hebrew voucher terms
        "שובר",
        "שובר דיגיטלי",
        "שובר אישי",
        "שובר בסך",
        "תו קנייה",
        "תו קניה",
        "תו מתנה",
        "כרטיס מתנה",
        "גיפט קארד",
        "קוד למימוש ההטבה",
        "קוד למימוש השובר",
        "קוד למימוש התו",
        "יתרת השובר",
        "יתרת התו",
        "הוטען לזכותך",
        "קיבלת שובר",
        "קיבלת תו",
        "לצפייה בשובר",
        "לצפיה בשובר",
        "תודה על רכישתך",
        "מימוש ההטבה",
        "אתר ההטבות",
        "ממשק מולטיפאס",
        
        // English voucher terms
        "voucher",
        "e-voucher",
        "gift card",
        "giftcard",
        "e-gift",
        "store credit",
        "voucher code",
        "gift card code",
        "redeem your voucher",
        "redeem gift card"
    )
    
    /**
     * Promo/coupon/marketing terms indicating promotional content.
     * Messages with ONLY these terms (without strong voucher terms) are discarded.
     */
    val COUPON_PROMO_TERMS = setOf(
        // Hebrew promo terms
        "קופון",
        "קוד קופון",
        "קוד הנחה",
        "קוד הטבה",
        "הנחה",
        "הנחות",
        "מבצע",
        "מבצעים",
        "סייל",
        "דיל",
        "1+1",
        "עד %",
        "% הנחה",
        "משלוח חינם",
        "ללא כפל מבצעים",
        "מינ' הזמנה",
        "להזמנה",
        "בלעדי לחברי VIP",
        "תקף ל-",
        "ימים אחרונים",
        "עד גמר המלאי",
        
        // English promo terms
        "coupon",
        "promo code",
        "promocode",
        "discount",
        "sale",
        "deal",
        "% off",
        "limited time",
        "only today",
        "free shipping",
        "order now"
    )
    
    /**
     * Trusted voucher domains.
     * URLs containing these domains are considered valid voucher access points.
     */
    val TRUSTED_VOUCHER_DOMAINS = setOf(
        "myconsumers.pluxee.co.il",
        "cibus.pluxee.co.il",
        "pluxee.co.il",
        "edenred.co.il",
        "shufersal.co.il"
    )
    
    /**
     * Checks if text contains any term from the given set (case-insensitive).
     */
    fun containsAnyTerm(text: String, terms: Set<String>): Boolean {
        val lowerText = text.lowercase()
        return terms.any { term -> lowerText.contains(term.lowercase()) }
    }
    
    /**
     * Checks if URL contains any trusted domain.
     */
    fun containsTrustedDomain(url: String, customDomains: List<String> = emptyList()): Boolean {
        val lowerUrl = url.lowercase()
        val allDomains = TRUSTED_VOUCHER_DOMAINS + customDomains
        return allDomains.any { domain -> lowerUrl.contains(domain.lowercase()) }
    }
}

