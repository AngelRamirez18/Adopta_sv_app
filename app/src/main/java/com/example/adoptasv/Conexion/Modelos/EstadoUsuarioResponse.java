package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

public class EstadoUsuarioResponse {
    public String message;
    @SerializedName("estado_cuenta") public String estadoCuenta;
}
