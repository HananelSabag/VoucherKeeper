package com.hananel.voucherkeeper.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hananel.voucherkeeper.data.local.entity.ApprovedSenderEntity
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import com.hananel.voucherkeeper.data.preferences.PreferencesManager
import com.hananel.voucherkeeper.data.repository.SenderRepository
import com.hananel.voucherkeeper.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 * Manages app preferences and approved senders.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val senderRepository: SenderRepository,
    private val voucherRepository: VoucherRepository
) : ViewModel() {
    
    // Theme preference
    val theme: StateFlow<String> = preferencesManager.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesManager.setTheme(theme)
        }
    }
    
    // Language preference
    val language: StateFlow<String> = preferencesManager.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "auto"
    )
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesManager.setLanguage(language)
        }
    }
    
    // Notification preferences
    val notifyApproved: StateFlow<Boolean> = preferencesManager.notifyApprovedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
    fun setNotifyApproved(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotifyApproved(enabled)
        }
    }
    
    val notifyPending: StateFlow<Boolean> = preferencesManager.notifyPendingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
    fun setNotifyPending(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotifyPending(enabled)
        }
    }
    
    // Strict mode
    val strictMode: StateFlow<Boolean> = preferencesManager.strictModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    fun setStrictMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setStrictMode(enabled)
        }
    }
    
    // Approved senders
    val approvedSenders: StateFlow<List<ApprovedSenderEntity>> = senderRepository
        .getAllApprovedSenders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addApprovedSender(phone: String, name: String?) {
        viewModelScope.launch {
            senderRepository.addApprovedSender(phone, name)
        }
    }
    
    fun removeApprovedSender(phone: String) {
        viewModelScope.launch {
            senderRepository.removeApprovedSender(phone)
        }
    }
    
    fun updateApprovedSender(sender: ApprovedSenderEntity) {
        viewModelScope.launch {
            senderRepository.updateApprovedSender(sender)
        }
    }
    
    /**
     * Export all approved vouchers to CSV file.
     * Returns the File object if successful, null otherwise.
     */
    suspend fun exportVouchersToCSV(context: Context): File? {
        return try {
            // Get all approved vouchers
            val vouchers = voucherRepository.getApprovedVouchers().first()
            
            if (vouchers.isEmpty()) {
                return null
            }
            
            // Get current locale for language-specific filename (safe access)
            val locale = try {
                context.resources.configuration.locales[0]
            } catch (e: Exception) {
                Locale.getDefault()
            }
            val isHebrew = locale.language == "he" || locale.language == "iw"
            
            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = if (isHebrew) {
                "שוברים_מאושרים_$timestamp.csv"
            } else {
                "Approved_Vouchers_$timestamp.csv"
            }
            
            // Create file in Downloads folder
            val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            val csvFile = File(downloadsDir, filename)
            
            // Write CSV content
            csvFile.bufferedWriter().use { writer ->
                // Write UTF-8 BOM for proper Excel display
                writer.write("\uFEFF")
                
                // Write header based on language
                val header = if (isHebrew) {
                    "שם שובר,סכום,קוד,קישור,שולח,תאריך קבלה"
                } else {
                    "Voucher Name,Amount,Code,URL,Sender,Date Received"
                }
                writer.write(header)
                writer.newLine()
                
                // Write voucher data
                vouchers.forEach { voucher ->
                    val row = listOf(
                        escapeCSV(voucher.merchantName ?: ""),
                        escapeCSV(voucher.amount ?: ""),
                        escapeCSV(voucher.redeemCode ?: ""),
                        escapeCSV(voucher.voucherUrl ?: ""),
                        escapeCSV(voucher.senderName ?: voucher.senderPhone),
                        formatDate(voucher.timestamp)
                    ).joinToString(",")
                    
                    writer.write(row)
                    writer.newLine()
                }
            }
            
            csvFile
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error exporting CSV", e)
            null
        }
    }
    
    /**
     * Escape CSV field (handle commas, quotes, newlines).
     */
    private fun escapeCSV(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
    
    /**
     * Format timestamp to readable date.
     */
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

