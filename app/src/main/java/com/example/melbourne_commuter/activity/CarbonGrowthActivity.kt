package com.example.melbourne_commuter.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.melbourne_commuter.R



import android.content.Intent

import android.widget.ArrayAdapter
import android.widget.Toast

import com.example.melbourne_commuter.databinding.CarbonGrowthBinding

class CarbonGrowthActivity : AppCompatActivity() {

    private lateinit var binding: CarbonGrowthBinding

    companion object {
        const val EXTRA_MODE = "mode"                 // "growth" | "carbon"
        const val EXTRA_START_YEAR = "start_year"
        const val EXTRA_END_YEAR = "end_year"
        const val EXTRA_VEHICLE_TYPE = "vehicle_type"
        const val EXTRA_MILEAGE_KM = "mileage_km"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CarbonGrowthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupYearSpinners()
        setupVehicleSpinner()

        // Growth -> DisplayChart
        binding.btnShowGrowth.setOnClickListener {
            val start = binding.spinnerYearFrom.selectedItem.toString().toInt()
            val end = binding.spinnerYearTo.selectedItem.toString().toInt()

            if (start > end) {
                Toast.makeText(this, "Start year must be ≤ End year", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DisplayChartActivity::class.java).apply {
                putExtra(EXTRA_MODE, "growth")
                putExtra(EXTRA_START_YEAR, start)
                putExtra(EXTRA_END_YEAR, end)
            }
            startActivity(intent)
        }

        // Carbon -> DisplayChart
        binding.btnShowCarbon.setOnClickListener {
            val vehicle = binding.spinnerVehicle.selectedItem?.toString().orEmpty()
            val mileageText = binding.inputMileage.text?.toString()?.trim().orEmpty()
            val mileage = mileageText.filter { it.isDigit() }.toIntOrNull()

            if (vehicle.isBlank()) {
                Toast.makeText(this, "Please select a vehicle type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (mileage == null) {
                Toast.makeText(this, "Enter mileage in km", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DisplayChartActivity::class.java).apply {
                putExtra(EXTRA_MODE, "carbon")
                putExtra(EXTRA_VEHICLE_TYPE, vehicle)
                putExtra(EXTRA_MILEAGE_KM, mileage)
            }
            startActivity(intent)
        }
    }

    private fun setupYearSpinners() {
        val years = (2000..2030).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        binding.spinnerYearFrom.adapter = adapter
        binding.spinnerYearTo.adapter = adapter

        // Defaults like screenshot
        binding.spinnerYearFrom.setSelection(years.indexOf("2020").coerceAtLeast(0))
        binding.spinnerYearTo.setSelection(years.indexOf("2021").coerceAtLeast(0))
    }

    private fun setupVehicleSpinner() {
        val vehicles = listOf(
            "Vehicle/sedan/hybrid",
            "SUV",
            "Truck",
            "Motorbike",
            "Electric (EV)"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, vehicles)
        binding.spinnerVehicle.adapter = adapter
        binding.spinnerVehicle.setSelection(0)
    }
}
