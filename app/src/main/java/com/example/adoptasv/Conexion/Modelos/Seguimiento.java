package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class Seguimiento {
    public int id;
    @SerializedName("solicitud_id")        public int solicitudId;
    @SerializedName("mascota_id")          public int mascotaId;
    @SerializedName("user_id")             public int userId;
    public String comentario;
    @SerializedName("estado_mascota")      public String estadoMascota;
    @SerializedName("observacion_refugio") public String observacionRefugio;
    @SerializedName("estado_seguimiento")  public String estadoSeguimiento;
    @SerializedName("foto_url")            public String fotoUrl;
    @SerializedName("created_at")          public String createdAt;
    public Mascota mascota;
    public Solicitud solicitud;
    public User adoptante;
}