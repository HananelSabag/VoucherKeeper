package com.hananel.voucherkeeper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }
        
        // Extract SMS messages from intent
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        
        messages?.forEach { smsMessage ->
            val senderPhone = smsMessage.displayOriginatingAddress ?: return@forEach
            val bodyText = smsMessage.messageBody ?: return@forEach
            val timestamp = smsMessage.timestampMillis
            
            // Create SMSMessage object
            val message = SMSMessage(
                senderPhone = senderPhone,
                senderName = null, // Could fetch from contacts if needed
                bodyText = bodyText,
                timestamp = timestamp
            )
            
            // Process message asynchronously
            receiverScope.launch {
                try {
                    val decision = voucherRepository.processSmsMessage(message)
                    
                    // Send notification based on decision
                    when (decision) {
                        is VoucherDecision.Approved -> {
                            val merchantName = decision.extractedData.merchantName
                            notificationHelper.notifyVoucherApproved(merchantName)
                        }
                        is VoucherDecision.Pending -> {
                            notificationHelper.notifyPendingReview()
                        }
                        is VoucherDecision.Discard -> {
                            // Silent - no notification
                        }
                    }
                } catch (e: Exception) {
                    // Log error silently - don't crash
                    e.printStackTrace()
                }
            }
        }
    }
}

