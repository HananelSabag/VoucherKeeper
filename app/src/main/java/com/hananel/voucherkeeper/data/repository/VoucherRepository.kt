package com.hananel.voucherkeeper.data.repository

import com.hananel.voucherkeeper.data.local.dao.ApprovedSenderDao
import com.hananel.voucherkeeper.data.local.dao.TrustedDomainDao
import com.hananel.voucherkeeper.data.local.dao.VoucherDao
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import com.hananel.voucherkeeper.domain.parser.ExtractedData
import com.hananel.voucherkeeper.domain.parser.ParserEngine
import com.hananel.voucherkeeper.domain.parser.SMSMessage
import com.hananel.voucherkeeper.domain.parser.VoucherDecision
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
        // Check if sender is approved (by phone or by saved name)
        val isApprovedSender = if (smsMessage.senderName != null) {
            // Check by name first (for saved contacts like "Shufersal", "Leumi")
            approvedSenderDao.isApprovedSenderByNameOrPhone(smsMessage.senderName)
        } else {
            // Check by phone number
            approvedSenderDao.isApprovedSender(smsMessage.senderPhone)
        }
        
        // Get custom trusted domains
        val customDomains = trustedDomainDao.getAllDomainsList()
        
        // Run parser engine
        val decision = parserEngine.process(smsMessage, isApprovedSender, customDomains)
        
        // Store voucher if approved or pending
        when (decision) {
            is VoucherDecision.Approved -> {
                insertVoucherFromDecision(smsMessage, decision.extractedData, "approved")
            }
            is VoucherDecision.Pending -> {
                insertVoucherFromDecision(smsMessage, decision.extractedData, "pending")
            }
            is VoucherDecision.Discard -> {
                // Do nothing - message rejected
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
}

