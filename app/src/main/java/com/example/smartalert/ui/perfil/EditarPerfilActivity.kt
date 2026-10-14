package com.example.smartalert.ui.perfil

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalert.R
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.model.*
import com.example.smartalert.utils.PreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.*

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_actualizar)

        auth = FirebaseAuth.getInstance()

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPass = findViewById<EditText>(R.id.etContrasena)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)

        val userId = PreferencesHelper.getUserId(this)

        // 🔹 CARGAR DATOS ACTUALES
        RetrofitClient.usuarioApi.buscarUsuario(UsuarioRequest(userId))
            .enqueue(object : Callback<Usuario> {
                override fun onResponse(
                    call: Call<Usuario>,
                    response: Response<Usuario>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val usuario = response.body()!!
                        etNombre.setText(usuario.nombre)
                        etCorreo.setText(usuario.correo)
                        // ❌ NUNCA se carga la contraseña
                    } else {
                        toast("Error al cargar perfil")
                    }
                }

                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    toast("Error de conexión")
                }
            })

        // 🔹 BOTÓN ACTUALIZAR
        btnActualizar.setOnClickListener {

            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoCorreo = etCorreo.text.toString().trim()
            val nuevaPass = etPass.text.toString().trim()

            if (nuevoNombre.isEmpty() || nuevoCorreo.isEmpty()) {
                toast("Nombre y correo obligatorios")
                return@setOnClickListener
            }

            val userFirebase = auth.currentUser
            if (userFirebase == null) {
                toast("Sesión no válida")
                return@setOnClickListener
            }

            // 🔐 PASO 1: FIREBASE
            actualizarFirebase(
                userFirebase,
                nuevoCorreo,
                nuevaPass
            ) {
                // 🌐 PASO 2: BACKEND
                actualizarBackend(
                    userId,
                    nuevoNombre,
                    nuevoCorreo,
                    nuevaPass
                )
            }
        }
    }

    // 🔐 ACTUALIZAR FIREBASE
    private fun actualizarFirebase(
        user: FirebaseUser,
        correo: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        user.updateEmail(correo)
            .addOnSuccessListener {
                if (pass.isNotEmpty()) {
                    user.updatePassword(pass)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener {
                            toast("Error al actualizar contraseña")
                        }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener {
                toast("Error Firebase: vuelve a iniciar sesión")
            }
    }

    // 🌐 ACTUALIZAR BACKEND
    private fun actualizarBackend(
        id: Int,
        nombre: String,
        correo: String,
        pass: String
    ) {
        val request = UsuarioUpdateRequest(
            id = id,
            nombre = nombre,
            correo = correo,
            password = pass.ifEmpty { null },
            imagen = null
        )

        RetrofitClient.usuarioApi.actualizarUsuario(request)
            .enqueue(object : Callback<UsuarioResponse> {

                override fun onResponse(
                    call: Call<UsuarioResponse>,
                    response: Response<UsuarioResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        toast("Cambios guardados exitosamente")
                        finish()
                    } else {
                        toast("No se pudieron guardar los cambios")
                    }
                }

                override fun onFailure(call: Call<UsuarioResponse>, t: Throwable) {
                    toast("Error de conexión con el servidor")
                }
            })
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
