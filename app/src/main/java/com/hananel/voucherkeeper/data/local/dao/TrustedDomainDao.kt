package com.hananel.voucherkeeper.data.local.dao

import androidx.room.*
import com.hananel.voucherkeeper.data.local.entity.TrustedDomainEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for trusted domains management.
 */
@Dao
interface TrustedDomainDao {
    
    @Query("SELECT * FROM trusted_domains ORDER BY domain ASC")
    fun getAllTrustedDomains(): Flow<List<TrustedDomainEntity>>
    
    @Query("SELECT domain FROM trusted_domains")
    suspend fun getAllDomainsList(): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomain(domain: TrustedDomainEntity)
    
    @Delete
    suspend fun deleteDomain(domain: TrustedDomainEntity)
    
    @Query("DELETE FROM trusted_domains WHERE domain = :domain")
    suspend fun deleteDomainByName(domain: String)
}

