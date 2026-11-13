package com.aman.shiftmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aman.shiftmate.ui.theme.ShiftMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShiftMateTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Navigation graph: Home -> History -> Shift detail
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(onOpenHistory = { navController.navigate("history") })
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
                    }
                }
            }
        }
    }
}