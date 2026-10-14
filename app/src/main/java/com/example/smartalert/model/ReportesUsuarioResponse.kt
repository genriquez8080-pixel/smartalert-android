package com.example.smartalert.model

import com.google.gson.annotations.SerializedName

data class ReportesUsuarioResponse(

    @SerializedName("reportes")
    val reportes: List<Reporte>
)

