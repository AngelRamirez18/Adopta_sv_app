package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

/**
 * Solicitud de un adoptante (cliente) para convertirse en voluntario de un refugio.
 * El cliente la crea desde su Perfil eligiendo un refugio; el admin de ese refugio la
 * gestiona en el mismo panel que las solicitudes de adopción. Al aprobarla, el backend
 * promueve al usuario a voluntario y le asigna el refugio_id de la solicitud.
 *
 * Los nombres de campos se toleran en varias formas (`alternate`) porque el contrato
 * exacto de la API puede variar.
 */
public class SolicitudVoluntario {
    public int id;
    @SerializedName("user_id")    public int userId;
    @SerializedName("refugio_id") public int refugioId;
    // El backend usa "estado" (enum pendiente/aprobada/rechazada) y "comentario" (respuesta del refugio).
    @SerializedName(value = "estado", alternate = {"estado_solicitud"}) public String estado;
    public String mensaje;
    public String comentario;
    @SerializedName("created_at") public String createdAt;

    // La relación se llama solicitante(); se tolera user/adoptante por compatibilidad.
    @SerializedName(value = "solicitante", alternate = {"user", "adoptante"}) public User user;
    public Refugio refugio;
}
