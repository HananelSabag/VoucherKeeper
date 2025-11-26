# Voucher Keeper 🎫

**An intelligent Android app that automatically detects, classifies, and stores real monetary vouchers from SMS messages.**

---

## 📱 Overview

Voucher Keeper is designed for tech professionals who receive digital vouchers (Cibus, Pluxee, gift cards) and want a clean, organized way to track them without the noise of promotional messages.

### Key Features

- **Automatic Detection**: Monitors incoming SMS and identifies real vouchers
- **Smart Classification**: Uses rule-based decision tree to separate vouchers from marketing content
- **Manual Review**: Unknown senders go to a pending queue for user approval
- **Bilingual Support**: Full Hebrew and English localization with RTL support
- **Modern UI**: Material 3 design with Light/Dark themes
- **Battery Optimized**: Uses only SMS broadcast receiver, no background services

---

## 🧠 Classification Engine

The app uses a strict decision tree with three possible outcomes:

1. **APPROVED** - Real voucher from trusted sender
2. **PENDING** - Looks like a voucher but unknown sender (manual review)
3. **DISCARD** - Promotional/marketing content

### Decision Logic

```
FLAGS:
- isApprovedSender (sender in approved list)
- hasStrongVoucherWord (contains voucher terms)
- hasAccessPoint (has URL with trusted domain OR redemption code)
- hasCouponPromoWord (contains promo/marketing terms)

RULES:
1. Pre-filter: IF hasCouponPromoWord AND NOT hasStrongVoucherWord → DISCARD
2. Approved: IF isApprovedSender AND hasStrongVoucherWord AND hasAccessPoint → APPROVED
3. Pending: IF NOT isApprovedSender AND hasStrongVoucherWord AND hasAccessPoint → PENDING
4. Default: → DISCARD
```

---

## 🏗️ Architecture

**Pattern**: MVVM (Model-View-ViewModel)  
**UI**: Jetpack Compose  
**DI**: Hilt (Dagger)  
**Database**: Room  
**Async**: Kotlin Coroutines + Flow

### Project Structure

```
com.hananel.voucherkeeper/
├── data/
│   ├── local/
│   │   ├── entity/           # Room entities
│   │   ├── dao/              # Data Access Objects
│   │   └── VoucherDatabase   # Room database
│   ├── repository/           # Repository layer
│   └── preferences/          # DataStore preferences
├── domain/
│   └── parser/               # Parser Engine (business logic)
│       ├── WordBanks         # Term definitions
│       ├── Models            # Data classes
│       └── ParserEngine      # Classification logic
├── ui/
│   ├── screen/               # Compose screens
│   ├── components/           # Reusable UI components
│   ├── viewmodel/            # ViewModels
│   ├── navigation/           # Navigation setup
│   └── theme/                # Material 3 theme
├── receiver/                 # SMS Broadcast Receiver
├── util/                     # Utilities (permissions, notifications)
└── di/                       # Hilt dependency injection
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| Min SDK | 33 (Android 13) |
| UI Framework | Jetpack Compose |
| Architecture | MVVM |
| DI | Hilt |
| Database | Room |
| Async | Coroutines + Flow |
| Preferences | DataStore |
| Theme | Material 3 |

---

## 📦 Setup & Build

### Prerequisites
- Android Studio Hedgehog or newer
- Kotlin 1.9+
- Min SDK 33 (Android 13)

### Build Instructions

1. Clone the repository
```bash
git clone https://github.com/yourusername/voucher-keeper.git
cd voucher-keeper
```

2. Open in Android Studio
3. Sync Gradle
4. Run on device or emulator (Android 13+)

---

## 🎨 Screens

1. **Approved Vouchers** - Main screen with all confirmed vouchers
2. **Pending Review** - Messages awaiting manual approval/rejection
3. **Settings** - Theme, language, notifications, approved senders

---

## 🔐 Permissions

- **RECEIVE_SMS** - Listen to incoming SMS messages
- **READ_SMS** - Read message content for parsing
- **POST_NOTIFICATIONS** - Notify user of new vouchers (Android 13+)
- **READ_CONTACTS** (optional) - Display contact names instead of phone numbers

All permissions are requested through modern permission flows with rationale dialogs.

---

## 🌍 Localization

Fully localized in:
- **English** (default)
- **Hebrew** (עברית) with full RTL support

All strings are externalized - **zero hardcoded text** in code.

---

## 🎯 Word Banks

### Strong Voucher Terms
Hebrew: שובר, תו קנייה, כרטיס מתנה, קוד למימוש, etc.  
English: voucher, gift card, store credit, redeem code, etc.

### Promo/Coupon Terms (Filtered Out)
Hebrew: קופון, הנחה, מבצע, סייל, דיל, etc.  
English: coupon, discount, sale, deal, promo code, etc.

### Trusted Domains
- `pluxee.co.il` (Cibus, MultiPass)
- `edenred.co.il`
- `shufersal.co.il`

---

## 👤 Author

**Hananel Sabag**  
Portfolio project showcasing modern Android development practices.

---

## 📄 License

This project is open-source. See LICENSE file for details.

---

## 🚀 Future Enhancements

- Export vouchers as PDF
- Expiration date extraction and alerts
- Google Drive backup
- ML-based classification (deep learning)
- Merchant logo detection
- Biometric app lock

---

## 🤝 Contributing

Contributions are welcome! Please open an issue or submit a pull request.

---

## 📞 Support

For issues or questions, please open a GitHub issue.

---

**Built with ❤️ using Kotlin & Jetpack Compose**

