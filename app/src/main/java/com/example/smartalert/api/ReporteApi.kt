package com.example.smartalert.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import com.example.smartalert.model.Reporte
import com.example.smartalert.model.ReportesUsuarioResponse

interface ReporteApi {
    @Multipart
    @POST("crear_reporte")
    fun crearReporte(
        @Part("id_usuario") idUsuario: RequestBody,
        @Part("codigo_postal") codigoPostal: RequestBody,
        @Part("colonia") colonia: RequestBody,
        @Part("ciudad") ciudad: RequestBody,
        @Part("estado") estado: RequestBody,
        @Part("id_tipo") idTipo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        @Part("severidad") severidad: RequestBody,
        @Part imagen: MultipartBody.Part? = null
    ): Call<Void>

    @FormUrlEncoded
    @POST("reportes_cercanos")
    fun getReportesCercanos(
        @Field("latitud") latitud: Double,
        @Field("longitud") longitud: Double,
        @Field("radio") radio: Int = 10
    ): Call<List<Reporte>>

    @FormUrlEncoded
    @POST("reportes_usuario")
    fun getReportesUsuario(
        @Field("id_usuario") idUsuario: Int
    ): Call<ReportesUsuarioResponse>

    @FormUrlEncoded
    @PUT("actualizar_reporte")
    fun actualizarReporte(
        @Field("id_reporte") idReporte: Int,
        @Field("id_tipo") idTipo: Int,
        @Field("descripcion") descripcion: String,
        @Field("latitud") latitud: Double,
        @Field("longitud") longitud: Double,
        @Field("codigo_postal") codigoPostal: String,
        @Field("colonia") colonia: String,
        @Field("ciudad") ciudad: String,
        @Field("estado") estado: String
    ): Call<Void>


    @FormUrlEncoded
    @POST("buscar_reporte")
    fun buscarReporte(
        @Field("id_reporte") idReporte: Int
    ): Call<Reporte>

    @FormUrlEncoded
    @POST("eliminar_reporte")
    fun eliminarReporte(
        @Field("id_reporte") idReporte: Int
    ): Call<Void>




}



