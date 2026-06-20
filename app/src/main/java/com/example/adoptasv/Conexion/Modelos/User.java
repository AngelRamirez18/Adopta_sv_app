package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class User {
    public int id;
    @SerializedName("firebase_uid") public String firebaseUid;
    public String name;
    public String email;
    public String telefono;
    public String direccion;
    @SerializedName("foto_perfil_url") public String fotoPerfil;
    @SerializedName("estado_cuenta")  public String estadoCuenta;
    public List<String> roles;
    public List<String> permissions;
    public Refugio refugio;
}