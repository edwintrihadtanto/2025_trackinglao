package com.example.mybottomnavigation.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mybottomnavigation.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
            val resep = binding.tvNoResep.text.toString()
            val poli = binding.tvNoPoli.text.toString()
            val jam = binding.tvJam.text.toString()
            val status = binding.tvStatus.text.toString()
        }
    }