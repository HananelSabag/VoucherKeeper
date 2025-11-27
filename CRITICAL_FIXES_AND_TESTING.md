# Critical Fixes and Testing Guide
**Date:** November 27, 2025  
**Status:** READY FOR TESTING

---

## 🔴 Critical Bug Found and Fixed

### The Problem
The app was **NOT saving approved vouchers** to the database. In `VoucherRepository.kt`, the `Approved` case in the decision tree had an empty block:

```kotlin
when (decision) {
    is VoucherDecision.Approved -> {
        // EMPTY! Not saving!
    }
    is VoucherDecision.Pending -> {
        insertVoucherFromDecision(...)
    }
}
```

### The Fix
Added the missing save call:
```kotlin
is VoucherDecision.Approved -> {
    insertVoucherFromDecision(smsMessage, decision.extractedData, "approved")
}
```

---

## 🔍 Comprehensive Debug Logging Added

Added extensive logging to track the entire SMS processing flow:

### Log Tags to Monitor
- **`VoucherKeeper_SMS`** - SMS Receiver (when messages arrive)
- **`VoucherKeeper_Repo`** - Repository (decision making)
- **`VoucherKeeper_Parser`** - Parser Engine (classification logic)

### How to View Logs
```bash
# Filter for all VoucherKeeper logs
adb logcat | grep "VoucherKeeper"

# Or use Android Studio Logcat with filter: "VoucherKeeper"
```

### What You'll See
When an SMS arrives, you'll see:
1. **SMS Receiver:** Message received, sender, body preview
2. **Repository:** Checking if sender is approved, custom domains
3. **Parser:** All decision flags (hasUrl, hasStrongVoucherWord, etc.)
4. **Parser:** Extracted data (merchant, amount, URL, code)
5. **Parser:** Final decision with reasoning
6. **Repository:** Database save confirmation
7. **SMS Receiver:** Notification sent

---

## 📋 How the Logic Works (Spec-Compliant)

### Decision Tree
```
1. PRE-FILTER: If hasCouponPromoWord AND NOT hasStrongVoucherWord → DISCARD
2. APPROVED: If isApprovedSender AND hasStrongVoucherWord AND hasAccessPoint → APPROVED
3. PENDING: If NOT isApprovedSender AND hasStrongVoucherWord AND hasAccessPoint → PENDING
4. Otherwise → DISCARD
```

### Key Terms
- **hasAccessPoint** = hasTrustedDomain OR hasRedeemCode
- **Strong Voucher Words** = "שובר", "תו קנייה", "כרטיס מתנה", "voucher", "gift card", etc.
- **Coupon/Promo Words** = "קופון", "הנחה", "מבצע", "coupon", "discount", "sale", etc.
- **Trusted Domains** = pluxee.co.il, cibus.pluxee.co.il, shufersal.co.il, etc.

---

## ✅ Testing Checklist

### Prerequisites
1. **Permissions Granted**
   - SMS (RECEIVE_SMS, READ_SMS)
   - Notifications (POST_NOTIFICATIONS)
   - Check in: Settings → Apps → Voucher Keeper → Permissions

2. **SMS Receiver Registered**
   - Already configured in `AndroidManifest.xml`
   - Priority: 999 (high priority)

### Test Scenarios

#### Test 1: Message from UNKNOWN sender (should go to PENDING)
```
From: Any phone number NOT in approved senders
Message: "קיבלת שובר בסך 100 ₪ למימוש בכתובת: https://pluxee.co.il/voucher?code=ABC123"

Expected:
- Log shows: "→ PENDING: Unknown sender but looks like voucher"
- Voucher appears in "Pending Review" screen
- Notification: "Voucher awaiting review"
```

#### Test 2: Message from APPROVED sender (should go to APPROVED)
```
Step 1: Add sender to Approved Senders
- Go to "Approved Senders" screen
- Add phone number: 0501234567
- Name: "Test Sender"

Step 2: Send SMS from that number
Message: "קיבלת שובר בסך 50 ₪ קוד: XYZ789 https://shufersal.co.il/gift"

Expected:
- Log shows: "→ APPROVED: All criteria met!"
- Voucher appears in "Approved Vouchers" screen immediately
- Notification: "New voucher added from Test Sender"
```

