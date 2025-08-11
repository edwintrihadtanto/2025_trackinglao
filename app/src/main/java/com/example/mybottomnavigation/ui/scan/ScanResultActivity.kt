package com.example.mybottomnavigation.ui.scan

import android.app.AlertDialog
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mybottomnavigation.data.model.ScanResultRequest
import com.example.mybottomnavigation.data.model.ScanResultResponse
import com.example.mybottomnavigation.data.network.ApiConfig
import com.example.mybottomnavigation.databinding.ActivityScanresultBinding
import retrofit2.Call

class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanresultBinding
//    private var medrec: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanresultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan action bar (jika perlu)
        supportActionBar?.hide()
        val medrec = intent.getStringExtra("medrec_key") ?: "0"
        // Ambil hasil scan dari intent
        val resultText = intent.getStringExtra("scan_result") ?: "Tidak ada hasil"
//        binding.tvResult.text = resultText
        binding.tvResult.text = "Sedang Memproses data: $resultText"

        kirimHasilScan(resultText, medrec)
    }

    private fun kirimHasilScan(kode: String, medrec: String) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Tidak ada koneksi internet.", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        val request = ScanResultRequest(kode_scan = kode, medrec)
        Log.e("ResultScan", request.toString())

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
                            "${res?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (res?.success == true) {
                            val pasien = res.data
                            val option      = response.body()?.option
                            val displayText = """
                               ✅ Scan Berhasil
                               Tgl Kunjung : ${pasien?.tgl_kunj ?: "-"}
                               No. Rekammedis : ${pasien?.kode_scan ?: "-"}
                               Nama Pasien : ${pasien?.nama_pasien ?: "-"}
                               Nama Unit : ${pasien?.nama_unit ?: "-"}
                               Waktu Pengantaran : ${pasien?.jam_daftar ?: "-"}
                               Status Pengantaran : ${pasien?.status_obat ?: "-"}
                               ${pasien?.info ?: ""}""".trimIndent()
                            val html = response.body()?.html
                            val displayTextHTML = """
                               ${html?.ifBlank { "<div style='text-align:left;'>-</div>" }}""".trimIndent()
                            if (option == 1){
                                animateResultText(displayText)
                            }else{
                                animateResultText(displayTextHTML)
                            }
//                            binding.tvResult.text = displayText

                            // Tampilkan dialog konfirmasi
                            AlertDialog.Builder(this@ScanResultActivity)
                                .setTitle("Konfirmasi")
                                .setMessage("Data pasien berhasil ditemukan.\nIngin melakukan scan lagi?")
                                .setPositiveButton("Scan Lagi") { _, _ ->
                                    finish() // Kembali ke activity scan sebelumnya
                                }
                                .setNegativeButton("Tidak", null)
                                .show()
                        }else{
                            val displayText = """ 
                                ❌ ${res?.message ?: "Gagal mengambil data"} """.trimIndent()
                            animateResultText(displayText)
//                            binding.tvResult.text = displayText
                        }

                    } else {
                        binding.tvResult.text = "❌ Gagal kirim scan. Code: ${response.code()}"
                        /*Toast.makeText(
                            this@ScanResultActivity,
                            "Gagal kirim scan. Code: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()*/
                    }
                    showLoading(false)
                }

                override fun onFailure(call: Call<ScanResultResponse>, t: Throwable) {
                    showLoading(false)

                    Log.e("ErrorKoneksi", "onFailure: ${t.localizedMessage}", t)
                    val isNetworkError = t is java.net.UnknownHostException || t is java.net.ConnectException
                    val userMessage = if (isNetworkError) {
                        "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
                    } else {
                        "Terjadi kesalahan. Silakan coba lagi nanti."
                    }

                    Toast.makeText(this@ScanResultActivity, userMessage, Toast.LENGTH_LONG).show()
                    /*Toast.makeText(
                        this@ScanResultActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ErrorKoneksi", "${t.message}");
                    showLoading(false)*/
                }
            })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.alpha = 0f
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        } else {
            binding.progressBar.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.progressBar.visibility = View.GONE
                }
                .start()
        }
    }

    private fun animateResultText(displayText: String) {
        binding.tvResult.translationY = 100f
        binding.tvResult.alpha = 0f
        binding.tvResult.text = displayText
        binding.tvResult.gravity = Gravity.CENTER
        binding.tvResult.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    fun parseError(response: retrofit2.Response<*>): ScanResultResponse {
        return try {
            val converter = ApiConfig.getRetrofit()
                .responseBodyConverter<ScanResultResponse>(ScanResultResponse::class.java, arrayOf())
            response.errorBody()?.let {
                converter.convert(it)
            } ?: ScanResultResponse(false, "Unknown error", 0, null, "HTML")
        } catch (_: Exception) {
            ScanResultResponse(false, "Gagal Load BackEnd!", 0, null, "HTML")
        }
    }
}