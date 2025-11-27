package com.hananel.voucherkeeper.data.repository

import com.hananel.voucherkeeper.data.local.dao.ApprovedSenderDao
import com.hananel.voucherkeeper.data.local.entity.ApprovedSenderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for approved senders management.
 */
@Singleton
class SenderRepository @Inject constructor(
    private val approvedSenderDao: ApprovedSenderDao
) {
    
    /**
     * Get all approved senders.
     */
    fun getAllApprovedSenders(): Flow<List<ApprovedSenderEntity>> {
        return approvedSenderDao.getAllApprovedSenders()
    }
    
    /**
     * Check if phone number is an approved sender.
     */
    suspend fun isApprovedSender(phone: String): Boolean {
        return approvedSenderDao.isApprovedSender(phone)
    }
    
    /**
     * Add a sender to approved list.
     */
    suspend fun addApprovedSender(phone: String, name: String? = null) {
        val sender = ApprovedSenderEntity(phone = phone, name = name)
        approvedSenderDao.insertSender(sender)
    }
    
    /**
     * Remove sender from approved list.
     */
    suspend fun removeApprovedSender(phone: String) {
        approvedSenderDao.deleteSenderByPhone(phone)
    }
    
    /**
     * Update an approved sender's details.
     */
    suspend fun updateApprovedSender(sender: ApprovedSenderEntity) {
        approvedSenderDao.updateSender(sender)
    }
}

