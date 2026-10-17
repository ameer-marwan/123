package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AlertsHubDialog
import com.example.ui.components.SproutIntroCinematic
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalizationProvider
import com.example.ui.screens.CropRotationScreen
import com.example.ui.screens.ExecutiveReportScreen
import com.example.ui.screens.FinancialScreen
import com.example.ui.screens.GeminiAgronomistScreen
import com.example.ui.screens.MapTelemetryScreen
import com.example.ui.screens.SoilStudioScreen
import com.example.ui.screens.UserDashboardScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.FieldShiftTheme
import com.example.viewmodel.FieldShiftUiState
import com.example.viewmodel.FieldShiftViewModel

enum class MainTab(val titleKey: String, val icon: ImageVector) {
    MAP("tabMap", Icons.Default.Public),
    SOIL("tabSoil", Icons.Default.Yard),
    ROTATION("tabRotation", Icons.Default.Sync),
    FINANCE("tabFinance", Icons.Default.AttachMoney),
    MY_FARMS("tabFarms", Icons.Default.Agriculture),
    REPORT("tabReport", Icons.Default.Assessment),
    AI_ADVISOR("tabAiAdvisor", Icons.Default.AutoAwesome)
}

class MainActivity : ComponentActivity() {
    private val viewModel: FieldShiftViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            FieldShiftTheme(themeMode = uiState.activeTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    FieldShiftApp(viewModel = viewModel)

                    // Cinematic Sprout 3D Intro Animation overlay (Tap anywhere to skip)
                    AnimatedVisibility(
                        visible = uiState.showIntroAnimation,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        SproutIntroCinematic(
                            onFinished = { viewModel.dismissIntro() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Alerts Hub Dialog overlay
                    if (uiState.showAlertsHub) {
                        AlertsHubDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.setShowAlertsHub(false) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldShiftApp(viewModel: FieldShiftViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val strings = LocalizationProvider.getStrings(uiState.activeLanguage)

    var currentTab by remember { mutableStateOf(MainTab.MAP) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val unacknowledgedAlertsCount = alerts.count { !it.isAcknowledged }

    // Android back navigation handler
    BackHandler(enabled = currentTab != MainTab.MAP) {
        currentTab = MainTab.MAP
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTabletOrIpad = maxWidth >= 650.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.appTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = {
                        // Alerts Bell with Active Badge Counter
                        IconButton(
                            onClick = { viewModel.setShowAlertsHub(true) },
                            modifier = Modifier.testTag("alerts_bell_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unacknowledgedAlertsCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFFF5252),
                                            contentColor = Color.White
                                        ) {
                                            Text("$unacknowledgedAlertsCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    if (unacknowledgedAlertsCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "Alerts",
                                    tint = if (unacknowledgedAlertsCount > 0) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Replay Intro Button
                        IconButton(
                            onClick = { viewModel.replayIntro() },
                            modifier = Modifier.testTag("replay_intro_button")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = strings.replayIntro, tint = MaterialTheme.colorScheme.onSurface)
                        }

                        // Theme Switcher Button
                        Box {
                            IconButton(
                                onClick = { showThemeMenu = true },
                                modifier = Modifier.testTag("theme_switcher_button")
                            ) {
                                Icon(
                                    when (uiState.activeTheme) {
                                        AppThemeMode.AGRITECH_EMERALD -> Icons.Default.Eco
                                        AppThemeMode.DARK -> Icons.Default.DarkMode
                                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                                    },
                                    contentDescription = "Switch Theme",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🌿 Agritech Emerald (Signature)") },
                                    onClick = {
                                        viewModel.setTheme(AppThemeMode.AGRITECH_EMERALD)
                                        showThemeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🌙 OLED Slate Dark") },
                                    onClick = {
                                        viewModel.setTheme(AppThemeMode.DARK)
                                        showThemeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("☀️ Sunlight Clean Light") },
                                    onClick = {
                                        viewModel.setTheme(AppThemeMode.LIGHT)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }

                        // Language Selector Button
                        Box {
                            IconButton(
                                onClick = { showLanguageMenu = true },
                                modifier = Modifier.testTag("language_selector_button")
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.onSurface)
                            }

                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text("${lang.flag} ${lang.displayName}") },
                                        onClick = {
                                            viewModel.setLanguage(lang)
                                            showLanguageMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                // Only show bottom navigation on compact phone screens
                if (!isTabletOrIpad) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        MainTab.values().forEach { tab ->
                            val tabLabel = getTabLabel(tab, strings)
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tabLabel,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tabLabel,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (isTabletOrIpad) {
                // iPad / Tablet Canonical Layout: Left NavigationRail + Right Content Pane
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainTab.values().forEach { tab ->
                            val tabLabel = getTabLabel(tab, strings)
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(tab.icon, contentDescription = tabLabel, modifier = Modifier.size(22.dp))
                                },
                                label = {
                                    Text(tabLabel, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        TabContent(currentTab = currentTab, viewModel = viewModel, uiState = uiState) {
                            currentTab = MainTab.AI_ADVISOR
                        }
                    }
                }
            } else {
                // Mobile layout
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabContent(currentTab = currentTab, viewModel = viewModel, uiState = uiState) {
                        currentTab = MainTab.AI_ADVISOR
                    }
                }
            }
        }
    }
}

@Composable
private fun TabContent(
    currentTab: MainTab,
    viewModel: FieldShiftViewModel,
    uiState: FieldShiftUiState,
    onNavigateToChat: () -> Unit
) {
    when (currentTab) {
        MainTab.MAP -> MapTelemetryScreen(viewModel = viewModel, uiState = uiState)
        MainTab.SOIL -> SoilStudioScreen(
            viewModel = viewModel,
            uiState = uiState,
            onNavigateToChat = onNavigateToChat
        )
        MainTab.ROTATION -> CropRotationScreen(viewModel = viewModel, uiState = uiState)
        MainTab.FINANCE -> FinancialScreen(viewModel = viewModel, uiState = uiState)
        MainTab.MY_FARMS -> UserDashboardScreen(viewModel = viewModel, uiState = uiState)
        MainTab.REPORT -> ExecutiveReportScreen(uiState = uiState)
        MainTab.AI_ADVISOR -> GeminiAgronomistScreen(viewModel = viewModel, uiState = uiState)
    }
}

private fun getTabLabel(tab: MainTab, strings: com.example.ui.localization.LocalizedStrings): String {
    return when (tab) {
        MainTab.MAP -> strings.tabMap
        MainTab.SOIL -> strings.tabSoil
        MainTab.ROTATION -> strings.tabRotation
        MainTab.FINANCE -> strings.tabFinance
        MainTab.MY_FARMS -> strings.tabFarms
        MainTab.REPORT -> strings.tabReport
        MainTab.AI_ADVISOR -> strings.tabAiAdvisor
    }
}
