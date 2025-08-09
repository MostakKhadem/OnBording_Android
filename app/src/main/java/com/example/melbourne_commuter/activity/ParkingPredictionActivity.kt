package com.example.melbourne_commuter.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.melbourne_commuter.R


import android.app.DatePickerDialog

import android.view.inputmethod.InputMethodManager

import com.example.melbourne_commuter.databinding.ParkingPredictionBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ParkingPredictionActivity : AppCompatActivity() {

    private lateinit var binding: ParkingPredictionBinding
    private val cal: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("d MMMM, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ParkingPredictionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
//        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Default date = today
        binding.inputDate.setText(dateFormat.format(cal.time))

        // Date picker
        binding.inputDate.setOnClickListener {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, day ->
                cal.set(year, month, day)
                binding.inputDate.setText(dateFormat.format(cal.time))
            }, y, m, d).show()
        }

        // Show result
        binding.btnShowResult.setOnClickListener {
            hideKeyboard()
            val location = binding.inputLocation.text?.toString()?.trim().orEmpty()
            val date = binding.inputDate.text?.toString()?.trim().orEmpty()

            val display = if (location.isBlank()) {
                "Please enter a location."
            } else {
                "Location: $location\nDate: $date"
            }
            binding.resultText.text = display
            binding.resultText.visibility = android.view.View.VISIBLE
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { view -> imm.hideSoftInputFromWindow(view.windowToken, 0) }
    }
}
