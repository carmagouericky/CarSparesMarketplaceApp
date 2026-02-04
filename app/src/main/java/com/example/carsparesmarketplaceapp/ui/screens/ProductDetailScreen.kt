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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carsparesmarketplaceapp.FirebaseRepo
import com.example.carsparesmarketplaceapp.OrderItem
import com.example.carsparesmarketplaceapp.Product
import com.example.carsparesmarketplaceapp.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    repo: FirebaseRepo,
    productId: String,
    buyerId: String,
    onBack: () -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAddingToCart by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- BMW M THEME COLORS ---
    val mLightBlue = Color(0xFF53A5DA)
    val mDarkBlue = Color(0xFF20265D)
    val mRed = Color(0xFFD1242F)
    val bgGrey = Color(0xFFF5F5F5)

    // Gradient Brush
    val mGradientBrush = Brush.horizontalGradient(
        colors = listOf(mLightBlue, mDarkBlue, mRed)
    )

    // Fetch product when screen loads
    LaunchedEffect(productId) {
        repo.fetchAllProducts()
            .onSuccess { list ->
                product = list.find { it.id == productId }
                isLoading = false
            }
            .onFailure { error ->
                isLoading = false
                showToast(context, error.localizedMessage ?: "Failed to load product")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", fontWeight = FontWeight.Bold) },
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
        containerColor = bgGrey,
        bottomBar = {
            // Sticky Bottom Bar for Action Button
            if (product != null) {
                Surface(
                    color = Color.White,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                if (isAddingToCart) return@Button
                                isAddingToCart = true
                                val item = product!!

                                val cartItem = OrderItem(
                                    productId = item.id,
                                    productName = item.name,
                                    productImage = item.imageUrl,
                                    sellerId = item.sellerId,
                                    sellerName = item.sellerName,
                                    qty = 1,
                                    unitPrice = item.price
                                )

                                coroutineScope.launch {
                                    repo.addItemToCart(buyerId, cartItem)
                                        .onSuccess {
                                            showToast(context, "✅ Added to Cart")
                                            isAddingToCart = false
                                        }
                                        .onFailure {
                                            showToast(context, "❌ Failed to add")
                                            isAddingToCart = false
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(mGradientBrush),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isAddingToCart) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text(
                                            "Add to Cart",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = mLightBlue)
            }
        } else {
            product?.let { item ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // 1. PRODUCT IMAGE HERO SECTION
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart, // Placeholder
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text("No Image Available", color = Color.Gray)
                            }
                        }
                    }

                    // 2. PRODUCT DETAILS CONTAINER
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        // Price & Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KES ${String.format("%.2f", item.price)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = mDarkBlue
                            )

                            // Simple Stock Badge
                            Surface(
                                color = if(item.quantity > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if(item.quantity > 0) "In Stock" else "Out of Stock",
                                    color = if(item.quantity > 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Product Name
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Seller Info Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(mLightBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Store, null, tint = mLightBlue)
                                }
                                Spacer(modifier = Modifier.size(12.dp))
                                Column {
                                    Text("Sold by", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = if(item.sellerName.isNotEmpty()) item.sellerName else "Verified Seller",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = mDarkBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(20.dp))

                        // Description
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = mDarkBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray,
                            lineHeight = 24.sp
                        )

                        // Extra space at bottom so scrolling doesn't hide text behind the sticky button
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Product not found", color = Color.Gray)
            }
        }
    }
}