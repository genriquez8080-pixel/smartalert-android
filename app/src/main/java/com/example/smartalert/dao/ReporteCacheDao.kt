package com.example.smartalert.dao

import android.content.ContentValues
import android.content.Context
import com.example.smartalert.db.SmartAlertDBHelper
import com.example.smartalert.model.ReporteCache

class ReporteCacheDao(context: Context) : ICrud<ReporteCache> {

    private val dbHelper = SmartAlertDBHelper(context)

    override fun insertar(obj: ReporteCache): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("id_reporte", obj.idReporte)
            put("usuario_nombre", obj.usuarioNombre)
            put("fecha_reporte", obj.fechaReporte)
            put("colonia", obj.colonia)
            put("ciudad", obj.ciudad)
            put("descripcion", obj.descripcion)
            put("imagen_url", obj.imagenUrl)
        }

        return db.insertWithOnConflict(
            "reportes_cache",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    override fun actualizar(obj: ReporteCache): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("usuario_nombre", obj.usuarioNombre)
            put("fecha_reporte", obj.fechaReporte)
            put("colonia", obj.colonia)
            put("ciudad", obj.ciudad)
            put("descripcion", obj.descripcion)
            put("imagen_url", obj.imagenUrl)
        }

        return db.update(
            "reportes_cache",
            values,
            "id_reporte = ?",
            arrayOf(obj.idReporte.toString())
        )
    }

    override fun eliminar(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete("reportes_cache", "id_reporte = ?", arrayOf(id.toString()))
    }

    fun limpiarTodo(): Int {
        val db = dbHelper.writableDatabase
        return db.delete("reportes_cache", null, null)
    }

    override fun obtenerTodos(): List<ReporteCache> {
        val lista = mutableListOf<ReporteCache>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM reportes_cache ORDER BY fecha_reporte DESC",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    ReporteCache(
                        idReporte = cursor.getInt(cursor.getColumnIndexOrThrow("id_reporte")),
                        usuarioNombre = cursor.getString(cursor.getColumnIndexOrThrow("usuario_nombre")),
                        fechaReporte = cursor.getString(cursor.getColumnIndexOrThrow("fecha_reporte")),
                        colonia = cursor.getString(cursor.getColumnIndexOrThrow("colonia")),
                        ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ciudad")),
                        descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                        imagenUrl = cursor.getString(cursor.getColumnIndexOrThrow("imagen_url"))
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }

    override fun obtenerPorId(id: Int): ReporteCache? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM reportes_cache WHERE id_reporte = ?",
            arrayOf(id.toString())
        )

        val item = if (cursor.moveToFirst()) {
            ReporteCache(
                idReporte = cursor.getInt(cursor.getColumnIndexOrThrow("id_reporte")),
                usuarioNombre = cursor.getString(cursor.getColumnIndexOrThrow("usuario_nombre")),
                fechaReporte = cursor.getString(cursor.getColumnIndexOrThrow("fecha_reporte")),
                colonia = cursor.getString(cursor.getColumnIndexOrThrow("colonia")),
                ciudad = cursor.getString(cursor.getColumnIndexOrThrow("ciudad")),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                imagenUrl = cursor.getString(cursor.getColumnIndexOrThrow("imagen_url"))
            )
        } else null

        cursor.close()
        return item
    }
}
