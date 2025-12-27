package com.hananel.voucherkeeper.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.data.local.entity.ApprovedSenderEntity
import com.hananel.voucherkeeper.ui.viewmodel.SettingsViewModel

/**
 * Approved Senders screen - manage trusted senders list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovedSendersScreen(
    onShowHelp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val approvedSenders by viewModel.approvedSenders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
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
                            text = stringResource(R.string.approved_senders_title),
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
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.action_add_approved_sender))
                }
            )
        }
    ) { paddingValues ->
        if (approvedSenders.isEmpty()) {
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
                    items = approvedSenders,
                    key = { it.phone }
                ) { sender ->
                    SenderCard(
                        sender = sender,
                        onDelete = { viewModel.removeApprovedSender(it.phone) },
                        onEdit = { updatedSender -> 
                            viewModel.updateApprovedSender(updatedSender) 
                        }
                    )
                }
            }
        }
    }
    
    // Add sender dialog
    if (showAddDialog) {
        AddSenderDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { phone, name ->
                viewModel.addApprovedSender(phone, name)
                showAddDialog = false
            }
        )
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
            text = "👥",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.approved_senders_empty),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.approved_senders_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SenderCard(
    sender: ApprovedSenderEntity,
    onDelete: (ApprovedSenderEntity) -> Unit,
    onEdit: (ApprovedSenderEntity) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (sender.name != null) {
                    Text(
                        text = sender.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = sender.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = sender.phone,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Row {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Delete confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.approved_senders_delete_title)) },
            text = { Text(stringResource(R.string.approved_senders_delete_message, sender.name ?: sender.phone)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(sender)
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
    
    // Edit dialog
    if (showEditDialog) {
        EditSenderDialog(
            sender = sender,
            onDismiss = { showEditDialog = false },
            onEdit = { updatedSender ->
                onEdit(updatedSender)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSenderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit
) {
    var phonePrefix by remember { mutableStateOf("+972") }
    var phoneNumber by remember { mutableStateOf("") }
    var systemName by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var expandedPrefixMenu by remember { mutableStateOf(false) }
    
    val prefixes = listOf(
        "+972" to "🇮🇱 Israel",
        "+1" to "🇺🇸 USA",
        "+44" to "🇬🇧 UK",
        "+33" to "🇫🇷 France",
        "+49" to "🇩🇪 Germany"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.approved_senders_add),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Compact hint - just one line
                Text(
                    text = "💡 " + stringResource(R.string.approved_sender_explanation_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                
                // Phone OR System Name - clearer layout
                Text(
                    text = "📱 " + stringResource(R.string.sender_type_phone),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = if (systemName.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPrefixMenu,
                        onExpandedChange = { 
                            if (systemName.isBlank()) expandedPrefixMenu = it 
                        },
                        modifier = Modifier.width(110.dp)
                    ) {
                        OutlinedTextField(
                            value = phonePrefix,
                            onValueChange = {},
                            readOnly = true,
                            enabled = systemName.isBlank(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPrefixMenu) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPrefixMenu,
                            onDismissRequest = { expandedPrefixMenu = false }
                        ) {
                            prefixes.forEach { (prefix, label) ->
                                DropdownMenuItem(
                                    text = { Text("$prefix $label") },
                                    onClick = {
                                        phonePrefix = prefix
                                        expandedPrefixMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Force LTR for phone number field
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                                // Handle pasted Israeli number starting with 0
                                val cleaned = input.filter { it.isDigit() }
                                phoneNumber = if (cleaned.startsWith("0") && cleaned.length >= 9) {
                                    // Convert 0549999999 (mobile) or 026780270 (landline) to without 0
                                    cleaned.substring(1)
                                } else {
                                    cleaned
                                }
                            showError = false
                        },
                            placeholder = { 
                                Text(
                                    "050-999-9999, 02-999-9999",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                )
                            },
                        enabled = systemName.isBlank(),
                        isError = showError && phoneNumber.isBlank() && systemName.isBlank(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    }
                }
                
                // OR divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                
                // System Name
                Text(
                    text = "🏢 " + stringResource(R.string.sender_type_system),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = if (phoneNumber.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = systemName,
                    onValueChange = { 
                        systemName = it
                        showError = false
                    },
                    placeholder = { Text("Cibus, Shufersal") },
                    enabled = phoneNumber.isBlank(),
                    isError = showError && phoneNumber.isBlank() && systemName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider()
                
                // Display Name (optional)
                Text(
                    text = "👤 " + stringResource(R.string.sender_display_name) + " (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholder = { Text(stringResource(R.string.display_name_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showError) {
                    Text(
                        text = "⚠️ Enter phone number OR system name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hasPhone = phoneNumber.isNotBlank()
                    val hasSystemName = systemName.isNotBlank()
                    
                    if (!hasPhone && !hasSystemName) {
                        showError = true
                    } else {
                        val identifier = if (hasPhone) {
                            "$phonePrefix$phoneNumber"
                        } else {
                            systemName.trim()
                        }
                        
                        val finalDisplayName = displayName.trim().takeIf { it.isNotBlank() }
                        onAdd(identifier, finalDisplayName)
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(horizontal = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSenderDialog(
    sender: ApprovedSenderEntity,
    onDismiss: () -> Unit,
    onEdit: (ApprovedSenderEntity) -> Unit
) {
    // Detect if existing is phone number (starts with +) or system name
    val isPhoneNumber = sender.phone.startsWith("+")
    
    var phonePrefix by remember { 
        mutableStateOf(
            if (isPhoneNumber) {
                sender.phone.substring(0, sender.phone.indexOfFirst { it.isDigit() } + 1)
            } else {
                "+972"
            }
        )
    }
    var phoneNumber by remember { 
        mutableStateOf(
            if (isPhoneNumber) {
                sender.phone.substring(phonePrefix.length)
            } else {
                ""
            }
        )
    }
    var systemName by remember { mutableStateOf(if (!isPhoneNumber) sender.phone else "") }
    var displayName by remember { mutableStateOf(sender.name ?: "") }
    var showError by remember { mutableStateOf(false) }
    var expandedPrefixMenu by remember { mutableStateOf(false) }
    
    val prefixes = listOf(
        "+972" to "🇮🇱 Israel",
        "+1" to "🇺🇸 USA",
        "+44" to "🇬🇧 UK",
        "+33" to "🇫🇷 France",
        "+49" to "🇩🇪 Germany"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.approved_senders_edit_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Phone OR System Name - clearer layout
                Text(
                    text = "📱 " + stringResource(R.string.sender_type_phone),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = if (systemName.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPrefixMenu,
                        onExpandedChange = { 
                            if (systemName.isBlank()) expandedPrefixMenu = it 
                        },
                        modifier = Modifier.width(110.dp)
                    ) {
                        OutlinedTextField(
                            value = phonePrefix,
                            onValueChange = {},
                            readOnly = true,
                            enabled = systemName.isBlank(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPrefixMenu) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPrefixMenu,
                            onDismissRequest = { expandedPrefixMenu = false }
                        ) {
                            prefixes.forEach { (prefix, label) ->
                                DropdownMenuItem(
                                    text = { Text("$prefix $label") },
                                    onClick = {
                                        phonePrefix = prefix
                                        expandedPrefixMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Force LTR for phone number field
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                                // Handle pasted Israeli number starting with 0
                                val cleaned = input.filter { it.isDigit() }
                                phoneNumber = if (cleaned.startsWith("0") && cleaned.length >= 9) {
                                    // Convert 0549999999 (mobile) or 026780270 (landline) to without 0
                                    cleaned.substring(1)
                                } else {
                                    cleaned
                                }
                            showError = false
                        },
                            placeholder = { 
                                Text(
                                    "050-999-9999, 02-999-9999",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                )
                            },
                        enabled = systemName.isBlank(),
                        isError = showError && phoneNumber.isBlank() && systemName.isBlank(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    }
                }
                
                // OR divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                
                // System Name
                Text(
                    text = "🏢 " + stringResource(R.string.sender_type_system),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = if (phoneNumber.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = systemName,
                    onValueChange = { 
                        systemName = it
                        showError = false
                    },
                    placeholder = { Text("Cibus, Shufersal") },
                    enabled = phoneNumber.isBlank(),
                    isError = showError && phoneNumber.isBlank() && systemName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider()
                
                // Display Name (optional)
                Text(
                    text = "👤 " + stringResource(R.string.sender_display_name) + " (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholder = { Text(stringResource(R.string.display_name_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showError) {
                    Text(
                        text = "⚠️ Enter phone number OR system name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hasPhone = phoneNumber.isNotBlank()
                    val hasSystemName = systemName.isNotBlank()
                    
                    if (!hasPhone && !hasSystemName) {
                        showError = true
                    } else {
                        val identifier = if (hasPhone) {
                            "$phonePrefix$phoneNumber"
                        } else {
                            systemName.trim()
                        }
                        
                        val updatedSender = sender.copy(
                            phone = identifier,
                            name = displayName.trim().takeIf { it.isNotBlank() }
                        )
                        onEdit(updatedSender)
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
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(horizontal = 16.dp)
    )
}

