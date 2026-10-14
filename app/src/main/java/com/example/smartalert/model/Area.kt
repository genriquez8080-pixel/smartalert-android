package com.example.smartalert.model

import com.google.gson.annotations.SerializedName

data class Area(

    @SerializedName("id_area")
    var idArea: Int? = null,

    @SerializedName("codigo_postal")
    var codigoPostal: String,

    @SerializedName("colonia")
    var colonia: String,

    @SerializedName("ciudad")
    var ciudad: String,

    @SerializedName("estado")
    var estado: String,

    @SerializedName("latitud")
    var latitud: Double,

    @SerializedName("longitud")
    var longitud: Double,

    @SerializedName("total_reportes")
    var totalReportes: Int = 0,

    @SerializedName("indice_peligro")
    var indicePeligro: Int = 0,

    @SerializedName("nivel_reputacion")
    var nivelReputacion: String = "Alta"   // 👈 TEXTO, igual que el enum de la BD
)
