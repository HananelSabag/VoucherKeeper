package com.hananel.voucherkeeper.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hananel.voucherkeeper.MainActivity
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing app notifications.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_VOUCHERS = "vouchers_channel"
        const val CHANNEL_PENDING = "pending_channel"
        const val NOTIFICATION_ID_VOUCHER = 1001
        const val NOTIFICATION_ID_PENDING = 1002
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels (required for Android 8.0+).
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Vouchers channel
            val vouchersChannel = NotificationChannel(
                CHANNEL_VOUCHERS,
                context.getString(R.string.notification_channel_vouchers),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new approved vouchers"
            }
            
            // Pending review channel
            val pendingChannel = NotificationChannel(
                CHANNEL_PENDING,
                context.getString(R.string.notification_channel_pending),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for vouchers awaiting review"
            }
            
            notificationManager.createNotificationChannel(vouchersChannel)
            notificationManager.createNotificationChannel(pendingChannel)
        }
    }
    
    /**
     * Send notification for a new approved voucher.
     */
    fun notifyVoucherApproved(merchantName: String?, senderName: String? = null, senderPhone: String? = null) {
        // Check permission
        if (!PermissionHandler.hasNotificationPermission(context)) {
            return
        }
        
        // Check user preference
        val notifyEnabled = runBlocking { preferencesManager.notifyApprovedFlow.first() }
        if (!notifyEnabled) {
            return
        }
        
        // Determine display name: merchantName > senderName > phone > default
        val displayName = merchantName 
            ?: senderName 
            ?: senderPhone?.let { "שובר חדש" } 
            ?: context.getString(R.string.notification_voucher_approved_title)
        
        val title = context.getString(R.string.notification_voucher_approved_title)
        val message = if (merchantName != null || senderName != null) {
            context.getString(R.string.notification_voucher_approved_message, displayName)
        } else {
            "שובר חדש נוסף לרשימה המאושרים"
        }
        
        // Create intent to open app on Approved screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "approved")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_VOUCHER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_VOUCHERS)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_VOUCHER, notification)
    }
    
    /**
     * Send notification for a new pending voucher.
     */
    fun notifyPendingReview() {
        // Check permission
        if (!PermissionHandler.hasNotificationPermission(context)) {
            return
        }
        
        // Check user preference
        val notifyEnabled = runBlocking { preferencesManager.notifyPendingFlow.first() }
        if (!notifyEnabled) {
            return
        }
        
        val title = context.getString(R.string.notification_pending_title)
        val message = context.getString(R.string.notification_pending_message)
        
        // Create intent to open app on Pending screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "pending")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_PENDING,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_PENDING)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_PENDING, notification)
    }
}

