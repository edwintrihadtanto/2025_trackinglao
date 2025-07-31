package com.example.mybottomnavigation.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    fun getApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://apprssm.rssoedono.jatimprov.go.id/") // gunakan 10.0.2.2 untuk localhost di emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}