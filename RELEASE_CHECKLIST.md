# Voucher Keeper v1.0.0 - Release Checklist ✅

## Build Information
- **Version Code:** 1
- **Version Name:** 1.0.0
- **Target SDK:** 35 (Android 15)
- **Min SDK:** 33 (Android 13)
- **Package Name:** com.hananel.voucherkeeper

## Release Files
Location: `release/` folder

### 1. AAB (Android App Bundle)
- ✅ File: `Voucher-Keeper-v1.0.0.aab`
- ✅ Size: 5.99 MB
- ✅ Signed: Yes (release keystore)
- ✅ ProGuard: Enabled (minifyEnabled=true, shrinkResources=true)

### 2. Keystore (KEEP SECURE!)
- ✅ File: `release.keystore`
- ✅ Alias: `voucherkeeper`
- ✅ Password: `VoucherKeeper2024!`
- ⚠️ **IMPORTANT:** Store this keystore securely! You'll need it for all future updates.

## Google Play Store Requirements

### ✅ App Configuration
- [x] App name: "Voucher Keeper"
- [x] Package name: com.hananel.voucherkeeper
- [x] Version ready: 1.0.0
- [x] Target SDK 34+ (we have 35) ✓
- [x] Icons present in all densities
- [x] Signed with release key

### ✅ Permissions Justification
All permissions are required and justified:
- `RECEIVE_SMS` + `READ_SMS` - Core functionality (voucher detection)
- `POST_NOTIFICATIONS` - User notifications
- `WAKE_LOCK` - Background SMS processing
- `RECEIVE_BOOT_COMPLETED` - Auto-start after reboot
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - Background reliability

### ✅ Privacy & Security
- [x] Privacy Policy: `privacy_policy.html` created
- [x] Data Safety: All data processed locally, no external transmission
- [x] No analytics/tracking
- [x] No ads
- [x] Screen orientation: Locked to portrait

### ✅ Code Quality
- [x] ProGuard rules configured
- [x] Logs removed in release builds
- [x] No debug code
- [x] No TODOs remaining in production code

## Before Upload to Google Play

### 1. Update Privacy Policy
- [ ] Replace `hananel.sabag@example.com` with your actual email in `privacy_policy.html`
- [ ] Upload privacy policy to a public URL (required by Google Play)
- [ ] Or use the HTML content directly in Play Console

### 2. Create Store Listing Assets
Prepare these assets for the Play Console:

#### App Details
- **Short description** (80 chars max):
  ```
  Smart SMS organizer for Cibus, Pluxee & digital vouchers. Auto-detect & save!
  ```

- **Full description** (4000 chars max):
  ```
  🎟️ Never Lose a Voucher Again!
  
  Voucher Keeper is your intelligent assistant for managing digital vouchers from Cibus, Pluxee, Tenbis, Shufersal, and other popular services in Israel.
  
  ✨ KEY FEATURES:
  • 🤖 Auto-Detection: Analyzes incoming SMS and automatically saves real vouchers
  • 🔍 Smart Filtering: Filters out spam and saves only legitimate vouchers
  • 📱 Pending Review: Review uncertain messages before approving
  • 👥 Approved Senders: Whitelist trusted senders for instant auto-approval
  • 🔒 Strict Mode: Optional - only save vouchers from approved senders
  • 📊 Organization: View all vouchers grouped by sender with total amounts
  • 📤 Export: Generate beautiful PDF reports of your vouchers
  • 🌐 Bilingual: Full Hebrew (RTL) & English support
  
  🔐 YOUR PRIVACY IS SACRED:
  • ALL data stays on your device
  • NO data sent to external servers
  • NO analytics or tracking
  • NO ads
  
  Perfect for tech workers, freelancers, and anyone receiving digital vouchers via SMS!
  
  Created with ❤️ by Hananel Sabag
  ```

