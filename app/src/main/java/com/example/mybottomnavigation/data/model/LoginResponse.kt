package com.example.mybottomnavigation.data.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? // Nullable, supaya tidak crash kalau gagal login
    // Tambah field lain jika ada
)

data class UserData(
    val nama: String,
    val medrec: String
)