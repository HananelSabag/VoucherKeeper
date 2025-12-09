package com.hananel.voucherkeeper.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.domain.parser.ParserEngine
import com.hananel.voucherkeeper.ui.theme.*

/**
 * Beautiful screen for manually adding a voucher.
 * Features modern design with gradients and smooth animations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVoucherScreen(
    onSave: (String, String?, String?, String?, String?) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val parserEngine = remember { ParserEngine() }
    
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
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.add_voucher_title),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Info card with gradient
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.add_voucher_info),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Smart Paste Section with beautiful design
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box {
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        GradientStartGreen.copy(alpha = 0.12f),
                                        GradientEndGreen.copy(alpha = 0.02f)
                                    )
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.add_voucher_smart_paste_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = stringResource(R.string.add_voucher_smart_paste_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = pastedMessage,
                            onValueChange = { 
                                pastedMessage = it
                                showParseSuccess = false
                            },
                            label = { Text(stringResource(R.string.add_voucher_paste_message)) },
                            placeholder = { Text(stringResource(R.string.add_voucher_paste_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(14.dp)
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
                            enabled = pastedMessage.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.add_voucher_parse_button),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = showParseSuccess,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.add_voucher_parse_success),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Divider with label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.add_voucher_manual_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            
            // Manual Input Fields
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Voucher Title (required)
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { 
                            merchantName = it
                            showMerchantError = false
                        },
                        label = { 
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(R.string.add_voucher_merchant))
                                Text("*", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        placeholder = { Text(stringResource(R.string.add_voucher_merchant_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showMerchantError && merchantName.isBlank(),
                        shape = RoundedCornerShape(14.dp),
                        supportingText = {
                            if (showMerchantError && merchantName.isBlank()) {
                                Text(
                                    text = stringResource(R.string.add_voucher_merchant_required),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    
                    // Amount (optional)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.add_voucher_amount_label)) },
                        placeholder = { Text("100 ₪") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        supportingText = {
                            Text(
                                text = stringResource(R.string.add_voucher_amount_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    HorizontalDivider()
                    
                    // URL
                    OutlinedTextField(
                        value = voucherUrl,
                        onValueChange = { 
                            voucherUrl = it
                            showAccessPointError = false
                        },
                        label = { Text(stringResource(R.string.add_voucher_url_label)) },
                        placeholder = { Text("https://...") },
                        leadingIcon = {
                            Text("🔗", modifier = Modifier.padding(start = 8.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    
                    // OR divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "OR",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                    
                    // Redeem code
                    OutlinedTextField(
                        value = redeemCode,
                        onValueChange = { 
                            redeemCode = it
                            showAccessPointError = false
                        },
                        label = { Text(stringResource(R.string.add_voucher_code_label)) },
                        placeholder = { Text("ABC123XYZ") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank(),
                        shape = RoundedCornerShape(14.dp),
                        supportingText = {
                            if (showAccessPointError && voucherUrl.isBlank() && redeemCode.isBlank()) {
                                Text(
                                    text = stringResource(R.string.add_voucher_access_point_required),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.add_voucher_code_hint),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save button with gradient style
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.add_voucher_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
