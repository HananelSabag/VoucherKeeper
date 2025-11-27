package com.hananel.voucherkeeper.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import com.hananel.voucherkeeper.ui.components.VoucherCard
import com.hananel.voucherkeeper.ui.viewmodel.ApprovedVouchersViewModel

/**
 * Approved Vouchers screen - displays user's approved vouchers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovedVouchersScreen(
    onAddVoucher: () -> Unit = {},
    onShowHelp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: ApprovedVouchersViewModel = hiltViewModel()
) {
    val vouchers by viewModel.approvedVouchers.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = stringResource(R.string.approved_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddVoucher,
                containerColor = MaterialTheme.colorScheme.primary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.action_add_voucher_manually))
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
            // Group vouchers by sender for summary stats
            val vouchersBySender = vouchers.groupBy { voucher ->
                // Group by normalized phone or sender name
                val normalizedPhone = com.hananel.voucherkeeper.util.PhoneNumberHelper.normalize(
                    voucher.senderPhone
                )
                voucher.senderName ?: normalizedPhone
            }
            
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
                    val senderKey = voucher.senderName 
                        ?: com.hananel.voucherkeeper.util.PhoneNumberHelper.normalize(voucher.senderPhone)
                    val senderVouchers = vouchersBySender[senderKey] ?: emptyList()
                    val otherVouchersCount = (senderVouchers.size - 1).coerceAtLeast(0)
                    
                    // Calculate total amount for this sender
                    val totalAmount = if (otherVouchersCount > 0) {
                        val amounts = senderVouchers.mapNotNull { it.amount }
                        if (amounts.isNotEmpty()) {
                            // Sum all amounts (parse the currency strings)
                            val total = amounts.sumOf { amountStr ->
                                amountStr.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
                            }
                            if (total > 0) "₪${String.format("%.2f", total)}" else null
                        } else null
                    } else null
                    
                    VoucherCard(
                        voucher = voucher,
                        onDelete = { viewModel.deleteVoucher(it) },
                        onUpdateName = { id, name -> viewModel.updateVoucherName(id, name) },
                        otherVouchersCount = otherVouchersCount,
                        totalAmount = totalAmount
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
            text = "🎫",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.approved_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.approved_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

