package com.example.smartalert.ui.reportes

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.smartalert.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class ActivityMapSelection : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var selectedMarker: Marker? = null
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_selection)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        btnConfirmar.setOnClickListener {
            confirmarUbicacion()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        obtenerUbicacionActual()

        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng

            selectedMarker?.remove()
            selectedMarker = map.addMarker(
                MarkerOptions().position(latLng).title("Ubicación seleccionada")
            )

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        }
    }

    private fun obtenerUbicacionActual() {
        val fused = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        map.isMyLocationEnabled = true

        fused.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f))
            }
        }
    }

    private fun confirmarUbicacion() {
        if (selectedLatLng == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val geocoder = Geocoder(this, Locale("es", "MX"))
        val addressList = geocoder.getFromLocation(
            selectedLatLng!!.latitude,
            selectedLatLng!!.longitude,
            1
        )

        var direccionFormateada = "Ubicación seleccionada"
        var codigoPostal = ""
        var colonia = ""
        var ciudad = ""
        var estado = ""

        if (!addressList.isNullOrEmpty()) {
            val a = addressList[0]

            val calle = a.thoroughfare ?: ""
            colonia = a.subLocality ?: "Sin dato"
            ciudad = a.locality ?: "Sin dato"
            estado = a.adminArea ?: "Sin dato"
            codigoPostal = a.postalCode ?: ""

            direccionFormateada = listOf(calle, colonia, ciudad).filter { it.isNotBlank() }
                .joinToString(", ")
        }

        val data = intent
        data.putExtra("lat", selectedLatLng!!.latitude)
        data.putExtra("lng", selectedLatLng!!.longitude)
        data.putExtra("direccion", direccionFormateada)
        data.putExtra("codigo_postal", codigoPostal)
        data.putExtra("colonia", colonia)
        data.putExtra("ciudad", ciudad)
        data.putExtra("estado", estado)

        setResult(RESULT_OK, data)
        finish()
    }
}