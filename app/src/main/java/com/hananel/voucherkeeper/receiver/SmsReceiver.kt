package com.hananel.voucherkeeper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Telephony
import android.util.Log
import com.hananel.voucherkeeper.data.repository.VoucherRepository
import com.hananel.voucherkeeper.domain.parser.SMSMessage
import com.hananel.voucherkeeper.domain.parser.VoucherDecision
import com.hananel.voucherkeeper.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Broadcast Receiver for incoming SMS messages.
 * Listens to SMS_RECEIVED broadcasts and processes voucher messages.
 * 
 * Uses Hilt for dependency injection.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var voucherRepository: VoucherRepository
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    // Coroutine scope for async operations
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "VoucherKeeper_SMS"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "=== SMS RECEIVER TRIGGERED ===")
        Log.d(TAG, "Intent action: ${intent?.action}")
        Log.d(TAG, "App state: Background receiver active")
        
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(TAG, "Not an SMS_RECEIVED action, ignoring")
            return
        }
        
        // Extract SMS messages from intent
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        Log.d(TAG, "Extracted ${messages?.size ?: 0} SMS message parts")
        
        if (messages.isNullOrEmpty()) {
            Log.d(TAG, "No messages to process")
            return
        }
        
        // Group messages by sender and combine multipart messages
        // Use originatingAddress (actual phone number) as the grouping key
        val messagesBySender = messages.groupBy { it.originatingAddress ?: "" }
        
        messagesBySender.forEach { (actualPhone, parts) ->
            if (actualPhone.isEmpty()) return@forEach
            
            // Get display name (might be "Shufersal" or the number itself)
            val displayName = parts.firstOrNull()?.displayOriginatingAddress
            
            // If display name is different from actual phone, it's a saved contact name
            val senderName = if (displayName != null && displayName != actualPhone) {
                displayName
            } else {
                null
            }
            
            // Combine all parts into one message body
            val fullBody = parts.sortedBy { it.timestampMillis }
                .joinToString("") { it.messageBody ?: "" }
            
            val timestamp = parts.first().timestampMillis
            
            Log.d(TAG, "=== PROCESSING COMBINED SMS ===")
            Log.d(TAG, "Actual phone: $actualPhone")
            Log.d(TAG, "Display name: ${senderName ?: "(same as phone)"}")
            Log.d(TAG, "Parts combined: ${parts.size}")
            Log.d(TAG, "Full body length: ${fullBody.length} chars")
            Log.d(TAG, "Body preview: ${fullBody.take(150)}...")
            Log.d(TAG, "Timestamp: $timestamp")
            
            // Create SMSMessage object with full combined body
            val message = SMSMessage(
                senderPhone = actualPhone,      // Always the actual phone number
                senderName = senderName,         // Saved contact name (e.g., "Shufersal")
                bodyText = fullBody,
                timestamp = timestamp
            )
            
            // Acquire wake lock for THIS specific message processing
            val powerManager = context?.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "VoucherKeeper::SMS::$actualPhone::$timestamp"
            )
            wakeLock?.acquire(30000) // 30 seconds max
            Log.d(TAG, "Wake lock acquired")
            
            // Process message asynchronously
            receiverScope.launch {
                try {
                    Log.d(TAG, "Starting voucher repository processing...")
                    val decision = voucherRepository.processSmsMessage(message)
                    Log.d(TAG, "Decision: ${decision.javaClass.simpleName}")
                    
                    // Send notification based on decision
                    when (decision) {
                        is VoucherDecision.Approved -> {
                            val merchantName = decision.extractedData.merchantName
                            Log.d(TAG, "✓ APPROVED - Merchant: $merchantName")
                            notificationHelper.notifyVoucherApproved(
                                merchantName = merchantName,
                                senderName = message.senderName,
                                senderPhone = message.senderPhone
                            )
                        }
                        is VoucherDecision.Pending -> {
                            Log.d(TAG, "⚠ PENDING REVIEW")
                            notificationHelper.notifyPendingReview()
                        }
                        is VoucherDecision.Discard -> {
                            Log.d(TAG, "✗ DISCARDED - Not a voucher")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "ERROR processing SMS: ${e.message}", e)
                    e.printStackTrace()
                } finally {
                    // Release wake lock after processing
                    try {
                        wakeLock?.let {
                            if (it.isHeld) {
                                it.release()
                                Log.d(TAG, "Wake lock released")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error releasing wake lock: ${e.message}")
                    }
                }
            }
        }
    }
}

