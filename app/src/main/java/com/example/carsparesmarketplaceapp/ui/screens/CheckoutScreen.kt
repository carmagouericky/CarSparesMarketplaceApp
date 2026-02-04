package com.example.carsparesmarketplaceapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carsparesmarketplaceapp.FirebaseRepo
import com.example.carsparesmarketplaceapp.Order
import com.example.carsparesmarketplaceapp.OrderItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    repo: FirebaseRepo,
    buyerId: String,
    buyerName: String,
    onBack: () -> Unit,
    onProceedPayment: (Double) -> Unit
) {
    var items by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- BMW M THEME COLORS ---
    val mLightBlue = Color(0xFF53A5DA)
    val mDarkBlue = Color(0xFF20265D)
    val mRed = Color(0xFFD1242F)
    val bgGrey = Color(0xFFF5F5F5)

    // Gradient Button Brush
    val mGradientBrush = Brush.horizontalGradient(
        colors = listOf(mLightBlue, mDarkBlue, mRed)
    )

    // Fetch cart items
    LaunchedEffect(buyerId) {
        repo.getCartItems(buyerId).onSuccess { items = it }
    }

    val totalAmount = items.sumOf { it.unitPrice * it.qty }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = mDarkBlue
                )
            )
        },
        containerColor = bgGrey
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 1. ORDER SUMMARY TITLE
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleLarge,
                color = mDarkBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 2. LIST OF ITEMS (Scrollable)
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Takes up available space
                    .fillMaxWidth()
            ) {
                items(items) { item ->
                    CheckoutItemCard(item = item, priceColor = mDarkBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. PAYMENT METHOD CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = mLightBlue)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Payment Method", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("M-PESA Express", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50)) // Green check
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. TOTAL & ACTION AREA
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Total Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        Text(
                            text = "KES ${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = mDarkBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Gradient Proceed Button
                    Button(
                        onClick = {
                            if (isProcessing) return@Button
                            isProcessing = true
                            scope.launch {
                                try {
                                    // 1 Reduce stock
                                    items.forEach { repo.reduceProductStock(it.productId, it.qty) }

                                    // 2 Split orders by seller
                                    val ordersBySeller = items.groupBy { it.sellerId }
                                    ordersBySeller.forEach { (sellerId, sellerItems) ->
                                        val sellerTotal = sellerItems.sumOf { it.unitPrice * it.qty }
                                        repo.placeOrder(
                                            Order(
                                                id = "",
                                                buyerId = buyerId,
                                                buyerName = buyerName,
                                                sellerId = sellerId,
                                                sellerName = sellerItems.first().sellerName,
                                                items = sellerItems,
                                                totalAmount = sellerTotal,
                                                status = "pending",
                                                paymentMethod = "MPESA",
                                                createdAt = System.currentTimeMillis()
                                            )
                                        )
                                    }

                                    // 3 Clear cart
                                    repo.clearCart(buyerId)

                                    // 4 Proceed to payment
                                    onProceedPayment(totalAmount)

                                } catch (e: Exception) {
                                    isProcessing = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        contentPadding = PaddingValues(0.dp), // Remove default padding for gradient
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        // Gradient Box
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(mGradientBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    "Pay Now",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER COMPOSABLE FOR CART ITEMS ---
@Composable
fun CheckoutItemCard(item: OrderItem, priceColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)), // Light blue bg
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.qty}x",
                    fontWeight = FontWeight.Bold,
                    color = priceColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Item Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Seller: ${item.sellerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Price
            Text(
                text = "KES ${item.unitPrice * item.qty}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = priceColor
            )
        }
    }
}