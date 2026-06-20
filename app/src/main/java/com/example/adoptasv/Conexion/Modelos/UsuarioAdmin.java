package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Usuario tal como lo devuelve el panel de administración (GET /api/users).
 * A diferencia de {@link User}, aquí los roles llegan como objetos {id,name}
 * (Spatie) en lugar de strings, por eso se modela aparte.
 */
public class UsuarioAdmin {
    public int id;
    public String name;
    public String email;
    public String telefono;
    @SerializedName("estado_cuenta") public String estadoCuenta;
    public List<Rol> roles;

    public static class Rol {
        public int id;
        public String name;
    }

    /** Devuelve el nombre del primer rol, o "—" si no tiene. */
    public String rolPrincipal() {
        if (roles != null && !roles.isEmpty() && roles.get(0).name != null) {
            return roles.get(0).name;
        }
        return "—";
    }
}
