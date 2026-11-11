package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtharThemeMode
import com.example.overlay.DevicePermissionHelper
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AllContentScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HadithScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.QuranPagerScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharTheme
import com.example.ui.viewmodel.AtharDestination
import com.example.ui.viewmodel.AtharViewModel
import androidx.compose.ui.platform.LocalContext

data class BottomNavItem(
    val destination: AtharDestination,
    val icon: ImageVector,
    val label: String
)

@Composable
fun AtharMainContainer(
    viewModel: AtharViewModel
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AtharThemeMode.SYSTEM -> isSystemDark
        AtharThemeMode.LIGHT -> false
        AtharThemeMode.DARK -> true
    }

    val currentDestination by viewModel.currentDestination.collectAsState()

    // Full RTL support guaranteed
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AtharTheme(darkTheme = isDark) {
            val navItems = listOf(
                BottomNavItem(AtharDestination.Home, Icons.Filled.Home, "الرئيسية"),
                BottomNavItem(AtharDestination.Quran, Icons.AutoMirrored.Filled.MenuBook, "القرآن"),
                BottomNavItem(AtharDestination.Hadith, Icons.Filled.FormatQuote, "الحديث"),
                BottomNavItem(AtharDestination.Adhkar, Icons.Filled.VolunteerActivism, "الأذكار"),
                BottomNavItem(AtharDestination.AllContent, Icons.Filled.AutoAwesome, "كل المحتوى"),
                BottomNavItem(AtharDestination.Favorites, Icons.Filled.Bookmark, "المفضلة"),
                BottomNavItem(AtharDestination.Settings, Icons.Filled.Settings, "الإعدادات")
            )

            // Android Back button handling
            BackHandler(enabled = currentDestination != AtharDestination.Home && currentDestination != AtharDestination.Splash && currentDestination != AtharDestination.Permissions) {
                viewModel.navigateTo(AtharDestination.Home)
            }

            if (currentDestination == AtharDestination.Splash) {
                SplashScreen(
                    onStartClick = {
                        val needsPermissions = !DevicePermissionHelper.canDrawOverlays(context)
                        if (needsPermissions) {
                            viewModel.navigateTo(AtharDestination.Permissions)
                        } else {
                            viewModel.navigateTo(AtharDestination.Home)
                        }
                    }
                )
            } else if (currentDestination == AtharDestination.Permissions) {
                PermissionsScreen(
                    onContinueClick = {
                        viewModel.prefs.setHasCompletedPermissionsOnboarding(true)
                        viewModel.navigateTo(AtharDestination.Home)
                    }
                )
            } else {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            modifier = Modifier.testTag("athar_bottom_navigation")
                        ) {
                            navItems.forEach { item ->
                                val selected = currentDestination == item.destination
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(item.destination) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            fontSize = 10.5.sp,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AtharDeepGreen,
                                        selectedTextColor = AtharGold,
                                        indicatorColor = AtharGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.testTag("nav_item_${item.destination.route}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentDestination, label = "screen_crossfade") { dest ->
                            when (dest) {
                                AtharDestination.Splash -> {
                                    SplashScreen(onStartClick = { viewModel.navigateTo(AtharDestination.Home) })
                                }
                                AtharDestination.Home -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                                AtharDestination.Quran -> {
                                    QuranPagerScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { viewModel.navigateTo(AtharDestination.Home) }
                                    )
                                }
                                AtharDestination.Hadith -> {
                                    HadithScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { viewModel.navigateTo(AtharDestination.Home) }
                                    )
                                }
                                AtharDestination.Adhkar -> {
                                    AdhkarScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { viewModel.navigateTo(AtharDestination.Home) }
                                    )
                                }
                                AtharDestination.AllContent -> {
                                    AllContentScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { viewModel.navigateTo(AtharDestination.Home) }
                                    )
                                }
                                AtharDestination.Favorites -> {
                                    FavoritesScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { viewModel.navigateTo(AtharDestination.Home) }
                                    )
                                }
                                AtharDestination.Settings -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { viewModel.navigateTo(AtharDestination.Home) },
                                        onNavigateToPermissions = { viewModel.navigateTo(AtharDestination.Permissions) }
                                    )
                                }
                                AtharDestination.Permissions -> {
                                    PermissionsScreen(
                                        onContinueClick = {
                                            viewModel.prefs.setHasCompletedPermissionsOnboarding(true)
                                            viewModel.navigateTo(AtharDestination.Home)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
