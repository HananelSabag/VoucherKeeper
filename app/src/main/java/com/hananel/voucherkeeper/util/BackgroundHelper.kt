package com.hananel.voucherkeeper.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Helper for managing background operation permissions.
 * Ensures the app can receive SMS even when in background or device is sleeping.
 * 
 * @author Hananel Sabag
 */
object BackgroundHelper {
    
    private const val TAG = "VoucherKeeper_Background"
    
    /**
     * Check if battery optimization is disabled for this app.
     * When disabled, the app can work freely in background.
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            Log.d(TAG, "Battery optimization disabled: $isIgnoringBatteryOptimizations")
            isIgnoringBatteryOptimizations
        } else {
            true // Not applicable for older versions
        }
    }
    
    /**
     * Open system settings to request battery optimization exemption.
     * This allows the app to work in background without restrictions.
     */
    fun requestBatteryOptimizationExemption(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d(TAG, "Opened battery optimization settings")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open battery optimization settings", e)
                // Fallback: open general battery optimization settings
                openBatteryOptimizationSettings(context)
            }
        }
    }
    
    /**
     * Open general battery optimization settings page.
     */
    fun openBatteryOptimizationSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d(TAG, "Opened general battery optimization settings")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open battery optimization settings", e)
            }
        }
    }
    
    /**
     * Get a user-friendly explanation of battery optimization.
     */
    fun getBatteryOptimizationExplanation(): String {
        return """
            To ensure Voucher Keeper can detect SMS messages even when the app is closed:
            
            1. Disable battery optimization for this app
            2. Keep the app installed (don't clear data)
            3. Don't force-stop the app manually
            
            This allows the SMS receiver to work 24/7 in the background.
        """.trimIndent()
    }
}







