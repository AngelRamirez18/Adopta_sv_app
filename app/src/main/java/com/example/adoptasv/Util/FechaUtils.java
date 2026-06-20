package com.example.adoptasv.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Convierte las fechas ISO/timestamp que devuelve la API a un formato corto
 * legible en español. Tolera tanto "2026-06-18" como "2026-06-18T01:04:55Z".
 */
public final class FechaUtils {

    private FechaUtils() {}

    private static final String[] FORMATOS_ENTRADA = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
    };

    /** Devuelve algo como "18 jun 2026" o el string original si no se puede parsear. */
    public static String formatoCorto(String fecha) {
        if (fecha == null || fecha.isEmpty()) return "";
        for (String patron : FORMATOS_ENTRADA) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(patron, Locale.US);
                Date d = in.parse(fecha);
                if (d != null) {
                    return new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES")).format(d);
                }
            } catch (Exception ignored) {
                // probar siguiente formato
            }
        }
        // Fallback: recortar a la parte de fecha si viene con hora
        return fecha.length() >= 10 ? fecha.substring(0, 10) : fecha;
    }
}
