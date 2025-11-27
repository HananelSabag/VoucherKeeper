# 📱 הסבר מלא: איך SMS Sender Name עובד באנדרואיד

## 🔍 מה אנדרואיד מחזירה לנו?

כשמגיעה הודעת SMS, אנדרואיד מחזירה **שני דברים**:

### 1. `originatingAddress` - המספר האמיתי
זה **תמיד** המספר שממנו ה-SMS נשלחה (או ה-sender ID של המערכת).

**דוגמאות:**
- `+972501234567` (מספר רגיל)
- `50555` (short code של בנק)
- `Bank-Leumi` (alpha sender)

### 2. `displayOriginatingAddress` - מה שמוצג למשתמש
זה מה שאפליקציית ה-SMS של גוגל **מציגה** למשתמש.

**דוגמאות:**
- אם יש איש קשר שמור: `"אמא"` (במקום המספר)
- אם זה sender ID: `"Bank Leumi"` (במקום short code)
- אם אין שם שמור: המספר עצמו

---

## 🎯 איך הקוד שלנו עובד?

### בקובץ `SmsReceiver.kt` (שורות 65-76):

```kotlin
// Get actual phone number
val actualPhone = message.originatingAddress  // המספר האמיתי

// Get display name
val displayName = message.displayOriginatingAddress

// If display name is different, save it
val senderName = if (displayName != null && displayName != actualPhone) {
    displayName  // "Bank Leumi", "Shufersal", "אמא"
} else {
    null  // אין שם, רק מספר
}

val smsMessage = SMSMessage(
    senderPhone = actualPhone,  // תמיד המספר/sender ID האמיתי
    senderName = senderName     // השם המוצג (אם קיים)
)
```

---

## 🏦 תרחיש 1: בנק לאומי (Alpha Sender)

### מה אנדרואיד שולחת:
```
originatingAddress = "BankLeumi"
displayOriginatingAddress = "Bank Leumi"
```

### מה נשמר ב-SMSMessage:
```kotlin
senderPhone = "BankLeumi"
senderName = "Bank Leumi"  (כי זה שונה!)
```

### איך ההשוואה עובדת:

```kotlin
// In VoucherRepository.kt (lines 68-81)

// 1️⃣ בדיקה לפי מספר/sender ID:
val isApprovedByPhone = allApprovedSenders.firstOrNull { sender ->
    PhoneNumberHelper.areEqual(sender.phone, smsMessage.senderPhone)
    // האם יש מישהו עם phone = "BankLeumi"?
}

// 2️⃣ בדיקה לפי שם:
val isApprovedByName = if (smsMessage.senderName != null) {
    approvedSenderDao.isApprovedSenderByNameOrPhone(smsMessage.senderName)
    // האם יש מישהו עם phone = "Bank Leumi"? (exact match)
} else {
    false
}

// 3️⃣ מאושר אם אחד מהם עובד:
val isApprovedSender = isApprovedByPhone || isApprovedByName
```

---

## 📱 תרחיש 2: מספר רגיל עם איש קשר שמור

### מה אנדרואיד שולחת:
```
originatingAddress = "+972501234567"
displayOriginatingAddress = "אמא"  (שמור אצלך באנשי קשר)
```

### מה נשמר ב-SMSMessage:
```kotlin
senderPhone = "+972501234567"
senderName = "אמא"
```

### איך ההשוואה עובדת:
```kotlin
// 1️⃣ בדיקת מספר:
isApprovedByPhone = areEqual("+972501234567", approvedSender.phone)
→ אם שמרת את המספר, זה יתאים! ✅

// 2️⃣ בדיקת שם:
isApprovedByName = approvedSenderDao.isApprovedSenderByNameOrPhone("אמא")
→ אם שמרת "אמא" כשם מערכת, זה יתאים! ✅
```

---

## 🏢 תרחיש 3: Cibus (Short Code + Display Name)

### מה אנדרואיד שולחת:
```
originatingAddress = "50555"  (short code)
displayOriginatingAddress = "Cibus"
```

### מה נשמר ב-SMSMessage:
```kotlin
senderPhone = "50555"
senderName = "Cibus"
```

### איך ההשוואה עובדת:
```kotlin
// 1️⃣ בדיקת מספר:
isApprovedByPhone = areEqual("50555", approvedSender.phone)
→ אם שמרת 50555, זה יתאים! ✅

// 2️⃣ בדיקת שם:
isApprovedByName = approvedSenderDao.isApprovedSenderByNameOrPhone("Cibus")
→ אם שמרת "Cibus" כשם מערכת, זה יתאים! ✅
```

