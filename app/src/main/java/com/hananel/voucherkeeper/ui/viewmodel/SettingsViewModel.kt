package com.hananel.voucherkeeper.ui.viewmodel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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
import java.io.FileOutputStream
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
            // Sync the display name to all existing vouchers from this sender
            // Always sync - even if name is null (to clear old names if needed)
            voucherRepository.syncSenderNameToVouchers(phone, name)
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
            // Sync the display name to all existing vouchers from this sender
            voucherRepository.syncSenderNameToVouchers(sender.phone, sender.name)
        }
    }
    
    /**
     * Export all approved vouchers to a styled PDF file.
     * Returns the File object if successful, null otherwise.
     */
    suspend fun exportVouchersToPDF(context: Context): File? {
        return try {
            // Get all approved vouchers
            val vouchers = voucherRepository.getApprovedVouchers().first()
            
            if (vouchers.isEmpty()) {
                return null
            }
            
            // Get current locale for language-specific content
            val locale = try {
                context.resources.configuration.locales[0]
            } catch (e: Exception) {
                Locale.getDefault()
            }
            val isHebrew = locale.language == "he" || locale.language == "iw"
            
            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportDate = SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date())
            val filename = if (isHebrew) {
                "שוברים_מאושרים_$timestamp.pdf"
            } else {
                "Approved_Vouchers_$timestamp.pdf"
            }
            
            // Create PDF document
            val pdfDocument = PdfDocument()
            
            // Page settings (A4)
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            val margin = 40f
            val contentWidth = pageWidth - 2 * margin
            
            // Paint objects
            val titlePaint = Paint().apply {
                color = Color.parseColor("#1976D2") // Material Blue
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#757575") // Gray
                textSize = 12f
                isAntiAlias = true
            }
            
            val headerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val cellPaint = Paint().apply {
                color = Color.parseColor("#212121")
                textSize = 10f
                isAntiAlias = true
            }
            
            val linkPaint = Paint().apply {
                color = Color.parseColor("#1976D2")
                textSize = 10f
                isAntiAlias = true
            }
            
            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#1976D2")
                style = Paint.Style.FILL
            }
            
            val rowBgPaint = Paint().apply {
                color = Color.parseColor("#F5F5F5")
                style = Paint.Style.FILL
            }
            
            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 1f
            }
            
            // Column widths (adjusted for content)
            val colWidths = if (isHebrew) {
                floatArrayOf(120f, 60f, 100f, 140f, 95f) // שם, סכום, קוד, קישור, תאריך
            } else {
                floatArrayOf(120f, 60f, 100f, 140f, 95f) // Name, Amount, Code, Link, Date
            }
            
            val headers = if (isHebrew) {
                listOf("שם שובר", "סכום", "קוד מימוש", "קישור", "תאריך")
            } else {
                listOf("Voucher Name", "Amount", "Code", "Link", "Date")
            }
            
            val rowHeight = 35f
            val headerHeight = 30f
            var currentY = margin + 60f // Start after title
            var pageNumber = 1
            var currentPage: PdfDocument.Page? = null
            var canvas: Canvas? = null
            
            fun startNewPage() {
                currentPage?.let { pdfDocument.finishPage(it) }
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage!!.canvas
                pageNumber++
                currentY = margin
                
                // Draw title on first page only
                if (pageNumber == 2) { // First page (pageNumber incremented)
                    val title = if (isHebrew) "דו״ח שוברים מאושרים" else "Approved Vouchers Report"
                    val subtitle = if (isHebrew) "עודכן: $exportDate" else "Generated: $exportDate"
                    
                    // RTL text alignment for Hebrew
                    if (isHebrew) {
                        titlePaint.textAlign = Paint.Align.RIGHT
                        subtitlePaint.textAlign = Paint.Align.RIGHT
                        canvas?.drawText(title, pageWidth - margin, margin + 25f, titlePaint)
                        canvas?.drawText(subtitle, pageWidth - margin, margin + 45f, subtitlePaint)
                    } else {
                        canvas?.drawText(title, margin, margin + 25f, titlePaint)
                        canvas?.drawText(subtitle, margin, margin + 45f, subtitlePaint)
                    }
                    
                    currentY = margin + 70f
                }
                
                // Draw table header
                var headerX = margin
                canvas?.drawRect(margin, currentY, pageWidth - margin, currentY + headerHeight, headerBgPaint)
                
                headers.forEachIndexed { index, header ->
                    val textX = if (isHebrew) {
                        headerX + colWidths[index] - 5f
                    } else {
                        headerX + 5f
                    }
                    headerPaint.textAlign = if (isHebrew) Paint.Align.RIGHT else Paint.Align.LEFT
                    canvas?.drawText(header, textX, currentY + 20f, headerPaint)
                    headerX += colWidths[index]
                }
                
                currentY += headerHeight
            }
            
            // Start first page
            startNewPage()
            
            // Draw voucher rows
            vouchers.forEachIndexed { index, voucher ->
                // Check if we need a new page
                if (currentY + rowHeight > pageHeight - margin) {
                    startNewPage()
                }
                
                // Alternate row background
                if (index % 2 == 0) {
                    canvas?.drawRect(margin, currentY, pageWidth - margin, currentY + rowHeight, rowBgPaint)
                }
                
                // Draw row data
                var cellX = margin
                val rowData = listOf(
                    voucher.merchantName ?: "-",
                    voucher.amount ?: "-",
                    voucher.redeemCode ?: "-",
                    truncateText(voucher.voucherUrl ?: "-", 25),
                    formatDate(voucher.timestamp)
                )
                
                rowData.forEachIndexed { colIndex, text ->
                    val paint = if (colIndex == 3 && voucher.voucherUrl != null) linkPaint else cellPaint
                    val textX = if (isHebrew) {
                        cellX + colWidths[colIndex] - 5f
                    } else {
                        cellX + 5f
                    }
                    paint.textAlign = if (isHebrew) Paint.Align.RIGHT else Paint.Align.LEFT
                    canvas?.drawText(text, textX, currentY + 22f, paint)
                    cellX += colWidths[colIndex]
                }
                
                // Draw bottom line
                canvas?.drawLine(margin, currentY + rowHeight, pageWidth - margin, currentY + rowHeight, linePaint)
                
                currentY += rowHeight
            }
            
            // Draw summary footer
            currentY += 20f
            if (currentY + 60f > pageHeight - margin) {
                startNewPage()
                currentY += 20f
            }
            
            val summaryPaint = Paint().apply {
                color = Color.parseColor("#424242")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val totalVouchers = vouchers.size
            val totalAmount = calculateTotalAmount(vouchers)
            
            val summaryText = if (isHebrew) {
                "סה״כ: $totalVouchers שוברים${if (totalAmount.isNotEmpty()) " | שווי: $totalAmount" else ""}"
            } else {
                "Total: $totalVouchers vouchers${if (totalAmount.isNotEmpty()) " | Value: $totalAmount" else ""}"
            }
            
            summaryPaint.textAlign = if (isHebrew) Paint.Align.RIGHT else Paint.Align.LEFT
            val summaryX = if (isHebrew) pageWidth - margin else margin
            canvas?.drawText(summaryText, summaryX, currentY, summaryPaint)
            
            // Finish last page
            currentPage?.let { pdfDocument.finishPage(it) }
            
            // Save to file
            val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(downloadsDir, filename)
            
            FileOutputStream(pdfFile).use { output ->
                pdfDocument.writeTo(output)
            }
            
            pdfDocument.close()
            
            pdfFile
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error exporting PDF", e)
            null
        }
    }
    
    /**
     * Truncate text to fit in column.
     */
    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.take(maxLength - 3) + "..."
        } else {
            text
        }
    }
    
    /**
     * Calculate total amount from vouchers (if they have numeric amounts).
     */
    private fun calculateTotalAmount(vouchers: List<VoucherEntity>): String {
        var total = 0.0
        var hasAmount = false
        
        vouchers.forEach { voucher ->
            voucher.amount?.let { amountStr ->
                // Extract numeric value
                val numericValue = amountStr.replace(Regex("[^0-9.,]"), "")
                    .replace(",", ".")
                    .toDoubleOrNull()
                if (numericValue != null) {
                    total += numericValue
                    hasAmount = true
                }
            }
        }
        
        return if (hasAmount) {
            "₪${String.format("%.2f", total)}"
        } else {
            ""
        }
    }
    
    /**
     * Format timestamp to readable date.
     */
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

