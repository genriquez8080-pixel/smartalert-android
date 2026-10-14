package com.example.smartalert.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.smartalert.R
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.model.Reporte
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapaFragment : Fragment(R.layout.fragment_mapa), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Para limpiar círculos anteriores
    private val circles: MutableList<Circle> = mutableListOf()

    // Para refrescar al volver al mapa
    private var lastLatLng: LatLng? = null

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineGranted || coarseGranted) {
                enableMyLocationAndLoadReportes()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Listener para tocar círculos
        googleMap?.setOnCircleClickListener { circle ->
            val descripcion = circle.tag as? String ?: "Reporte"
            Toast.makeText(requireContext(), descripcion, Toast.LENGTH_SHORT).show()
        }

        enableMyLocationAndLoadReportes()
    }

    override fun onResume() {
        super.onResume()
        lastLatLng?.let { cargarReportesAprobadosCercanos(it) }
    }

    private fun enableMyLocationAndLoadReportes() {
        val context = context ?: return

        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            try {
                googleMap?.isMyLocationEnabled = true
            } catch (_: SecurityException) { }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    val center = if (location != null) {
                        LatLng(location.latitude, location.longitude)
                    } else {
                        LatLng(19.4326, -99.1332) // fallback CDMX
                    }

                    lastLatLng = center
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 16f))

                    cargarReportesAprobadosCercanos(center)
                }
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun cargarReportesAprobadosCercanos(center: LatLng) {
        RetrofitClient.reporteApi
            .getReportesCercanos(center.latitude, center.longitude, radio = 10)
            .enqueue(object : Callback<List<Reporte>> {

                override fun onResponse(
                    call: Call<List<Reporte>>,
                    response: Response<List<Reporte>>
                ) {
                    if (!response.isSuccessful) return

                    val reportes = response.body().orEmpty()
                    val aprobados = reportes.filter { r ->
                        r.estado.equals("aprobado", ignoreCase = true) &&
                                r.latitud != null && r.longitud != null
                    }

                    pintarCirculos(aprobados)
                }

                override fun onFailure(call: Call<List<Reporte>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    private fun pintarCirculos(reportes: List<Reporte>) {
        val map = googleMap ?: return

        // Limpiar círculos anteriores
        circles.forEach { it.remove() }
        circles.clear()

        reportes.forEach { r ->
            val pos = LatLng(r.latitud!!, r.longitud!!)

            val color = when (r.severidad) {
                in 0..2 -> Color.YELLOW
                3 -> Color.parseColor("#FFA500") // naranja
                in 4..6 -> Color.RED
                else -> Color.GRAY
            }

            val fillColor = Color.argb(85, Color.red(color), Color.green(color), Color.blue(color))

            val circle = map.addCircle(
                CircleOptions()
                    .center(pos)
                    .radius(50.0) // metros, ajustar según preferencia
                    .strokeColor(color)
                    .fillColor(fillColor) // semi-transparente
                    .strokeWidth(3f)
                    .clickable(true)
            )

            circle.tag = r.descripcion ?: "Reporte"
            circles.add(circle)
        }
    }

}
