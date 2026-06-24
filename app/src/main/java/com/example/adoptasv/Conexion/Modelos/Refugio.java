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
    // Algunos endpoints devuelven lat/lng sueltos; otros los anidan en "coordenadas".
    public double latitud;
    public double longitud;
    public Coordenadas coordenadas;
    public boolean activo;
    @SerializedName("distancia_km") public Double distanciaKm;
    public List<Mascota> mascotas;

    public static class Coordenadas {
        public Double lat;
        public Double lng;
    }

    /** Latitud venga suelta o anidada en "coordenadas". */
    public double getLat() {
        if (coordenadas != null && coordenadas.lat != null) return coordenadas.lat;
        return latitud;
    }

    /** Longitud venga suelta o anidada en "coordenadas". */
    public double getLng() {
        if (coordenadas != null && coordenadas.lng != null) return coordenadas.lng;
        return longitud;
    }
}