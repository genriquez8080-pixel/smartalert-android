package com.example.smartalert.model

data class ReporteCache(
    val idReporte: Int,
    override val usuarioNombre: String,
    val fechaReporte: String?,
    val colonia: String?,
    val ciudad: String?,
    val descripcion: String?,
    val imagenUrl: String?
) : FeedItemContract {

    override val fecha: String?
        get() = fechaReporte

    override val descripcionTexto: String
        get() = descripcion ?: ""

    override val coloniaCiudad: String
        get() {
            val col = colonia?.trim().orEmpty()
            val cd = ciudad?.trim().orEmpty()
            return if (col.isNotBlank() || cd.isNotBlank()) "$col, $cd" else "Sin dirección"
        }

    override val imagen: String?
        get() = imagenUrl
}
