package com.example.melbourne_commuter.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.melbourne_commuter.R

import com.example.melbourne_commuter.databinding.AboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: AboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use toolbar as ActionBar with back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.about_title)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


    }
}
