package com.example.smartalert.api

import com.example.smartalert.model.Area
import retrofit2.Call
import retrofit2.http.GET

interface AreaApi {

    @GET("listar_areas")
    fun listarAreas(): Call<List<Area>>
}
