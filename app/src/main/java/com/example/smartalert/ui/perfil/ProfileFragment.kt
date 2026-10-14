package com.example.smartalert.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalert.R
import com.example.smartalert.api.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.example.smartalert.model.Reporte
import com.example.smartalert.model.UsuarioRequest
import com.example.smartalert.model.ReportesUsuarioResponse
import com.example.smartalert.utils.PreferencesHelper
import com.example.smartalert.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var rvFeed: RecyclerView
    private lateinit var adapter: ProfileReportesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        tvNombre = view.findViewById(R.id.tvNombreUsuario)
        tvCorreo = view.findViewById(R.id.tvCorreo)
        rvFeed = view.findViewById(R.id.rvFeed)

        // RecyclerView con lista mutable
        adapter = ProfileReportesAdapter(mutableListOf<Reporte>())
        rvFeed.layoutManager = LinearLayoutManager(requireContext())
        rvFeed.adapter = adapter

        // Botón "Actualizar mis datos"
        view.findViewById<View>(R.id.btnGuardarPerfil).setOnClickListener {
            startActivity(Intent(requireContext(), EditarPerfilActivity::class.java))
        }

        // Botón "Cerrar sesión"
        view.findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            cerrarSesion()
        }

        // Cargar perfil y reportes
        cargarPerfil()
        cargarReportes()
    }

    override fun onResume() {
        super.onResume()
        // Refresca al volver de EditarPerfilActivity
        cargarPerfil()
        cargarReportes()
    }

    // ===============================
    // PERFIL
    // ===============================
    private fun cargarPerfil() {
        val idUsuario = PreferencesHelper.getUserId(requireContext())
        if (idUsuario == -1) {
            toast("Usuario no logueado")
            return
        }

        RetrofitClient.usuarioApi.buscarUsuario(UsuarioRequest(idUsuario))
            .enqueue(object : Callback<com.example.smartalert.model.Usuario> {
                override fun onResponse(
                    call: Call<com.example.smartalert.model.Usuario>,
                    response: Response<com.example.smartalert.model.Usuario>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val usuario = response.body()!!
                        tvNombre.text = usuario.nombre
                        tvCorreo.text = usuario.correo
                    } else {
                        toast("No se pudo cargar el perfil")
                    }
                }

                override fun onFailure(
                    call: Call<com.example.smartalert.model.Usuario>,
                    t: Throwable
                ) {
                    toast("Error de conexión (perfil)")
                }
            })
    }

    // ===============================
    // REPORTES DEL USUARIO
    // ===============================
    private fun cargarReportes() {
        val idUsuario = PreferencesHelper.getUserId(requireContext())
        if (idUsuario == -1) return

        RetrofitClient.reporteApi.getReportesUsuario(idUsuario)
            .enqueue(object : Callback<ReportesUsuarioResponse> {
                override fun onResponse(
                    call: Call<ReportesUsuarioResponse>,
                    response: Response<ReportesUsuarioResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        // Actualiza la lista mutable del adapter
                        adapter.actualizar(response.body()!!.reportes)
                    } else {
                        toast("No se pudieron cargar los reportes")
                    }
                }

                override fun onFailure(call: Call<ReportesUsuarioResponse>, t: Throwable) {
                    toast("Error de conexión (reportes)")
                }
            })
    }

    // ===============================
    // CERRAR SESIÓN
    // ===============================
    private fun cerrarSesion() {
        // 1️⃣ Cerrar sesión Firebase
        FirebaseAuth.getInstance().signOut()

        // 2️⃣ Limpiar datos locales
        PreferencesHelper.clear(requireContext())

        // 3️⃣ Ir a Login y limpiar pila
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // 4️⃣ Cerrar activity actual
        requireActivity().finish()
    }

    // ===============================
    // TOAST UTILITY
    // ===============================
    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
