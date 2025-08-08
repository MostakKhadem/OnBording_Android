package com.example.melbourne_commuter.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.melbourne_commuter.R



import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.melbourne_commuter.databinding.MapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: MapBinding
    private var map: GoogleMap? = null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) enableMyLocation()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.melbourne_commuter.R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSearch.setOnClickListener {
            val q = binding.etSearch.text.toString().trim()
            if (q.isEmpty()) {
                Toast.makeText(this, "Enter a place to search", Toast.LENGTH_SHORT).show()
            } else {
                searchAndMove(q)
            }
        }

        binding.btnMyLocation.setOnClickListener {
            requestLocationPermissionIfNeeded()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isZoomControlsEnabled = true

        // Default view: Melbourne CBD
        val melbourne = LatLng(-37.8136, 144.9631)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne, 12f))

        requestLocationPermissionIfNeeded()
    }

    private fun requestLocationPermissionIfNeeded() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun enableMyLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                map?.isMyLocationEnabled = true
            }
        } catch (_: SecurityException) { /* ignore */ }
    }

    private fun searchAndMove(query: String) {
        lifecycleScope.launchWhenStarted {
            val result = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(this@MapActivity, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val list = geocoder.getFromLocationName(query, 1)
                    list?.firstOrNull()
                } catch (e: Exception) {
                    null
                }
            }

            if (result != null) {
                val latLng = LatLng(result.latitude, result.longitude)
                map?.clear()
                map?.addMarker(MarkerOptions().position(latLng).title(query))
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(this@MapActivity, "Place not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
