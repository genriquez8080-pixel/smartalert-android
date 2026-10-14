package com.example.smartalert.dao

interface ICrud<T> {
    fun insertar(obj: T): Long
    fun actualizar(obj: T): Int
    fun eliminar(id: Int): Int
    fun obtenerTodos(): List<T>
    fun obtenerPorId(id: Int): T?
}
