package com.example.melbourne_commuter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.melbourne_commuter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFindParking.setOnClickListener {
            Toast.makeText(this, "Finding Parking...", Toast.LENGTH_SHORT).show()
        }

        binding.btnPrediction.setOnClickListener {
            Toast.makeText(this, "Predicting Availability...", Toast.LENGTH_SHORT).show()
        }

        binding.btnCarbon.setOnClickListener {
            Toast.makeText(this, "Viewing Carbon & Growth...", Toast.LENGTH_SHORT).show()
        }

        binding.btnAbout.setOnClickListener {
            Toast.makeText(this, "Opening About...", Toast.LENGTH_SHORT).show()
        }

        binding.btnRefresh.setOnClickListener {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        }
    }
}
