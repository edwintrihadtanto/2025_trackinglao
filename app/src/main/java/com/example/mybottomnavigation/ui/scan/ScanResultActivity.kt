package com.example.mybottomnavigation.ui.scan

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mybottomnavigation.data.model.LoginRequest
import com.example.mybottomnavigation.data.model.ScanResultRequest
import com.example.mybottomnavigation.data.model.ScanResultResponse
import com.example.mybottomnavigation.data.network.ApiConfig
import com.example.mybottomnavigation.databinding.ActivityScanresultBinding
import retrofit2.Call

class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanresultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanresultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan action bar (jika perlu)
        supportActionBar?.hide()

        // Ambil hasil scan dari intent
        val resultText = intent.getStringExtra("scan_result") ?: "Tidak ada hasil"
        binding.tvResult.text = resultText

        kirimHasilScan(resultText)
    }

    private fun kirimHasilScan(kode: String) {
        val request = ScanResultRequest(kode_scan = kode)
        Log.e("ResultScan", ScanResultRequest(kode_scan = kode).toString());

        ApiConfig.getApiService().kirimScanResult(request)
            .enqueue(object : retrofit2.Callback<ScanResultResponse> {
                override fun onResponse(
                    call: Call<ScanResultResponse>,
                    response: retrofit2.Response<ScanResultResponse>
                ) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        Toast.makeText(
                            this@ScanResultActivity,
                            "Berhasil: ${res?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ScanResultActivity,
                            "Gagal kirim scan. Code: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ScanResultResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ScanResultActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

}