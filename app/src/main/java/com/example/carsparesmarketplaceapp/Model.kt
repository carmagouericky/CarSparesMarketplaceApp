package com.example.carsparesmarketplaceapp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "buyer"
)

data class Product(
    val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val qty: Int = 1,
    val unitPrice: Double = 0.0
)

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "pending",
    val paymentMethod: String = "MPESA",
    val mpesaCode: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
