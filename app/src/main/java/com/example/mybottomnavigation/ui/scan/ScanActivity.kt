package com.example.mybottomnavigation.ui.scan

import android.animation.ObjectAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.mybottomnavigation.R
import com.example.mybottomnavigation.databinding.ActivityScanBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private var mediaPlayer: MediaPlayer? = null
    private var lastScannedValue: String? = null
    private var alreadyRedirected = false
    private var isFlashOn = false
    private var camera: Camera? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide() // ⬅️ Sembunyikan judul/actionbar

        mediaPlayer = MediaPlayer.create(this, R.raw.beep)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Minta izin kamera
        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        binding.scanLine.post {
            val overlayHeight = 600f // tinggi kotak scan (sesuai ScanOverlayView)
            val overlayTop = (binding.root.height - overlayHeight) / 2f
            val overlayBottom = overlayTop + overlayHeight

            ObjectAnimator.ofFloat(binding.scanLine, "translationY", overlayTop, overlayBottom).apply {
                duration = 2000
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        }

        // Toggle flashlight
        binding.btnFlashlight.setOnClickListener {
            camera?.let {
                isFlashOn = !isFlashOn
                it.cameraControl.enableTorch(isFlashOn)
                binding.btnFlashlight.setImageResource(
                    if (isFlashOn) R.drawable.ic_flash_off else R.drawable.ic_flash_on
                )
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val barcodeAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    barcodeAnalyzer
                )
            } catch (e: Exception) {
                Log.e("ScanActivity", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        if (rawValue != lastScannedValue && !alreadyRedirected) {
                            lastScannedValue = rawValue
                            alreadyRedirected = true // ⬅️ Cegah redirect berulang
                            Log.d("ScanActivity", "Barcode detected: $rawValue")

                            runOnUiThread {
                                mediaPlayer?.start()
                                val intent = Intent(this@ScanActivity, ScanResultActivity::class.java)
                                intent.putExtra("scan_result", rawValue)
                                startActivity(intent)
                                finish() // ⬅️ Tutup activity ini setelah redirect
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("ScanActivity", "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}
