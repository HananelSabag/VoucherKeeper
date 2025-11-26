package com.hananel.voucherkeeper.data.local.dao

import androidx.room.*
import com.hananel.voucherkeeper.data.local.entity.ApprovedSenderEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for approved senders management.
 */
@Dao
interface ApprovedSenderDao {
    
    @Query("SELECT * FROM approved_senders ORDER BY name ASC")
    fun getAllApprovedSenders(): Flow<List<ApprovedSenderEntity>>
    
    @Query("SELECT * FROM approved_senders WHERE phone = :phone LIMIT 1")
    suspend fun getSenderByPhone(phone: String): ApprovedSenderEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM approved_senders WHERE phone = :phone)")
    suspend fun isApprovedSender(phone: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM approved_senders WHERE LOWER(name) = LOWER(:name) OR phone = :name)")
    suspend fun isApprovedSenderByNameOrPhone(name: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSender(sender: ApprovedSenderEntity)
    
    @Delete
    suspend fun deleteSender(sender: ApprovedSenderEntity)
    
    @Query("DELETE FROM approved_senders WHERE phone = :phone")
    suspend fun deleteSenderByPhone(phone: String)
}

