package com.example.melbourne_commuter.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.melbourne_commuter.MainActivity
import com.example.melbourne_commuter.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.proceedButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.termsText.setOnClickListener {
            showTermsDialog()
        }
    }

    private fun showTermsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(com.example.melbourne_commuter.R.string.terms_title))
            .setMessage(getString(com.example.melbourne_commuter.R.string.terms_body))
            .setPositiveButton(getString(com.example.melbourne_commuter.R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
