package com.aman.shiftmate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aman.shiftmate.ui.theme.ShiftMateTheme
import com.aman.shiftmate.work.ClockReminderWorker

/** One tab in the bottom bar. */
private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Clock", Icons.Default.Home),
    Tab("history", "History", Icons.AutoMirrored.Filled.List),
    Tab("summary", "Summary", Icons.Default.DateRange),
    Tab("profile", "Profile", Icons.Default.Person)
)

class MainActivity : ComponentActivity() {

    // Result of the system permission dialog. If denied, reminders are simply not shown.
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ClockReminderWorker.schedule(this)

        setContent {
            ShiftMateTheme {
                var showRationale by remember { mutableStateOf(needsNotificationPermission()) }

                // Explain why we ask, before the system dialog appears
                if (showRationale) {
                    AlertDialog(
                        onDismissRequest = { showRationale = false },
                        title = { Text("Clock-out reminders") },
                        text = {
                            Text("ShiftMate can remind you if you forget to clock out. This needs permission to show notifications.")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showRationale = false
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) { Text("Continue") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRationale = false }) { Text("Not now") }
                        }
                    )
                }

                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Only show the bar on the main tabs, not on the detail screen
                        if (tabs.any { it.route == currentRoute }) {
                            NavigationBar {
                                tabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentRoute == tab.route,
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen()
                        }
                        composable("history") {
                            HistoryScreen(
                                onShiftClick = { id -> navController.navigate("detail/$id") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        // The shift id is appended to the route and read in the ViewModel
                        composable(
                            route = "detail/{shiftId}",
                            arguments = listOf(navArgument("shiftId") { type = NavType.LongType })
                        ) {
                            ShiftDetailScreen(onBack = { navController.popBackStack() })
                        }
                        composable("summary") {
                            SummaryScreen()
                        }
                        composable("profile") {
                            ProfileScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}