---

## 🎯 סיכום: מה כדאי לשמור?

### אופציה 1: שמירה לפי שם המערכת (מומלץ!)
**דוגמה:** שמור `"Bank Leumi"` בשדה "System Name"

**יתרונות:**
- ✅ פשוט - רק לכתוב את השם
- ✅ אינטואיטיבי - "בנק לאומי"
- ✅ עובד גם אם יש מספרים שונים

**חסרונות:**
- ⚠️ חייב exact match (Bank Leumi ≠ BankLeumi)

### אופציה 2: שמירה לפי Sender ID/Short Code
**דוגמה:** שמור `"BankLeumi"` או `"50555"` בשדה "Phone Number"

**יתרונות:**
- ✅ יציב - ה-sender ID לא משתנה
- ✅ ייחודי - לכל חברה יש sender ID אחד

**חסרונות:**
- ⚠️ קשה לדעת - צריך לבדוק מה ה-sender ID האמיתי
- ⚠️ לא אינטואיטיבי - מה זה "BankLeumi"?

---

## 🧪 איך לבדוק מה מתקבל?

### שלב 1: קבל הודעה מהבנק
פשוט תחכה שהבנק ישלח לך SMS

### שלב 2: בדוק ב-LogCat
```bash
adb logcat | grep "VoucherKeeper"
```

**תראה משהו כזה:**
```
=== PROCESSING COMBINED SMS ===
Actual phone: BankLeumi           ← זה ה-sender ID האמיתי
Display name: Bank Leumi           ← זה מה שמוצג באפליקציה
```

### שלב 3: שמור את המתאים
- אם יש "Actual phone" שונה מ"Display name" → אפשר לשמור אחד מהם
- אם הם זהים → רק אחד קיים

---

## 🔍 הבדל בין איש קשר לשם מערכת

### 👤 איש קשר ששמרת במכשיר (אמא, אבא, חבר):
```
originatingAddress = "+972501234567"  (מספר אמיתי)
displayOriginatingAddress = "אמא"     (שמור אצלך)
```
**בקוד:**
```
senderPhone = "+972501234567"
senderName = "אמא"
```

### 🏢 שם מערכת (בנק, חברה):
```
originatingAddress = "BankLeumi"       (sender ID)
displayOriginatingAddress = "Bank Leumi"  (display name)
```
**בקוד:**
```
senderPhone = "BankLeumi"
senderName = "Bank Leumi"
```

---

## ⚡ למה זה חשוב?

### בלי sender name matching:
- ❌ צריך לדעת את ה-sender ID המדויק
- ❌ לא אינטואיטיבי
- ❌ קשה לשמור

### עם sender name matching:
- ✅ פשוט לשמור "Bank Leumi"
- ✅ עובד גם אם ה-sender ID משתנה
- ✅ אינטואיטיבי

---

## 📊 טבלת השוואה

| מקור SMS | originatingAddress | displayOriginatingAddress | senderPhone | senderName | איך לשמור? |
|----------|-------------------|---------------------------|-------------|------------|-----------|
| בנק לאומי | `BankLeumi` | `Bank Leumi` | `BankLeumi` | `Bank Leumi` | `"Bank Leumi"` (שם) |
| Cibus | `50555` | `Cibus` | `50555` | `Cibus` | `"Cibus"` (שם) |
| Shufersal | `Shufersal` | `Shufersal` | `Shufersal` | null | `"Shufersal"` (שם) |
| איש קשר | `+972501234567` | `אמא` | `+972501234567` | `אמא` | `+972501234567` (מספר) |
| מספר רגיל | `+972501234567` | `+972501234567` | `+972501234567` | null | `+972501234567` (מספר) |

---

## ✅ סיכום

1. **אנדרואיד תמיד מחזירה מספר אמיתי** (`originatingAddress`)
2. **אם יש שם מוצג שונה, נשמר ב-`senderName`**
3. **ההשוואה בודקת גם מספר וגם שם** (OR logic)
4. **לשמות מערכת (בנק, חברה) - עדיף לשמור לפי שם**
5. **למספרים רגילים - שמור לפי מספר**

**הקוד שלנו תומך בשניהם!** 🎯

