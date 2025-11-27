package com.hananel.voucherkeeper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                    Text(
                        text = stringResource(R.string.approved_senders_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
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
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.approved_senders_add)
                )
            }
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
                        onDelete = { viewModel.removeApprovedSender(it.phone) }
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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun SenderCard(
    sender: ApprovedSenderEntity,
    onDelete: (ApprovedSenderEntity) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
            
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
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
}

@Composable
private fun AddSenderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.approved_senders_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

