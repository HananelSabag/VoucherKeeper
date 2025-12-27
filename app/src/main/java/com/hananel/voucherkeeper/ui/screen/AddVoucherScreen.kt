package com.hananel.voucherkeeper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hananel.voucherkeeper.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for manually adding a voucher.
 * Allows users to input voucher details directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVoucherScreen(
    onSave: (String, String?, String?, String?, String?) -> Unit,
    onBack: () -> Unit
) {
    var merchantName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var voucherUrl by remember { mutableStateOf("") }
    var redeemCode by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var showMerchantError by remember { mutableStateOf(false) }
    var showAccessPointError by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_voucher_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.add_voucher_info),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Merchant name (required)
            OutlinedTextField(
                value = merchantName,
                onValueChange = { 
                    merchantName = it
                    showMerchantError = false
                },
                label = { Text(stringResource(R.string.add_voucher_merchant) + " *") },
                modifier = Modifier.fillMaxWidth(),
                isError = showMerchantError && merchantName.isBlank(),
                supportingText = if (showMerchantError && merchantName.isBlank()) {
                    { Text(stringResource(R.string.add_voucher_merchant_required)) }
                } else null
            )
            
            // Amount (optional)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.add_voucher_amount_label)) },
                placeholder = { Text("100 ₪") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Voucher URL (required - URL or code)
            OutlinedTextField(
                value = voucherUrl,
                onValueChange = { 
                    voucherUrl = it
                    showAccessPointError = false
                },
                label = { Text(stringResource(R.string.add_voucher_url_label) + " *") },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank()
            )
            
            Text(
                text = "או",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // Redeem code (required - URL or code)
            OutlinedTextField(
                value = redeemCode,
                onValueChange = { 
                    redeemCode = it
                    showAccessPointError = false
                },
                label = { Text(stringResource(R.string.add_voucher_code_label) + " *") },
                placeholder = { Text("ABC123XYZ") },
                modifier = Modifier.fillMaxWidth(),
                isError = showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank(),
                supportingText = if (showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank()) {
                    { Text(stringResource(R.string.add_voucher_access_point_required)) }
                } else null
            )
            
            // Sender phone (optional)
            OutlinedTextField(
                value = senderPhone,
                onValueChange = { senderPhone = it },
                label = { Text(stringResource(R.string.add_voucher_sender_label)) },
                placeholder = { Text("0501234567") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save button
            Button(
                onClick = {
                    val hasAccessPoint = voucherUrl.isNotBlank() || redeemCode.isNotBlank()
                    
                    when {
                        merchantName.isBlank() -> {
                            showMerchantError = true
                        }
                        !hasAccessPoint -> {
                            showAccessPointError = true
                        }
                        else -> {
                            onSave(
                                merchantName.trim(),
                                amount.trim().takeIf { it.isNotBlank() },
                                voucherUrl.trim().takeIf { it.isNotBlank() },
                                redeemCode.trim().takeIf { it.isNotBlank() },
                                senderPhone.trim().takeIf { it.isNotBlank() }
                            )
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_voucher_save))
            }
        }
    }
}

