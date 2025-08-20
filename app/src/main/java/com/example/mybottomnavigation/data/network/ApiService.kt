package com.example.mybottomnavigation.data.network
import com.example.mybottomnavigation.data.model.LoginRequest
import com.example.mybottomnavigation.data.model.LoginResponse
import com.example.mybottomnavigation.data.model.LogoutRequest
import com.example.mybottomnavigation.data.model.LogoutResponse
import com.example.mybottomnavigation.data.model.ScanResultRequest
import com.example.mybottomnavigation.data.model.ScanResultResponse
import com.example.mybottomnavigation.data.model.VersiAPKResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
//    @POST("actlog/data/login")
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    @Headers("Content-Type: application/json")
    @POST("logout")
    fun logout(@Body request: LogoutRequest): Call<LogoutResponse>
    @Headers("Content-Type: application/json")
//    @POST("actlog/data/scanresult")
    @POST("scanresult")
    fun kirimScanResult(@Body request: ScanResultRequest): Call<ScanResultResponse>
    @POST("infopengiriman")
    fun infopengiriman(@Body request: ScanResultRequest): Call<ScanResultResponse>

    @Headers("Content-Type: application/json")
    @GET("cekversiterbaru")
    fun cekversiterbaru(): Call<VersiAPKResponse>
}