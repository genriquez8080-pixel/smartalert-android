package com.example.smartalert.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SmartAlertDBHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE reportes_cache (
                id_reporte INTEGER PRIMARY KEY,
                usuario_nombre TEXT NOT NULL,
                fecha_reporte TEXT,
                colonia TEXT,
                ciudad TEXT,
                descripcion TEXT,
                imagen_url TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS reportes_cache")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "smartalert.db"
        private const val DATABASE_VERSION = 1
    }
}
