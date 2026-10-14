package com.example.smartalert.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartalert.R
import com.example.smartalert.api.RetrofitClient
import com.example.smartalert.model.CorreoRequest
import com.example.smartalert.model.Usuario
import com.example.smartalert.ui.home.HomeActivity
import com.example.smartalert.utils.PreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtError = findViewById<TextView>(R.id.txtError)
        val btnGoToRegister = findViewById<TextView>(R.id.btnGoToRegister)

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {

            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showError(txtError, "Completa ambos campos")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    // 🔥 PEDIR USUARIO AL BACKEND
                    RetrofitClient.usuarioApi.buscarUsuarioPorCorreo(
                        CorreoRequest(email)
                    ).enqueue(object : Callback<Usuario> {

                        override fun onResponse(
                            call: Call<Usuario>,
                            response: Response<Usuario>
                        ) {
                            val usuario = response.body()

                            if (usuario == null || usuario.id_usuario == null) {
                                showError(txtError, "No se pudo obtener usuario")
                                return
                            }

                            // 🔐 GUARDAR ID REAL
                            PreferencesHelper.saveUserId(
                                this@LoginActivity,
                                usuario.id_usuario
                            )

                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        }

                        override fun onFailure(call: Call<Usuario>, t: Throwable) {
                            showError(txtError, "Error al conectar con backend")
                        }
                    })
                }
                .addOnFailureListener {
                    showError(txtError, "Credenciales incorrectas")
                }
        }
    }

    private fun showError(txtError: TextView, msg: String) {
        txtError.text = msg
        txtError.visibility = View.VISIBLE
    }
}