package com.example.smartalert.utils

import android.content.Context

object PreferencesHelper {

    private const val PREFS_NAME = "smartalert_prefs"
    private const val KEY_ID_USUARIO = "id_usuario"

    fun saveUserId(context: Context, idUsuario: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ID_USUARIO, idUsuario).apply()
    }

    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_ID_USUARIO, -1)
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }


}
