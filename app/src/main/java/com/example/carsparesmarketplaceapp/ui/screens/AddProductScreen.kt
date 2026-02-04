package com.example.carsparesmarketplaceapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carsparesmarketplaceapp.FirebaseRepo
import com.example.carsparesmarketplaceapp.Product
import com.example.carsparesmarketplaceapp.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    repo: FirebaseRepo,
    sellerId: String,
    onDone: () -> Unit
) {
    // Form State
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    // --- BMW M THEME COLORS ---
    val mLightBlue = Color(0xFF53A5DA)
    val mDarkBlue = Color(0xFF20265D)
    val mRed = Color(0xFFD1242F)
    val bgGrey = Color(0xFFF5F5F5)

    // Gradient for the Button
    val mGradientBrush = Brush.horizontalGradient(
        colors = listOf(mLightBlue, mDarkBlue, mRed)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Part", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = mDarkBlue
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGrey)
                .padding(padding)
                .verticalScroll(rememberScrollState()) // Allow scrolling for small screens
                .padding(20.dp)
        ) {

            // 1. MODERN IMAGE UPLOAD ZONE
            Text(
                text = "Product Image",
                style = MaterialTheme.typography.labelLarge,
                color = mDarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        // If no image, show a dashed-style light blue border (using solid for simplicity)
                        color = if (imageUri == null) mLightBlue.copy(alpha = 0.5f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { pickImage.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // Show Selected Image
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // "Change" Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap to Change", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Placeholder UI
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Upload",
                            tint = mLightBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to upload photo", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. INPUT FIELDS
            ModernTextField(value = name, onValueChange = { name = it }, label = "Part Name", accentColor = mDarkBlue)
            Spacer(modifier = Modifier.height(12.dp))

            ModernTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                accentColor = mDarkBlue,
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Row for Price and Qty
            androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    ModernTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = "Price (KES)",
                        accentColor = mDarkBlue,
                        keyboardType = KeyboardType.Number
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    ModernTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = "Quantity",
                        accentColor = mDarkBlue,
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. SUBMIT BUTTON WITH GRADIENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (uploading) Color.Gray else Color.Transparent) // Fallback for disabled
                    .background(if (!uploading) mGradientBrush else Brush.linearGradient(listOf(Color.Gray, Color.Gray)))
                    .clickable(enabled = !uploading) {
                        if (name.isBlank() || price.isBlank()) {
                            showToast(context, "Please fill all required fields.")
                            return@clickable
                        }
                        scope.launch {
                            uploading = true
                            val imageUrl = imageUri?.let { repo.uploadProductImage(it, sellerId).getOrNull() } ?: ""

                            val product = Product(
                                sellerId = sellerId,
                                name = name,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                quantity = qty.toIntOrNull() ?: 1,
                                imageUrl = imageUrl
                            )

                            repo.addProduct(product).fold(
                                onSuccess = {
                                    showToast(context, "✅ Product Added")
                                    onDone()
                                },
                                onFailure = {
                                    showToast(context, it.localizedMessage ?: "Error adding product")
                                }
                            )
                            uploading = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Publish Product",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- HELPER COMPOSABLE FOR CLEAN INPUTS ---
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            focusedLabelColor = accentColor,
            cursorColor = accentColor,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )
}