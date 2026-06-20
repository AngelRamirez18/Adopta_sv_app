package com.example.adoptasv.Conexion.Modelos;

import com.example.adoptasv.Conexion.LenientBooleanAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Mascota {
    public int id;
    public String nombre;
    public String especie;
    public String raza;
    @SerializedName("edad_meses")      public Integer edadMeses;
    public String sexo;
    public String tamano;
    public String descripcion;
    public String personalidad;
    @SerializedName("estado_salud")    public String estadoSalud;
    @JsonAdapter(LenientBooleanAdapter.class) public boolean vacunas;
    @JsonAdapter(LenientBooleanAdapter.class) public boolean esterilizado;
    @SerializedName("foto_url")        public String fotoUrl;
    @SerializedName("fotos_extra")     public List<String> fotosExtra;
    @SerializedName("estado_adopcion") public String estadoAdopcion;
    @SerializedName("creado_en")       public String creadoEn;
    public Refugio refugio;
}