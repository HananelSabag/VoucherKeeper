package com.hananel.voucherkeeper.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.ui.viewmodel.SettingsViewModel
import com.hananel.voucherkeeper.util.PermissionHandler

/**
 * Onboarding screen shown on first app launch.
 * Explains features and requests necessary permissions.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    showCloseButton: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }
    var hasSmsPermission by remember { mutableStateOf(PermissionHandler.hasSmsPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(PermissionHandler.hasNotificationPermission(context)) }
    var strictModeEnabled by remember { mutableStateOf(false) }
    var showPermissionsWarning by remember { mutableStateOf(false) }
    var showStrictModeWarning by remember { mutableStateOf(false) }
    
    // SMS permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions[Manifest.permission.READ_SMS] == true && 
                          permissions[Manifest.permission.RECEIVE_SMS] == true
    }
    
    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }
    
    val requestPermissions: () -> Unit = {
        if (!hasSmsPermission) {
            smsPermissionLauncher.launch(PermissionHandler.getSmsPermissions())
        }
        PermissionHandler.getNotificationPermission()?.let { permission ->
            if (!hasNotificationPermission) {
                notificationPermissionLauncher.launch(permission)
            }
        }
        Unit
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Close button (only when opened from help)
            if (showCloseButton) {
                IconButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .zIndex(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = if (showCloseButton) 64.dp else 32.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentPage) {
                        0 -> WelcomePage(
                            hasSmsPermission = hasSmsPermission,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestSms = {
                                if (!hasSmsPermission) {
                                    smsPermissionLauncher.launch(PermissionHandler.getSmsPermissions())
                                }
                            },
                            onRequestNotifications = {
                                PermissionHandler.getNotificationPermission()?.let { permission ->
                                    if (!hasNotificationPermission) {
                                        notificationPermissionLauncher.launch(permission)
                                    }
                                }
                            }
                        )
                        else -> PermissionsAndSendersPage(
                            hasSmsPermission = hasSmsPermission,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestSms = {
                                if (!hasSmsPermission) {
                                    smsPermissionLauncher.launch(PermissionHandler.getSmsPermissions())
                                }
                            },
                            onRequestNotifications = {
                                PermissionHandler.getNotificationPermission()?.let { permission ->
                                    if (!hasNotificationPermission) {
                                        notificationPermissionLauncher.launch(permission)
                                    }
                                }
                            },
                            strictModeEnabled = strictModeEnabled,
                            onStrictModeChange = { strictModeEnabled = it },
                            onAddSender = { phone, name ->
                                viewModel.addApprovedSender(phone, name)
                            }
                        )
                    }
                }
                
                // Bottom section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Page indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(2) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentPage) 12.dp else 8.dp)
                                    .background(
                                        color = if (index == currentPage) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    
                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentPage > 0) {
                            OutlinedButton(
                                onClick = { currentPage-- },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.cd_back_button))
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (currentPage < 1) {
                                    // Check permissions before moving to next page
                                    if (!hasSmsPermission || !hasNotificationPermission) {
                                        showPermissionsWarning = true
                                    } else {
                                        currentPage++
                                    }
                                } else {
                                    // Final page - check strict mode
                                    if (!strictModeEnabled) {
                                        showStrictModeWarning = true
                                    } else {
                                        // Save strict mode and complete
                                        viewModel.setStrictMode(true)
                                        onComplete()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (currentPage < 1) 
                                    stringResource(R.string.onboarding_next) 
                                else 
                                    stringResource(R.string.onboarding_done)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Permissions Warning Dialog
    if (showPermissionsWarning) {
        AlertDialog(
            onDismissRequest = { showPermissionsWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.onboarding_permissions_warning_title)) },
            text = { Text(stringResource(R.string.onboarding_permissions_warning_message)) },
            confirmButton = {
                TextButton(onClick = { showPermissionsWarning = false }) {
                    Text(stringResource(R.string.onboarding_understood))
                }
            }
        )
    }
    
    // Strict Mode Warning Dialog
    if (showStrictModeWarning) {
        AlertDialog(
            onDismissRequest = { showStrictModeWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.onboarding_strict_mode_warning_title)) },
            text = { Text(stringResource(R.string.onboarding_strict_mode_warning_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStrictModeWarning = false
                        // Complete with strict mode OFF
                        onComplete()
                    }
                ) {
                    Text(stringResource(R.string.onboarding_understood))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStrictModeWarning = false }) {
                    Text(stringResource(R.string.onboarding_go_back))
                }
            }
        )
    }
}

@Composable
private fun WelcomePage(
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestSms: () -> Unit,
    onRequestNotifications: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.onboarding_welcome_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // How it works
            Text(
                text = stringResource(R.string.onboarding_how_it_works),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FeatureCard(
                title = stringResource(R.string.onboarding_auto_title),
                description = stringResource(R.string.onboarding_auto_desc)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            FeatureCard(
                title = stringResource(R.string.onboarding_manual_title),
                description = stringResource(R.string.onboarding_manual_desc)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Privacy
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_privacy_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.onboarding_privacy_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Permissions Section
            Text(
                text = stringResource(R.string.onboarding_permissions_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // SMS Permission
            PermissionItemWithButton(
                text = stringResource(R.string.onboarding_permissions_sms),
                granted = hasSmsPermission,
                buttonText = stringResource(R.string.onboarding_grant_sms),
                onGrantClick = onRequestSms
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Notification Permission
            PermissionItemWithButton(
                text = stringResource(R.string.onboarding_permissions_notif),
                granted = hasNotificationPermission,
                buttonText = stringResource(R.string.onboarding_grant_notifications),
                onGrantClick = onRequestNotifications
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Created by
        Text(
            text = stringResource(R.string.onboarding_created_by),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun PermissionsAndSendersPage(
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestSms: () -> Unit,
    onRequestNotifications: () -> Unit,
    strictModeEnabled: Boolean,
    onStrictModeChange: (Boolean) -> Unit,
    onAddSender: (String, String?) -> Unit
) {
    var showAddSenderDialog by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Approved Senders Section
        Text(
            text = stringResource(R.string.onboarding_approved_senders_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.onboarding_approved_senders_subtitle),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.onboarding_approved_senders_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Add Sender Button
        OutlinedButton(
            onClick = { showAddSenderDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.onboarding_add_sender))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        HorizontalDivider()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Strict Mode Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (strictModeEnabled) 
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_strict_mode_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_strict_mode_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_strict_mode_enable),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = strictModeEnabled,
                        onCheckedChange = onStrictModeChange
                    )
                }
                
                if (!strictModeEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "✓ " + stringResource(R.string.onboarding_strict_mode_recommendation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
    
    // Add Sender Dialog (functional for onboarding)
    if (showAddSenderDialog) {
        AddSenderOnboardingDialog(
            onDismiss = { showAddSenderDialog = false },
            onAdd = { phone, name ->
                onAddSender(phone, name)
                showAddSenderDialog = false
            }
        )
    }
}

@Composable
private fun FeatureCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PermissionItemWithButton(
    text: String, 
    granted: Boolean,
    buttonText: String,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (granted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
            
            if (!granted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
private fun AddSenderOnboardingDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.onboarding_add_sender)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_approved_senders_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.approved_senders_phone_hint) + " *") },
                    placeholder = { Text(stringResource(R.string.approved_senders_phone_placeholder)) },
                    isError = showError && phone.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.approved_senders_name_hint)) },
                    placeholder = { Text(stringResource(R.string.approved_senders_name_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = stringResource(R.string.approved_senders_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (phone.isBlank()) {
                        showError = true
                    } else {
                        onAdd(phone.trim(), name.trim().takeIf { it.isNotBlank() })
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

