package com.example.adoptasv.Fragments;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adoptasv.Adaptadores.SeguimientoAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.Seguimiento;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.R;
import com.example.adoptasv.Util.EstadoUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Detalle de una solicitud — "Estado de Adopción": tarjeta de mascota,
 * línea de tiempo del proceso, próximo paso y seguimientos asociados.
 */
public class SolicitudDetalleFragment extends Fragment {

    private static final String ARG_SOLICITUD_ID = "solicitud_id";

    private static final int COLOR_DONE    = 0xFF6F4627; // café
    private static final int COLOR_CURRENT = 0xFFFC9D41; // naranja
    private static final int COLOR_PENDING = 0xFFD5C3B8; // gris cálido
    private static final int COLOR_OK      = 0xFF2E7D32; // verde
    private static final int COLOR_BAD     = 0xFFC62828; // rojo

    private int solicitudId;
    private String refugioTelefono = null;

    private ProgressBar progressBar;
    private TextView tvError;
    private NestedScrollView scrollContent;
    private ImageView ivFoto;
    private TextView tvNombre, tvEstado, tvRaza, tvRefugio, tvProximoPaso, tvSinSeguimientos;
    private LinearLayout llTimeline, llRefugio;
    private MaterialButton btnContactar, btnSeguimiento;
    private RecyclerView rvSeguimientos;
    private SeguimientoAdapter seguimientoAdapter;

    public static SolicitudDetalleFragment newInstance(int solicitudId) {
        SolicitudDetalleFragment f = new SolicitudDetalleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SOLICITUD_ID, solicitudId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solicitudId = getArguments().getInt(ARG_SOLICITUD_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solicitud_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar       = view.findViewById(R.id.progressBar);
        tvError           = view.findViewById(R.id.tvError);
        scrollContent     = view.findViewById(R.id.scrollContent);
        ivFoto            = view.findViewById(R.id.ivFotoMascota);
        tvNombre          = view.findViewById(R.id.tvNombreMascota);
        tvEstado          = view.findViewById(R.id.tvEstado);
        tvRaza            = view.findViewById(R.id.tvRaza);
        tvRefugio         = view.findViewById(R.id.tvRefugio);
        llRefugio         = view.findViewById(R.id.llRefugio);
        llTimeline        = view.findViewById(R.id.llTimeline);
        tvProximoPaso     = view.findViewById(R.id.tvProximoPaso);
        tvSinSeguimientos = view.findViewById(R.id.tvSinSeguimientos);
        rvSeguimientos    = view.findViewById(R.id.rvSeguimientos);
        btnContactar      = view.findViewById(R.id.btnContactar);
        btnSeguimiento    = view.findViewById(R.id.btnSeguimiento);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        seguimientoAdapter = new SeguimientoAdapter(new ArrayList<>());
        rvSeguimientos.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSeguimientos.setAdapter(seguimientoAdapter);

        btnContactar.setOnClickListener(v -> contactarRefugio());
        btnSeguimiento.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, new SeguimientoFragment())
                .addToBackStack(null)
                .commit());

