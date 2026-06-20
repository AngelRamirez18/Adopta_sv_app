package com.example.adoptasv.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Construye un MultipartBody.Part a partir de un Uri de contenido (galería),
 * leyendo los bytes con el ContentResolver. Evita dependencias de ruta de archivo.
 */
public final class MultipartUtils {

    private MultipartUtils() {}

    /** Crea la parte multipart para subir una imagen, o null si falla la lectura. */
    public static MultipartBody.Part fotoPart(Context context, Uri uri, String fieldName) {
        try {
            ContentResolver resolver = context.getContentResolver();
            String mime = resolver.getType(uri);
            if (mime == null) mime = "image/*";

            byte[] bytes = leerBytes(resolver, uri);
            if (bytes == null) return null;

            RequestBody body = RequestBody.create(MediaType.parse(mime), bytes);
            String nombre = nombreArchivo(resolver, uri);
            return MultipartBody.Part.createFormData(fieldName, nombre, body);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] leerBytes(ContentResolver resolver, Uri uri) throws IOException {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) return null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }

    private static String nombreArchivo(ContentResolver resolver, Uri uri) {
        String nombre = "foto.jpg";
        try (android.database.Cursor c = resolver.query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    String dn = c.getString(idx);
                    if (dn != null && !dn.isEmpty()) nombre = dn;
                }
            }
        } catch (Exception ignored) {}
        return nombre;
    }
}
