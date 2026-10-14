package com.example.smartalert.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.model.Usuario
import com.example.smartalert.model.UsuarioResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "LoginViewModel"

    // Inicio de sesión en Firebase
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Intentando login con email: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Login Firebase exitoso")
                    onResult(true, null)
                } else {
                    Log.e(TAG, "Error login Firebase: ${task.exception?.message}")
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Registro en Firebase y Backend
    fun registerFirebase(
        nombre: String,
        email: String,
        password: String,
        onResult: (Boolean, Int?) -> Unit
    ) {
        Log.d(TAG, "Intentando registrar usuario Firebase: $email")
        // 1️⃣ Crear usuario en Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Registro Firebase exitoso, ahora registrando en backend")
                    val usuario = Usuario(
                        nombre = nombre,
                        correo = email,
                        contrasena = password // será encriptada en backend
                    )

                    // 2️⃣ Llamada a backend
                    RetrofitClient.usuarioApi.crearUsuario(usuario)
                        .enqueue(object : Callback<UsuarioResponse> {
                            override fun onResponse(
                                call: Call<UsuarioResponse>,
                                response: Response<UsuarioResponse>
                            ) {
                                Log.d(TAG, "Retrofit onResponse: ${response.code()} ${response.message()}")
                                if (response.isSuccessful && response.body() != null) {
                                    val idUsuario = response.body()!!.usuario.id_usuario
                                    Log.d(TAG, "Usuario creado en backend con ID: $idUsuario")
                                    onResult(true, idUsuario)
                                } else {
                                    Log.e(TAG, "Error al crear usuario en backend: ${response.errorBody()?.string()}")
                                    onResult(false, null)
                                }
                            }

                            override fun onFailure(call: Call<UsuarioResponse>, t: Throwable) {
                                Log.e(TAG, "Fallo en llamada a backend: ${t.message}")
                                onResult(false, null)
                            }
                        })
                } else {
                    Log.e(TAG, "Error al registrar en Firebase: ${task.exception?.message}")
                    onResult(false, null)
                }
            }
    }
}