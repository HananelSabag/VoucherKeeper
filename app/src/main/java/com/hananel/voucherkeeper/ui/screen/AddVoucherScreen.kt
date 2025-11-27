package com.hananel.voucherkeeper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.domain.parser.ParserEngine
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

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
    // Get ParserEngine from DI
    val context = androidx.compose.ui.platform.LocalContext.current
    val parserEngine = remember {
        val appContext = context.applicationContext as? android.app.Application
        com.hananel.voucherkeeper.domain.parser.ParserEngine()
    }
    var pastedMessage by remember { mutableStateOf("") }
    var merchantName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var voucherUrl by remember { mutableStateOf("") }
    var redeemCode by remember { mutableStateOf("") }
    var showMerchantError by remember { mutableStateOf(false) }
    var showAccessPointError by remember { mutableStateOf(false) }
    var showParseSuccess by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_voucher_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            
            // Smart Paste Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.add_voucher_smart_paste_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = stringResource(R.string.add_voucher_smart_paste_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = pastedMessage,
                        onValueChange = { pastedMessage = it },
                        label = { Text(stringResource(R.string.add_voucher_paste_message)) },
                        placeholder = { Text(stringResource(R.string.add_voucher_paste_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    Button(
                        onClick = {
                            if (pastedMessage.isNotBlank()) {
                                val extracted = parserEngine.extractFromText(pastedMessage)
                                extracted.merchantName?.let { merchantName = it }
                                extracted.amount?.let { amount = it }
                                extracted.voucherUrl?.let { voucherUrl = it }
                                extracted.redeemCode?.let { redeemCode = it }
                                showParseSuccess = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = pastedMessage.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_voucher_parse_button))
                    }
                    
                    if (showParseSuccess) {
                        Text(
                            text = "✓ " + stringResource(R.string.add_voucher_parse_success),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            HorizontalDivider()
            
            Text(
                text = stringResource(R.string.add_voucher_manual_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Voucher Title (required) - replaces merchant name
            OutlinedTextField(
                value = merchantName,
                onValueChange = { 
                    merchantName = it
                    showMerchantError = false
                },
                label = { Text(stringResource(R.string.add_voucher_merchant) + " *") },
                placeholder = { Text(stringResource(R.string.add_voucher_merchant_hint)) },
                modifier = Modifier.fillMaxWidth(),
                isError = showMerchantError && merchantName.isBlank(),
                supportingText = {
                    Text(
                        text = if (showMerchantError && merchantName.isBlank()) {
                            stringResource(R.string.add_voucher_merchant_required)
                        } else {
                            "Card header - merchant, store, or description"
                        },
                        color = if (showMerchantError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            // Amount (optional)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.add_voucher_amount_label)) },
                placeholder = { Text("100 ₪") },
                supportingText = { Text(stringResource(R.string.add_voucher_amount_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Voucher URL (required - URL or code)
            OutlinedTextField(
                value = voucherUrl,
                onValueChange = { 
                    voucherUrl = it
                    showAccessPointError = false
                },
                label = { Text(stringResource(R.string.add_voucher_url_label)) },
                placeholder = { Text("https://...") },
                supportingText = { Text(stringResource(R.string.add_voucher_url_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank()
            )
            
            Text(
                text = "━━━━ OR ━━━━",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Redeem code (required - URL or code)
            OutlinedTextField(
                value = redeemCode,
                onValueChange = { 
                    redeemCode = it
                    showAccessPointError = false
                },
                label = { Text(stringResource(R.string.add_voucher_code_label)) },
                placeholder = { Text("ABC123XYZ") },
                modifier = Modifier.fillMaxWidth(),
                isError = showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank(),
                supportingText = {
                    Text(
                        text = if (showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank()) {
                            stringResource(R.string.add_voucher_access_point_required)
                        } else {
                            stringResource(R.string.add_voucher_code_hint)
                        },
                        color = if (showAccessPointError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                                null // No phone for manual entry - it's trusted
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

