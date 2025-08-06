package com.example.mybottomnavigation.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mybottomnavigation.R
import com.example.mybottomnavigation.databinding.ActivityMainBinding
import com.example.mybottomnavigation.ui.login.LoginActivity
import com.example.mybottomnavigation.ui.scan.ScanActivity
import com.example.mybottomnavigation.ui.scan.ScanResultActivity
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil nama dan medrec dari SharedPreferences
        val sharedPref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val namaPasien = sharedPref.getString("NAMA_PASIEN", null)
        val medrecPasien = sharedPref.getString("MEDREC", null)
        val tgllahirPasien = sharedPref.getString("TGLLAHIR", null)
        val versiAPK = sharedPref.getString("VERSIAPK", null)
        Log.d("SharedPreferences", "Nama Pasien: $namaPasien, Medrec Pasien: $medrecPasien")
        // 2. Kalau belum login, kembali ke LoginActivity
        if (namaPasien == null || medrecPasien == null) {
            Log.d("MainActivity", "Data dibaca dari SharedPreferences: NAMA_PASIEN=$namaPasien, MEDREC=$medrecPasien")

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }

        // 3. Tampilkan sapaan
        binding.tvWelcome.text = "No. Medrec : $medrecPasien\nTgl. Lahir : $tgllahirPasien\nHalo, $namaPasien"

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

                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                    finish() // ⬅️ Tutup activity ini setelah redirect
                }
            }
        }
        binding.btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
            sharedPref.edit { clear() } // Ini akan menghapus semua data login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
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
}
