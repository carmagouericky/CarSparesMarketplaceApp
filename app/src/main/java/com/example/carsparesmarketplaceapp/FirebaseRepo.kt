package com.example.carsparesmarketplaceapp

import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRepo {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

    // 🔹 Get current logged user
    fun currentUser() = auth.currentUser

    // 🔹 Register user
    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<User> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = res.user!!.uid
            val user = User(uid, name, email, role)
            db.child("users").child(uid).setValue(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Register error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Login user
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val res = auth.signInWithEmailAndPassword(email, password).await()
            val uid = res.user!!.uid
            val snap = db.child("users").child(uid).get().await()
            val user = snap.getValue(User::class.java)
                ?: User(uid, res.user?.displayName ?: "", email, "buyer")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Login error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Upload product image
    suspend fun uploadProductImage(uri: Uri, sellerId: String): Result<String> {
        return try {
            val key = "product_images/${sellerId}_${System.currentTimeMillis()}.jpg"
            val ref = storage.child(key)
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Upload image error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Add product
    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val key = db.child("products").push().key ?: throw Exception("No key generated")
            val newProduct = product.copy(id = key)
            db.child("products").child(key).setValue(newProduct).await()
            Result.success(key)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Add product error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Fetch all products
    suspend fun fetchAllProducts(): Result<List<Product>> {
        return try {
            val snap = db.child("products").get().await()
            val list = snap.children.mapNotNull { it.getValue(Product::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Fetch products error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Fetch buyer orders
    suspend fun fetchBuyerOrders(buyerId: String): Result<List<Order>> {
        return try {
            val snap = db.child("orders").orderByChild("buyerId").equalTo(buyerId).get().await()
            val orders = snap.children.mapNotNull { it.getValue(Order::class.java) }
            Result.success(orders)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Fetch buyer orders error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Fetch seller orders
    suspend fun fetchSellerOrders(sellerId: String): Result<List<Order>> {
        return try {
            val snap = db.child("orders").orderByChild("sellerId").equalTo(sellerId).get().await()
            val orders = snap.children.mapNotNull { it.getValue(Order::class.java) }
            Result.success(orders)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Fetch seller orders error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Update order status
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            db.child("orders").child(orderId).child("status").setValue(newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Update order status error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Place order (single item)
    suspend fun placeOrder(order: Order): Result<String> {
        return try {
            val key = db.child("orders").push().key ?: throw Exception("No key generated")
            val orderWithId = order.copy(id = key)
            db.child("orders").child(key).setValue(orderWithId).await()
            Result.success(key)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Place order error: ${e.message}", e)
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Reduce product stock after purchase
    suspend fun reduceProductStock(productId: String, qty: Int) {
        withContext(Dispatchers.IO) {
            try {
                val snap = db.child("products").child(productId).get().await()
                val product = snap.getValue(Product::class.java)
                if (product != null) {
                    val newQty = (product.quantity - qty).coerceAtLeast(0)
                    db.child("products").child(productId).child("quantity").setValue(newQty).await()
                }
            } catch (e: Exception) {
                Log.e("FirebaseRepo", "Reduce stock error: ${e.message}", e)
            }
        }
    }

    // 🔹 Error translator
    private fun handleFirebaseException(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
            is FirebaseAuthUserCollisionException -> "This email is already registered"
            is FirebaseAuthInvalidUserException -> "Account not found. Please register first."
            is FirebaseNetworkException -> "No internet connection. Please try again."
            is FirebaseFirestoreException -> "Database error occurred. Please try again later."
            is StorageException -> "File upload failed. Please retry."
            else -> e.localizedMessage ?: "An unexpected error occurred"
        }
    }

    // 🔹 Add item to cart
    suspend fun addItemToCart(buyerId: String, item: OrderItem): Result<Unit> {
        return try {
            val key = db.child("carts").child(buyerId).push().key ?: throw Exception("No key generated")
            db.child("carts").child(buyerId).child(key).setValue(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Fetch cart items (one-time fetch)
    suspend fun fetchCartItems(buyerId: String): Result<List<OrderItem>> {
        return try {
            val snap = db.child("carts").child(buyerId).get().await()
            val items = snap.children.mapNotNull { it.getValue(OrderItem::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Clear cart
    suspend fun clearCart(buyerId: String): Result<Unit> {
        return try {
            db.child("carts").child(buyerId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Get cart items (real-time listener)
    suspend fun getCartItems(buyerId: String): Result<List<OrderItem>> {
        return try {
            val snap = db.child("carts").child(buyerId).get().await()
            val items = snap.children.mapNotNull { it.getValue(OrderItem::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(Exception(handleFirebaseException(e)))
        }
    }

    // 🔹 Clear cart and notify callback (for non-suspend usage, optional)
    fun clearCartRealtime(buyerId: String, onComplete: () -> Unit, onError: (Exception) -> Unit) {
        db.child("carts").child(buyerId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onComplete()
                else onError(task.exception ?: Exception("Unknown error"))
            }
    }

}