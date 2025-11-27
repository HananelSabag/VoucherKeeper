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
     * EXPANDED based on ChatGPT analysis of real Israeli vouchers.
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
        "הטבה",
        "הטבת",
        "הטבתך",
        "קוד מימוש",
        "קוד אישי",
        "קוד נטען",
        "קוד הטבה",
        "קוד לרכישה",
        "קוד למימוש",
        "קוד קופון אישי",
        "ההטבה למימוש",
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
        "redeem gift card",
        "redeem code",
        "redeem",
        "benefit code",
        "personal code"
    )
    
    /**
     * HARD SPAM INDICATORS - these ALWAYS indicate spam/marketing.
     * If ANY of these appear, the message is DISCARDED immediately,
     * even if it contains strong voucher words.
     */
    val HARD_SPAM_INDICATORS = setOf(
        // Hebrew hard spam indicators
        "עד גמר המלאי",
        "בתוקף עד",
        "תקף עד",
        "להצטרפות לערוץ",
        "להסרה שלחו",
        "ללא כפל מבצעים",
        "המוקדם מבניהם",
        "כפוף לתקנון",
        "קופונים משתלמים",
        "ערוץ המבצעים",
        "מגוון קופונים",
        "שוברים שיאים",
        
        // English hard spam indicators
        "while supplies last",
        "limited quantity",
        "terms and conditions apply",
        "unsubscribe",
        "opt out"
    )
    
    /**
     * Promo/coupon/marketing terms indicating promotional content.
     * Messages with ONLY these terms (without strong voucher terms) are discarded.
     * EXPANDED based on ChatGPT analysis - blocks pizza/food/retail spam.
     */
    val COUPON_PROMO_TERMS = setOf(
        // Hebrew promo terms
        "קופון",
        "קוד קופון",
        "קוד הנחה",
        "מבצע",
        "מבצעים",
        "הנחה",
        "הנחות",
        "ב-50% הנחה",
        "30% הנחה",
        "40% הנחה",
        "1+1",
        "תפריט",
        "משפחתית",
        "מגוונים",
        "מוצר ב-",
        "משלוח",
        "איסוף",
        "מבצע השבוע",
        "הטבה לכולם",
        "פיצה",
        "סלטים",
        "שנייה ב-50",
        "הצעה מיוחדת",
        "סייל",
        "דיל",
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
        "promotion",
        "special deal",
        "% off",
        "limited time",
        "flash sale",
        "menu",
        "order now",
        "buy now",
        "only today",
        "free shipping"
    )
    
    /**
     * Trusted voucher domains.
     * URLs containing these domains are considered valid voucher access points.
     * EXPANDED based on ChatGPT analysis - covers all legitimate Israeli voucher sources.
     */
    val TRUSTED_VOUCHER_DOMAINS = setOf(
        "pluxee.co.il",
        "myconsumers.pluxee.co.il",
        "cibus.pluxee.co.il",
        "edenred.co.il",
        "shufersal.co.il",
        "shufersal.club",
        "ems.to",
        "vp4.me",
        "r.vp4.me",
        "fls.cx",
        "l5k.me",
        "yellow.co.il",
        "yellow.onelink.me"
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
     * More flexible matching - checks if the domain appears anywhere in the URL.
     */
    fun containsTrustedDomain(url: String, customDomains: List<String> = emptyList()): Boolean {
        val lowerUrl = url.lowercase()
        val allDomains = TRUSTED_VOUCHER_DOMAINS + customDomains
        
        // Try exact domain matching first
        val hasExactMatch = allDomains.any { domain -> 
            val lowerDomain = domain.lowercase()
            // Check if domain appears in URL (not case sensitive)
            lowerUrl.contains(lowerDomain)
        }
        
        if (hasExactMatch) {
            android.util.Log.d("VoucherKeeper_WordBanks", "Found trusted domain in URL: $url")
            return true
        }
        
        // Fallback: check for partial matches (e.g., "pluxee" in "myconsumers.pluxee.co.il")
        val hasPartialMatch = allDomains.any { domain ->
            val domainParts = domain.lowercase().split(".")
            domainParts.any { part -> 
                part.length > 4 && lowerUrl.contains(part)
            }
        }
        
        if (hasPartialMatch) {
            android.util.Log.d("VoucherKeeper_WordBanks", "Found partial trusted domain match in URL: $url")
        }
        
        return hasPartialMatch
    }
}

