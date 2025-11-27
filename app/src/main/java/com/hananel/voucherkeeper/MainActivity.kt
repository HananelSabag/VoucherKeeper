package com.hananel.voucherkeeper

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.hananel.voucherkeeper.util.LocaleHelper
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.hananel.voucherkeeper.ui.viewmodel.PendingReviewViewModel
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
    
    override fun attachBaseContext(newBase: Context) {
        // Apply saved language preference from SharedPreferences (used early in lifecycle)
        val sharedPrefs = newBase.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language", "auto") ?: "auto"
        val context = LocaleHelper.applyLanguage(newBase, languageCode)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge with proper insets handling
        WindowCompat.setDecorFitsSystemWindows(window, true)
        
        // Get navigation target from notification intent
        val navigateTo = intent?.getStringExtra("navigate_to")
        
        setContent {
            val theme by preferencesManager.themeFlow.collectAsState(initial = "system")
            
            VoucherKeeperTheme(
                themeSetting = theme
            ) {
                VoucherKeeperAppWithOnboarding(preferencesManager, navigateTo)
            }
        }
    }
}

@Composable
fun VoucherKeeperAppWithOnboarding(
    preferencesManager: PreferencesManager,
    initialRoute: String? = null
) {
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
                onShowHelp = { showHelpDialog = true },
                initialRoute = initialRoute
            )
        }
    }
}

@Composable
fun VoucherKeeperApp(
    onShowHelp: () -> Unit = {},
    initialRoute: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Get pending count for badge
    val pendingViewModel: PendingReviewViewModel = hiltViewModel()
    val pendingVouchers by pendingViewModel.pendingVouchers.collectAsState()
    val pendingCount = pendingVouchers.size
    
    // Navigate to initial route from notification
    LaunchedEffect(initialRoute) {
        initialRoute?.let { route ->
            when (route) {
                "pending" -> navController.navigate(Screen.Pending.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                "approved" -> navController.navigate(Screen.Approved.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
    
    val navigateToSettings = {
        navController.navigate(Screen.Settings.route) {
            launchSingleTop = true
        }
    }
    
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Hide bottom bar on Settings screen
            if (currentDestination?.route != Screen.Settings.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                // Pending Review (Left) - Orange/Warning color
                NavigationBarItem(
                    icon = { 
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = if (pendingCount > 99) "99+" else pendingCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Warning, 
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    label = { 
                        Text(
                            stringResource(R.string.nav_pending),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (currentDestination?.hierarchy?.any { it.route == Screen.Pending.route } == true) 
                                androidx.compose.ui.text.font.FontWeight.Bold 
                            else 
                                androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Pending.route } == true,
                    onClick = {
                        navController.navigate(Screen.Pending.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.tertiary,
                        selectedTextColor = MaterialTheme.colorScheme.tertiary,
                        indicatorColor = MaterialTheme.colorScheme.tertiaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
                
                // Approved Vouchers (Center - Default) - Green/Success color
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.CheckCircle, 
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { 
                        Text(
                            stringResource(R.string.nav_approved),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (currentDestination?.hierarchy?.any { it.route == Screen.Approved.route } == true) 
                                androidx.compose.ui.text.font.FontWeight.Bold 
                            else 
                                androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Approved.route } == true,
                    onClick = {
                        navController.navigate(Screen.Approved.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
                
                // Approved Senders (Right) - Blue/Primary color
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.AccountCircle, 
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { 
                        Text(
                            stringResource(R.string.nav_approved_senders),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (currentDestination?.hierarchy?.any { it.route == Screen.ApprovedSenders.route } == true) 
                                androidx.compose.ui.text.font.FontWeight.Bold 
                            else 
                                androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.ApprovedSenders.route } == true,
                    onClick = {
                        navController.navigate(Screen.ApprovedSenders.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
                }
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
                    onBack = { navController.popBackStack() },
                    onNavigateToApprovedSenders = {
                        navController.navigate(Screen.ApprovedSenders.route) {
                            launchSingleTop = true
                        }
                    }
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