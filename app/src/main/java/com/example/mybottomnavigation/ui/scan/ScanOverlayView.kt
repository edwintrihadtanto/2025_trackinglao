package com.example.mybottomnavigation.ui.scan

//import android.view.Shader
import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.RectF
//import android.graphics.RenderEffect
//import android.graphics.Shader
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.mybottomnavigation.R
import androidx.core.graphics.withSave

class ScanOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paintDim = Paint().apply {
        color = "#88000000".toColorInt() // semi-transparan
    }

    private val paintBorder = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.WHITE
        alpha = 30 // transparan
        strokeWidth = 1f
    }

    private val cornerRadius = 20f
    private var scanRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val width = 600f
        val height = 600f
        val left = (w - width) / 2
        val top = (h - height) / 2
        scanRect = RectF(left, top, left + width, top + height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Area luar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+, tambahkan efek blur
            setRenderEffect(
                RenderEffect.createBlurEffect(
                    20f, 20f, Shader.TileMode.CLAMP
                )
            )
        }

        // Gambar area luar (di luar scanRect)
        canvas.withSave {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                clipOutRect(scanRect)
            } else {
                @Suppress("DEPRECATION")
                clipRect(scanRect, android.graphics.Region.Op.DIFFERENCE)
            }
            drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintDim)
        }

        // Gambar border kotak scan
        canvas.drawRoundRect(scanRect, cornerRadius, cornerRadius, paintBorder)

        // Tambahkan grid halus
        drawGrid(canvas, scanRect)
    }

    private fun drawGrid(canvas: Canvas, rect: RectF) {
        val step = 40f
        for (x in rect.left + step..rect.right - step step step) {
            canvas.drawLine(x, rect.top, x, rect.bottom, gridPaint)
        }
        for (y in rect.top + step..rect.bottom - step step step) {
            canvas.drawLine(rect.left, y, rect.right, y, gridPaint)
        }
    }

    // Extension untuk range with step di Float
    private infix fun ClosedFloatingPointRange<Float>.step(step: Float) = sequence {
        var current = start
        while (current <= endInclusive) {
            yield(current)
            current += step
        }
    }
}
