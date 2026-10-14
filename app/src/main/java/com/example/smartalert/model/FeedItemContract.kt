package com.example.smartalert.model

interface FeedItemContract {
    val usuarioNombre: String
    val fecha: String?
    val descripcionTexto: String
    val coloniaCiudad: String
    val imagen: String?
}
