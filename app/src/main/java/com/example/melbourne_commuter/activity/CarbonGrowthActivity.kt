package com.example.melbourne_commuter.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.melbourne_commuter.data.api.CarGrowthApi
import com.example.melbourne_commuter.data.model.CarGrowth
import com.example.melbourne_commuter.databinding.CarbonGrowthBinding
import com.example.melbourne_commuter.network.RetrofitProvider
import com.google.gson.Gson
import kotlinx.coroutines.launch

class CarbonGrowthActivity : AppCompatActivity() {

    private lateinit var binding: CarbonGrowthBinding

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_GROWTH_JSON = "growth_json"
        const val EXTRA_START_YEAR = "start_year"
        const val EXTRA_END_YEAR = "end_year"
        const val EXTRA_VEHICLE_TYPE = "vehicle_type"
        const val EXTRA_MILEAGE_KM = "mileage_km"
    }

    private val api: CarGrowthApi by lazy {
        RetrofitProvider.retrofit.create(CarGrowthApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CarbonGrowthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        //binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupYearSpinners()
        setupVehicleSpinner()

        // === Australian Car Growth -> call API ===
        binding.btnShowGrowth.setOnClickListener {
            val start = binding.spinnerYearFrom.selectedItem.toString().toInt()
            val end = binding.spinnerYearTo.selectedItem.toString().toInt()

            if (start > end) {
                Toast.makeText(this, "Start year must be ≤ End year", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setGrowthLoading(true)

            lifecycleScope.launch {
                try {
                    // GET /api/car_growth?from=YYYY&to=YYYY
                    val list: List<CarGrowth> = api.getCarGrowth(start, end)
                    val json = Gson().toJson(list)

                    startActivity(
                        Intent(this@CarbonGrowthActivity, DisplayChartActivity::class.java).apply {
                            putExtra(EXTRA_MODE, "growth_online")
                            putExtra(EXTRA_GROWTH_JSON, json)
                            putExtra(EXTRA_START_YEAR, start)
                            putExtra(EXTRA_END_YEAR, end)
                        }
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this@CarbonGrowthActivity,
                        "Failed to load growth: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    setGrowthLoading(false)
                }
            }
        }

        // === Carbon Footprint -> send selected values ===
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

            startActivity(
                Intent(this, DisplayChartActivity::class.java).apply {
                    putExtra(EXTRA_MODE, "carbon")
                    putExtra(EXTRA_VEHICLE_TYPE, vehicle)
                    putExtra(EXTRA_MILEAGE_KM, mileage)
                }
            )
        }
    }

    private fun setGrowthLoading(loading: Boolean) {
        binding.btnShowGrowth.isEnabled = !loading
        binding.btnShowGrowth.text = if (loading) "Loading..." else "Show Result"
    }

    private fun setupYearSpinners() {
        val years = (2000..2030).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        binding.spinnerYearFrom.adapter = adapter
        binding.spinnerYearTo.adapter = adapter

        binding.spinnerYearFrom.setSelection(years.indexOf("2020").coerceAtLeast(0))
        binding.spinnerYearTo.setSelection(years.indexOf("2021").coerceAtLeast(0))
    }

    private fun setupVehicleSpinner() {
        val vehicles = listOf(
            "Hybrid",
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
