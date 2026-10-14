package com.example.smartalert.ui.home

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.smartalert.R
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.databinding.FragmentContributeBinding
import com.example.smartalert.model.TipoReporte
import com.example.smartalert.model.TipoReporteResponse
import com.example.smartalert.ui.reportes.ActivityMapSelection
import com.example.smartalert.utils.PreferencesHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale

class ContributeFragment : Fragment(R.layout.fragment_contribute) {

    private var _binding: FragmentContributeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var listaTiposReporte: List<TipoReporte> = emptyList()

    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private var reportLat: Double? = null
    private var reportLng: Double? = null

    private var codigoPostal = ""
    private var colonia = ""
    private var ciudad = ""
    private var estado = ""

    // ---------------- MAPA ----------------
    private val mapSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (!isAdded) return@registerForActivityResult

            if (result.resultCode == Activity.RESULT_OK) {
                val lat = result.data?.getDoubleExtra("lat", 0.0)
                val lng = result.data?.getDoubleExtra("lng", 0.0)

                if (lat != null && lng != null) {
                    reportLat = lat
                    reportLng = lng
                    procesarDireccion(lat, lng)
                }
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                abrirCamara()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de cámara denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    // ---------------- PERMISO UBICACIÓN ----------------
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) getCurrentLocation()
            else Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }

    // ---------------- GALERÍA ----------------
    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (!isAdded) return@registerForActivityResult
            uri?.let {
                selectedImageUri = it
                binding.imgPreview.setImageURI(it)
                binding.imgPreview.visibility = View.VISIBLE
            }
        }

    // ---------------- CÁMARA ----------------
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!isAdded) return@registerForActivityResult
            if (success && cameraImageUri != null) {
                selectedImageUri = cameraImageUri
                binding.imgPreview.setImageURI(cameraImageUri)
                binding.imgPreview.visibility = View.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContributeBinding.inflate(inflater, container, false)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        getCurrentLocation()
        cargarTiposReporte()

        binding.etDireccion.setOnClickListener {
            mapSelectionLauncher.launch(
                Intent(requireContext(), ActivityMapSelection::class.java)
            )
        }

        binding.btnAgregarFoto.setOnClickListener {
            mostrarOpcionesFoto()
        }

        binding.btnEnviar.setOnClickListener {
            enviarReporte()
        }

        return binding.root
    }

    // ---------------- DIÁLOGO FOTO ----------------
    private fun mostrarOpcionesFoto() {
        val opciones = arrayOf("Tomar foto", "Elegir de galería")

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Agregar foto")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamara()
                    1 -> openGalleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamara()
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        val context = requireContext()
        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")

        cameraImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        takePictureLauncher.launch(cameraImageUri)
    }

    // ---------------- DIRECCIÓN ----------------
    private fun procesarDireccion(lat: Double, lng: Double) {
        Thread {
            try {
                val geo = Geocoder(requireContext(), Locale.getDefault())
                val list: List<Address> = geo.getFromLocation(lat, lng, 1) ?: emptyList()

                if (list.isNotEmpty()) {
                    val addr = list.first()

                    codigoPostal = addr.postalCode ?: ""
                    colonia = addr.subLocality ?: ""
                    ciudad = addr.locality ?: ""
                    estado = addr.adminArea ?: ""

                    val direccion = buildString {
                        if (!addr.thoroughfare.isNullOrEmpty()) append(addr.thoroughfare)
                        if (colonia.isNotEmpty()) append(", $colonia")
                        if (ciudad.isNotEmpty()) append(", $ciudad")
                    }

                    activity?.runOnUiThread {
                        if (isAdded) binding.etDireccion.setText(direccion)
                    }
                }
            } catch (_: Exception) {}
        }.start()
    }

    // ---------------- UBICACIÓN ----------------
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (!isAdded) return@addOnSuccessListener
            location?.let {
                reportLat = it.latitude
                reportLng = it.longitude
                procesarDireccion(it.latitude, it.longitude)
            }
        }
    }

    // ---------------- TIPOS DE REPORTE ----------------
    private fun cargarTiposReporte() {
        RetrofitClient.tipoReporteApi.getTiposReporte()
            .enqueue(object : Callback<TipoReporteResponse> {
                override fun onResponse(
                    call: Call<TipoReporteResponse>,
                    response: Response<TipoReporteResponse>
                ) {
                    if (!isAdded) return
                    if (response.isSuccessful && response.body() != null) {
                        listaTiposReporte = response.body()!!.tipos
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            listaTiposReporte.map { it.nombre_tipo }
                        )
                        adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                        )
                        binding.spinnerTipoReporte.adapter = adapter
                    }
                }

                override fun onFailure(call: Call<TipoReporteResponse>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(context, "Error al cargar tipos", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ---------------- ENVIAR REPORTE ----------------
    private fun enviarReporte() {
        val descripcion = binding.etDescripcion.text.toString()
        val rating = binding.ratingBar.rating.toInt()
        val idUsuario = PreferencesHelper.getUserId(requireContext())

        if (idUsuario == -1 || reportLat == null || reportLng == null) {
            Toast.makeText(context, "Datos incompletos", Toast.LENGTH_SHORT).show()
            return
        }

        fun String.part(): RequestBody =
            this.toRequestBody("text/plain".toMediaType())

        var imagenPart: MultipartBody.Part? = null
        selectedImageUri?.let {
            val file = uriToFile(it)
            val body = file.asRequestBody("image/*".toMediaTypeOrNull())
            imagenPart = MultipartBody.Part.createFormData("imagen", file.name, body)
        }

        val idTipo = listaTiposReporte
            .getOrNull(binding.spinnerTipoReporte.selectedItemPosition)?.id_tipo ?: 1

        RetrofitClient.reporteApi.crearReporte(
            idUsuario.toString().part(),
            codigoPostal.part(),
            colonia.part(),
            ciudad.part(),
            estado.part(),
            idTipo.toString().part(),
            descripcion.part(),
            reportLat.toString().part(),
            reportLng.toString().part(),
            rating.toString().part(),
            imagenPart
        ).enqueue(object : Callback<Void> {

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!isAdded) return
                if (response.isSuccessful) {
                    Toast.makeText(context, "Reporte enviado", Toast.LENGTH_SHORT).show()
                    (activity as? HomeActivity)?.mostrarMapa()
                } else {
                    Toast.makeText(context, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(context, "Fallo: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ---------------- URI → FILE ----------------
    private fun uriToFile(uri: Uri): File {
        val ctx = requireContext()
        val file = File(ctx.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
