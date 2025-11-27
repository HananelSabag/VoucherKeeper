# Voucher Keeper - Workflow State

## Current Phase
**🎨 Phase 5: UI Design Refinement - IN PROGRESS**

---

## Plan

### Phase 1: Project Structure & Core Setup
1. **Dependencies & Build Configuration**
   - Configure build.gradle (app-level & project-level)
   - Add Jetpack Compose, Room, Material 3, Hilt/DI
   - Set up Kotlin DSL and version catalogs
   - Configure AndroidManifest permissions

2. **Database Schema (Room)**
   - Entity: `VoucherEntity` (id, status, merchant, amount, url, code, raw, timestamp, sender)
   - Entity: `ApprovedSenderEntity` (phone, name)
   - Entity: `TrustedDomainEntity` (domain)
   - Entity: `AppSettingsEntity` (language, theme, notifications)
   - DAOs for all entities
   - Database singleton with migrations

3. **Localization Foundation**
   - strings.xml (default - English)
   - strings.xml (he - Hebrew, RTL)
   - All UI strings, no hardcoded text
   - Dimension resources for spacing

4. **Theme System**
   - Material 3 dynamic color scheme
   - Light/Dark theme support
   - Color.kt, Type.kt, Theme.kt
   - Proper contrast ratios for accessibility

5. **Core Parser Engine**
   - WordBanks.kt (strongVoucherTerms, couponPromoTerms, trustedDomains)
   - SMSMessage data class
   - VoucherDecision sealed class (Approved, Pending, Discard)
   - ExtractedData data class
   - ParserEngine.kt with decision tree logic
   - Unit tests for parser

---

### Phase 2: Business Logic Layer
6. **Repository Pattern**
   - VoucherRepository (CRUD operations)
   - SettingsRepository (app settings)
   - SenderRepository (approved senders management)

7. **ViewModels**
   - ApprovedVouchersViewModel
   - PendingReviewViewModel
   - SettingsViewModel
   - Shared state management with StateFlow

8. **SMS Broadcast Receiver**
   - SmsReceiver.kt (listens to incoming SMS)
   - Integration with ParserEngine
   - Permission handling logic
   - Insert to Room DB based on decision

---

### Phase 3: UI Implementation
9. **Navigation Setup**
   - Bottom Navigation scaffold
   - Navigation component routes
   - Settings screen access

10. **Approved Vouchers Screen**
    - LazyColumn with voucher cards
    - Card design: merchant, amount, code/url, timestamp
    - Empty state (illustration + text)
    - Pull-to-refresh (optional)

11. **Pending Review Screen**
    - Similar card layout
    - Approve/Reject buttons per card
    - Badge count on bottom nav

12. **Settings Screen**
    - Language selector (Auto/Hebrew/English)
    - Theme selector (System/Light/Dark)
    - Notifications toggles
    - Approved Senders management (add/remove)
    - Trusted Domains management (advanced)
    - About section (Hananel Sabag credit)

13. **Onboarding Flow**
    - Welcome screen with feature highlights
    - "Don't show again" checkbox
    - Persistent preference
    - Help button (?) in toolbar to reopen

---

### Phase 4: Polish & Production Ready
14. **Notifications**
    - NotificationManager wrapper
    - Channels for approved/pending
    - Localized notification text

15. **Permission Flows**
    - Runtime permission requests (SMS, Notifications)
    - Permission rationale dialogs
    - Graceful degradation

16. **Splash Screen**
    - Android 12+ Splash Screen API
    - Logo + Hananel Sabag attribution

17. **Accessibility**
    - Content descriptions for all interactive elements
    - Screen reader support (TalkBack tested)
    - Sufficient touch target sizes (48dp min)
    - Proper focus order

18. **Testing & QA**
    - Unit tests for parser logic
    - Integration tests for repository
    - UI tests for critical flows
    - Test on RTL layout (Hebrew)
    - Test light/dark themes

---

## Log

### 2025-11-26
- **Created workflow_state.md** - Detailed 18-step plan covering foundation, business logic, UI, and polish.
- **Phase 1 Complete**: Dependencies, Room database, localization (EN/HE), theme system
- **Phase 2 Complete**: Repository layer, ViewModels, SMS Receiver with Hilt DI
- **Phase 3 Complete**: Navigation, all screens (Approved/Pending/Settings), voucher cards
- **Phase 4 Complete**: Notifications, permissions, onboarding flow
- **Core Engine Complete**: Parser Engine with exact decision tree implementation
- **Project Skeleton Ready**: Fully functional foundation ready for testing and refinement

---

---

### Phase 5: UI Design Refinement (November 27, 2025)
19. **Color Scheme Overhaul**
    - Fix light mode background color (remove pinkish tint)
    - Update bottom navigation to use theme-aware colors
    - Replace hardcoded colors in VoucherCard with semantic theme colors
    - Enhance color palette for better light/dark mode adaptation

20. **Component Polish**
    - Add rounded corners to Settings screen dropdowns
    - Ensure consistent border radius across all cards
    - Update empty states with visual icons
    - Complete typography system definition

21. **Theme Enhancement**
    - Define complete Material 3 color scheme with all tokens
    - Improve contrast ratios for accessibility
    - Ensure visual consistency across light/dark themes

---

## Notes
- Zero hardcoded strings - everything via strings.xml
- Material 3 with dynamic theming (light/dark)
- Accessibility baked in from start
- Target audience: Israeli tech workers receiving Cibus/Tenbis vouchers
- Portfolio-ready code quality
- **UI Design Analysis:** See UI_DESIGN_ANALYSIS.md for detailed recommendations

