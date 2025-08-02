package com.example.mybottomnavigation.data.network
import com.example.mybottomnavigation.data.model.LoginRequest
import com.example.mybottomnavigation.data.model.LoginResponse
import com.example.mybottomnavigation.data.model.ScanResultRequest
import com.example.mybottomnavigation.data.model.ScanResultResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("actlog/data/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("actlog/data/scanresult")
    fun kirimScanResult(@Body request: ScanResultRequest): Call<ScanResultResponse>
}