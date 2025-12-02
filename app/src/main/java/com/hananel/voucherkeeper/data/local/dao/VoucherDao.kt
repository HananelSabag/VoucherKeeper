package com.hananel.voucherkeeper.data.local.dao

import androidx.room.*
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for voucher database operations.
 */
@Dao
interface VoucherDao {
    
    @Query("SELECT * FROM vouchers WHERE status = 'approved' ORDER BY timestamp DESC")
    fun getApprovedVouchers(): Flow<List<VoucherEntity>>
    
    @Query("SELECT * FROM vouchers WHERE status = 'pending' ORDER BY timestamp DESC")
    fun getPendingVouchers(): Flow<List<VoucherEntity>>
    
    @Query("SELECT COUNT(*) FROM vouchers WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>
    
    @Query("SELECT * FROM vouchers WHERE id = :id")
    suspend fun getVoucherById(id: Long): VoucherEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long
    
    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)
    
    @Query("UPDATE vouchers SET status = 'approved' WHERE id = :id")
    suspend fun approveVoucher(id: Long)
    
    @Delete
    suspend fun deleteVoucher(voucher: VoucherEntity)
    
    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteVoucherById(id: Long)
    
    @Query("DELETE FROM vouchers WHERE status = 'pending'")
    suspend fun deleteAllPending()
    
    /**
     * Update sender name for all vouchers from a specific phone number.
     * Used for syncing when a sender is added/updated in approved senders list.
     */
    @Query("UPDATE vouchers SET senderName = :newName WHERE senderPhone = :phone")
    suspend fun updateSenderNameByPhone(phone: String, newName: String?)
    
    /**
     * Get all vouchers from a specific sender phone (for sync operations).
     */
    @Query("SELECT * FROM vouchers WHERE senderPhone = :phone")
    suspend fun getVouchersByPhone(phone: String): List<VoucherEntity>
}

