package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FotoExtraResponse {
    @SerializedName("foto_url") public String fotoUrl;
    @SerializedName("fotos_extra") public List<String> fotosExtra;
}
