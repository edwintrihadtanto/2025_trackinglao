package com.example.mybottomnavigation.ui.login

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mybottomnavigation.data.model.LoginRequest
import com.example.mybottomnavigation.data.model.LoginResponse
import com.example.mybottomnavigation.data.network.ApiConfig
import com.example.mybottomnavigation.databinding.ActivityLoginBinding
import com.example.mybottomnavigation.ui.main.MainActivity
import java.util.Calendar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
//    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.medrecEditText.setText("6465872");
        binding.tanggalEditText.setText("1992-08-03")
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
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            val medrec = binding.medrecEditText.text.toString()
            val tanggal = binding.tanggalEditText.text.toString()
            when {
                medrec.isEmpty() -> {
                    binding.medrecEditTextLayout.error = "Masukkan No. Medrec"
                }
                tanggal.isEmpty() -> {
                    binding.tanggalEditTextLayout.error = "Masukkan tanggal lahir"
                }
                else -> {
                    val loginRequest = LoginRequest(medrec, tanggal)
                    val apiService = ApiConfig.getApiService()
                    Log.e("Login Failed", LoginRequest(medrec, tanggal).toString());

                    apiService.login(loginRequest).enqueue(object : retrofit2.Callback<LoginResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<LoginResponse>,
                            response: retrofit2.Response<LoginResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                val namaPasien = response.body()?.data?.nama
                                val medrecPasien = response.body()?.data?.medrec

    //                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
    //                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    //                            startActivity(intent)
    //                            finish()

                                // SIMPAN KE SHARED PREFERENCES
                                val sharedPref = getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
                                sharedPref.edit()
                                    .putString("NAMA_PASIEN", namaPasien)
                                    .putString("MEDREC", medrecPasien)
                                    .apply()

                                // Lanjut ke MainActivity
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
    //                            binding.tanggalEditTextLayout.error = "Login gagal: ${response.body()?.message}"
                                // Login gagal, tampilkan Toast
                                val errorMessage = response.body()?.message ?: "Login gagal : "
                                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            Log.d("LoginResponse", response.body().toString())
                        }

                        override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
    //                        binding.tanggalEditTextLayout.error = "Terjadi kesalahan jaringan"
                            Toast.makeText(this@LoginActivity, "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
                        }
                    })
                    Log.d("Login Success", loginRequest.toString())


                }
            }
        }
    }



}