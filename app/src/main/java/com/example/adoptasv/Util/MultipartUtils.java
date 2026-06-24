package com.example.adoptasv.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Construye un MultipartBody.Part a partir de un Uri de contenido (galería),
 * redimensionando y comprimiendo la imagen antes de subirla. Esto evita el
 * error 500 del servidor por imágenes demasiado grandes (p. ej. fotos de 2+ MB).
 */
public final class MultipartUtils {

    private static final int MAX_DIM = 1024;   // lado máximo en píxeles
    private static final int JPEG_QUALITY = 70;

    private MultipartUtils() {}

    /** Crea la parte multipart para subir una imagen comprimida, o null si falla la lectura. */
    public static MultipartBody.Part fotoPart(Context context, Uri uri, String fieldName) {
        try {
            byte[] bytes = comprimir(context.getContentResolver(), uri);
            if (bytes == null) return null;

            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            return MultipartBody.Part.createFormData(fieldName, "foto.jpg", body);
        } catch (Exception e) {
            return null;
        }
    }

    /** Decodifica, escala a MAX_DIM, corrige rotación EXIF y comprime a JPEG. */
    private static byte[] comprimir(ContentResolver resolver, Uri uri) throws IOException {
        // 1) Leer dimensiones sin cargar el bitmap completo en memoria.
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) return null;
            BitmapFactory.decodeStream(in, null, bounds);
        }

        // 2) Decodificar con submuestreo para no reventar memoria con fotos enormes.
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = calcularInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIM);
        Bitmap bmp;
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) return null;
            bmp = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bmp == null) return null;

        // 3) Escalar al lado máximo manteniendo proporción.
        bmp = escalar(bmp, MAX_DIM);

        // 4) Corregir rotación según EXIF (fotos de cámara suelen venir giradas).
        bmp = corregirOrientacion(resolver, uri, bmp);

        // 5) Comprimir a JPEG.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        bmp.recycle();
        return out.toByteArray();
    }

    private static int calcularInSampleSize(int width, int height, int max) {
        int sample = 1;
        while (width / sample > max * 2 || height / sample > max * 2) {
            sample *= 2;
        }
        return sample;
    }

    private static Bitmap escalar(Bitmap bmp, int max) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        if (w <= max && h <= max) return bmp;
        float ratio = Math.min((float) max / w, (float) max / h);
        int nw = Math.round(w * ratio);
        int nh = Math.round(h * ratio);
        Bitmap escalado = Bitmap.createScaledBitmap(bmp, nw, nh, true);
        if (escalado != bmp) bmp.recycle();
        return escalado;
    }

    private static Bitmap corregirOrientacion(ContentResolver resolver, Uri uri, Bitmap bmp) {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) return bmp;
            ExifInterface exif = new ExifInterface(in);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int grados;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:  grados = 90;  break;
                case ExifInterface.ORIENTATION_ROTATE_180: grados = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: grados = 270; break;
                default: return bmp;
            }
            Matrix m = new Matrix();
            m.postRotate(grados);
            Bitmap rotado = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
            if (rotado != bmp) bmp.recycle();
            return rotado;
        } catch (Exception e) {
            return bmp;
        }
    }
}
