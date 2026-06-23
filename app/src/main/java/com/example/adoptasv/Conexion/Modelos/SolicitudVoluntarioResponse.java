package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class SolicitudVoluntarioResponse {
    public int id;
    public String estado;
    public String mensaje;
    public String comentario;
    public User solicitante;
    public Refugio refugio;
    @SerializedName("created_at") public String creadoEn;
}
