package com.example.melbourne_commuter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.melbourne_commuter.activity.AboutActivity
import com.example.melbourne_commuter.activity.CarbonGrowthActivity
import com.example.melbourne_commuter.activity.MapActivity
import com.example.melbourne_commuter.activity.ParkingPredictionActivity
import com.example.melbourne_commuter.databinding.ActivityMainBinding

//AIzaSyA50vWKlZF-ZvrfVDuPcANNMSGn1KpaNTs

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFindParking.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        binding.btnPrediction.setOnClickListener {
            startActivity(Intent(this, ParkingPredictionActivity::class.java))
        }

        binding.btnCarbon.setOnClickListener {
            startActivity(Intent(this, CarbonGrowthActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.btnRefresh.setOnClickListener {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        }
    }
}
