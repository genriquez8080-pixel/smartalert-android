package com.example.smartalert.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val BASE_URL = "http://192.168.0.105/smartalert_backend/public/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL + "api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val areaApi: AreaApi by lazy { retrofit.create(AreaApi::class.java) }
    val reporteApi: ReporteApi by lazy { retrofit.create(ReporteApi::class.java) }
    val tipoReporteApi: TipoReporteApi by lazy { retrofit.create(TipoReporteApi::class.java) }
    val usuarioApi: UsuarioApi by lazy { retrofit.create(UsuarioApi::class.java) }
}
