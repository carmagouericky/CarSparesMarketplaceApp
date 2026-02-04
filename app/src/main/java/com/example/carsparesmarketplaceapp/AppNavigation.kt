package com.example.carsparesmarketplaceapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.carsparesmarketplaceapp.ui.screens.AddProductScreen
import com.example.carsparesmarketplaceapp.ui.screens.CartScreen
import com.example.carsparesmarketplaceapp.ui.screens.CheckoutScreen
import com.example.carsparesmarketplaceapp.ui.screens.HomeScreen
import com.example.carsparesmarketplaceapp.ui.screens.LoginScreen
import com.example.carsparesmarketplaceapp.ui.screens.MyOrdersScreen
import com.example.carsparesmarketplaceapp.ui.screens.PaymentScreen
import com.example.carsparesmarketplaceapp.ui.screens.ProductDetailScreen
import com.example.carsparesmarketplaceapp.ui.screens.ProfileScreen
import com.example.carsparesmarketplaceapp.ui.screens.RegisterScreen
import com.example.carsparesmarketplaceapp.ui.screens.SellerDashboardScreen
import com.example.carsparesmarketplaceapp.ui.screens.SellerOrdersScreen
import com.example.carsparesmarketplaceapp.ui.screens.navigation.BottomNavBar
import com.example.carsparesmarketplaceapp.ui.screens.navigation.BottomNavItem

@Composable
fun AppNavigation(repo: FirebaseRepo) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        /** LOGIN */
        composable("login") {
            LoginScreen(repo = repo, navController = navController) { buyerId ->
                navController.navigate("main/$buyerId") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        /** REGISTER */
        composable("register") {
            RegisterScreen(repo = repo, navController = navController)
        }

        /** MAIN SCREEN WITH BOTTOM NAVIGATION */
        composable("main/{buyerId}") { backStack ->
            val buyerId = backStack.arguments?.getString("buyerId") ?: ""
            MainScreen(repo = repo, buyerId = buyerId, rootNavController = navController)
        }

        /** PRODUCT DETAIL */
        composable("productDetail/{productId}/{buyerId}") { backStack ->
            val productId = backStack.arguments?.getString("productId") ?: ""
            val buyerId = backStack.arguments?.getString("buyerId") ?: ""

            ProductDetailScreen(
                repo = repo,
                productId = productId,
                buyerId = buyerId,
                onBack = { navController.popBackStack() }
            )
        }

        /** CART */
        composable("cart/{buyerId}") { backStack ->
            val buyerId = backStack.arguments?.getString("buyerId") ?: ""

            CartScreen(
                repo = repo,
                buyerId = buyerId,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        /** CHECKOUT */
        composable("checkout/{buyerId}") { backStack ->
            val buyerId = backStack.arguments?.getString("buyerId") ?: ""

            CheckoutScreen(
                repo = repo,
                buyerId = buyerId,
                buyerName = String(),
                onBack = { navController.popBackStack() },
                onProceedPayment = { amount ->
                    navController.navigate("payment/$buyerId/$amount")
                }
            )
        }

        /** PAYMENT */
        composable("payment/{buyerId}/{amount}") { backStack ->
            val buyerId = backStack.arguments?.getString("buyerId") ?: ""
            val amount = backStack.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0

            PaymentScreen(
                repo = repo,
                buyerId = buyerId,
                amount = amount,
                onPaymentSuccess = {
                    navController.navigate("main/$buyerId") {
                        popUpTo("main/$buyerId") { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        /** SELLER DASHBOARD */
        composable("sellerDashboard/{sellerId}") { backStack ->
            val sellerId = backStack.arguments?.getString("sellerId") ?: ""

            SellerDashboardScreen(
                repo = repo,
                sellerId = sellerId,
                onBack = { navController.popBackStack() },
                onViewOrders = { id ->
                    navController.navigate("sellerOrders/$id")
                }
            )

        }

        /** SELLER ORDERS */
        composable("sellerOrders/{sellerId}") { backStack ->
            val sellerId = backStack.arguments?.getString("sellerId") ?: ""

            SellerOrdersScreen(
                repo = repo,
                sellerId = sellerId,
                onBack = { navController.popBackStack() }
            )
        }

        /** ADD PRODUCT */
        composable("addProduct/{sellerId}") { backStack ->
            val sellerId = backStack.arguments?.getString("sellerId") ?: ""

            AddProductScreen(
                repo = repo,
                sellerId = sellerId,
                onDone = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    repo: FirebaseRepo,
    buyerId: String,
    rootNavController: NavController
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(bottomNavController, buyerId) }
    ) { innerPadding ->

        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            /** HOME */
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    repo = repo,
                    onOpenDetail = { productId ->
                        rootNavController.navigate("productDetail/$productId/$buyerId")
                    },
                    onAddProduct = { sellerId ->
                        rootNavController.navigate("addProduct/$sellerId")
                    }
                )
            }

            /** CART */
            composable(BottomNavItem.Cart.route) {
                CartScreen(
                    repo = repo,
                    buyerId = buyerId,
                    navController = rootNavController,
                    onBack = { bottomNavController.popBackStack() }
                )
            }

            /** ORDERS */
            composable(BottomNavItem.Orders.route) {
                MyOrdersScreen(
                    repo = repo,
                    buyerId = buyerId,
                    onBack = { bottomNavController.popBackStack() }
                )
            }

            /** PROFILE */
            composable("${BottomNavItem.Profile.route}/{buyerId}") { backStack ->
                val uid = backStack.arguments?.getString("buyerId") ?: buyerId

                ProfileScreen(
                    buyerId = uid,
                    onLogout = {
                        rootNavController.navigate("login") {
                            popUpTo("main/$buyerId") { inclusive = true }
                        }
                    },
                    onOpenSellerDashboard = { sellerId ->
                        rootNavController.navigate("sellerDashboard/$sellerId")
                    }
                )
            }
        }
    }
}
