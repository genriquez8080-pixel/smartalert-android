package com.example.smartalert.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))

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
            viewModel.login(email, password) { ok, error ->
                if (ok) onLoginSuccess()
                else message = error
            }
        }) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = { onGoToRegister() }) {
            Text("Crear una cuenta")
        }

        message?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}
