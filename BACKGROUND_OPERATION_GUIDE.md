# 📱 Background Operation Guide - Voucher Keeper

## ✅ Configuration Complete

Your app is now **fully configured** to receive SMS messages even when:
- ✅ App is **closed**
- ✅ Device is **sleeping**
- ✅ Device is **rebooted**
- ✅ App is in **background**

---

## 🔐 Permissions Added

### AndroidManifest.xml
```xml
<!-- Background Operation Permissions -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

### What Each Permission Does:

1. **WAKE_LOCK** 🔋
   - Wakes device when SMS arrives
   - Ensures processing completes even if device is sleeping
   - Automatically released after 30 seconds

2. **RECEIVE_BOOT_COMPLETED** 🔄
   - Re-enables SMS receiver after device restart
   - Critical for 24/7 operation

3. **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS** ⚡
   - Allows app to bypass Doze mode
   - Prevents Android from killing the SMS receiver

---

## 🛠️ Components Added

### 1. BootReceiver
**Location:** `app/src/main/java/com/hananel/voucherkeeper/receiver/BootReceiver.kt`

**Purpose:** Ensures SMS receiver stays active after device reboot

**Listens for:**
- `ACTION_BOOT_COMPLETED` - Normal boot
- `ACTION_LOCKED_BOOT_COMPLETED` - Boot with encryption
- `QUICKBOOT_POWERON` - Fast boot (some manufacturers)

### 2. Enhanced SmsReceiver
**Added:**
- Wake lock acquisition (30 seconds)
- Background state logging
- Automatic wake lock release after processing

### 3. BackgroundHelper
**Location:** `app/src/main/java/com/hananel/voucherkeeper/util/BackgroundHelper.kt`

**Features:**
- Check if battery optimization is disabled
- Request battery optimization exemption
- Helper functions for background management

---

## 📋 SMS Receiver Configuration

```xml
<receiver
    android:name=".receiver.SmsReceiver"
    android:exported="true"
    android:enabled="true"
    android:directBootAware="true"
    android:permission="android.permission.BROADCAST_SMS">
    <intent-filter android:priority="999">
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

### Key Attributes:

- **exported="true"** - Allows system to send SMS broadcasts to app
- **enabled="true"** - Receiver is always active
- **directBootAware="true"** - Works even before device is unlocked
- **priority="999"** - High priority (processes SMS early)

---

## 🧪 Testing Background Operation

### Test 1: App Closed in Background
```bash
# 1. Open app and add approved sender
# 2. Close app completely (swipe from recent apps)
# 3. Send test SMS from that sender
# 4. Check logcat:
adb logcat | grep VoucherKeeper

# Expected logs:
VoucherKeeper_SMS: === SMS RECEIVER TRIGGERED ===
VoucherKeeper_SMS: App state: Background receiver active
VoucherKeeper_SMS: Wake lock acquired for SMS processing
VoucherKeeper_Repo: === VOUCHER REPOSITORY - Processing SMS ===
VoucherKeeper_Parser: → APPROVED: All criteria met!
VoucherKeeper_SMS: Wake lock released
```

### Test 2: Device Sleeping
```bash
# 1. Lock device (screen off)
# 2. Wait 5 minutes (device enters deep sleep)
# 3. Send test SMS
# 4. Device should wake briefly, process SMS, and sleep again
# 5. Check logs after unlocking device
```

### Test 3: After Reboot
```bash
# 1. Reboot device
adb reboot

# 2. Check boot receiver logs:
adb logcat | grep VoucherKeeper_Boot

# Expected:
VoucherKeeper_Boot: === BOOT RECEIVER TRIGGERED ===
VoucherKeeper_Boot: Device booted - SMS receiver is ready

# 3. Send test SMS (app should receive it without opening)
```

---

## ⚙️ User Settings to Configure

### Critical: Disable Battery Optimization

**Why?** Android's battery optimization can kill background receivers in Doze mode.

**How to Check:**
```kotlin
BackgroundHelper.isBatteryOptimizationDisabled(context)
```

**How to Request Exemption:**
```kotlin
BackgroundHelper.requestBatteryOptimizationExemption(context)
```

**Manual Steps for User:**
1. Settings → Apps → Voucher Keeper
2. Battery → Unrestricted
3. Or: Settings → Battery → Battery optimization → All apps → Voucher Keeper → Don't optimize

---

## 📊 Background Operation Flow

