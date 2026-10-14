package com.example.smartalert.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.smartalert.ui.login.LoginScreen
import com.example.smartalert.ui.login.RegisterScreen
import com.example.smartalert.ui.login.LoginViewModel
import com.example.smartalert.ui.theme.SmartAlertTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SmartAlertTheme {
                AuthNav()
            }
        }
    }
}

@Composable
fun AuthNav() {
    // estado para saber qué pantalla mostrar
    var showLogin by remember { mutableStateOf(true) }

    // ViewModel para login/registro
    val viewModel = remember { LoginViewModel() }

    if (showLogin) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                // TODO: aquí luego mandas a la pantalla principal
            },
            onGoToRegister = { showLogin = false }
        )
    } else {
        RegisterScreen(
            viewModel = viewModel,
            onRegisterSuccess = {
                showLogin = true   // después de registrar, vuelve al login
            },
            onCancel = {
                showLogin = true   // si cancela, también vuelve al login
            }
        )
    }
}
