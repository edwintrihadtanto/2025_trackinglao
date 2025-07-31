package com.example.mybottomnavigation.ui.splashscreen

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import com.example.mybottomnavigation.R
import com.example.mybottomnavigation.databinding.ActivitySplashScreenBinding
import com.example.mybottomnavigation.ui.login.LoginActivity

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Fullscreen
        /*window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()*/
        setupView()

        // Animasi Fade-In untuk logo dan title
        animateSplash()

        // Pindah ke LoginActivity setelah 3 detik
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }

    private fun animateSplash() {
        val logoAnimator = ObjectAnimator.ofFloat(binding.splashLogo, "alpha", 0f, 1f)
        logoAnimator.duration = 1500
        logoAnimator.start()

        val titleAnimator = ObjectAnimator.ofFloat(binding.splashTitle, "translationY", 100f, 0f)
        titleAnimator.duration = 2000
        titleAnimator.start()
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
}