```
┌─────────────────────────────────────┐
│   SMS Arrives (Device may be        │
│   closed/sleeping)                   │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   System wakes app's BroadcastReceiver│
│   (Android handles this automatically)│
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   SmsReceiver.onReceive() triggered  │
│   - Acquires WAKE_LOCK (30s)         │
│   - Logs: "Background receiver active"│
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Repository processes SMS           │
│   - Check approved sender            │
│   - Run parser engine                │
│   - Save to database                 │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Send notification (if approved/pending)│
│   - User sees notification           │
│   - Can open app to view             │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Release WAKE_LOCK                  │
│   - Device can sleep again           │
└─────────────────────────────────────┘
```

---

## 🔍 Debugging Background Issues

### If SMS not received when app is closed:

1. **Check Permissions**
   ```bash
   adb shell dumpsys package com.hananel.voucherkeeper | findstr "permission"
   ```
   Verify all permissions are granted.

2. **Check Battery Optimization**
   ```bash
   adb shell dumpsys deviceidle whitelist | findstr voucherkeeper
   ```
   Should show the app is whitelisted.

3. **Check Receiver is Registered**
   ```bash
   adb shell dumpsys package com.hananel.voucherkeeper | findstr "SmsReceiver"
   ```
   Should show enabled=true.

4. **Check if App is Force-Stopped**
   ```bash
   adb shell dumpsys package com.hananel.voucherkeeper | findstr "stopped"
   ```
   Should show: `stopped=false`
   
   If `stopped=true`, the app was force-stopped and receivers are disabled.
   **Solution:** Open app once to re-enable.

5. **Force Enable Battery Whitelist (Testing Only)**
   ```bash
   adb shell dumpsys deviceidle whitelist +com.hananel.voucherkeeper
   ```

---

## 🚫 What Can Prevent Background Operation

### 1. Force Stop
- **What:** User manually force-stops app in settings
- **Effect:** All receivers disabled until app is opened again
- **Solution:** Don't force-stop; just close normally

### 2. Battery Optimization Enabled
- **What:** Android's Doze mode restricts background activity
- **Effect:** Receiver may not wake during deep sleep
- **Solution:** Disable battery optimization for the app

### 3. App Data Cleared
- **What:** User clears app data/cache
- **Effect:** All settings and receivers reset
- **Solution:** Reopen app to reinitialize

### 4. Third-Party Battery Savers
- **What:** Manufacturer-specific battery management (Samsung, Xiaomi, etc.)
- **Effect:** May kill receivers aggressively
- **Solution:** Add app to "Protected apps" or "Auto-start" whitelist

---

## 📱 Manufacturer-Specific Issues

### Samsung
- Settings → Battery → Background usage limits
- Add Voucher Keeper to "Never sleeping apps"

### Xiaomi/MIUI
- Settings → Battery & performance → Manage apps battery usage
- Set to "No restrictions"
- Settings → Permissions → Autostart → Enable for Voucher Keeper

### Huawei/EMUI
- Settings → Battery → App launch
- Set to "Manage manually"
- Enable: Auto-launch, Secondary launch, Run in background

### OnePlus
- Settings → Battery → Battery optimization
- Select "Don't optimize"

---

## ✅ Verification Checklist

Before testing, verify:

- [ ] SMS permissions granted (RECEIVE_SMS, READ_SMS)
- [ ] Notification permission granted (POST_NOTIFICATIONS)
- [ ] Battery optimization disabled
- [ ] App not force-stopped
- [ ] Approved sender added to list
- [ ] Logcat monitoring active

---

## 🎯 Expected Behavior

### ✅ Should Work:
- App closed (swiped from recent apps)
- Device screen off (sleeping)
- Device in Doze mode (long sleep)
- After device reboot
- During phone call
- In airplane mode (if SMS over WiFi enabled)

### ❌ Won't Work:
- App force-stopped manually
- Permissions revoked
- Battery optimization enabled + deep Doze
- App uninstalled 😄

---

## 📈 Success Indicators

You'll know background operation is working when:

1. **Logs show**: "App state: Background receiver active"
2. **Wake lock acquired** even with screen off
3. **Notifications appear** even when app is closed
4. **Database updates** visible when you open app
5. **Works after reboot** without opening app

---

## 🎉 Summary

Your app now has **enterprise-grade background operation**:

✅ Wake locks for reliable processing  
✅ Boot receiver for 24/7 availability  
✅ Battery optimization exemption  
✅ Direct boot awareness  
✅ High-priority SMS reception  
✅ Automatic wake lock management  

**The app will receive and process SMS even when completely closed!** 🚀

Test it by closing the app completely and sending a test SMS. Check logcat to see the magic happen! ✨


