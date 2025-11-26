package com.hananel.voucherkeeper.ui.navigation

/**
 * Sealed class representing app navigation destinations.
 */
sealed class Screen(val route: String) {
    data object Approved : Screen("approved")
    data object Pending : Screen("pending")
    data object ApprovedSenders : Screen("approved_senders")
    data object Settings : Screen("settings")
    data object AddVoucher : Screen("add_voucher")
}

