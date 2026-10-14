package com.example.smartalert.repositories

import android.content.Context
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.dao.ReporteCacheDao
import com.example.smartalert.model.Reporte
import com.example.smartalert.model.ReporteCache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedRepository(context: Context) {

    private val dao = ReporteCacheDao(context)

    fun cargarFeed(
        lat: Double,
        lng: Double,
        onSuccess: (List<ReporteCache>) -> Unit,
        onFallback: (List<ReporteCache>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        RetrofitClient.reporteApi.getReportesCercanos(lat, lng).enqueue(object : Callback<List<Reporte>> {
            override fun onResponse(call: Call<List<Reporte>>, response: Response<List<Reporte>>) {
                if (response.isSuccessful && response.body() != null) {
                    val apiList = response.body()!!

                    val cacheList = apiList.mapNotNull { r ->
                        val id = r.idReporte ?: return@mapNotNull null
                        ReporteCache(
                            idReporte = id,
                            usuarioNombre = r.usuario?.nombre ?: "Usuario",
                            fechaReporte = r.fechaReporte,
                            colonia = r.area?.colonia,
                            ciudad = r.area?.ciudad,
                            descripcion = r.descripcion,
                            imagenUrl = r.imagenUrl
                        )
                    }

                    // Guardar cache (reemplaza todo)
                    dao.limpiarTodo()
                    cacheList.forEach { dao.insertar(it) }

                    onSuccess(cacheList)
                } else {
                    // si la API respondió pero mal, usamos cache
                    onFallback(dao.obtenerTodos())
                }
            }

            override fun onFailure(call: Call<List<Reporte>>, t: Throwable) {
                onFallback(dao.obtenerTodos())
                onError(t)
            }
        })
    }
}
