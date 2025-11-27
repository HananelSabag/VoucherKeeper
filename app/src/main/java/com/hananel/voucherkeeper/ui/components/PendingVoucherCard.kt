package com.hananel.voucherkeeper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component displaying a pending voucher with approve/reject actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingVoucherCard(
    voucher: VoucherEntity,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onApproveWithSender: (VoucherEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFFFF3E0) // Light orange
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Merchant/Sender with fallback
            Column {
                Text(
                    text = voucher.merchantName 
                        ?: voucher.senderName 
                        ?: voucher.senderPhone,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Show sender info if different
                if (voucher.merchantName != null && voucher.senderName != null && voucher.merchantName != voucher.senderName) {
                    Text(
                        text = stringResource(R.string.voucher_from, voucher.senderName ?: voucher.senderPhone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Amount (if available)
            voucher.amount?.let { amount ->
                Text(
                    text = "💰 " + stringResource(R.string.voucher_amount, amount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = androidx.compose.ui.graphics.Color(0xFFE65100), // Dark orange
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Redeem code (if available)
            voucher.redeemCode?.let { code ->
                Text(
                    text = "🔑 " + stringResource(R.string.voucher_code, code),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // URL (if available)
            voucher.voucherUrl?.let { url ->
                Text(
                    text = "🔗 " + stringResource(R.string.voucher_url),
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color(0xFF1976D2) // Blue
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Timestamp
            Text(
                text = stringResource(R.string.voucher_date, formatTimestamp(voucher.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_reject))
                }
                
                Button(
                    onClick = { onApprove(voucher.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_approve))
                }
            }
            
            // Optional: Add sender to approved list
            TextButton(
                onClick = { onApproveWithSender(voucher) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_add_sender))
            }
        }
    }
    
    // Reject confirmation dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text(stringResource(R.string.dialog_reject_title)) },
            text = { Text(stringResource(R.string.dialog_reject_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject(voucher.id)
                        showRejectDialog = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

