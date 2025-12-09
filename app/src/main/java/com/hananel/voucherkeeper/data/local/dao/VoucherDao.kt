package com.hananel.voucherkeeper.data.local.dao

import androidx.room.*
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for voucher database operations.
 */
@Dao
interface VoucherDao {
    
    /**
     * Get approved vouchers sorted: active first (by timestamp desc), then redeemed (by redeemedAt desc)
     */
    @Query("""
        SELECT * FROM vouchers 
        WHERE status = 'approved' 
        ORDER BY isRedeemed ASC, 
                 CASE WHEN isRedeemed = 0 THEN timestamp ELSE redeemedAt END DESC
    """)
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
     * Mark a voucher as redeemed.
     */
    @Query("UPDATE vouchers SET isRedeemed = 1, redeemedAt = :redeemedAt WHERE id = :id")
    suspend fun markAsRedeemed(id: Long, redeemedAt: Long)
    
    /**
     * Unmark a voucher as redeemed (restore it).
     */
    @Query("UPDATE vouchers SET isRedeemed = 0, redeemedAt = NULL WHERE id = :id")
    suspend fun unmarkAsRedeemed(id: Long)
    
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

