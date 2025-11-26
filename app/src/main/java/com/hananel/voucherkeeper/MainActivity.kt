package com.hananel.voucherkeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hananel.voucherkeeper.data.preferences.PreferencesManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.hananel.voucherkeeper.ui.navigation.Screen
import com.hananel.voucherkeeper.ui.screen.AddVoucherScreen
import com.hananel.voucherkeeper.ui.screen.ApprovedSendersScreen
import com.hananel.voucherkeeper.ui.screen.ApprovedVouchersScreen
import com.hananel.voucherkeeper.ui.screen.OnboardingScreen
import com.hananel.voucherkeeper.ui.screen.PendingReviewScreen
import com.hananel.voucherkeeper.ui.screen.SettingsScreen
import com.hananel.voucherkeeper.ui.viewmodel.ApprovedVouchersViewModel
import com.hananel.voucherkeeper.ui.theme.VoucherKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity for Voucher Keeper.
 * Sets up navigation and theme.
 * 
 * @author Hananel Sabag
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val theme by preferencesManager.themeFlow.collectAsState(initial = "system")
            
            VoucherKeeperTheme(
                themeSetting = theme
            ) {
                VoucherKeeperAppWithOnboarding(preferencesManager)
            }
        }
    }
}

@Composable
fun VoucherKeeperAppWithOnboarding(preferencesManager: PreferencesManager) {
    val scope = rememberCoroutineScope()
    var showOnboarding by remember { mutableStateOf<Boolean?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    
    // Check onboarding status
    LaunchedEffect(Unit) {
        showOnboarding = !preferencesManager.onboardingShownFlow.first()
    }
    
    when {
        showOnboarding == null -> {
            // Loading state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        showOnboarding == true || showHelpDialog -> {
            OnboardingScreen(
                onComplete = {
                    scope.launch {
                        if (showOnboarding == true) {
                            preferencesManager.setOnboardingShown(true)
                        }
                        showOnboarding = false
                        showHelpDialog = false
                    }
                },
                showCloseButton = showHelpDialog // X button only when opened from help
            )
        }
        else -> {
            VoucherKeeperApp(
                onShowHelp = { showHelpDialog = true }
            )
        }
    }
}

@Composable
fun VoucherKeeperApp(onShowHelp: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val navigateToSettings = {
        navController.navigate(Screen.Settings.route) {
            launchSingleTop = true
        }
    }
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                // Pending Review (Left)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_pending)) },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Pending.route } == true,
                    onClick = {
                        navController.navigate(Screen.Pending.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                
                // Approved Vouchers (Center - Default)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_approved)) },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Approved.route } == true,
                    onClick = {
                        navController.navigate(Screen.Approved.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                
                // Approved Senders (Right)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_approved_senders)) },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.ApprovedSenders.route } == true,
                    onClick = {
                        navController.navigate(Screen.ApprovedSenders.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Approved.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Approved.route) {
                ApprovedVouchersScreen(
                    onAddVoucher = {
                        navController.navigate(Screen.AddVoucher.route)
                    },
                    onShowHelp = onShowHelp,
                    onNavigateToSettings = navigateToSettings
                )
            }
            
            composable(Screen.Pending.route) {
                PendingReviewScreen(
                    onShowHelp = onShowHelp,
                    onNavigateToSettings = navigateToSettings
                )
            }
            
            composable(Screen.ApprovedSenders.route) {
                ApprovedSendersScreen(
                    onShowHelp = onShowHelp,
                    onNavigateToSettings = navigateToSettings
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onShowHelp = onShowHelp,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.AddVoucher.route) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.Approved.route)
                }
                val viewModel: ApprovedVouchersViewModel = hiltViewModel(parentEntry)
                
                AddVoucherScreen(
                    onSave = { merchantName, amount, url, code, phone ->
                        viewModel.addVoucherManually(merchantName, amount, url, code, phone)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}