package com.example.smartalert.model

import com.google.gson.annotations.SerializedName

data class Reporte(

    @SerializedName("id_reporte")
    var idReporte: Int? = null,

    @SerializedName("id_usuario")
    var idUsuario: Int,

    @SerializedName("id_area")
    var idArea: Int,

    @SerializedName("id_tipo")
    var idTipo: Int,

    @SerializedName("descripcion")
    var descripcion: String?,

    @SerializedName("latitud")
    var latitud: Double?,

    @SerializedName("longitud")
    var longitud: Double?,

    @SerializedName("severidad")
    var severidad: Int,

    @SerializedName("imagen_url")
    var imagenUrl: String?,

    @SerializedName("estado")
    var estado: String = "pendiente",

    @SerializedName("fecha_reporte")
    var fechaReporte: String? = null,

    // 👇 OBJETOS ANIDADOS QUE ENVÍA LARAVEL
    @SerializedName("usuario")
    var usuario: Usuario? = null,

    @SerializedName("area")
    var area: Area? = null,

    @SerializedName("tipo")
    var tipo: TipoReporte? = null
) : FeedItemContract {

    override val usuarioNombre: String
        get() = usuario?.nombre ?: "Usuario"

    override val fecha: String?
        get() = fechaReporte

    override val descripcionTexto: String
        get() = descripcion ?: ""

    override val coloniaCiudad: String
        get() {
            val col = area?.colonia?.trim().orEmpty()
            val cd = area?.ciudad?.trim().orEmpty()
            return if (col.isNotBlank() || cd.isNotBlank()) "$col, $cd" else "Sin dirección"
        }

    override val imagen: String?
        get() = imagenUrl
}



