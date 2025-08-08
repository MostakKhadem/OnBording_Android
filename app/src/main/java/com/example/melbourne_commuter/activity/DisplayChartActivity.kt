package com.example.melbourne_commuter.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.melbourne_commuter.R


import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_END_YEAR
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_MILEAGE_KM
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_MODE
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_START_YEAR
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_VEHICLE_TYPE
import com.example.melbourne_commuter.databinding.DisplayChartBinding

class DisplayChartActivity : AppCompatActivity() {

    private lateinit var binding: DisplayChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DisplayChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val mode = intent.getStringExtra(EXTRA_MODE)

        if (mode == "growth") {
            val start = intent.getIntExtra(EXTRA_START_YEAR, -1)
            val end = intent.getIntExtra(EXTRA_END_YEAR, -1)
            binding.title.text = "Australian Car Growth"
            binding.subtitle.text = "Selected range: $start → $end"
        } else {
            val type = intent.getStringExtra(EXTRA_VEHICLE_TYPE).orEmpty()
            val km = intent.getIntExtra(EXTRA_MILEAGE_KM, 0)
            binding.title.text = "Carbon Footprint"
            binding.subtitle.text = "Vehicle: $type • Mileage: $km km"
        }
    }
}
