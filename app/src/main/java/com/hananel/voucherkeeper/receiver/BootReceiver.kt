package com.hananel.voucherkeeper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Boot Receiver - Ensures the SMS receiver remains enabled after device restart.
 * This is critical for background SMS monitoring to work after reboot.
 * 
 * @author Hananel Sabag
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "VoucherKeeper_Boot"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "=== BOOT RECEIVER TRIGGERED ===")
        Log.d(TAG, "Action: ${intent?.action}")
        
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Device booted - SMS receiver is ready")
                // The SMS receiver is automatically enabled by the system
                // We just log that the app is ready to receive SMS
            }
        }
    }
}






