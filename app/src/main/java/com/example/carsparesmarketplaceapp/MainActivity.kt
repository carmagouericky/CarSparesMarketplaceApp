package com.example.carsparesmarketplaceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.carsparesmarketplaceapp.ui.screens.theme.CarSparesMarketplaceAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = FirebaseRepo()

        setContent {
            CarSparesMarketplaceAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Start navigation from login
                    AppNavigation(repo)
                }
            }
        }
    }
}
