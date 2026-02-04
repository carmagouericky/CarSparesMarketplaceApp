package com.example.carsparesmarketplaceapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carsparesmarketplaceapp.FirebaseRepo
import com.example.carsparesmarketplaceapp.Order

@Composable
fun SellerDashboardScreen(
    repo: FirebaseRepo,
    sellerId: String,
    onBack: () -> Unit,
    onViewOrders: (String) -> Unit
) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }

    // --- BMW M COLORS ---
    val mLightBlue = Color(0xFF53A5DA)
    val mDarkBlue = Color(0xFF20265D)
    val mRed = Color(0xFFD1242F)
    val bgGrey = Color(0xFFF5F5F5)

    LaunchedEffect(sellerId) {
        repo.fetchSellerOrders(sellerId).onSuccess { fetchedOrders ->
            orders = fetchedOrders
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGrey)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Allow scrolling if charts get tall
    ) {

        // Header
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = mDarkBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Text("No sales yet.", color = Color.Gray)
        } else {
            val totalRevenue = orders.sumOf { it.totalAmount }
            val pendingOrders = orders.count { it.status == "pending" }
            val completedOrders = orders.count { it.status == "completed" }

            // 1. STATS CARDS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(title = "Revenue", value = "KES $totalRevenue", color = mDarkBlue, modifier = Modifier.weight(1f))
                StatCard(title = "Total Orders", value = "${orders.size}", color = mRed, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. REVENUE BAR CHART
            Text("Revenue Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().height(250.dp).padding(4.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    RevenueBarChart(orders = orders, barColor = mLightBlue)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. ORDER STATUS PIE CHART
            Text("Order Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().height(250.dp).padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // The Pie Chart
                    OrderStatusPieChart(
                        pending = pendingOrders,
                        completed = completedOrders,
                        pendingColor = mRed,
                        completedColor = mDarkBlue
                    )

                    // The Legend
                    Column {
                        LegendItem(color = mDarkBlue, label = "Completed")
                        Spacer(modifier = Modifier.height(8.dp))
                        LegendItem(color = mRed, label = "Pending")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = { onViewOrders(sellerId) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mDarkBlue)
            ) {
                Text("View All Orders List")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Back")
            }
        }
    }
}

// --- HELPER COMPOSABLES FOR CHARTS ---

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun RevenueBarChart(orders: List<Order>, barColor: Color) {
    // Take the last 7 orders for the chart, or all if less than 7
    val recentOrders = orders.takeLast(7)
    val maxVal = recentOrders.maxOfOrNull { it.totalAmount.toFloat() } ?: 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = size.width / (recentOrders.size * 2f)
        val spacing = barWidth

        recentOrders.forEachIndexed { index, order ->
            val barHeight = (order.totalAmount.toFloat() / maxVal) * size.height

            drawRect(
                color = barColor,
                topLeft = Offset(
                    x = (index * (barWidth + spacing)) + spacing / 2,
                    y = size.height - barHeight
                ),
                size = Size(width = barWidth, height = barHeight)
            )
        }
    }
}

@Composable
fun OrderStatusPieChart(pending: Int, completed: Int, pendingColor: Color, completedColor: Color) {
    val total = pending + completed
    val pendingSweep = if (total == 0) 0f else (pending.toFloat() / total) * 360f
    val completedSweep = if (total == 0) 0f else (completed.toFloat() / total) * 360f

    Box(modifier = Modifier.size(150.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 40f
            val radius = size.minDimension / 2 - strokeWidth

            // Draw Completed Arc
            drawArc(
                color = completedColor,
                startAngle = -90f,
                sweepAngle = completedSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Draw Pending Arc
            drawArc(
                color = pendingColor,
                startAngle = -90f + completedSweep,
                sweepAngle = pendingSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        }
        // Center Text
        Text(
            text = "$total",
            modifier = Modifier.align(Alignment.Center),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, shape = RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.size(8.dp))
        Text(label, fontSize = 14.sp)
    }
}