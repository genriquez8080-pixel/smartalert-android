package com.example.smartalert.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartalert.utils.PreferencesHelper
import androidx.compose.ui.platform.LocalContext
@Composable
fun RegisterScreen(
    viewModel: LoginViewModel,
    onRegisterSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Crear Cuenta", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            viewModel.registerFirebase(nombre, email, password) { ok, idUsuario ->
                if (ok && idUsuario != null) {
                    PreferencesHelper.saveUserId(context, idUsuario)
                    onRegisterSuccess()
                } else {
                    message = "Error al registrar usuario"
                }
            }
        }) {
            Text("Crear cuenta")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { onCancel() }) {
            Text("Cancelar")
        }

        message?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}
