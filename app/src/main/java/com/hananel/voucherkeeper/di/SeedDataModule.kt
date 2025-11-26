package com.hananel.voucherkeeper.di

import android.content.Context
import com.hananel.voucherkeeper.data.local.VoucherDatabase
import com.hananel.voucherkeeper.data.local.entity.TrustedDomainEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds initial data (trusted domains) into the database on first launch.
 */
@Singleton
class SeedDataModule @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: VoucherDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    init {
        seedTrustedDomains()
    }
    
    private fun seedTrustedDomains() {
        scope.launch {
            val existingDomains = database.trustedDomainDao().getAllDomainsList()
            
            if (existingDomains.isEmpty()) {
                // Seed default trusted domains
                val defaultDomains = listOf(
                    "myconsumers.pluxee.co.il",
                    "cibus.pluxee.co.il",
                    "pluxee.co.il",
                    "edenred.co.il",
                    "shufersal.co.il"
                )
                
                defaultDomains.forEach { domain ->
                    database.trustedDomainDao().insertDomain(TrustedDomainEntity(domain))
                }
            }
        }
    }
}