#### Graphics Required
- [ ] App icon (512x512 PNG)
- [ ] Feature graphic (1024x500 PNG/JPG)
- [ ] Phone screenshots (2-8 images, min 320px)
- [ ] 7-inch tablet screenshots (optional)
- [ ] 10-inch tablet screenshots (optional)

#### Content Rating
Answer Google's questionnaire:
- App contains no violence, mature content, or gambling
- App reads SMS but only for voucher management
- Target audience: Everyone

#### Pricing & Distribution
- [ ] Free app
- [ ] Primary country: Israel
- [ ] Secondary countries: (your choice)

### 3. Data Safety Form
Complete Google's Data Safety section:

**Data collected:**
- SMS messages (not shared, processed locally only)
- Phone numbers (sender identification, not shared)

**Data usage:**
- App functionality only
- No advertising
- No analytics

**Data security:**
- Data encrypted in transit: N/A (no transmission)
- Data encrypted at rest: Yes (Android default)
- Users can request data deletion: Yes (uninstall app)

## Upload Instructions

1. **Open Google Play Console**: https://play.google.com/console
2. **Create New App**:
   - App name: Voucher Keeper
   - Default language: Hebrew / English
   - App or game: App
   - Free or paid: Free
3. **Upload AAB**:
   - Go to: Release > Production > Create new release
   - Upload: `release/Voucher-Keeper-v1.0.0.aab`
   - Release name: "1.0.0"
   - Release notes (Hebrew):
     ```
     🎉 גרסה ראשונה של Voucher Keeper!
     
     ✨ תכונות:
     • זיהוי אוטומטי של שוברים מסמסים
     • ניהול שולחים מאושרים
     • סינון ספאם חכם
     • ייצוא PDF יפה ומעוצב
     • תמיכה מלאה בעברית ואנגלית
     ```
   - Release notes (English):
     ```
     🎉 First release of Voucher Keeper!
     
     ✨ Features:
     • Auto-detect vouchers from SMS
     • Manage approved senders
     • Smart spam filtering
     • Beautiful PDF export
     • Full Hebrew & English support
     ```

4. **Complete All Sections**:
   - Store listing (descriptions, graphics)
   - Content rating questionnaire
   - Target audience (Everyone)
   - News app declaration (No)
   - COVID-19 contact tracing (No)
   - Data safety form
   - Advertising ID (No, we don't use ads)
   - App content (complete all declarations)

5. **Submit for Review**

## Post-Upload

### Testing (Internal Track - Recommended)
Before going public, create an internal testing track:
1. Create internal test release
2. Add your email to testers list
3. Test the downloaded version thoroughly
4. Once confident, promote to production

### Future Updates
**IMPORTANT:** Always use the same keystore (`release.keystore`) for updates!

To release updates:
1. Update `versionCode` (increment by 1)
2. Update `versionName` (e.g., "1.0.1", "1.1.0")
3. Rebuild AAB: `gradlew bundleRelease`
4. Upload new AAB to Play Console

## Keystore Backup
⚠️ **CRITICAL:** Make multiple backups of `release/release.keystore`:
- Cloud storage (encrypted)
- External drive
- Password manager (if supports file storage)

**Losing this keystore means you can NEVER update the app!**

## Support Contacts
- Developer: Hananel Sabag
- Email: [UPDATE THIS in privacy_policy.html]
- GitHub: [Optional - add if open-sourcing]

---

## Launch Checklist Summary

- [x] AAB built and signed
- [x] Privacy policy created
- [ ] Privacy policy email updated
- [ ] Privacy policy hosted publicly
- [ ] Store listing text prepared
- [ ] Screenshots captured
- [ ] Graphics created (icon, feature graphic)
- [ ] Google Play Console account verified
- [ ] Content rating completed
- [ ] Data safety form completed
- [ ] AAB uploaded
- [ ] Internal testing completed (recommended)
- [ ] Production release submitted

---

**🎉 READY FOR LAUNCH! 🚀**

Good luck with your release! Remember to test thoroughly in internal track first.

