package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class Solicitud {
    public int id;
    @SerializedName("mascota_id")          public int mascotaId;
    @SerializedName("user_id")             public int userId;
    @SerializedName("refugio_id")          public int refugioId;
    public String estado;
    public String comentario;
    @SerializedName("puntaje_evaluacion")  public int puntajeEvaluacion;
    @SerializedName("respuestas_formulario") public Object respuestasFormulario;
    @SerializedName("created_at")          public String createdAt;
    public Mascota mascota;
    public User adoptante;
    public Refugio refugio;
}