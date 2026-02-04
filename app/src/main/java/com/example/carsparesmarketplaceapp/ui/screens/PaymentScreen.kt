package com.example.carsparesmarketplaceapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carsparesmarketplaceapp.FirebaseRepo
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(
    repo: FirebaseRepo,
    buyerId: String,
    amount: Double,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var pin by remember { mutableStateOf("") }
    var isClearingCart by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("M-PESA Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            when (step) {

                // STEP 1 — Enter PIN
                1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Enter M-PESA PIN to approve", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))

                    androidx.compose.material3.TextField(
                        value = pin,
                        onValueChange = { pin = it },
                        placeholder = { Text("Enter PIN") }
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(onClick = { step = 2 }, enabled = pin.length >= 4) {
                        Text("Confirm")
                    }
                }

                // STEP 2 — Processing animation
                2 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(20.dp))
                    Text("Processing Payment...")
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(3000)
                        step = 3
                    }
                }

                // STEP 3 — Success & clear cart
                3 -> {
                    if (isClearingCart) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(20.dp))
                            Text("Finalizing your order...")
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Payment Successful!", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = {
                                isClearingCart = true
                                scope.launch {
                                    val result = repo.clearCart(buyerId)
                                    if (result.isSuccess) {
                                        onPaymentSuccess()
                                    } else {
                                        // Handle error clearing cart
                                        isClearingCart = false
                                    }
                                }
                            }) {
                                Text("Continue")
                            }
                        }
                    }
                }
            }
        }
    }
}
