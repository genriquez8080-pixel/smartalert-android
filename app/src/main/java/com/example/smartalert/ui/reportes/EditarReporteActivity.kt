package com.example.smartalert.ui.reportes

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.smartalert.R
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.model.Reporte
import com.example.smartalert.model.TipoReporte
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class EditarReporteActivity : AppCompatActivity() {

    private lateinit var spinnerTipo: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var imgReporte: ImageView
    private lateinit var btnLocation: Button
    private lateinit var btnGuardar: Button
    private lateinit var tvUserName: TextView
    private lateinit var tvDireccion: TextView

    private var idReporte = 0
    private var latitud = 0.0
    private var longitud = 0.0
    private var codigoPostal = ""
    private var colonia = ""
    private var ciudad = ""
    private var estado = ""
    private var tipos: List<TipoReporte> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_reporte)

        spinnerTipo = findViewById(R.id.spinnerTipoReporte)
        etDescripcion = findViewById(R.id.etDescripcion)
        imgReporte = findViewById(R.id.imgReporte)
        btnLocation = findViewById(R.id.btnLocation)
        btnGuardar = findViewById(R.id.btnEnviar)
        tvUserName = findViewById(R.id.tvUserName)
        tvDireccion = findViewById(R.id.tvDireccion)

        idReporte = intent.getIntExtra("id_reporte", 0)

        cargarTipos()

        btnLocation.setOnClickListener {
            mapLauncher.launch(Intent(this, ActivityMapSelection::class.java))
        }

        btnGuardar.setOnClickListener {
            actualizarReporte()
        }
    }

    private fun cargarTipos() {
        RetrofitClient.tipoReporteApi.getTiposReporte()
            .enqueue(object : Callback<com.example.smartalert.model.TipoReporteResponse> {
                override fun onResponse(
                    call: Call<com.example.smartalert.model.TipoReporteResponse>,
                    response: Response<com.example.smartalert.model.TipoReporteResponse>
                ) {
                    tipos = response.body()?.tipos ?: emptyList()
                    spinnerTipo.adapter = ArrayAdapter(
                        this@EditarReporteActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        tipos.map { it.nombre_tipo }
                    )
                    precargarReporte()
                }

                override fun onFailure(call: Call<com.example.smartalert.model.TipoReporteResponse>, t: Throwable) {
                    Toast.makeText(this@EditarReporteActivity, "Error tipos", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun precargarReporte() {
        RetrofitClient.reporteApi.buscarReporte(idReporte)
            .enqueue(object : Callback<Reporte> {
                override fun onResponse(call: Call<Reporte>, response: Response<Reporte>) {
                    val r = response.body() ?: return

                    etDescripcion.setText(r.descripcion)
                    latitud = r.latitud ?: 0.0
                    longitud = r.longitud ?: 0.0
                    codigoPostal = r.area?.codigoPostal ?: ""
                    colonia = r.area?.colonia ?: "Sin dato"
                    ciudad = r.area?.ciudad ?: "Sin dato"
                    estado = r.area?.estado ?: "Sin dato"

                    tvUserName.text = r.usuario?.nombre ?: "Usuario"
                    tvDireccion.text = r.area?.let {
                        "${it.colonia}, ${it.ciudad}"
                    } ?: "Sin ubicación"

                    Glide.with(this@EditarReporteActivity)
                        .load(RetrofitClient.BASE_URL + r.imagenUrl)
                        .into(imgReporte)

                    r.tipo?.let {
                        val index = tipos.indexOfFirst { t -> t.id_tipo == it.id_tipo }
                        if (index >= 0) spinnerTipo.setSelection(index)
                    }
                }

                override fun onFailure(call: Call<Reporte>, t: Throwable) {
                    Toast.makeText(this@EditarReporteActivity, "Error reporte", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private val mapLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                latitud = result.data!!.getDoubleExtra("lat", 0.0)
                longitud = result.data!!.getDoubleExtra("lng", 0.0)
                tvDireccion.text = result.data!!.getStringExtra("direccion") ?: "Ubicación seleccionada"

                // Obtener datos de área desde el mapa
                codigoPostal = result.data!!.getStringExtra("codigo_postal") ?: ""
                colonia = result.data!!.getStringExtra("colonia") ?: "Sin dato"
                ciudad = result.data!!.getStringExtra("ciudad") ?: "Sin dato"
                estado = result.data!!.getStringExtra("estado") ?: "Sin dato"
            }
        }

    private fun actualizarReporte() {
        val tipo = tipos[spinnerTipo.selectedItemPosition]

        RetrofitClient.reporteApi.actualizarReporte(
            idReporte,
            tipo.id_tipo,
            etDescripcion.text.toString(),
            latitud,
            longitud,
            codigoPostal,
            colonia,
            ciudad,
            estado
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Toast.makeText(this@EditarReporteActivity, "Reporte actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EditarReporteActivity, "Error actualizar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Salir sin guardar?")
            .setPositiveButton("Sí") { _, _ -> super.onBackPressed() }
            .setNegativeButton("No", null)
            .show()
    }
}
