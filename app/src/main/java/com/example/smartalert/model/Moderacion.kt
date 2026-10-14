package com.example.smartalert.model

data class Moderacion(
    var idModeracion: Int? = null,
    var idReporte: Int,
    var idAdmin: Int,
    var accion: String,
    var motivo: String?,
    var fechaModificacion: String? = null
)
