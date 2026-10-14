package com.example.smartalert.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartalert.R
import com.example.smartalert.model.FeedItemContract
import java.text.SimpleDateFormat
import com.example.smartalert.api.RetrofitClient
import java.util.*
import java.util.concurrent.TimeUnit

class FeedAdapter(private var lista: List<FeedItemContract>) :
    RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    class PostViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvUsuario: TextView = v.findViewById(R.id.tvUsuario)
        val tvFecha: TextView = v.findViewById(R.id.tvFecha)
        val tvDireccion: TextView = v.findViewById(R.id.tvDireccion)
        val tvDescripcion: TextView = v.findViewById(R.id.tvDescripcion)
        val imgReporte: ImageView = v.findViewById(R.id.imgReporte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_post, parent, false)
        return PostViewHolder(v)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = lista[position]

        holder.tvUsuario.text = item.usuarioNombre
        holder.tvFecha.text = fechaInteligente(item.fecha)
        holder.tvDescripcion.text = item.descripcionTexto
        holder.tvDireccion.text = item.coloniaCiudad

        val img = item.imagen
        val urlImagen = img?.let { RetrofitClient.BASE_URL + it }

        Glide.with(holder.itemView.context)
            .load(urlImagen)
            .placeholder(R.drawable.bg_landscape)
            .error(R.drawable.bg_landscape)
            .into(holder.imgReporte)
    }

    fun actualizar(listaNueva: List<FeedItemContract>) {
        lista = listaNueva
        notifyDataSetChanged()
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

                    val formatoFecha = if (mismoAnio) {
                        SimpleDateFormat("dd MMM", Locale("es", "ES"))
                    } else {
                        SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                    }

                    formatoFecha.format(fecha)
                }
            }
        } catch (e: Exception) {
            "Sin fecha"
        }
    }
}