        if (solicitudId <= 0) {
            mostrarError("Solicitud no válida.");
            return;
        }
        cargarSolicitud();
    }

    private void cargarSolicitud() {
        progressBar.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getSolicitud(solicitudId).enqueue(new Callback<SingleResponse<Solicitud>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Solicitud>> call,
                                   @NonNull Response<SingleResponse<Solicitud>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindSolicitud(response.body().data);
                    scrollContent.setVisibility(View.VISIBLE);
                    cargarSeguimientos();
                } else {
                    mostrarError("No se pudo cargar la solicitud.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Solicitud>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void bindSolicitud(Solicitud s) {
        Mascota m = s.mascota;
        tvNombre.setText(m != null && m.nombre != null ? m.nombre : "Mascota");
        tvRaza.setText(buildRaza(m));
        EstadoUtils.aplicarBadge(tvEstado, m != null ? m.estadoAdopcion : s.estado);

        Refugio refugio = s.refugio != null ? s.refugio : (m != null ? m.refugio : null);
        if (refugio != null && refugio.nombre != null) {
            llRefugio.setVisibility(View.VISIBLE);
            tvRefugio.setText(refugio.nombre);
            refugioTelefono = refugio.telefono;
        } else {
            llRefugio.setVisibility(View.GONE);
        }

        String fotoUrl = m != null ? m.fotoUrl : null;
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(this).load(fotoUrl)
                    .placeholder(R.drawable.placeholder_mascota).error(R.drawable.placeholder_mascota)
                    .centerCrop().into(ivFoto);
        } else {
            ivFoto.setImageResource(R.drawable.placeholder_mascota);
        }

        construirTimeline(s.estado);
        tvProximoPaso.setText(proximoPaso(s.estado));
    }

    // ── Timeline ──────────────────────────────────────────

    private void construirTimeline(String estado) {
        llTimeline.removeAllViews();
        String e = estado == null ? "" : estado.toLowerCase();

        addPaso("Solicitud recibida", "Tu solicitud fue enviada al refugio",
                COLOR_DONE, R.drawable.ic_check_circle, true);

        if (e.equals("pendiente")) {
            addPaso("En revisión", "El refugio la revisará pronto",
                    COLOR_CURRENT, R.drawable.ic_eye, true);
            addPaso("Decisión final", "Pendiente de resultados",
                    COLOR_PENDING, R.drawable.ic_clock, false);
        } else if (e.equals("en_revision")) {
            addPaso("En revisión", "Tu solicitud está siendo evaluada",
                    COLOR_DONE, R.drawable.ic_check_circle, true);
            addPaso("Decisión final", "El refugio definirá en breve",
                    COLOR_CURRENT, R.drawable.ic_eye, false);
        } else if (e.equals("aprobada")) {
            addPaso("En revisión", "Evaluada por el refugio",
                    COLOR_DONE, R.drawable.ic_check_circle, true);
            addPaso("¡Aprobada!", "¡Felicidades, tu adopción fue aprobada!",
                    COLOR_OK, R.drawable.ic_check_circle, false);
        } else if (e.equals("rechazada")) {
            addPaso("En revisión", "Evaluada por el refugio",
                    COLOR_DONE, R.drawable.ic_check_circle, true);
            addPaso("No aprobada", "Esta vez no fue posible",
                    COLOR_BAD, R.drawable.ic_close_small, false);
        } else {
            addPaso("En proceso", "Seguí atento a las novedades",
                    COLOR_CURRENT, R.drawable.ic_eye, false);
        }
    }

    private void addPaso(String titulo, String sub, int color, int iconRes, boolean conector) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams filaLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fila.setLayoutParams(filaLp);

        // Columna del indicador (círculo + conector)
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(dp(34), ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout circulo = new FrameLayout(requireContext());
        circulo.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        circulo.setBackground(bg);

        ImageView icon = new ImageView(requireContext());
        FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(dp(18), dp(18), Gravity.CENTER);
        icon.setLayoutParams(iconLp);
        icon.setImageResource(iconRes);
        icon.setColorFilter(0xFFFFFFFF);
        circulo.addView(icon);
        col.addView(circulo);

        if (conector) {
            View linea = new View(requireContext());
            LinearLayout.LayoutParams lineaLp = new LinearLayout.LayoutParams(dp(2), dp(28));
            linea.setLayoutParams(lineaLp);
            linea.setBackgroundColor(0xFFD5C3B8);
            col.addView(linea);
        }
        fila.addView(col);

        // Texto
        LinearLayout texto = new LinearLayout(requireContext());
        texto.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textoLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textoLp.leftMargin = dp(12);
        textoLp.topMargin = dp(4);
        texto.setLayoutParams(textoLp);

        TextView tvT = new TextView(requireContext());
        tvT.setText(titulo);
        tvT.setTextColor(0xFF3E2723);
        tvT.setTextSize(15);
        tvT.setTypeface(tvT.getTypeface(), android.graphics.Typeface.BOLD);
        texto.addView(tvT);

        TextView tvS = new TextView(requireContext());
        tvS.setText(sub);
        tvS.setTextColor(0xFF51443C);
        tvS.setTextSize(13);
        texto.addView(tvS);

        fila.addView(texto);
        llTimeline.addView(fila);
    }

    private String proximoPaso(String estado) {
        String e = estado == null ? "" : estado.toLowerCase();
        switch (e) {
            case "pendiente":
                return "Tu solicitud fue recibida. El refugio la revisará pronto.";
            case "en_revision":
                return "El refugio está evaluando tu solicitud. Te avisaremos del resultado.";
            case "aprobada":
                return "¡Aprobada! Coordiná con el refugio para conocer a tu mascota y enviá tus seguimientos.";
            case "rechazada":
                return "Esta vez no fue posible, pero hay muchas mascotas esperando un hogar.";
            default:
                return "Seguí atento al estado de tu solicitud.";
        }
    }

    private void contactarRefugio() {
        if (refugioTelefono == null || refugioTelefono.isEmpty()) {
            Toast.makeText(getContext(), "El refugio no tiene teléfono registrado.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + refugioTelefono));
        startActivity(intent);
    }

    // ── Seguimientos ──────────────────────────────────────

    private void cargarSeguimientos() {
        ApiClient.getService().getSeguimientosDeSolicitud(solicitudId)
                .enqueue(new Callback<PaginatedResponse<Seguimiento>>() {
                    @Override
                    public void onResponse(@NonNull Call<PaginatedResponse<Seguimiento>> call,
                                           @NonNull Response<PaginatedResponse<Seguimiento>> response) {
                        if (!isAdded()) return;
                        List<Seguimiento> lista = response.isSuccessful() && response.body() != null
                                ? response.body().data : null;
                        if (lista != null && !lista.isEmpty()) {
                            seguimientoAdapter.updateData(lista);
                            tvSinSeguimientos.setVisibility(View.GONE);
                            rvSeguimientos.setVisibility(View.VISIBLE);
                        } else {
                            tvSinSeguimientos.setVisibility(View.VISIBLE);
                            rvSeguimientos.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PaginatedResponse<Seguimiento>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        tvSinSeguimientos.setVisibility(View.VISIBLE);
                        tvSinSeguimientos.setText("No se pudieron cargar los seguimientos.");
                        rvSeguimientos.setVisibility(View.GONE);
                    }
                });
    }

    private String buildRaza(Mascota m) {
        if (m == null) return "";
        String raza = (m.raza != null && !m.raza.isEmpty()) ? m.raza : capitalize(m.especie);
        String edad = formatEdad(m.edadMeses);
        if (raza != null && edad != null) return raza + " • " + edad;
        if (raza != null) return raza;
        return edad != null ? edad : "";
    }

    private String formatEdad(Integer meses) {
        if (meses == null) return null;
        if (meses < 12) return meses + (meses == 1 ? " mes" : " meses");
        int anios = meses / 12;
        return anios + (anios == 1 ? " año" : " años");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return null;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void mostrarError(String msg) {
        scrollContent.setVisibility(View.GONE);
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
