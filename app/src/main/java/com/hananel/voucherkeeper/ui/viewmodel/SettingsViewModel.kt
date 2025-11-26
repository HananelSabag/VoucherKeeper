package com.hananel.voucherkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hananel.voucherkeeper.data.local.entity.ApprovedSenderEntity
import com.hananel.voucherkeeper.data.preferences.PreferencesManager
import com.hananel.voucherkeeper.data.repository.SenderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 * Manages app preferences and approved senders.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val senderRepository: SenderRepository
) : ViewModel() {
    
    // Theme preference
    val theme: StateFlow<String> = preferencesManager.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesManager.setTheme(theme)
        }
    }
    
    // Language preference
    val language: StateFlow<String> = preferencesManager.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "auto"
    )
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesManager.setLanguage(language)
        }
    }
    
    // Notification preferences
    val notifyApproved: StateFlow<Boolean> = preferencesManager.notifyApprovedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
    fun setNotifyApproved(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotifyApproved(enabled)
        }
    }
    
    val notifyPending: StateFlow<Boolean> = preferencesManager.notifyPendingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
    fun setNotifyPending(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotifyPending(enabled)
        }
    }
    
    // Strict mode
    val strictMode: StateFlow<Boolean> = preferencesManager.strictModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    fun setStrictMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setStrictMode(enabled)
        }
    }
    
    // Approved senders
    val approvedSenders: StateFlow<List<ApprovedSenderEntity>> = senderRepository
        .getAllApprovedSenders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addApprovedSender(phone: String, name: String?) {
        viewModelScope.launch {
            senderRepository.addApprovedSender(phone, name)
        }
    }
    
    fun removeApprovedSender(phone: String) {
        viewModelScope.launch {
            senderRepository.removeApprovedSender(phone)
        }
    }
}

