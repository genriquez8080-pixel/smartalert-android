package com.example.smartalert.model

import com.google.gson.annotations.SerializedName

data class UsuarioUpdateRequest(

    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("correo")
    val correo: String,

    // 👇 CLAVE: nullable
    @SerializedName("contrasena")
    val password: String? = null,

    @SerializedName("imagen")
    val imagen: String? = null
)
