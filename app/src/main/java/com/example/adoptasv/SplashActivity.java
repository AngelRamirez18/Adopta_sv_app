package com.example.adoptasv;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvCargando;
    private int dotCount = 0;
    private boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Status bar transparente
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        setContentView(R.layout.activity_splash);

        ImageView paw1 = findViewById(R.id.paw1);
        ImageView paw2 = findViewById(R.id.paw2);
        ImageView paw3 = findViewById(R.id.paw3);
        ImageView paw4 = findViewById(R.id.paw4);
        ImageView paw5 = findViewById(R.id.paw5);
        ImageView paw6 = findViewById(R.id.paw6);
        TextView tvAppName = findViewById(R.id.tvAppName);
        tvCargando = findViewById(R.id.tvCargando);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);

        // Patitas con efecto pisada
        pisada(paw1, 0);
        pisada(paw2, 300);
        pisada(paw3, 600);
        pisada(paw4, 900);
        pisada(paw5, 1150);
        pisada(paw6, 1350);

        // Logo y textos
        aparecerTexto(tvAppName, 2050);
        aparecerTexto(tvCargando, 2200);
        aparecerTexto(tvSubtitle, 2350);

        // Animación puntos "Cargando..."
        handler.postDelayed(this::animarPuntos, 2200);

        // Ir al login
        handler.postDelayed(() -> {
            running = false;
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 3600);
    }

    private void animarPuntos() {
        if (!running) return;
        String[] puntos = {"", ".", "..", "..."};
        tvCargando.setText("Cargando" + puntos[dotCount % 4]);
        dotCount++;
        handler.postDelayed(this::animarPuntos, 400);
    }

    private void pisada(ImageView view, long delay) {
        handler.postDelayed(() -> {
            view.setAlpha(0f);
            view.setScaleX(0.2f);
            view.setScaleY(0.2f);

            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            alpha.setDuration(120);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.2f, 1.2f, 1f);
            scaleX.setDuration(300);
            scaleX.setInterpolator(new OvershootInterpolator(4f));

            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.2f, 1.2f, 1f);
            scaleY.setDuration(300);
            scaleY.setInterpolator(new OvershootInterpolator(4f));

            float rot = view.getRotation();
            ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation",
                    rot - 12f, rot + 4f, rot);
            rotation.setDuration(300);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(alpha, scaleX, scaleY, rotation);
            set.start();
        }, delay);
    }

    private void aparecerLogo(View view, long delay) {
        handler.postDelayed(() -> {
            view.setAlpha(0f);
            view.setScaleX(0f);
            view.setScaleY(0f);

            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            alpha.setDuration(400);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.1f, 1f);
            scaleX.setDuration(500);
            scaleX.setInterpolator(new OvershootInterpolator(2f));

            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.1f, 1f);
            scaleY.setDuration(500);
            scaleY.setInterpolator(new OvershootInterpolator(2f));

            AnimatorSet set = new AnimatorSet();
            set.playTogether(alpha, scaleX, scaleY);
            set.start();
        }, delay);
    }


    private void aparecerTexto(View view, long delay) {
        handler.postDelayed(() -> {
            view.setAlpha(0f);
            view.setTranslationY(24f);

            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            alpha.setDuration(350);

            ObjectAnimator translY = ObjectAnimator.ofFloat(view, "translationY", 24f, 0f);
            translY.setDuration(350);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(alpha, translY);
            set.start();
        }, delay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacksAndMessages(null);
    }
}