package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.adoptasv.Adaptadores.GaleriaFullscreenAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.Util.GlideUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de detalle de una mascota (estilo Material 3 — bento + hero 4:5).
 * Se abre al tocar una card en HomeFragment. Recibe el id de la mascota como
 * argumento y carga el detalle completo desde la API.
 */
public class DetallesMascotaFragment extends Fragment {

    private static final String ARG_MASCOTA_ID = "mascota_id";

    private int mascotaId;
    private Mascota mascota;

    private ProgressBar progressBar;
    private TextView tvError;
    private NestedScrollView scrollContent;
    private LinearLayout llBottomBar;

    private ImageView ivFoto, ivSexo;
    private LinearLayout llThumbsContainer, llThumbs, llUbicacion, llSalud;
    private TextView tvContador;
    private TextView tvNombre, tvUbicacion, tvRaza, tvEstadoAdopcion;
    private TextView tvEdadNum, tvEdadUnidad, tvSexo, tvTamano;
    private TextView tvLblAbout, tvDescripcion, tvLblPersonalidad, tvLblSalud;
    private ChipGroup cgPersonalidad;
    private MaterialButton btnAdoptar, btnChat;

    public DetallesMascotaFragment() {
        // Constructor vacío requerido
    }

    public static DetallesMascotaFragment newInstance(int mascotaId) {
        DetallesMascotaFragment fragment = new DetallesMascotaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MASCOTA_ID, mascotaId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mascotaId = getArguments().getInt(ARG_MASCOTA_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalles_mascota, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar      = view.findViewById(R.id.progressBar);
        tvError          = view.findViewById(R.id.tvError);
        scrollContent    = view.findViewById(R.id.scrollContent);
        llBottomBar      = view.findViewById(R.id.llBottomBar);

        ivFoto           = view.findViewById(R.id.ivFoto);
        ivSexo           = view.findViewById(R.id.ivSexo);
        llThumbsContainer = view.findViewById(R.id.llThumbsContainer);
        llThumbs         = view.findViewById(R.id.llThumbs);
        llUbicacion      = view.findViewById(R.id.llUbicacion);
        llSalud          = view.findViewById(R.id.llSalud);
        tvContador       = view.findViewById(R.id.tvContador);
        tvNombre         = view.findViewById(R.id.tvNombre);
        tvUbicacion      = view.findViewById(R.id.tvUbicacion);
        tvRaza           = view.findViewById(R.id.tvRaza);
        tvEstadoAdopcion = view.findViewById(R.id.tvEstadoAdopcion);
        tvEdadNum        = view.findViewById(R.id.tvEdadNum);
        tvEdadUnidad     = view.findViewById(R.id.tvEdadUnidad);
        tvSexo           = view.findViewById(R.id.tvSexo);
        tvTamano         = view.findViewById(R.id.tvTamano);
        tvLblAbout       = view.findViewById(R.id.tvLblAbout);
        tvDescripcion    = view.findViewById(R.id.tvDescripcion);
        tvLblPersonalidad = view.findViewById(R.id.tvLblPersonalidad);
        tvLblSalud       = view.findViewById(R.id.tvLblSalud);
        cgPersonalidad   = view.findViewById(R.id.cgPersonalidad);
        btnAdoptar       = view.findViewById(R.id.btnAdoptar);
        btnChat          = view.findViewById(R.id.btnChat);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        ImageButton btnFavorito = view.findViewById(R.id.btnFavorito);
        btnFavorito.setOnClickListener(v ->
                Toast.makeText(getContext(), "Favoritos — próximamente", Toast.LENGTH_SHORT).show());

        btnChat.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mensajes — próximamente", Toast.LENGTH_SHORT).show());

        btnAdoptar.setOnClickListener(v -> confirmarAdopcion());

        if (mascotaId <= 0) {
            mostrarError("Mascota no válida");
            return;
        }
        cargarDetalle();
    }

    private void cargarDetalle() {
        progressBar.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        llBottomBar.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getMascota(mascotaId).enqueue(new Callback<SingleResponse<Mascota>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Mascota>> call,
                                   @NonNull Response<SingleResponse<Mascota>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    mascota = response.body().data;
                    bindMascota(mascota);
                    scrollContent.setVisibility(View.VISIBLE);
                    llBottomBar.setVisibility(View.VISIBLE);
                } else {
                    mostrarError("No se pudo cargar la mascota");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Mascota>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void bindMascota(Mascota m) {
        tvNombre.setText(m.nombre);
        tvRaza.setText(buildRaza(m));

        // Edad (número + unidad)
        bindEdad(m.edadMeses);

        // Sexo (ícono + texto)
        bindSexo(m.sexo);

        // Tamaño
        tvTamano.setText(m.tamano != null && !m.tamano.isEmpty() ? capitalize(m.tamano) : "—");

        // Ubicación (refugio)
        bindUbicacion(m.refugio);

        // Galería (foto principal + fotos extra)
        bindGaleria(m);

        // Descripción
        setTextOrHide(tvLblAbout, tvDescripcion, m.descripcion);

        // Personalidad (chips)
        bindPersonalidad(m.personalidad);

        // Salud y cuidados
        bindSalud(m);

        // Estado de adopción
        bindEstadoAdopcion(m.estadoAdopcion);
    }

    private void bindEdad(Integer meses) {
        if (meses == null) {
            tvEdadNum.setText("—");
            tvEdadUnidad.setText("");
            return;
        }
        if (meses < 12) {
            tvEdadNum.setText(String.valueOf(meses));
            tvEdadUnidad.setText(meses == 1 ? "mes" : "meses");
        } else {
            int anios = meses / 12;
            tvEdadNum.setText(String.valueOf(anios));
            tvEdadUnidad.setText(anios == 1 ? "año" : "años");
        }
    }

    private void bindSexo(String sexo) {
        if (sexo == null) {
            tvSexo.setText("—");
            ivSexo.setVisibility(View.INVISIBLE);
            return;
        }
        tvSexo.setText(capitalize(sexo));
        ivSexo.setVisibility(View.VISIBLE);
        String s = sexo.toLowerCase();
        boolean hembra = s.startsWith("h") || s.startsWith("f");
        ivSexo.setImageResource(hembra ? R.drawable.ic_female : R.drawable.ic_male);
    }

    private void bindUbicacion(Refugio refugio) {
        String texto = null;
        if (refugio != null) {
            if (refugio.direccion != null && !refugio.direccion.isEmpty()) {
                texto = refugio.direccion;
            } else if (refugio.nombre != null && !refugio.nombre.isEmpty()) {
                texto = refugio.nombre;
            }
        }
        if (texto != null) {
            tvUbicacion.setText(texto);
            llUbicacion.setVisibility(View.VISIBLE);
        } else {
            llUbicacion.setVisibility(View.GONE);
        }
    }

    private void bindGaleria(Mascota m) {
        List<String> fotos = new ArrayList<>();
        if (m.fotoUrl != null && !m.fotoUrl.isEmpty()) fotos.add(m.fotoUrl);
        if (m.fotosExtra != null) {
            for (String f : m.fotosExtra) {
                if (f != null && !f.isEmpty()) fotos.add(f);
            }
        }

        // Foto principal
        cargarHero(fotos.isEmpty() ? null : fotos.get(0));

        llThumbs.removeAllViews();
        if (fotos.size() <= 1) {
            llThumbsContainer.setVisibility(View.GONE);
            tvContador.setVisibility(View.GONE);
            return;
        }

        llThumbsContainer.setVisibility(View.VISIBLE);
        tvContador.setVisibility(View.VISIBLE);
        tvContador.setText("1/" + fotos.size());

        int sizePx = dp(48);
        for (int i = 0; i < fotos.size(); i++) {
            final int index = i;
            final String url = fotos.get(i);
            ImageView thumb = new ImageView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
            if (i > 0) lp.leftMargin = dp(6);
            thumb.setLayoutParams(lp);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setAlpha(i == 0 ? 1f : 0.7f);
            
            GlideUtils.cargarConAuth(requireContext(), url, thumb);
            
            thumb.setOnClickListener(v -> {
                cargarHero(url);
                tvContador.setText((index + 1) + "/" + fotos.size());
                for (int j = 0; j < llThumbs.getChildCount(); j++) {
                    llThumbs.getChildAt(j).setAlpha(j == index ? 1f : 0.7f);
                }
            });
            llThumbs.addView(thumb);
        }
    }

    private void cargarHero(String url) {
        GlideUtils.cargarConAuth(requireContext(), url, ivFoto);
    }

    private void bindPersonalidad(String personalidad) {
        cgPersonalidad.removeAllViews();
        if (personalidad == null || personalidad.trim().isEmpty()) {
            tvLblPersonalidad.setVisibility(View.GONE);
            cgPersonalidad.setVisibility(View.GONE);
            return;
        }
        tvLblPersonalidad.setVisibility(View.VISIBLE);
        cgPersonalidad.setVisibility(View.VISIBLE);

        // Separa por comas; si no hay comas, muestra el texto completo como un chip
        String[] rasgos = personalidad.split("[,;]");
        for (String rasgo : rasgos) {
            String t = rasgo.trim();
            if (t.isEmpty()) continue;
            cgPersonalidad.addView(crearChip(capitalize(t)));
        }
    }

    private TextView crearChip(String texto) {
        TextView chip = new TextView(requireContext());
        chip.setText(texto);
        chip.setTextColor(0xFF6F4627);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
        chip.setBackgroundResource(R.drawable.bg_chip_personalidad);
        chip.setPadding(dp(16), dp(8), dp(16), dp(8));
        return chip;
    }

    private void bindSalud(Mascota m) {
        llSalud.removeAllViews();
        boolean hayAlgo = false;

        if (m.vacunas) {
            llSalud.addView(crearFilaSalud("Vacunas al día"));
            hayAlgo = true;
        }
        if (m.esterilizado) {
            llSalud.addView(crearFilaSalud("Esterilizado"));
            hayAlgo = true;
        }
        if (m.estadoSalud != null && !m.estadoSalud.trim().isEmpty()) {
            llSalud.addView(crearFilaSalud(m.estadoSalud.trim()));
            hayAlgo = true;
        }

        tvLblSalud.setVisibility(hayAlgo ? View.VISIBLE : View.GONE);
        llSalud.setVisibility(hayAlgo ? View.VISIBLE : View.GONE);
    }

    private View crearFilaSalud(String texto) {
        LinearLayout fila = new LinearLayout(requireContext());
        LinearLayout.LayoutParams filaLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filaLp.bottomMargin = dp(8);
        fila.setLayoutParams(filaLp);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setBackgroundResource(R.drawable.bg_health_row);
        fila.setPadding(dp(16), dp(16), dp(16), dp(16));

        // Círculo con check
        FrameLayout badge = new FrameLayout(requireContext());
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(dp(32), dp(32));
        badge.setLayoutParams(badgeLp);
        badge.setBackgroundResource(R.drawable.bg_circle_check);

        ImageView check = new ImageView(requireContext());
        FrameLayout.LayoutParams checkLp = new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER);
        check.setLayoutParams(checkLp);
        check.setImageResource(R.drawable.ic_check_circle);
        badge.addView(check);
        fila.addView(badge);

        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvLp.leftMargin = dp(16);
        tv.setLayoutParams(tvLp);
        tv.setText(texto);
        tv.setTextColor(0xFF1B1C1A);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        fila.addView(tv);

        return fila;
    }

    private void bindEstadoAdopcion(String estado) {
        if (estado == null || estado.isEmpty()) {
            tvEstadoAdopcion.setText("Adoptable");
            btnAdoptar.setEnabled(true);
            return;
        }
        boolean disponible = "disponible".equalsIgnoreCase(estado);
        tvEstadoAdopcion.setText(disponible ? "Adoptable" : capitalize(estado.replace("_", " ")));

        if (disponible) {
            btnAdoptar.setEnabled(true);
            btnAdoptar.setText("Adoptar ahora");
        } else {
            btnAdoptar.setEnabled(false);
            btnAdoptar.setText("No disponible");
        }
    }

    private void confirmarAdopcion() {
        if (mascota == null) return;

        View form = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_solicitud_adopcion, null);

        EditText etTipoVivienda = form.findViewById(R.id.etTipoVivienda);
        SwitchMaterial swTienePatio = form.findViewById(R.id.swTienePatio);
        SwitchMaterial swOtrosAnimales = form.findViewById(R.id.swOtrosAnimales);
        EditText etExperiencia = form.findViewById(R.id.etExperiencia);
        EditText etHorasEnCasa = form.findViewById(R.id.etHorasEnCasa);
        EditText etCompromiso = form.findViewById(R.id.etCompromiso);

        new AlertDialog.Builder(requireContext())
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar solicitud", (dialog, which) -> {
                    String tipoVivienda = etTipoVivienda.getText().toString().trim();
                    String experiencia = etExperiencia.getText().toString().trim();
                    String horasStr = etHorasEnCasa.getText().toString().trim();
                    String compromiso = etCompromiso.getText().toString().trim();

                    if (tipoVivienda.isEmpty()) {
                        etTipoVivienda.setError("Campo obligatorio");
                        return;
                    }
                    if (experiencia.isEmpty()) {
                        etExperiencia.setError("Campo obligatorio");
                        return;
                    }
                    if (horasStr.isEmpty()) {
                        etHorasEnCasa.setError("Campo obligatorio");
                        return;
                    }
                    int horas;
                    try {
                        horas = Integer.parseInt(horasStr);
                        if (horas < 0 || horas > 24) {
                            etHorasEnCasa.setError("Debe ser entre 0 y 24");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        etHorasEnCasa.setError("Número inválido");
                        return;
                    }
                    if (compromiso.isEmpty()) {
                        etCompromiso.setError("Campo obligatorio");
                        return;
                    }

                    Map<String, Object> datosAdicionales = new HashMap<>();
                    datosAdicionales.put("tipo_vivienda", tipoVivienda);
                    datosAdicionales.put("tiene_patio", swTienePatio.isChecked());
                    datosAdicionales.put("otros_animales", swOtrosAnimales.isChecked());
                    datosAdicionales.put("experiencia", experiencia);
                    datosAdicionales.put("horas_en_casa", horas);
                    datosAdicionales.put("compromiso", compromiso);

                    enviarSolicitud(datosAdicionales);
                })
                .show();
    }

