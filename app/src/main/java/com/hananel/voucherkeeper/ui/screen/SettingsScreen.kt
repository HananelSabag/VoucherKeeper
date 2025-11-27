package com.hananel.voucherkeeper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hananel.voucherkeeper.BuildConfig
import com.hananel.voucherkeeper.R
import com.hananel.voucherkeeper.ui.viewmodel.SettingsViewModel

/**
 * Settings screen - app preferences and configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onShowHelp: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateToApprovedSenders: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()
    var pendingLanguageChange by remember { mutableStateOf(false) }
    val notifyApproved by viewModel.notifyApproved.collectAsState()
    val notifyPending by viewModel.notifyPending.collectAsState()
    val strictMode by viewModel.strictMode.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShowHelp) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.cd_help_button)
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
            // Appearance Section
            Text(
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            SettingsDropdown(
                label = stringResource(R.string.settings_theme),
                value = theme,
                options = mapOf(
                    "system" to stringResource(R.string.theme_system),
                    "light" to stringResource(R.string.theme_light),
                    "dark" to stringResource(R.string.theme_dark)
                ),
                onValueChange = { 
                    viewModel.setTheme(it)
                }
            )
            
            SettingsDropdown(
                label = stringResource(R.string.settings_language),
                value = language,
                options = mapOf(
                    "auto" to stringResource(R.string.language_auto),
                    "en" to stringResource(R.string.language_english),
                    "he" to stringResource(R.string.language_hebrew)
                ),
                onValueChange = { 
                    viewModel.setLanguage(it)
                    pendingLanguageChange = true
                }
            )
            
            // Handle language change with proper timing
            LaunchedEffect(pendingLanguageChange) {
                if (pendingLanguageChange) {
                    kotlinx.coroutines.delay(300) // Wait for dropdown animation
                    (context as? android.app.Activity)?.recreate()
                    pendingLanguageChange = false
                }
            }
            
            HorizontalDivider()
            
            // Notifications Section
            Text(
                text = stringResource(R.string.settings_notifications),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            SettingsSwitch(
                label = stringResource(R.string.settings_notify_approved),
                checked = notifyApproved,
                onCheckedChange = { viewModel.setNotifyApproved(it) }
            )
            
            SettingsSwitch(
                label = stringResource(R.string.settings_notify_pending),
                checked = notifyPending,
                onCheckedChange = { viewModel.setNotifyPending(it) }
            )
            
            HorizontalDivider()
            
            // Filters Section
            Text(
                text = stringResource(R.string.settings_filters),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            SettingsSwitchWithDescription(
                label = stringResource(R.string.settings_strict_mode),
                description = stringResource(R.string.settings_strict_mode_desc),
                checked = strictMode,
                onCheckedChange = { viewModel.setStrictMode(it) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Button to manage approved senders
            OutlinedButton(
                onClick = onNavigateToApprovedSenders,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp) // Rounded corners!
            ) {
                Text(stringResource(R.string.settings_approved_senders))
            }
            
            HorizontalDivider()
            
            // About Section
            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = stringResource(R.string.settings_author),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    label: String,
    value: String,
    options: Map<String, String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = options[value] ?: value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), // Rounded corners!
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        expanded = false
                        onValueChange(key)
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsSwitchWithDescription(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

