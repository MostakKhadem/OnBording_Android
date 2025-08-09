package com.example.melbourne_commuter.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.melbourne_commuter.R
import com.example.melbourne_commuter.data.api.ParkingApi
import com.example.melbourne_commuter.data.model.ParkingRecord
import com.example.melbourne_commuter.databinding.MapBinding
import com.example.melbourne_commuter.network.MelbourneRetrofit
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: MapBinding
    private var map: GoogleMap? = null

    // API client
    private val api: ParkingApi by lazy {
        MelbourneRetrofit.retrofit.create(ParkingApi::class.java)
    }

    // Location client (for "My Location")
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

    // Ask runtime location permission
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    // optional radius circle (removed on refresh)
    private var radiusCircle: Circle? = null

    // default search radius for nearby queries (not used on Refresh)
    private val RADIUS_METERS = 800

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Search address → center + fetch nearby (kept)
        binding.btnSearch.setOnClickListener {
            val q = binding.etSearch.text.toString().trim()
            if (q.isEmpty()) {
                Toast.makeText(this, "Enter an address", Toast.LENGTH_SHORT).show()
            } else {
                searchAndCenter(q)
            }
        }

        // Use device location → center + fetch nearby (kept)
        binding.btnMyLocation.setOnClickListener {
            requestLocationPermissionIfNeeded { getCurrentLocationAndCenter() }
        }

        // REFRESH: clear UI + markers + radius, then show ALL available bays (no filtering)
        binding.btnRefreshApi.setOnClickListener {
            clearSearchUI()
            clearRadius()
            fetchAndShowAll() // <— ONLY shows available parking from API
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isZoomControlsEnabled = true

        // Default: Melbourne CBD
        val melbourne = LatLng(-37.8136, 144.9631)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne, 13f))

        requestLocationPermissionIfNeeded { enableMyLocation() }

        // When user taps a marker (or its info window), ask Eco vs Regular
        map?.setOnMarkerClickListener { marker ->
            showEcoDialog(marker)
            true // consume, we handle it
        }
        map?.setOnInfoWindowClickListener { marker ->
            showEcoDialog(marker)
        }

        // First load: show all available
        fetchAndShowAll()
    }

    // ------------- Eco dialog -------------

    private fun showEcoDialog(marker: Marker) {
        val dest = marker.position
        AlertDialog.Builder(this)
            .setTitle("Eco Parking?")
            .setMessage("Do you want Eco Parking?\n\nEco will search nearby public transport around the selected spot.")
            .setPositiveButton("Yes (Eco)") { _, _ ->
                // Search public transport near this parking spot in Google Maps
                openMapsSearchNear(dest, "tram stop OR bus stop OR train station")
            }
            .setNegativeButton("No (Navigate)") { _, _ ->
                // Regular navigation to the parking spot
                openMapsNavigation(dest)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun openMapsNavigation(latLng: LatLng) {
        val uri = Uri.parse("google.navigation:q=${latLng.latitude},${latLng.longitude}&mode=d")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        startActivity(intent)
    }

    private fun openMapsSearchNear(latLng: LatLng, query: String) {
        // geo intent that opens Google Maps search near coordinates
        val uri = Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        startActivity(intent)
    }

    // ------------------  REFRESH helpers  ------------------

    private fun clearSearchUI() {
        binding.etSearch.setText("")
        binding.etSearch.clearFocus()
        hideKeyboard()
    }

    private fun clearRadius() {
        radiusCircle?.remove()
        radiusCircle = null
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { v -> imm.hideSoftInputFromWindow(v.windowToken, 0) }
    }

    // ------------------  Permissions & My Location  ------------------

    private fun requestLocationPermissionIfNeeded(onGranted: (() -> Unit)? = null) {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            onGranted?.invoke()
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
            val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
                map?.isMyLocationEnabled = true
            }
        } catch (_: SecurityException) { /* ignore */ }
    }

    private fun getCurrentLocationAndCenter() {
        try {
            val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) return

            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val here = LatLng(loc.latitude, loc.longitude)
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(here, 15f))
                    // Nearby flow (kept)
                    fetchAndShowNearby(here, RADIUS_METERS)
                } else {
                    Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Location error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (_: SecurityException) { }
    }

    // ------------------  Search address flow (kept)  ------------------

    private fun searchAndCenter(query: String) {
        lifecycleScope.launch {
            val geo = withContext(Dispatchers.IO) {
                try {
                    val g = Geocoder(this@MapActivity, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    g.getFromLocationName(query, 1)?.firstOrNull()
                } catch (e: Exception) { null }
            }
            if (geo != null) {
                val point = LatLng(geo.latitude, geo.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
                fetchAndShowNearby(point, RADIUS_METERS)
            } else {
                Toast.makeText(this@MapActivity, "Address not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ------------------  SHOW ALL AVAILABLE (used by Refresh)  ------------------

    private fun fetchAndShowAll() {
        val m = map ?: return
        lifecycleScope.launch {
            try {
                setLoading(true)

                val batchSize = 50
                val maxPages = 6 // ~300 points
                val all = mutableListOf<ParkingRecord>()
                var page = 0

                while (page < maxPages) {
                    val resp = withContext(Dispatchers.IO) {
                        api.getUnoccupied(limit = batchSize, offset = page * batchSize)
                    }
                    if (resp.results.isEmpty()) break
                    all += resp.results
                    page++
                }

                if (all.isEmpty()) {
                    val fb = withContext(Dispatchers.IO) { api.getUnoccupied(limit = 30, offset = 0) }
                    all += fb.results
                }

                m.clear()
                clearRadius()

                if (all.isEmpty()) {
                    Toast.makeText(this@MapActivity, "No unoccupied bays found.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val bounds = LatLngBounds.Builder()
                var count = 0
                all.forEach { r ->
                    val loc = r.location ?: return@forEach
                    val pos = LatLng(loc.lat, loc.lon)
                    val title = "Zone: ${r.zone_number ?: "N/A"}"
                    val snippet = "Kerbside: ${r.kerbsideid ?: "N/A"} • ${r.status_timestamp ?: "-"}"

                    m.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(title)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    bounds.include(pos)
                    count++
                }

                m.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                Toast.makeText(this@MapActivity, "Loaded $count available spots", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MapActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    // ------------------  SHOW NEARBY (used by search / my location)  ------------------

    private fun fetchAndShowNearby(center: LatLng, radiusMeters: Int) {
        val m = map ?: return
        lifecycleScope.launch {
            try {
                setLoading(true)

                val batchSize = 50
                val maxPages = 10 // up to 500 rows
                val collected = mutableListOf<ParkingRecord>()
                var page = 0

                while (page < maxPages) {
                    val resp = withContext(Dispatchers.IO) {
                        api.getUnoccupied(limit = batchSize, offset = page * batchSize)
                    }
                    if (resp.results.isEmpty()) break

                    resp.results.forEach { r ->
                        r.location?.let { gp ->
                            val d = distanceMeters(center.latitude, center.longitude, gp.lat, gp.lon)
                            if (d <= radiusMeters) collected += r
                        }
                    }

                    if (collected.size >= 25) break
                    page++
                }

                if (collected.isEmpty()) {
                    val fb = withContext(Dispatchers.IO) { api.getUnoccupied(limit = 30, offset = 0) }
                    fb.results.forEach { r ->
                        r.location?.let { gp ->
                            val d = distanceMeters(center.latitude, center.longitude, gp.lat, gp.lon)
                            if (d <= radiusMeters) collected += r
                        }
                    }
                }

                m.clear()
                clearRadius()

                if (collected.isEmpty()) {
                    Toast.makeText(this@MapActivity, "No unoccupied bays near you.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val bounds = LatLngBounds.Builder()
                collected.forEach { r ->
                    val pos = LatLng(r.location!!.lat, r.location.lon)
                    val title = "Zone: ${r.zone_number ?: "N/A"}"
                    val snippet = "Kerbside: ${r.kerbsideid ?: "N/A"} • ${r.status_timestamp ?: "-"}"

                    m.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(title)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    bounds.include(pos)
                }
                bounds.include(center)

                m.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))

                // Draw radius circle for context (removed on Refresh)
                radiusCircle = m.addCircle(
                    CircleOptions()
                        .center(center)
                        .radius(radiusMeters.toDouble())
                        .strokeColor(0x88215D89.toInt())
                        .fillColor(0x33215D89)
                        .strokeWidth(2f)
                )

                Toast.makeText(this@MapActivity, "Found ${collected.size} spots nearby", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MapActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRefreshApi.isEnabled = !loading
        binding.btnMyLocation.isEnabled = !loading
        binding.btnSearch.isEnabled = !loading
        binding.btnRefreshApi.text = if (loading) "Loading…" else "Refresh"
    }

    // Haversine distance in meters
    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
