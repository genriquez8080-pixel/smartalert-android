package com.example.smartalert.api

import com.example.smartalert.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface UsuarioApi {

    @POST("crear_usuario")
    fun crearUsuario(@Body usuario: Usuario): Call<UsuarioResponse>

    @POST("buscar_usuario")
    fun buscarUsuario(@Body body: UsuarioRequest): Call<Usuario>

    @PUT("actualizar_usuario")
    fun actualizarUsuario(@Body body: UsuarioUpdateRequest): Call<UsuarioResponse>

    @POST("buscar_usuario_correo")
    fun buscarUsuarioPorCorreo(@Body body: CorreoRequest): Call<Usuario>
}
