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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carsparesmarketplaceapp.FirebaseRepo
import com.example.carsparesmarketplaceapp.Order
import com.example.carsparesmarketplaceapp.showToast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(
    repo: FirebaseRepo,
    sellerId: String,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // --- BMW M THEME COLORS ---
    val mLightBlue = Color(0xFF53A5DA)
    val mDarkBlue = Color(0xFF20265D)
    val bgGrey = Color(0xFFF5F5F5)

    // Fetch orders for this seller
    LaunchedEffect(Unit) {
        scope.launch {
            val res = repo.fetchSellerOrders(sellerId)
            if (res.isSuccess) {
                // Sort by newest first
                orders = res.getOrDefault(emptyList()).sortedByDescending { it.createdAt }
            } else {
                showToast(context, "Failed to load orders")
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incoming Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = mDarkBlue,
                    navigationIconContentColor = mDarkBlue
                )
            )
        },
        containerColor = bgGrey
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = mLightBlue
                )
                orders.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No orders yet.", color = Color.Gray)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        SellerOrderCard(
                            order = order,
                            onStatusChanged = {
                                // Refresh orders after status update
                                scope.launch {
                                    val res = repo.fetchSellerOrders(sellerId)
                                    if (res.isSuccess) {
                                        orders = res.getOrDefault(emptyList()).sortedByDescending { it.createdAt }
                                    }
                                }
                            },
                            repo = repo
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    order: Order,
    onStatusChanged: () -> Unit,
    repo: FirebaseRepo
) {
    val scope = rememberCoroutineScope()

    // Theme Colors
    val mLightBlue = Color(0xFF53A5DA)
    val mDarkBlue = Color(0xFF20265D)
    val mRed = Color(0xFFD1242F)

    // Format Data
    val shortId = order.id.takeLast(8).uppercase()
    val dateString = remember(order.createdAt) {
        val sdf = SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault())
        sdf.format(Date(order.createdAt))
    }

    // Gradient for Action Button
    val mGradientBrush = Brush.horizontalGradient(
        colors = listOf(mLightBlue, mDarkBlue, mRed)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Order #$shortId",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = mDarkBlue
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (order.buyerName.isNotEmpty()) order.buyerName else "Guest Buyer",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Date Badge
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = dateString,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.qty}x  ${item.productName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Text(
                        text = "KES ${item.unitPrice * item.qty}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = mDarkBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Total: KES ${order.totalAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = mDarkBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ACTION BUTTONS
            when (order.status.lowercase()) {
                "pending" -> {
                    Button(
                        onClick = {
                            scope.launch {
                                repo.updateOrderStatus(order.id, "processing").onSuccess { onStatusChanged() }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .background(mGradientBrush), // M-Sport Gradient
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Accept & Process Order", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                "processing" -> {
                    Button(
                        onClick = {
                            scope.launch {
                                repo.updateOrderStatus(order.id, "completed").onSuccess { onStatusChanged() }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Solid Green
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark as Completed", fontWeight = FontWeight.Bold)
                    }
                }

                "completed" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Order Completed", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }

                "cancelled" -> {
                    Text(
                        text = "Order Cancelled",
                        color = mRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}