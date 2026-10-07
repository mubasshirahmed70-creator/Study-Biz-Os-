package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "হোম", Icons.Filled.Home, Icons.Outlined.Home)
    object Study : Screen("study", "পড়াশোনা", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
    object Business : Screen("business", "ড্রপশিপিং", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag)
    object Planner : Screen("planner", "রুটিন", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Progress : Screen("progress", "অগ্রগতি", Icons.Filled.Analytics, Icons.Outlined.Analytics)

    object FocusTimer : Screen("focus_timer", "ফোকাস", Icons.Filled.Timer, Icons.Outlined.Timer)
    object AIAssistant : Screen("ai_assistant", "ভয়েস এআই", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Settings : Screen("settings", "সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Study, Business, Planner, Progress)
    }
}
