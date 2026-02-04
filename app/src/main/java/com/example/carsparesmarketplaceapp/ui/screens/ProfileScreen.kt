package com.example.carsparesmarketplaceapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun ProfileScreen(
    buyerId: String,
    onLogout: () -> Unit,
    onOpenSellerDashboard: (String) -> Unit
) {
    // 1. Get User Details directly from Firebase Auth
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Logic to display a name:
    // If displayName is null (common in simple email signups),
    // we extract the name from the email (e.g., "john@gmail.com" -> "John")
    val displayName = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        ?: "Valued Member"

    val email = currentUser?.email ?: "No Email"
    val sellerId = buyerId // Using the same ID for seller dashboard

    // --- BMW M COLORS ---
    val mLightBlue = Color(0xFF53A5DA)
    val mDarkBlue = Color(0xFF20265D)
    val mRed = Color(0xFFD1242F)
    val bgGrey = Color(0xFFF5F5F5)

    // Gradient for the Avatar Border
    val mGradientBorder = Brush.linearGradient(
        colors = listOf(mLightBlue, mDarkBlue, mRed)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGrey)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // --- PROFILE HEADER ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(4.dp, mGradientBorder, CircleShape)
                .padding(4.dp) // Gap between border and image
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Pic",
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = mDarkBlue
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- ACTION CARDS ---

        // 1. Seller Dashboard Button
        ProfileOptionCard(
            title = "Seller Dashboard",
            subtitle = "Manage your products & orders",
            icon = Icons.Default.Settings, // Or a specific dashboard icon
            iconColor = mLightBlue,
            onClick = { onOpenSellerDashboard(sellerId) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Account Details (Placeholder for future)
        ProfileOptionCard(
            title = "Account Details",
            subtitle = "Edit personal information",
            icon = Icons.Default.AccountCircle,
            iconColor = mDarkBlue,
            onClick = { /* Navigate to Edit Profile */ }
        )

        Spacer(modifier = Modifier.weight(1f)) // Push logout to bottom

        // --- LOGOUT BUTTON ---
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = mRed
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, mRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Helper Composable for Menu Items
@Composable
fun ProfileOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}