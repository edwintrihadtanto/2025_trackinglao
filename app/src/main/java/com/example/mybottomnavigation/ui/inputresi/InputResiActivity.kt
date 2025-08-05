package com.example.mybottomnavigation.ui.inputresi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.mybottomnavigation.databinding.ActivityInputresiBinding
import com.example.mybottomnavigation.ui.detail.DetailActivity

class InputResiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputresiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputresiBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.btnSubmit.setOnClickListener {
            val resi = binding.resiEditText.text.toString()
            when {
                resi.isEmpty() -> {
                    binding.resiEditTextLayout.error = "Masukkan nomor resi yang benar dan sesuai"
                }
                else -> {
                    val intent = Intent(this, DetailActivity::class.java).also {
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBarInputResi.alpha = 0f
            binding.progressBarInputResi.visibility = View.VISIBLE
            binding.progressBarInputResi.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        } else {
            binding.progressBarInputResi.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.progressBarInputResi.visibility = View.GONE
                }
                .start()
        }
    }
}