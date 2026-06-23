package com.example.adoptasv.Conexion.Modelos;

public class CambiarEstadoSolicitudRequest {
    public String estado;
    public String comentario;

    public CambiarEstadoSolicitudRequest(String estado, String comentario) {
        this.estado = estado;
        this.comentario = comentario;
    }
}
