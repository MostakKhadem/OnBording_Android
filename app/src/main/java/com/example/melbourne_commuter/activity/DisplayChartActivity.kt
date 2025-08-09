package com.example.melbourne_commuter.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_END_YEAR
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_GROWTH_JSON
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_MILEAGE_KM
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_MODE
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_START_YEAR
import com.example.melbourne_commuter.activity.CarbonGrowthActivity.Companion.EXTRA_VEHICLE_TYPE
import com.example.melbourne_commuter.data.model.CarGrowth
import com.example.melbourne_commuter.databinding.DisplayChartBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.roundToInt

class DisplayChartActivity : AppCompatActivity() {

    private lateinit var binding: DisplayChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DisplayChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        //binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        when (intent.getStringExtra(EXTRA_MODE)) {
            "growth_online" -> showGrowthChart()
            "carbon"        -> showCarbonChart()
            else            -> showEmpty()
        }
    }

    // -------- Growth Chart (BarChart) ----------
    private fun showGrowthChart() {
        val start = intent.getIntExtra(EXTRA_START_YEAR, -1)
        val end   = intent.getIntExtra(EXTRA_END_YEAR, -1)
        val json  = intent.getStringExtra(EXTRA_GROWTH_JSON).orEmpty()

        val type = object : TypeToken<List<CarGrowth>>() {}.type
        val items: List<CarGrowth> = Gson().fromJson(json, type)

        binding.title.text = "Australian Car Growth ($start → $end)"
        binding.subtitle.text = "Growth percentage per year-range"

        // Build entries
        val entries = ArrayList<BarEntry>()
        val labels  = ArrayList<String>()

        items.forEachIndexed { index, item ->
            entries += BarEntry(index.toFloat(), item.growth_percentage.toFloat())
            labels  += "${item.year_from}→${item.year_to}"
        }

        val dataSet = BarDataSet(entries, "% growth").apply {
            color = Color.parseColor("#2BB673")         // green-ish
            valueTextColor = Color.DKGRAY
            valueTextSize = 12f
        }

        val barData = BarData(dataSet).apply {
            setDrawValues(true)
            barWidth = 0.5f
        }

        val chart = binding.barChart
        chart.data = barData

        // X Axis labels (year_from→year_to)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(labels)
            textColor = Color.DKGRAY
            textSize = 12f
        }

        // Left axis is %; Right axis off
        chart.axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
            textColor = Color.DKGRAY
            textSize = 12f
        }
        chart.axisRight.isEnabled = false

        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.setFitBars(true)
        chart.setPinchZoom(true)
        chart.animateY(900)

        chart.invalidate()
    }

    // -------- Carbon Chart (BarChart) ----------
    private fun showCarbonChart() {
        val type = intent.getStringExtra(EXTRA_VEHICLE_TYPE).orEmpty()
        val km   = intent.getIntExtra(EXTRA_MILEAGE_KM, 0)

        // Dummy emission factors (kg CO2 per km)
        val factor = when {
            type.contains("electric", ignoreCase = true) -> 0.05
            type.contains("motor", ignoreCase = true)    -> 0.09
            type.contains("truck", ignoreCase = true)    -> 0.30
            type.contains("suv", ignoreCase = true)      -> 0.20
            else                                         -> 0.12 // vehicle/sedan/hybrid default
        }
        val emissionsKg = (km * factor)

        binding.title.text = "Carbon Footprint"
        binding.subtitle.text = "Vehicle: $type • Mileage: $km km • Est. CO₂: ${emissionsKg.roundToInt()} kg"

        val entries = arrayListOf(
            BarEntry(0f, km.toFloat()),
            BarEntry(1f, emissionsKg.toFloat())
        )
        val labels = listOf("Mileage (km)", "CO₂ (kg)")

        val dataSet = BarDataSet(entries, "Mileage & Estimated CO₂").apply {
            colors = listOf(
                Color.parseColor("#215D89"),  // blue for mileage
                Color.parseColor("#F48CA1")   // pink for CO2
            )
            valueTextColor = Color.DKGRAY
            valueTextSize = 12f
        }

        val barData = BarData(dataSet).apply { barWidth = 0.5f }

        val chart = binding.barChart
        chart.data = barData

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(labels)
            textColor = Color.DKGRAY
            textSize = 12f
        }

        chart.axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
            textColor = Color.DKGRAY
            textSize = 12f
        }
        chart.axisRight.isEnabled = false

        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.setFitBars(true)
        chart.setPinchZoom(true)
        chart.animateY(900)
        chart.invalidate()
    }

    private fun showEmpty() {
        binding.title.text = "Display Chart"
        binding.subtitle.text = "No data to display"
        binding.barChart.clear()
    }
}
