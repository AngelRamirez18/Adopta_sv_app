package com.example.adoptasv.Conexion;

import com.example.adoptasv.Conexion.Modelos.AddVoluntarioRequest;
import com.example.adoptasv.Conexion.Modelos.AddVoluntarioResponse;
import com.example.adoptasv.Conexion.Modelos.CambiarEstadoSolicitudRequest;
import com.example.adoptasv.Conexion.Modelos.CambiarEstadoUsuarioRequest;
import com.example.adoptasv.Conexion.Modelos.ConvertirReporteRequest;
import com.example.adoptasv.Conexion.Modelos.DeleteFotoExtraRequest;
import com.example.adoptasv.Conexion.Modelos.EstadoUsuarioResponse;
import com.example.adoptasv.Conexion.Modelos.FotoExtraResponse;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.MessageResponse;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.PanelResumen;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.Reporte;
import com.example.adoptasv.Conexion.Modelos.Seguimiento;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.Conexion.Modelos.SolicitudVoluntarioRequest;
import com.example.adoptasv.Conexion.Modelos.SolicitudVoluntarioResponse;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.Conexion.Modelos.UsuarioAdmin;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ── Auth ──────────────────────────────────────────────
    @GET("auth/me")
    Call<User> getMe();

    @POST("auth/logout")
    Call<Map<String, String>> logout();

    // ── Perfil ────────────────────────────────────────────
    @GET("perfil")
    Call<User> getPerfil();

    @PUT("perfil")
    Call<User> updatePerfil(@Body Map<String, Object> body);

    @Multipart
    @POST("perfil/foto")
    Call<Map<String, String>> uploadFotoPerfil(@Part MultipartBody.Part foto);

    // ── Users (admin) ─────────────────────────────────────
    @GET("users")
    Call<PaginatedResponse<UsuarioAdmin>> getUsers();

    @GET("users/{id}")
    Call<User> getUser(@Path("id") int id);

    @PATCH("users/{id}/role")
    Call<Map<String, Object>> updateRole(@Path("id") int id, @Body Map<String, String> body);

    @PATCH("users/{id}/estado")
    Call<EstadoUsuarioResponse> updateEstadoUsuario(
            @Path("id") int id,
            @Body CambiarEstadoUsuarioRequest body);

    // ── Refugios ──────────────────────────────────────────
    @GET("refugios")
    Call<List<Refugio>> getRefugios();

    @POST("refugios")
    Call<Refugio> createRefugio(@Body Map<String, Object> body);

    @GET("refugios/{id}")
    Call<Refugio> getRefugio(@Path("id") int id);

    @PUT("refugios/{id}")
    Call<Refugio> updateRefugio(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("refugios/{id}")
    Call<Map<String, String>> deleteRefugio(@Path("id") int id);

    @Multipart
    @POST("refugios/{id}/logo")
    Call<Map<String, String>> uploadLogoRefugio(
            @Path("id") int id,
            @Part MultipartBody.Part logo);

    @POST("refugios/{id}/voluntarios")
    Call<AddVoluntarioResponse> addVoluntario(
            @Path("id") int id,
            @Body AddVoluntarioRequest body);

    @POST("refugios/{id}/solicitudes-voluntario")
    Call<SolicitudVoluntarioResponse> solicitarVoluntariado(
            @Path("id") int id,
            @Body SolicitudVoluntarioRequest body);

    // ── Mascotas ──────────────────────────────────────────
    @GET("mascotas")
    Call<PaginatedResponse<Mascota>> getMascotas(
            @Query("especie") String especie,
            @Query("sexo") String sexo,
            @Query("tamano") String tamano,
            @Query("refugio_id") Integer refugioId,
            @Query("page") Integer page);

    @POST("mascotas")
    Call<SingleResponse<Mascota>> createMascota(@Body Map<String, Object> body);

    @GET("mascotas/{id}")
    Call<SingleResponse<Mascota>> getMascota(@Path("id") int id);

    @PUT("mascotas/{id}")
    Call<SingleResponse<Mascota>> updateMascota(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("mascotas/{id}")
    Call<Map<String, String>> deleteMascota(@Path("id") int id);

    @Multipart
    @POST("mascotas/{id}/foto")
    Call<Map<String, String>> uploadFotoMascota(
            @Path("id") int id,
            @Part MultipartBody.Part foto);

    @Multipart
    @POST("mascotas/{id}/fotos-extra")
    Call<FotoExtraResponse> uploadFotoExtraMascota(
            @Path("id") int id,
            @Part MultipartBody.Part foto);

    @HTTP(method = "DELETE", path = "mascotas/{id}/fotos-extra", hasBody = true)
    Call<FotoExtraResponse> deleteFotoExtraMascota(
            @Path("id") int id,
            @Body DeleteFotoExtraRequest body);

    // ── Solicitudes ───────────────────────────────────────
    // La API envuelve las colecciones de Resource en { "data": [...] }
    @GET("solicitudes/mis-solicitudes")
    Call<PaginatedResponse<Solicitud>> getMisSolicitudes();

    @POST("solicitudes")
    Call<Solicitud> createSolicitud(@Body Map<String, Object> body);

    @GET("solicitudes/{id}")
    Call<SingleResponse<Solicitud>> getSolicitud(@Path("id") int id);

    @GET("solicitudes")
    Call<PaginatedResponse<Solicitud>> getSolicitudes(@Query("estado") String estado);

    @PATCH("solicitudes/{id}/estado")
    Call<SingleResponse<Solicitud>> updateEstadoSolicitud(
            @Path("id") int id,
            @Body Map<String, Object> body);

    @GET("solicitudes/{id}/seguimientos")
    Call<PaginatedResponse<Seguimiento>> getSeguimientosDeSolicitud(@Path("id") int id);

    // ── Seguimientos ──────────────────────────────────────
    @GET("seguimientos/mis-seguimientos")
    Call<PaginatedResponse<Seguimiento>> getMisSeguimientos();

    @POST("seguimientos")
    Call<SingleResponse<Seguimiento>> createSeguimiento(@Body Map<String, Object> body);

    @Multipart
    @POST("seguimientos/{id}/foto")
    Call<Map<String, String>> uploadFotoSeguimiento(
            @Path("id") int id,
            @Part MultipartBody.Part foto);

    @GET("seguimientos/{id}")
    Call<SingleResponse<Seguimiento>> getSeguimiento(@Path("id") int id);

    @PATCH("seguimientos/{id}/observacion")
    Call<Seguimiento> addObservacion(
            @Path("id") int id,
            @Body Map<String, Object> body);

    // ── Reportes ──────────────────────────────────────────
    @GET("reportes")
    Call<PaginatedResponse<Reporte>> getReportes(@Query("estado") String estado);

    @POST("reportes")
    Call<SingleResponse<Reporte>> createReporte(@Body Map<String, Object> body);

    @GET("reportes/{id}")
    Call<SingleResponse<Reporte>> getReporte(@Path("id") int id);

    @PATCH("reportes/{id}/estado")
    Call<SingleResponse<Reporte>> updateEstadoReporte(
            @Path("id") int id,
            @Body Map<String, String> body);

    @Multipart
    @POST("reportes/{id}/foto")
    Call<Map<String, String>> uploadFotoReporte(
            @Path("id") int id,
            @Part MultipartBody.Part foto);

    @POST("reportes/{id}/convertir-mascota")
    Call<SingleResponse<Mascota>> convertirReporte(
            @Path("id") int id,
            @Body ConvertirReporteRequest body);

    // ── Mapa ──────────────────────────────────────────────
    @GET("mapa/refugios")
    Call<List<Refugio>> getRefugiosCercanos(
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("radio") Double radio);

    @GET("mapa/reportes")
    Call<List<Reporte>> getReportesCercanos(
            @Query("lat") Double lat,
            @Query("lng") Double lng,
            @Query("radio") Double radio);

    // ── Panel refugio ─────────────────────────────────────
    @GET("panel/resumen")
    Call<PanelResumen> getPanelResumen();

    @GET("panel/mascotas")
    Call<PaginatedResponse<Mascota>> getPanelMascotas(@Query("estado") String estado);

    @PATCH("panel/mascotas/{id}/estado")
    Call<SingleResponse<Mascota>> updateEstadoMascota(
            @Path("id") int id,
            @Body Map<String, String> body);

    @GET("panel/solicitudes")
    Call<PaginatedResponse<Solicitud>> getPanelSolicitudes(@Query("estado") String estado);

    @GET("panel/seguimientos")
    Call<PaginatedResponse<Seguimiento>> getPanelSeguimientos(@Query("estado") String estado);

    @PATCH("solicitudes-voluntario/{id}/estado")
    Call<SolicitudVoluntarioResponse> updateEstadoSolicitudVoluntario(
            @Path("id") int id,
            @Body CambiarEstadoSolicitudRequest body);
}