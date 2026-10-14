package com.example.smartalert.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartalert.R
import com.example.smartalert.utils.PreferencesHelper

class RegisterActivity : ComponentActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputPasswordConfirm: EditText
    private lateinit var btnRegister: Button
    private lateinit var txtError: TextView
    private lateinit var btnGoToLogin: TextView

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Views
        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmailRegister)
        inputPassword = findViewById(R.id.inputPasswordRegister)
        inputPasswordConfirm = findViewById(R.id.inputPasswordConfirm)
        btnRegister = findViewById(R.id.btnRegister)
        txtError = findViewById(R.id.txtErrorRegister)
        btnGoToLogin = findViewById(R.id.btnGoToLogin)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Ir al login
        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Registro de usuario
        btnRegister.setOnClickListener {
            val name = inputName.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val pass = inputPassword.text.toString().trim()
            val pass2 = inputPasswordConfirm.text.toString().trim()

            txtError.visibility = TextView.GONE

            // Validaciones básicas
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
                showError("Completa todos los campos")
                return@setOnClickListener
            }

            if (pass != pass2) {
                showError("Las contraseñas no coinciden")
                return@setOnClickListener
            }

            // Llamar al ViewModel que registra en Firebase y backend
            viewModel.registerFirebase(name, email, pass) { success, idUsuario ->
                if (success && idUsuario != null) {
                    // Guardar ID en preferencias
                    PreferencesHelper.saveUserId(this, idUsuario)

                    Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()

                    // Ir al login
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    showError("Error al registrar usuario. Intenta nuevamente.")
                }
            }
        }
    }

    private fun showError(msg: String) {
        txtError.text = msg
        txtError.visibility = TextView.VISIBLE
    }
}
