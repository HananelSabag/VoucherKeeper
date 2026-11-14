# 📱 Voucher Keeper

**An intelligent Android app that automatically detects, classifies, and organizes real monetary vouchers from SMS messages.**

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-33-orange.svg)](https://developer.android.com/about/versions/13)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)](#license)

---

## 🎯 Problem Statement

In today's digital economy, Israeli tech workers and consumers receive dozens of SMS messages daily containing:
- 💳 Real monetary vouchers (Cibus, Pluxee, gift cards)
- 🎟️ Store credits and digital redemption codes
- 📢 Marketing spam and promotional "deals"
- 🔔 Discount coupons and sales notifications

**The Challenge:** Valuable vouchers get buried in marketing noise, expire unused, or are accidentally deleted.

**Voucher Keeper** solves this by using an intelligent rule-based engine that automatically identifies and preserves ONLY real monetary assets while filtering out all promotional content.

---

## ✨ Key Features

### 🤖 **Intelligent SMS Processing**
- **Real-time SMS monitoring** with zero battery impact
- **Advanced parser engine** with dual-language support (Hebrew/English)
- **Smart URL filtering** - distinguishes voucher links from T&C links
- **Automatic classification** into Approved, Pending, or Discard categories

### 🎨 **Modern Material 3 UI**
- **Jetpack Compose** with clean, intuitive design
- **Dynamic color theming** (light/dark modes)
- **Fully bilingual** (Hebrew RTL + English LTR)
- **Smooth animations** and gesture-based interactions

### ✏️ **Complete User Control**
- **Full voucher editing** - Fix any parser errors on approved vouchers
- **Pre-approval editing** - Correct mistakes before saving pending vouchers
- **Approved sender management** - Whitelist trusted contacts
- **Manual voucher entry** with smart paste & auto-extract

### 📊 **Smart Organization**
- **Approved vouchers list** with grouping by sender
- **Pending review queue** for manual verification
- **Voucher counter** showing total saved value
- **Additional voucher aggregation** per sender with total amounts

### 🔔 **Intelligent Notifications**
- Configurable alerts for new approved vouchers
- Pending review notifications
- Respects user preferences and quiet hours

---

## 🏗️ Architecture

**Clean Architecture** with MVVM pattern:

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  ├─ Screens (Approved, Pending, etc.)  │
│  ├─ Components (Cards, Dialogs)        │
│  └─ Theme (Material 3 Dynamic)         │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         ViewModel Layer                 │
│  ├─ ApprovedVouchersViewModel          │
│  ├─ PendingReviewViewModel             │
│  └─ SettingsViewModel                  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│        Domain Layer                     │
│  ├─ ParserEngine (Decision Tree)       │
│  ├─ PhoneNumberHelper                  │
│  └─ Business Logic                     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Data Layer                     │
│  ├─ Repository Pattern                 │
│  ├─ Room Database (Local)              │
│  ├─ DataStore (Preferences)            │
│  └─ SMS BroadcastReceiver              │
└─────────────────────────────────────────┘
```

---

## 🧠 The Parser Engine

### Decision Tree Logic

```
┌─────────────────┐
│  Incoming SMS   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│  Extract: URL, Code,        │
│  Amount, Sender, Keywords   │
└────────┬────────────────────┘
         │
         ▼
   ┌─────────────┐      NO
   │ Marketing?  ├──────────┐
   └──────┬──────┘          │
         │ YES              │
         ▼                  │
   ┌─────────┐              │
   │ DISCARD │              │
   └─────────┘              │
                            │
         ┌──────────────────┘
         │
         ▼
┌────────────────────────┐
│ Has Voucher Markers?   │
│ (URL/Code + Keywords)  │
└────────┬───────────────┘
         │
    ┌────┴─────┐
    │          │
   YES        NO
    │          │
    │          ▼
    │    ┌─────────┐
    │    │ DISCARD │
    │    └─────────┘
    │
    ▼
┌──────────────────┐
│ Approved Sender? │
└────┬──────┬──────┘
     │      │
    YES    NO
     │      │
     ▼      ▼
┌─────────┐ ┌─────────┐
│APPROVED │ │ PENDING │
└─────────┘ └─────────┘
```

### Intelligent URL Filtering

The parser distinguishes between:
- ✅ **Voucher URLs**: Direct redemption links
- ❌ **Terms URLs**: Regulations, T&C, privacy policies

**Supports both languages:**
```kotlin
termsKeywords = [
  "תקנון", "תנאים", "פרטיות",        // Hebrew
  "terms", "conditions", "privacy"    // English
]
```

**URL decoding** handles Hebrew characters in URLs properly.

---

## 🛠️ Technology Stack

| Category | Technologies |
|----------|-------------|
| **Language** | Kotlin 100% |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Hilt (Dagger) |
| **Database** | Room (SQLite) |
| **Async** | Kotlin Coroutines + Flow |
| **Preferences** | DataStore (Preferences) |
| **Localization** | Android Resources (strings.xml) |
| **Theme** | Material 3 Dynamic Colors |
| **Notifications** | NotificationCompat |
| **Build System** | Gradle (Kotlin DSL) |

---

## 📸 Features in Detail

### 🎯 **Smart Paste & Auto-Extract**
Paste an entire SMS message, and the parser automatically extracts:
- Merchant name
- Amount
- Voucher URL
- Redemption code
- Sender phone

### ✏️ **Full Editing Capabilities**

**Approved Vouchers:**
- Edit all fields: title, amount, URL, code, display name
- Fix parser mistakes anytime
- Scrollable dialog for long content

**Pending Vouchers:**
- Edit BEFORE approving
- Fix errors immediately
- Warning banner for user awareness

### 👥 **Approved Sender Management**
- Add trusted contacts (phone or system name)
- Edit existing senders
- Automatic phone number normalization
- Smart handling of international prefixes

### 🎨 **Consistent UI/UX**
- **Icon-based navigation** - each screen has its identity
- **Smooth transitions** - no "jumping" headers
- **Color-coded tabs**:
  - 🟢 Approved (Green/Secondary)
  - 🟠 Pending (Orange/Tertiary)
  - 🔵 Contacts (Blue/Primary)

### 📊 **Smart Aggregation**
Shows additional vouchers from the same sender:
- "3 more from this sender · Total: ₪400"
- "3 more from this sender · (amounts incomplete)"

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK** 17 or higher
- **Android SDK** 33+ (Android 13)
- **Gradle** 8.2+

### Installation

**Just want to try it?** Download the APK from the
[latest release](https://github.com/HananelSabag/VoucherKeeper/releases/latest)
and install it on an Android 13+ phone.

**Build from source:**

1. **Clone the repository**
   ```bash
   git clone https://github.com/HananelSabag/VoucherKeeper.git
   cd VoucherKeeper
   ```

2. **Open in Android Studio**
   - File → Open → Select project folder
   - Wait for Gradle sync

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   # or click "Run" in Android Studio
   ```

### Required Permissions

The app requests these permissions at runtime:
- `RECEIVE_SMS` - Monitor incoming SMS messages
- `READ_SMS` - Access message content
- `POST_NOTIFICATIONS` - Show voucher notifications (Android 13+)

**Privacy Note:** All processing is done locally. No data is sent to external servers.

---

## 🎓 Code Quality & Best Practices

### ✅ **Clean Architecture**
- Separation of concerns (UI/Domain/Data)
- Repository pattern for data access
- Dependency injection with Hilt

### ✅ **Modern Android Development**
- Jetpack Compose (no XML layouts)
- Kotlin Coroutines for async operations
- Flow for reactive data streams
- StateFlow for UI state management

### ✅ **Code Documentation**
- Comprehensive KDoc comments
- Inline explanations for complex logic
- Clear function naming conventions

### ✅ **Error Handling**
- Graceful failure recovery
- User-friendly error messages
- Validation at all input points

### ✅ **Localization**
- Full Hebrew (RTL) and English (LTR) support
- All strings in `strings.xml`
- Dynamic language switching

### ✅ **Testing Ready**
- Testable architecture (MVVM)
- Repository abstraction for mocking
- Clear separation for unit testing

---

## 🔮 Future Enhancements

- [ ] Export vouchers as PDF
- [ ] Cloud backup (Google Drive integration)
- [ ] Biometric app lock
- [ ] Voucher expiration reminders
- [ ] Merchant logo recognition
- [ ] OCR for voucher images
- [ ] Widget for home screen

---

## 📝 Development Story

This project was born from a real-world problem experienced by tech workers in Israel who receive dozens of Cibus/Pluxee vouchers and gift cards but struggle to keep track of them.

**Development Timeline:**
- **Day 1:** Core architecture, SMS receiver, parser engine, basic UI
- **Day 2:** Advanced features, full editing, UI polish, comprehensive testing

**Built with collaboration between:**
- Product vision & requirements definition
- Real-time iterative development
- Continuous user feedback integration
- Professional code review standards

---

## 🤝 Contributing

This is a personal project developed for portfolio demonstration. 

**If you'd like to:**
- Report bugs → Open an issue
- Suggest features → Start a discussion
- Fork for learning → Attribution required

---

## 📄 License

**Copyright © 2024 Hananel Sabag. All Rights Reserved.**

This software and associated documentation files (the "Software") are proprietary and confidential.

**Restrictions:**
- ❌ No unauthorized copying, distribution, or modification
- ❌ No commercial use without explicit permission
- ❌ No redistribution in source or binary forms

**Permitted:**
- ✅ Viewing source code for educational purposes
- ✅ Referencing in portfolio or resume
- ✅ Citing in academic or professional contexts

For licensing inquiries or permission requests, please contact the author.

---

## 👨‍💻 Author

**Hananel Sabag**

🔗 [GitHub](https://github.com/HananelSabag) | 💼 [LinkedIn](#) | 📧 [Email](#)

*Passionate Android developer with expertise in modern Kotlin development, clean architecture, and intuitive UI/UX design. Experienced in building production-ready applications with focus on code quality, performance, and user experience.*

---

## 🙏 Acknowledgments

- Material Design 3 guidelines by Google
- Android Jetpack libraries
- Kotlin language features
- The Android developer community

---

<div align="center">

**Built with ❤️ using Kotlin & Jetpack Compose**

⭐ Star this repository if you find it helpful!

</div>
