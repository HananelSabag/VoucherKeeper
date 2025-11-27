package com.hananel.voucherkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import com.hananel.voucherkeeper.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Approved Vouchers screen.
 * Manages approved voucher list and deletion operations.
 */
@HiltViewModel
class ApprovedVouchersViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository
) : ViewModel() {
    
    /**
     * StateFlow of approved vouchers for UI observation.
     */
    val approvedVouchers: StateFlow<List<VoucherEntity>> = voucherRepository
        .getApprovedVouchers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Delete a voucher.
     */
    fun deleteVoucher(voucherId: Long) {
        viewModelScope.launch {
            voucherRepository.deleteVoucher(voucherId)
        }
    }
    
    /**
     * Add a voucher manually.
     */
    fun addVoucherManually(
        merchantName: String,
        amount: String?,
        voucherUrl: String?,
        redeemCode: String?,
        senderPhone: String?
    ) {
        viewModelScope.launch {
            voucherRepository.addVoucherManually(
                merchantName = merchantName,
                amount = amount,
                voucherUrl = voucherUrl,
                redeemCode = redeemCode,
                senderPhone = senderPhone
            )
        }
    }
    
    /**
     * Update voucher sender name.
     */
    fun updateVoucherName(voucherId: Long, newName: String) {
        viewModelScope.launch {
            voucherRepository.updateVoucherName(voucherId, newName)
        }
    }
    
    /**
     * Update voucher amount.
     */
    fun updateVoucherAmount(voucherId: Long, newAmount: String?) {
        viewModelScope.launch {
            voucherRepository.updateVoucherAmount(voucherId, newAmount)
        }
    }
}

