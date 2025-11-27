package com.hananel.voucherkeeper.data.repository

import android.util.Log
import com.hananel.voucherkeeper.data.local.dao.ApprovedSenderDao
import com.hananel.voucherkeeper.data.local.dao.TrustedDomainDao
import com.hananel.voucherkeeper.data.local.dao.VoucherDao
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import com.hananel.voucherkeeper.domain.parser.ExtractedData
import com.hananel.voucherkeeper.domain.parser.ParserEngine
import com.hananel.voucherkeeper.domain.parser.SMSMessage
import com.hananel.voucherkeeper.domain.parser.VoucherDecision
import com.hananel.voucherkeeper.util.PhoneNumberHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for voucher operations.
 * Handles voucher storage, retrieval, and SMS processing.
 */
@Singleton
class VoucherRepository @Inject constructor(
    private val voucherDao: VoucherDao,
    private val approvedSenderDao: ApprovedSenderDao,
    private val trustedDomainDao: TrustedDomainDao,
    private val parserEngine: ParserEngine
) {
    
    companion object {
        private const val TAG = "VoucherKeeper_Repo"
    }
    
    /**
     * Get all approved vouchers (Flow for reactive UI).
     */
    fun getApprovedVouchers(): Flow<List<VoucherEntity>> {
        return voucherDao.getApprovedVouchers()
    }
    
    /**
     * Get all pending vouchers (Flow for reactive UI).
     */
    fun getPendingVouchers(): Flow<List<VoucherEntity>> {
        return voucherDao.getPendingVouchers()
    }
    
    /**
     * Get pending vouchers count (for badge).
     */
    fun getPendingCount(): Flow<Int> {
        return voucherDao.getPendingCount()
    }
    
    /**
     * Process incoming SMS message.
     * Uses ParserEngine to classify and store if needed.
     * 
     * @return The classification decision
     */
    suspend fun processSmsMessage(smsMessage: SMSMessage): VoucherDecision {
        Log.d(TAG, "=== VOUCHER REPOSITORY - Processing SMS ===")
        Log.d(TAG, "Sender: ${smsMessage.senderPhone}")
        
        // Normalize incoming phone number
        val normalizedIncomingPhone = PhoneNumberHelper.normalize(smsMessage.senderPhone)
        Log.d(TAG, "Normalized incoming phone: $normalizedIncomingPhone")
        
        // Check if sender is approved (by phone or by saved name)
        // For phone matching, we normalize and compare all approved senders
        val allApprovedSenders = approvedSenderDao.getAllApprovedSendersList()
        val matchedSender = allApprovedSenders.firstOrNull { sender ->
            PhoneNumberHelper.areEqual(sender.phone, smsMessage.senderPhone)
        }
        val isApprovedByPhone = matchedSender != null
        
        // Check by display name (exact match for system names like "Shufersal")
        val isApprovedByName = if (smsMessage.senderName != null) {
            approvedSenderDao.isApprovedSenderByNameOrPhone(smsMessage.senderName)
        } else {
            false
        }
        
        val isApprovedSender = isApprovedByPhone || isApprovedByName
        
        // Get display name from approved sender if exists
        val displayName = matchedSender?.name ?: smsMessage.senderName
        
        Log.d(TAG, "Sender check:")
        Log.d(TAG, "  - Phone: ${smsMessage.senderPhone} → Approved: $isApprovedByPhone")
        Log.d(TAG, "  - Name: ${smsMessage.senderName ?: "(none)"} → Approved: $isApprovedByName")
        Log.d(TAG, "  - Display name from approved sender: ${displayName ?: "(none)"}")
        Log.d(TAG, "  - Final: $isApprovedSender")
        
        // Get custom trusted domains
        val customDomains = trustedDomainDao.getAllDomainsList()
        Log.d(TAG, "Custom domains count: ${customDomains.size}")
        
        // Run parser engine with updated SMS message (with display name if found)
        val updatedSmsMessage = if (displayName != null && displayName != smsMessage.senderName) {
            smsMessage.copy(senderName = displayName)
        } else {
            smsMessage
        }
        
        Log.d(TAG, "Running parser engine...")
        val decision = parserEngine.process(updatedSmsMessage, isApprovedSender, customDomains)
        Log.d(TAG, "Parser decision: ${decision.javaClass.simpleName}")
        
        // Store voucher if approved or pending (use updatedSmsMessage with display name)
        when (decision) {
            is VoucherDecision.Approved -> {
                Log.d(TAG, "Storing APPROVED voucher to database...")
                insertVoucherFromDecision(updatedSmsMessage, decision.extractedData, "approved")
                Log.d(TAG, "✓ Voucher saved successfully")
            }
            is VoucherDecision.Pending -> {
                Log.d(TAG, "Storing PENDING voucher to database...")
                insertVoucherFromDecision(updatedSmsMessage, decision.extractedData, "pending")
                Log.d(TAG, "✓ Pending voucher saved successfully")
            }
            is VoucherDecision.Discard -> {
                Log.d(TAG, "Message discarded - not saving to database")
            }
        }
        
        return decision
    }
    
    /**
     * Insert voucher entity from extracted data.
     */
    private suspend fun insertVoucherFromDecision(
        smsMessage: SMSMessage,
        extractedData: ExtractedData,
        status: String
    ) {
        val voucher = VoucherEntity(
            status = status,
            merchantName = extractedData.merchantName,
            amount = extractedData.amount,
            voucherUrl = extractedData.voucherUrl,
            redeemCode = extractedData.redeemCode,
            senderPhone = smsMessage.senderPhone,
            senderName = smsMessage.senderName,
            rawMessage = extractedData.rawMessage,
            timestamp = smsMessage.timestamp
        )
        voucherDao.insertVoucher(voucher)
    }
    
    /**
     * Approve a pending voucher (move to approved list).
     */
    suspend fun approveVoucher(voucherId: Long) {
        voucherDao.approveVoucher(voucherId)
    }
    
    /**
     * Reject and delete a voucher.
     */
    suspend fun rejectVoucher(voucherId: Long) {
        voucherDao.deleteVoucherById(voucherId)
    }
    
    /**
     * Delete a voucher.
     */
    suspend fun deleteVoucher(voucherId: Long) {
        voucherDao.deleteVoucherById(voucherId)
    }
    
    /**
     * Get voucher by ID.
     */
    suspend fun getVoucherById(id: Long): VoucherEntity? {
        return voucherDao.getVoucherById(id)
    }
    
    /**
     * Add a voucher manually (user-created).
     */
    suspend fun addVoucherManually(
        merchantName: String,
        amount: String?,
        voucherUrl: String?,
        redeemCode: String?,
        senderPhone: String?
    ) {
        val voucher = VoucherEntity(
            status = "approved",
            merchantName = merchantName,
            amount = amount,
            voucherUrl = voucherUrl,
            redeemCode = redeemCode,
            senderPhone = senderPhone ?: "Manual Entry",
            senderName = null,
            rawMessage = "Manually added voucher",
            timestamp = System.currentTimeMillis()
        )
        voucherDao.insertVoucher(voucher)
    }
    
    /**
     * Update voucher sender name.
     */
    suspend fun updateVoucherName(voucherId: Long, newName: String) {
        val voucher = voucherDao.getVoucherById(voucherId)
        voucher?.let {
            val updated = it.copy(senderName = newName.takeIf { name -> name.isNotBlank() })
            voucherDao.updateVoucher(updated)
        }
    }
}

