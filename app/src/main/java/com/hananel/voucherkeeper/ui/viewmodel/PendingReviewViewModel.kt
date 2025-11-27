package com.hananel.voucherkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import com.hananel.voucherkeeper.data.repository.SenderRepository
import com.hananel.voucherkeeper.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Pending Review screen.
 * Manages pending vouchers and approve/reject actions.
 */
@HiltViewModel
class PendingReviewViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val senderRepository: SenderRepository
) : ViewModel() {
    
    /**
     * StateFlow of pending vouchers for UI observation.
     */
    val pendingVouchers: StateFlow<List<VoucherEntity>> = voucherRepository
        .getPendingVouchers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Approve a voucher (moves to approved list).
     */
    fun approveVoucher(voucherId: Long) {
        viewModelScope.launch {
            voucherRepository.approveVoucher(voucherId)
        }
    }
    
    /**
     * Reject and delete a voucher.
     */
    fun rejectVoucher(voucherId: Long) {
        viewModelScope.launch {
            voucherRepository.rejectVoucher(voucherId)
        }
    }
    
    /**
     * Approve voucher and add sender to approved list.
     */
    fun approveVoucherAndSender(voucher: VoucherEntity) {
        viewModelScope.launch {
            voucherRepository.approveVoucher(voucher.id)
            senderRepository.addApprovedSender(
                phone = voucher.senderPhone,
                name = voucher.senderName
            )
        }
    }
    
    /**
     * Update voucher - fix parsing errors before approval.
     */
    fun updateVoucher(
        voucherId: Long,
        newName: String?,
        newAmount: String?,
        newMerchant: String?,
        newUrl: String?,
        newCode: String?
    ) {
        viewModelScope.launch {
            voucherRepository.updateVoucher(voucherId, newName, newAmount, newMerchant, newUrl, newCode)
        }
    }
}

