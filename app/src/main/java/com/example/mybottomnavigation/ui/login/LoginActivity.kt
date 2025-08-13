package com.example.mybottomnavigation.ui.login

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.mybottomnavigation.data.model.LoginRequest
import com.example.mybottomnavigation.data.model.LoginResponse
import com.example.mybottomnavigation.data.network.ApiConfig
import com.example.mybottomnavigation.databinding.ActivityLoginBinding
import com.example.mybottomnavigation.ui.main.MainActivity
import java.util.Calendar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var versionName: String? = null
//    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        val namaPasien = sharedPref.getString("NAMA_PASIEN", null)
        val medrecPasien = sharedPref.getString("MEDREC", null)

        if (namaPasien != null || medrecPasien != null) {
            Log.d("Login", "Data dibaca dari SharedPreferences: NAMA_PASIEN=$namaPasien, MEDREC=$medrecPasien")

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }
//        binding.medrecEditText.setText("6465872")
//        binding.tanggalEditText.setText("1992-08-03")
        val paInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = paInfo.versionName
        binding.appversi.text = versionName
        setupView()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        val tanggalEditText = binding.tanggalEditText

        tanggalEditText.setOnClickListener {
            // Menyembunyikan keyboard jika tampil
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(tanggalEditText.windowToken, 0)

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Membuat DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format tanggal dan set ke EditText
//                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    tanggalEditText.setText(formattedDate)
                },
                year, month, day
            )

            // Tampilkan dialog pemilih tanggal
            datePickerDialog.show()
        }

        binding.loginButton.setOnClickListener {
            showLoading(true)
            val medrec = binding.medrecEditText.text.toString()
            val tanggal = binding.tanggalEditText.text.toString()
            when {
                (medrec.length != 7) ->{
                    binding.medrecEditTextLayout.error = "Medrec Hanya 7 Karakter"
                    showLoading(false)
                }
                medrec.isEmpty() -> {
                    binding.medrecEditTextLayout.error = "Masukkan No. Medrec"
                    showLoading(false)
                }
                tanggal.isEmpty() -> {
                    binding.tanggalEditTextLayout.error = "Masukkan tanggal lahir"
                    showLoading(false)
                }
                else -> {
                    showLoading(true)
                    val loginRequest = LoginRequest(medrec, tanggal, versionName?: "")
                    val apiService = ApiConfig.getApiService()
                    Log.e("Login Failed", LoginRequest(medrec, tanggal, versionName?: "").toString())

                    apiService.login(loginRequest).enqueue(object : retrofit2.Callback<LoginResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<LoginResponse>,
                            response: retrofit2.Response<LoginResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                val namaPasien      = response.body()?.data?.nama
                                val medrecPasien    = response.body()?.data?.medrec
                                val tgllahirPasien  = response.body()?.data?.tgllahir
                                val versiapk        = response.body()?.data?.versiapk

                                // SIMPAN KE SHARED PREFERENCES
                                val sharedPref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
                                sharedPref.edit {
                                    putString("NAMA_PASIEN", namaPasien)
                                    putString("MEDREC", medrecPasien)
                                    putString("TGLLAHIR", tgllahirPasien)
                                    putString("VERSIAPK", versiapk)
                                    apply() // Gunakan apply() untuk menyimpan data secara asinkron
                                }
                                // Cek apakah data tersimpan dengan benar
                                Log.d("LoginActivity", "Data disimpan ke SharedPreferences: NAMA_PASIEN=$namaPasien, MEDREC=$medrecPasien")

                                // Lanjut ke MainActivity
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
    //                            binding.tanggalEditTextLayout.error = "Login gagal: ${response.body()?.message}"
                                val errorMessage = response.body()?.message ?: "Login gagal : Failed Respon BackEnd!"
                                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                showLoading(false)
                            }
                            Log.d("LoginResponse", response.body().toString())
                        }

                        override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
    //                        binding.tanggalEditTextLayout.error = "Terjadi kesalahan jaringan"
                            showLoading(false)
                            Toast.makeText(this@LoginActivity, "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
                        }
                    })
                    Log.d("Login Success", loginRequest.toString())
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBarLogin.alpha = 0f
            binding.progressBarLogin.visibility = View.VISIBLE
            binding.progressBarLogin.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        } else {
            binding.progressBarLogin.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.progressBarLogin.visibility = View.GONE
                }
                .start()
        }
    }

}