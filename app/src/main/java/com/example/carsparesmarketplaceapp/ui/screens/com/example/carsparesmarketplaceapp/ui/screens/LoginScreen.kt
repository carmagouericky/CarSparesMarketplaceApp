package com.example.carsparesmarketplaceapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val role: String = ""
)

class ProfileViewModel : ViewModel() {
    private val _userData = MutableStateFlow<UserProfile?>(null)
    val userData = _userData.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                val name = snapshot.getString("name") ?: ""
                val email = snapshot.getString("email") ?: auth.currentUser?.email ?: ""
                val role = snapshot.getString("role") ?: "Buyer"

                _userData.value = UserProfile(name, email, role)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}