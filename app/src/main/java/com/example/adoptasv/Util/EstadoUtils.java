package com.example.adoptasv.Util;

import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

/**
 * Asigna color y etiqueta a los badges de estado (solicitudes, seguimientos,
 * reportes y mascotas) respetando la paleta de la app.
 */
public final class EstadoUtils {

    private EstadoUtils() {}

    /** Aplica fondo redondeado + color de texto + etiqueta legible a un TextView. */
    public static void aplicarBadge(TextView tv, String estado) {
        int bg, text;
        String label;
        String e = estado == null ? "" : estado.toLowerCase().trim();

        switch (e) {
            // ── Solicitudes ──
            case "pendiente":
                bg = 0xFFFFF8E1; text = 0xFFF9A825; label = "Pendiente"; break;
            case "en_revision":
                bg = 0xFFE3F2FD; text = 0xFF1976D2; label = "En revisión"; break;
            case "aprobada":
                bg = 0xFFE8F5E9; text = 0xFF2E7D32; label = "Aprobada"; break;
            case "rechazada":
                bg = 0xFFFFEBEE; text = 0xFFC62828; label = "Rechazada"; break;

            // ── Seguimientos ──
            case "revisado":
                bg = 0xFFE8F5E9; text = 0xFF2E7D32; label = "Revisado"; break;
            case "observado":
                bg = 0xFFFFF3E0; text = 0xFFE65100; label = "Observado"; break;

            // ── Reportes SOS ──
            case "nuevo":
                bg = 0xFFFFEBEE; text = 0xFFC62828; label = "Nuevo"; break;
            case "en_atencion":
                bg = 0xFFE3F2FD; text = 0xFF1976D2; label = "En atención"; break;
            case "atendido":
                bg = 0xFFE8F5E9; text = 0xFF2E7D32; label = "Atendido"; break;
            case "cerrado":
                bg = 0xFFEDE0CC; text = 0xFF8B5E3C; label = "Cerrado"; break;

            // ── Mascotas ──
            case "disponible":
                bg = 0xFFE8F5E9; text = 0xFF2E7D32; label = "Disponible"; break;
            case "en_proceso":
                bg = 0xFFFFF8E1; text = 0xFFF9A825; label = "En proceso"; break;
            case "adoptada":
                bg = 0xFFEDE0CC; text = 0xFF8B5E3C; label = "Adoptada"; break;

            default:
                bg = 0xFFEDE0CC; text = 0xFF8B5E3C;
                label = estado == null || estado.isEmpty() ? "—" : capitalize(estado);
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(tv, 999));
        shape.setColor(bg);
        tv.setBackground(shape);
        tv.setTextColor(text);
        tv.setText(label);
    }

    private static float dp(TextView tv, int value) {
        return value * tv.getResources().getDisplayMetrics().density;
    }

    private static String capitalize(String s) {
        s = s.replace("_", " ");
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
