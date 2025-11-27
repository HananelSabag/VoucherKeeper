package com.hananel.voucherkeeper.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.data.local.entity.VoucherEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component displaying an approved voucher.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherCard(
    voucher: VoucherEntity,
    onDelete: (Long) -> Unit,
    onUpdateName: (Long, String) -> Unit = { _, _ -> },
    onUpdateAmount: (Long, String?) -> Unit = { _, _ -> },
    onUpdateVoucher: (id: Long, name: String?, amount: String?, merchant: String?, url: String?, code: String?) -> Unit = { _, _, _, _, _, _ -> },
    otherVouchersCount: Int = 0,
    totalAmount: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Sender Info with Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sender icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (voucher.senderPhone == "Manual Entry") 
                                Icons.Filled.Edit 
                            else 
                                Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = voucher.senderName 
                                ?: voucher.senderPhone.takeIf { it != "Manual Entry" }
                                ?: stringResource(R.string.voucher_no_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        if (voucher.senderName != null && voucher.senderPhone != "Manual Entry") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = voucher.senderPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        // Show summary badge if there are other vouchers from same sender
                        if (otherVouchersCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = if (totalAmount != null) {
                                            stringResource(R.string.voucher_additional_info, otherVouchersCount, totalAmount)
                                        } else {
                                            stringResource(R.string.voucher_additional_info_no_amount, otherVouchersCount)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.action_edit),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Voucher details
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Amount
                voucher.amount?.let { amount ->
                    VoucherDetailRow(
                        icon = Icons.Filled.Info,
                        label = stringResource(R.string.voucher_amount, amount),
                        color = MaterialTheme.colorScheme.secondary,
                        emphasized = true
                    )
                }
                
                // Redeem code
                voucher.redeemCode?.let { code ->
                    VoucherDetailRow(
                        icon = Icons.Filled.Star,
                        label = stringResource(R.string.voucher_code, code),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                // URL
                voucher.voucherUrl?.let { url ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle error silently
                                }
                            }
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_open_link),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Timestamp
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTimestamp(voucher.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Edit Dialog
    if (showEditDialog) {
        EditVoucherDialog(
            currentName = voucher.senderName ?: "",
            currentAmount = voucher.amount ?: "",
            currentMerchantName = voucher.merchantName ?: "",
            currentUrl = voucher.voucherUrl ?: "",
            currentCode = voucher.redeemCode ?: "",
            onDismiss = { showEditDialog = false },
            onConfirm = { newName, newAmount, newMerchant, newUrl, newCode ->
                // Update all fields
                onUpdateVoucher(
                    voucher.id,
                    newName.takeIf { it.isNotBlank() },
                    newAmount.takeIf { it.isNotBlank() },
                    newMerchant.takeIf { it.isNotBlank() },
                    newUrl.takeIf { it.isNotBlank() },
                    newCode.takeIf { it.isNotBlank() }
                )
                showEditDialog = false
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(voucher.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun VoucherDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    emphasized: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun EditVoucherDialog(
    currentName: String,
    currentAmount: String,
    currentMerchantName: String,
    currentUrl: String,
    currentCode: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: String, merchant: String, url: String, code: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var amount by remember { mutableStateOf(currentAmount) }
    var merchantName by remember { mutableStateOf(currentMerchantName) }
    var voucherUrl by remember { mutableStateOf(currentUrl) }
    var redeemCode by remember { mutableStateOf(currentCode) }
    var showError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.voucher_edit_full_dialog_title),
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Info banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.voucher_edit_full_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Display name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.voucher_edit_name_hint)) },
                    placeholder = { Text("My Store, Friendly Name...") },
                    supportingText = { Text("Optional friendly display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider()
                
                // Merchant/Title (required)
                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { 
                        merchantName = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.add_voucher_merchant) + " *") },
                    placeholder = { Text("Shufersal, Terminal X...") },
                    supportingText = { Text("Card title - shown prominently") },
                    isError = showError && merchantName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Amount (optional)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.add_voucher_amount_label)) },
                    placeholder = { Text("100 ₪") },
                    supportingText = { 
                        Text(
                            text = if (amount.isBlank()) {
                                stringResource(R.string.voucher_amount_not_found)
                            } else {
                                "Will display: $amount"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider()
                
                // URL
                OutlinedTextField(
                    value = voucherUrl,
                    onValueChange = { voucherUrl = it },
                    label = { Text(stringResource(R.string.add_voucher_url_label)) },
                    placeholder = { Text("https://...") },
                    supportingText = { Text("Link to use the voucher") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Redeem Code
                OutlinedTextField(
                    value = redeemCode,
                    onValueChange = { redeemCode = it },
                    label = { Text(stringResource(R.string.add_voucher_code_label)) },
                    placeholder = { Text("ABC123XYZ") },
                    supportingText = { Text("Code to redeem") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (merchantName.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(
                            name.trim(),
                            amount.trim(),
                            merchantName.trim(),
                            voucherUrl.trim(),
                            redeemCode.trim()
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