#### Test 3: Marketing message (should DISCARD)
```
From: Any number
Message: "מבצע! הנחה של 20% רק היום! קוד קופון: SALE20"

Expected:
- Log shows: "→ DISCARD: Marketing content"
- Nothing saved
- No notification
```

#### Test 4: Message without access point (should DISCARD)
```
From: Approved sender
Message: "תודה על רכישתך. קיבלת שובר"
(Has voucher word but NO URL or redemption code)

Expected:
- Log shows: "→ DISCARD: Did not meet criteria"
- Reason: "No access point (URL or code)"
- Nothing saved
```

---

## 🐛 Debugging Tips

### If Nothing Happens When SMS Arrives

1. **Check Permissions**
   ```bash
   adb shell dumpsys package com.hananel.voucherkeeper | grep -A3 "permissions:"
   ```

2. **Check if Receiver is Registered**
   ```bash
   adb shell dumpsys activity broadcasts | grep "SMS_RECEIVED"
   ```

3. **Verify App is in Foreground or Background**
   - SMS Receiver should work in both states
   - Check: Settings → Apps → Battery → Unrestricted

4. **Check Database State**
   ```bash
   # List approved senders
   adb shell run-as com.hananel.voucherkeeper sqlite3 /data/data/com.hananel.voucherkeeper/databases/voucher_keeper_db
   SELECT * FROM approved_senders;
   .exit
   ```

5. **Force Stop and Restart**
   ```bash
   adb shell am force-stop com.hananel.voucherkeeper
   # Then launch app manually
   ```

---

## 📱 Real-World Test Messages

### Valid Voucher Examples (Hebrew)
```
שלום! קיבלת שובר דיגיטלי בסך 100 ₪. 
לצפייה בשובר: https://myconsumers.pluxee.co.il/v/abc123
```

```
תודה על רכישתך! שובר מתנה בסך 250 ₪
קוד למימוש: GIFT250XYZ
```

### Valid Voucher Examples (English)
```
You have received a gift card worth $50
Redeem at: https://edenred.co.il/redeem?code=EN50ABC
```

### Invalid Marketing Examples
```
מבצע בלעדי! 1+1 על כל המוצרים עד סוף השבוע
קוד הנחה: SAVE30
```

---

## 🔐 Security & Privacy

- All SMS content is stored locally (Room database)
- No external API calls
- No data leaves the device
- User controls approved senders list
- Full transparency with logs

---

## 📊 Expected Behavior Summary

| Sender Type | Voucher Words | Access Point | Result |
|------------|---------------|--------------|---------|
| Approved | ✅ | ✅ | **APPROVED** |
| Unknown | ✅ | ✅ | **PENDING** |
| Approved | ✅ | ❌ | DISCARD |
| Any | ❌ (only promo) | Any | DISCARD |

---

## 🚀 Next Steps

1. **Build and Install**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Open Logcat**
   ```bash
   adb logcat | grep VoucherKeeper
   ```

3. **Add Test Approved Sender**
   - Open app → Approved Senders
   - Add your test phone number

4. **Send Test SMS**
   - From another phone
   - Use one of the example messages above

5. **Watch Logs**
   - You should see complete flow from SMS → Parser → Database → Notification

6. **Verify in App**
   - Check Approved Vouchers screen
   - Or Pending Review screen

---

## ✨ What Was Already Working

- ✅ UI and navigation
- ✅ Theme system (light/dark)
- ✅ Localization (Hebrew/English)
- ✅ Permissions handling in onboarding
- ✅ Database schema
- ✅ Word banks (strong terms, promo terms, domains)
- ✅ Pending voucher save logic
- ✅ Manual voucher creation

## 🔧 What Was Fixed

- ✅ **Approved voucher save logic** (was empty!)
- ✅ **Comprehensive debug logging** (all layers)
- ✅ **Error tracking** (try-catch with logs)

---

## 📞 Support

If issues persist after testing:

1. Share the **full logcat output** when SMS arrives
2. Confirm **permissions are granted** (show screenshot)
3. Verify **approved sender was added** (show screenshot)
4. Share **exact SMS message content** used for testing

The logs will reveal exactly where the flow breaks!


