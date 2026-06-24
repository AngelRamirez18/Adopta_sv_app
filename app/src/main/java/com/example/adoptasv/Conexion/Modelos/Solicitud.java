package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class Solicitud {
    public int id;
    @SerializedName("mascota_id")          public int mascotaId;
    @SerializedName("user_id")             public int userId;
    @SerializedName("refugio_id")          public int refugioId;
    @SerializedName("estado_solicitud")    public String estado;
    @SerializedName("comentario_refugio")  public String comentario;
    @SerializedName("puntaje_evaluacion")  public int puntajeEvaluacion;
    @SerializedName("respuestas_formulario") public RespuestasFormulario respuestasFormulario;
    @SerializedName("creado_en")           public String createdAt;
    public Mascota mascota;
    public User adoptante;
    public Refugio refugio;

    public static class RespuestasFormulario {
        @SerializedName("tipo_vivienda") public String tipoVivienda;
        @SerializedName("tiene_patio")   public boolean tienePatio;
        @SerializedName("otros_animales") public boolean otrosAnimales;
        @SerializedName("horas_en_casa") public Integer horasEnCasa;
        public String experiencia;
        public String compromiso;
    }
}