package com.example.carsparesmarketplaceapp.ui.screens.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController, buyerId: String) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Cart,
        BottomNavItem.Orders,
        BottomNavItem.Profile
    )

    val bmwMGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF009ADA), // Light Blue
            Color(0xFF132347), // Dark Blue
            Color(0xFFC8102E)  // Red
        )
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(brush = bmwMGradient),
        containerColor = Color.Transparent
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title, tint = Color.White) },
                label = { Text(item.title, color = Color.White) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        val route = if (item == BottomNavItem.Profile) {
                            "${BottomNavItem.Profile.route}/$buyerId"
                        } else {
                            item.route
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }

                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFFEEEEEE),
                    unselectedTextColor = Color(0xFFDDDDDD),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
