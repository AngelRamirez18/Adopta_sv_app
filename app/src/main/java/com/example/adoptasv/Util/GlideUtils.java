package com.example.adoptasv.Util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.adoptasv.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.concurrent.TimeUnit;

public class GlideUtils {

    /**
     * Carga una imagen con Glide inyectando el token de Firebase si la URL apunta a nuestra API.
     */
    public static void cargarConAuth(Context context, String url, ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.drawable.mascota);
            return;
        }

        // Si la URL es de nuestra API, necesitamos el token
        if (url.contains("api-adoptasv")) {
            GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + getSyncToken())
                    .build());

            Glide.with(context)
                    .load(glideUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.mascota)
                    .error(R.drawable.mascota)
                    .centerCrop()
                    .into(imageView);
        } else {
            // URLs externas (ej. fotos de Google)
            Glide.with(context)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.mascota)
                    .error(R.drawable.mascota)
                    .centerCrop()
                    .into(imageView);
        }
    }

    private static String getSyncToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return "";
        try {
            // Intentamos obtenerlo de forma síncrona con un timeout corto
            GetTokenResult result = Tasks.await(user.getIdToken(false), 5, TimeUnit.SECONDS);
            return result.getToken();
        } catch (Exception e) {
            return "";
        }
    }
}
