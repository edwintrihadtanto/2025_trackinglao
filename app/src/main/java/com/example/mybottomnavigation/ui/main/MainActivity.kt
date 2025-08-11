package com.example.mybottomnavigation.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.mybottomnavigation.R
import com.example.mybottomnavigation.data.model.LogoutRequest
import com.example.mybottomnavigation.data.model.LogoutResponse
import com.example.mybottomnavigation.data.network.ApiConfig
import com.example.mybottomnavigation.databinding.ActivityMainBinding
import com.example.mybottomnavigation.ui.login.LoginActivity
import com.example.mybottomnavigation.ui.scan.ScanActivity
import com.example.mybottomnavigation.ui.scan.ScanResultActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private lateinit var binding: ActivityMainBinding
    private var medrec: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil nama dan medrec dari SharedPreferences
        val sharedPref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val namaPasien = sharedPref.getString("NAMA_PASIEN", null)
        medrec = sharedPref.getString("MEDREC", null)
        val tgllahirPasien = sharedPref.getString("TGLLAHIR", null)
        val versiAPK = sharedPref.getString("VERSIAPK", null)
        Log.d("SharedPreferences", "Nama Pasien: $namaPasien, Medrec Pasien: $medrec")
        // 2. Kalau belum login, kembali ke LoginActivity
        if (namaPasien == null || medrec == null) {
            Log.d("MainActivity", "Data dibaca dari SharedPreferences: NAMA_PASIEN=$namaPasien, MEDREC=$medrec")

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = pInfo.versionName

        if (versionName != versiAPK){
            AlertDialog.Builder(this@MainActivity)
                .setCancelable(false)
                .setTitle("Info!")
                .setMessage("Versi terbaru Tracking Obat tersedia, Silahkan download / update aplikasi terbaru?")
                .setPositiveButton("Terima Kasih") { _, _ ->
                    val logoutRequest = LogoutRequest(medrec ?: "")
                    val apiService = ApiConfig.getApiService()
                    Log.e("Logout Request", logoutRequest.toString())

                    apiService.logout(logoutRequest).enqueue(object : retrofit2.Callback<LogoutResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<LogoutResponse>,
                            response: retrofit2.Response<LogoutResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(this@MainActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                                // Hapus data login dari SharedPreferences
                                getSharedPreferences("APP_PREF", MODE_PRIVATE).edit { clear() }

                                // Pindah ke LoginActivity
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                val logoutResponse = if (response.isSuccessful) {
                                    response.body()
                                } else {
                                    parseError(response) // <-- ambil pesan dari errorBody
                                }
                                Toast.makeText(this@MainActivity, logoutResponse?.message ?: "Logout gagal! Gagal Load BackEnd!", Toast.LENGTH_SHORT).show()
                                /*val errorMessage =
                                    response.body()?.message ?: "Logout gagal! Gagal Load BackEnd!"
                                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()*/
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<LogoutResponse>, t: Throwable) {
                            Toast.makeText(
                                this@MainActivity,
                                "Terjadi kesalahan jaringan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
                //.setNegativeButton("Batal", null)
                .show()
        }

        // 3. Tampilkan sapaan
        binding.tvWelcome.text = "No. Medrec : $medrec\nTgl. Lahir : $tgllahirPasien\nHalo, $namaPasien"

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupAction()
        supportActionBar?.hide()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == CAMERA_X_RESULT && resultCode == RESULT_OK) {
        val hasilScan = data?.getStringExtra("SCAN_RESULT")
        Toast.makeText(this, "Hasil: $hasilScan", Toast.LENGTH_LONG).show()
    }
}
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("DEPRECATION")
    private fun setupAction() {
        binding.btnScan.setOnClickListener {
//            startActivity(Intent(this@MainActivity, ScanActivity::class.java))
            val intent = Intent(this@MainActivity, ScanActivity::class.java)
            intent.putExtra("medrec_key", medrec)
            startActivityForResult(intent, CAMERA_X_RESULT)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        /*binding.btnCamera.setOnClickListener {
            startActivity(Intent(this@MainActivity, CameraActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }*/
        binding.btnResi.setOnClickListener {
            val nomorResi = binding.pencarianresi.text.toString()
            when {
                nomorResi.isEmpty() -> {
                    binding.pencarianresiLayout.error = "Nomor Resi Salah"
                }
                else -> {
                    val intent = Intent(this@MainActivity, ScanResultActivity::class.java)
                    intent.putExtra("scan_result", nomorResi)
                    intent.putExtra("medrec_key", medrec)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                    finish() // ⬅️ Tutup activity ini setelah redirect
                }
            }
        }
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya") { _, _ ->
                    val logoutRequest = LogoutRequest(medrec ?: "")
                    val apiService = ApiConfig.getApiService()
                    Log.e("Logout Request", logoutRequest.toString())

                    apiService.logout(logoutRequest).enqueue(object : retrofit2.Callback<LogoutResponse> {
                override fun onResponse(
                    call: retrofit2.Call<LogoutResponse>,
                    response: retrofit2.Response<LogoutResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@MainActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        // Hapus data login dari SharedPreferences
                        getSharedPreferences("APP_PREF", MODE_PRIVATE).edit { clear() }

                        // Pindah ke LoginActivity
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val logoutResponse = if (response.isSuccessful) {
                            response.body()
                        } else {
                            parseError(response) // <-- ambil pesan dari errorBody
                        }
                        Toast.makeText(this@MainActivity, logoutResponse?.message ?: "Logout gagal! Gagal Load BackEnd!", Toast.LENGTH_SHORT).show()
                        /*val errorMessage =
                            response.body()?.message ?: "Logout gagal! Gagal Load BackEnd!"
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()*/
                    }
                }

                override fun onFailure(call: retrofit2.Call<LogoutResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Terjadi kesalahan jaringan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val namaPasien = sharedPref.getString("NAMA_PASIEN", null)
        val medrecPasien = sharedPref.getString("MEDREC", null)

        if (namaPasien == null || medrecPasien == null) {
            Log.d("MainActivity", "Data tidak ditemukan, kembali ke LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            val medrecClean = (medrec ?: "").replace("-", "")
//          val logoutRequest = LogoutRequest(medrecClean)
            val logoutRequest = LogoutRequest(medrec ?: "")

            val apiService = ApiConfig.getApiService()
            Log.e("Logout Request", logoutRequest.toString())

            apiService.logout(logoutRequest).enqueue(object : retrofit2.Callback<LogoutResponse> {
                override fun onResponse(
                    call: retrofit2.Call<LogoutResponse>,
                    response: retrofit2.Response<LogoutResponse>
                ) {
                    val logoutResponse = if (response.isSuccessful && response.body()?.success == true) {
                        response.body()
                    } else {
                        parseError(response) // <-- ambil pesan dari errorBody
                    }

                    if (logoutResponse?.success == true) {
                        // Bersihkan session
                        getSharedPreferences("APP_PREF", MODE_PRIVATE).edit { clear() }
                        // Arahkan ke LoginActivity
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, logoutResponse?.message ?: "Logout gagal! Gagal Load BackEnd!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<LogoutResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun parseError(response: retrofit2.Response<*>): LogoutResponse {
        return try {
            val converter = ApiConfig.getRetrofit()
                .responseBodyConverter<LogoutResponse>(LogoutResponse::class.java, arrayOf())
            response.errorBody()?.let {
                converter.convert(it)
            } ?: LogoutResponse(false, "Unknown error")
        } catch (e: Exception) {
            LogoutResponse(false, "Gagal Load BackEnd!")
        }
    }
}
