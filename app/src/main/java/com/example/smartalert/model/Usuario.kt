package com.example.smartalert.model

import com.google.gson.annotations.SerializedName

data class Usuario(

    @SerializedName("id_usuario")
    val id_usuario: Int? = null,

    val nombre: String,

    val correo: String,

    @SerializedName("contrasena")
    val contrasena: String? = null,

    val rol: String? = null,

    @SerializedName("total_reportes")
    val totalReportes: Int? = null,

    @SerializedName("reputacion_usuario")
    val reputacionUsuario: Int? = null,

    @SerializedName("fecha_registro")
    val fechaRegistro: String? = null
)

data class CorreoRequest(
    val correo: String
)