    private void enviarSolicitud(Map<String, Object> datosAdicionales) {
        btnAdoptar.setEnabled(false);
        btnAdoptar.setText("Enviando…");

        Map<String, Object> body = new HashMap<>();
        body.put("mascota_id", mascota.id);
        body.put("respuestas_formulario", datosAdicionales);

        ApiClient.getService().createSolicitud(body).enqueue(new Callback<Solicitud>() {
            @Override
            public void onResponse(@NonNull Call<Solicitud> call, @NonNull Response<Solicitud> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "¡Solicitud enviada! El refugio se pondrá en contacto.",
                            Toast.LENGTH_LONG).show();
                    btnAdoptar.setText("Solicitud enviada");
                } else {
                    btnAdoptar.setEnabled(true);
                    btnAdoptar.setText("Adoptar ahora");
                    String msg = response.code() == 409
                            ? "Ya tenés una solicitud para esta mascota"
                            : "No se pudo enviar la solicitud";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Solicitud> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnAdoptar.setEnabled(true);
                btnAdoptar.setText("Adoptar ahora");
                Toast.makeText(getContext(),
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────

    private void setTextOrHide(TextView label, TextView value, String text) {
        if (text != null && !text.trim().isEmpty()) {
            value.setText(text);
            label.setVisibility(View.VISIBLE);
            value.setVisibility(View.VISIBLE);
        } else {
            label.setVisibility(View.GONE);
            value.setVisibility(View.GONE);
        }
    }

    private String buildRaza(Mascota m) {
        String raza = (m.raza != null && !m.raza.isEmpty()) ? m.raza : null;
        String especie = (m.especie != null && !m.especie.isEmpty()) ? capitalize(m.especie) : null;
        if (raza != null && especie != null) return raza + " · " + especie;
        if (raza != null) return raza;
        if (especie != null) return especie;
        return "";
    }

    private void mostrarError(String msg) {
        scrollContent.setVisibility(View.GONE);
        llBottomBar.setVisibility(View.GONE);
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
