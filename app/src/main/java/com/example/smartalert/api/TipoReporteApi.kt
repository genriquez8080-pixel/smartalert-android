package com.example.smartalert.api

import com.example.smartalert.model.TipoReporteResponse
import retrofit2.Call
import retrofit2.http.GET

interface TipoReporteApi {

    @GET("listar_tipos_reporte")
    fun getTiposReporte(): Call<TipoReporteResponse>

}
