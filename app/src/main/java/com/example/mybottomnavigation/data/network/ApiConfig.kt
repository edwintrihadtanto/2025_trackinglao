package com.example.mybottomnavigation.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    /*fun getApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://apprssm.rssoedono.jatimprov.go.id/mobile_track/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }*/
    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://apprssm.rssoedono.jatimprov.go.id/mobile_track/") // ganti base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(): ApiService {
        return getRetrofit().create(ApiService::class.java)
    }
}