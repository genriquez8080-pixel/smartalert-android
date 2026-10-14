package com.example.smartalert.ui.perfil

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartalert.R
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.model.Reporte
import com.example.smartalert.ui.reportes.EditarReporteActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ProfileReportesAdapter(
    private var lista: MutableList<Reporte>
) : RecyclerView.Adapter<ProfileReportesAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDescripcion: TextView = v.findViewById(R.id.tvDescripcion)
        val tvFecha: TextView = v.findViewById(R.id.tvFecha)
        val tvDireccion: TextView = v.findViewById(R.id.tvDireccion)
        val tvUsuario: TextView = v.findViewById(R.id.tvUsuario)
        val tvEstado: TextView = v.findViewById(R.id.tvEstado)
        val btnOpciones: ImageButton = v.findViewById(R.id.btnOpciones)
        val imgReporte: ImageView = v.findViewById(R.id.imgReporte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val reporte = lista[position]

        holder.tvUsuario.text = reporte.usuario?.nombre ?: "Usuario"
        holder.tvFecha.text = fechaInteligente(reporte.fechaReporte)
        holder.tvDescripcion.text = reporte.descripcion ?: ""
        holder.tvDireccion.text = reporte.area?.let { "${it.colonia}, ${it.ciudad}" } ?: "Sin dirección"

        val urlImagen = reporte.imagenUrl?.let {
            RetrofitClient.BASE_URL + it
        }


        Glide.with(holder.itemView.context)
            .load(urlImagen)
            .placeholder(R.drawable.bg_landscape)
            .error(R.drawable.bg_landscape)
            .into(holder.imgReporte)

        holder.tvEstado.text = reporte.estado.replaceFirstChar { it.uppercase() }
        holder.tvEstado.setTextColor(
            when (reporte.estado.lowercase(Locale.getDefault())) {
                "aprobado" -> holder.itemView.context.getColor(R.color.green)
                "pendiente" -> holder.itemView.context.getColor(R.color.yellow)
                "rechazado" -> holder.itemView.context.getColor(R.color.red)
                else -> holder.itemView.context.getColor(R.color.gray)
            }
        )

        holder.btnOpciones.setOnClickListener {
            reporte.idReporte?.let { id ->
                mostrarBottomSheet(it, id)
            }
        }
    }

    private fun mostrarBottomSheet(view: View, idReporte: Int) {
        val dialog = BottomSheetDialog(view.context)
        val sheetView = LayoutInflater.from(view.context)
            .inflate(R.layout.bottom_sheet_opciones_reporte, null)

        // 👉 EDITAR
        sheetView.findViewById<LinearLayout>(R.id.opEditar).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(view.context, EditarReporteActivity::class.java)
            intent.putExtra("id_reporte", idReporte)
            view.context.startActivity(intent)
        }

        // 👉 ELIMINAR
        sheetView.findViewById<LinearLayout>(R.id.opEliminar).setOnClickListener {
            dialog.dismiss()
            eliminarReporte(idReporte, view)
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun eliminarReporte(idReporte: Int, view: View) {
        RetrofitClient.reporteApi.eliminarReporte(idReporte).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(view.context, "Reporte eliminado", Toast.LENGTH_SHORT).show()

                    // ✅ Buscar el índice real en la lista y eliminar
                    val index = lista.indexOfFirst { it.idReporte == idReporte }
                    if (index != -1) {
                        lista.removeAt(index)
                        notifyItemRemoved(index)
                        notifyItemRangeChanged(index, lista.size)
                    }

                } else {
                    Toast.makeText(view.context, "No se pudo eliminar el reporte", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(view.context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fechaInteligente(fechaString: String?): String {
        if (fechaString.isNullOrEmpty()) return "Sin fecha"
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fecha = formato.parse(fechaString) ?: return "Sin fecha"

            val ahora = Date()
            val diffMillis = ahora.time - fecha.time

            val minutos = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val horas = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val dias = TimeUnit.MILLISECONDS.toDays(diffMillis)

            when {
                minutos < 60 -> "Hace ${minutos.coerceAtLeast(1)} min"
                horas < 24 -> "Hace ${horas}h"
                dias in 1..3 -> "Hace ${dias} días"
                else -> {
                    val calFecha = Calendar.getInstance().apply { time = fecha }
                    val calAhora = Calendar.getInstance()
                    val mismoAnio = calFecha.get(Calendar.YEAR) == calAhora.get(Calendar.YEAR)
                    val formatoFinal = if (mismoAnio) SimpleDateFormat("dd MMM", Locale("es", "ES"))
                    else SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                    formatoFinal.format(fecha)
                }
            }
        } catch (e: Exception) {
            "Sin fecha"
        }
    }

    fun actualizar(nueva: List<Reporte>) {
        lista = nueva.toMutableList()
        notifyDataSetChanged()
    }
}
