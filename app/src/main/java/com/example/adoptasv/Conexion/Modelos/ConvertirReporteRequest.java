package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class ConvertirReporteRequest {
    @SerializedName("refugio_id") public int refugioId;
    public String nombre;
    public String especie;
    public String sexo;
    public String raza;
    @SerializedName("edad_meses") public Integer edadMeses;
    public String tamano;
    public String descripcion;
    public boolean vacunas;
    public boolean esterilizado;
}
