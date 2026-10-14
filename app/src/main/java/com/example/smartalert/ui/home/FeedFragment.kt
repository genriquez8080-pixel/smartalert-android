package com.example.smartalert.ui.home

import android.Manifest
import com.google.android.gms.tasks.CancellationTokenSource
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.databinding.FragmentFeedBinding
import com.example.smartalert.model.Reporte
import com.example.smartalert.model.ReporteCache
import com.example.smartalert.repositories.FeedRepository
import com.example.smartalert.ui.feed.FeedAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var adapter: FeedAdapter
    private lateinit var repo: FeedRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        repo = FeedRepository(requireContext())
        adapter = FeedAdapter(emptyList())
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            obtenerUbicacion()
        }

        if (tienePermisoUbicacion()) {
            obtenerUbicacion()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }

        return binding.root
    }

    private fun tienePermisoUbicacion(): Boolean {
        val ctx = context ?: return false
        return ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        if (tienePermisoUbicacion()) obtenerUbicacion()
    }

    private fun obtenerUbicacion() {
        if (!tienePermisoUbicacion()) {
            binding.swipeRefresh.isRefreshing = false
            return
        }

        val cancellationTokenSource = CancellationTokenSource()
        @SuppressWarnings("MissingPermission")
        fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    cargarFeedConCache(loc.latitude, loc.longitude)
                } else {
                    binding.swipeRefresh.isRefreshing = false
                    Log.w("FeedFragment", "Ubicación nula, no se carga feed")
                }
            }
            .addOnFailureListener { e ->
                binding.swipeRefresh.isRefreshing = false
                Log.e("FeedFragment", "Error obteniendo ubicación: ${e.message}")
            }
    }

    private fun cargarFeedConCache(lat: Double, lng: Double) {
        binding.swipeRefresh.isRefreshing = true

        repo.cargarFeed(
            lat = lat,
            lng = lng,
            onSuccess = { lista: List<ReporteCache> ->
                if (!isAdded) return@cargarFeed
                binding.swipeRefresh.isRefreshing = false
                adapter.actualizar(lista)
                Log.d("FeedFragment", "Feed API + cache: ${lista.size}")
            },
            onFallback = { cache: List<ReporteCache> ->
                if (!isAdded) return@cargarFeed
                binding.swipeRefresh.isRefreshing = false
                adapter.actualizar(cache)
                Log.w("FeedFragment", "Mostrando cache SQLite: ${cache.size}")
            },
            onError = { t ->
                if (!isAdded) return@cargarFeed
                Toast.makeText(requireContext(), "Sin conexión, mostrando cache", Toast.LENGTH_SHORT).show()
                Log.e("FeedFragment", "Error API, usando cache", t)
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}