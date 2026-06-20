package com.example.adoptasv.Conexion.Modelos;

import com.google.gson.annotations.SerializedName;

/**
 * Resumen estadístico del panel del refugio.
 * GET /api/panel/resumen — respuesta raw (no envuelta en "data").
 */
public class PanelResumen {

    public Mascotas mascotas;
    public Solicitudes solicitudes;
    @SerializedName("seguimientos_pendientes") public int seguimientosPendientes;

    public static class Mascotas {
        public int disponibles;
        @SerializedName("en_proceso") public int enProceso;
        public int adoptadas;
    }

    public static class Solicitudes {
        public int pendientes;
        @SerializedName("en_revision") public int enRevision;
        public int aprobadas;
    }
}
