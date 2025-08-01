package com.example.mybottomnavigation.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mybottomnavigation.databinding.ActivityMainBinding
import com.example.mybottomnavigation.ui.camera.CameraActivity
import com.example.mybottomnavigation.ui.login.LoginActivity
import com.example.mybottomnavigation.ui.result.ResultActivity
import com.example.mybottomnavigation.ui.scan.ScanActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Ambil nama dan medrec dari SharedPreferences
        val sharedPref = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        val namaPasien = sharedPref.getString("NAMA_PASIEN", null)
        val medrecPasien = sharedPref.getString("MEDREC", null)

        // 2. Kalau belum login, kembali ke LoginActivity
        if (namaPasien == null || medrecPasien == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }

        // 3. Tampilkan sapaan
        binding.tvWelcome.text = "Halo, $namaPasien\nMedrec: $medrecPasien"

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

    private fun setupAction() {
        binding.btnScan.setOnClickListener {
//            startActivity(Intent(this@MainActivity, ScanActivity::class.java))
            val intent = Intent(this@MainActivity, ScanActivity::class.java)
            startActivityForResult(intent, CAMERA_X_RESULT)
        }

        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this@MainActivity, CameraActivity::class.java))
        }
        binding.btnResi.setOnClickListener {
            startActivity(Intent(this@MainActivity, ResultActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
