package com.sacredpath.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Bible",   Routes.HOME,         Icons.Filled.MenuBook,   Icons.Outlined.MenuBook),
    BottomNavItem("Study",   Routes.STUDY_HOME,   Icons.Filled.Search,     Icons.Outlined.Search),
    BottomNavItem("Quizzes", Routes.QUIZ_HOME,    Icons.Filled.Psychology, Icons.Outlined.Psychology),
    BottomNavItem("Prayer",  Routes.PRAYER_HOME,  Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
    BottomNavItem("Profile", Routes.PROFILE_HOME, Icons.Filled.Person,     Icons.Outlined.Person),
)

@Composable
fun SacredBottomBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop  = true
                            restoreState     = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
