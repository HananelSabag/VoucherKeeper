package com.hananel.voucherkeeper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import com.hananel.voucherkeeper.ui.components.PendingVoucherCard
import com.hananel.voucherkeeper.ui.viewmodel.PendingReviewViewModel

/**
 * Pending Review screen - displays vouchers awaiting manual approval.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingReviewScreen(
    onShowHelp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: PendingReviewViewModel = hiltViewModel()
) {
    val vouchers by viewModel.pendingVouchers.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.pending_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = onShowHelp) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.cd_help_button)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_settings_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (vouchers.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = vouchers,
                    key = { it.id }
                ) { voucher ->
                    PendingVoucherCard(
                        voucher = voucher,
                        onApprove = { viewModel.approveVoucher(it) },
                        onReject = { viewModel.rejectVoucher(it) },
                        onApproveWithSender = { viewModel.approveVoucherAndSender(it) },
                        onUpdateVoucher = { id: Long, name: String?, amount: String?, merchant: String?, url: String?, code: String? -> 
                            viewModel.updateVoucher(id, name, amount, merchant, url, code) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⏳",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.pending_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.pending_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

