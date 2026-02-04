package com.example.carsparesmarketplaceapp.ui.screens.navigation



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Cart : BottomNavItem("cart", Icons.Default.ShoppingCart, "Cart")
    object Orders : BottomNavItem("orders", Icons.Default.List, "Orders")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}
