package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Refugio {
    public int id;
    @SerializedName("user_id")  public int userId;
    public String nombre;
    public String direccion;
    public String telefono;
    public String correo;
    public String descripcion;
    @SerializedName("logo_url") public String logoUrl;
    public double latitud;
    public double longitud;
    public boolean activo;
    @SerializedName("distancia_km") public Double distanciaKm;
    public List<Mascota> mascotas;
}