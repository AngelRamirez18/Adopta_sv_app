package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class Reporte {
    public int id;
    public String descripcion;
    @SerializedName("foto_url")              public String fotoUrl;
    public double latitud;
    public double longitud;
    @SerializedName("direccion_referencia")  public String direccionReferencia;
    @SerializedName("estado_reporte")        public String estadoReporte;
    @SerializedName("distancia_km")          public Double distanciaKm;
    @SerializedName("created_at")            public String createdAt;
    public User reportador;
}