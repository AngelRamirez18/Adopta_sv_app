package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.R;
import com.example.adoptasv.Util.EstadoUtils;
import com.example.adoptasv.Util.FechaUtils;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Detalle de una solicitud para el refugio/admin: datos del adoptante, mascota,
 * respuestas del formulario, puntaje y acciones aprobar/rechazar.
 * GET /api/solicitudes/{id} · PATCH /api/solicitudes/{id}/estado.
 */
public class PanelSolicitudDetalleFragment extends Fragment {

    private static final String ARG_ID = "solicitud_id";

    private int solicitudId;

    private ProgressBar progressBar;
    private TextView tvError;
    private NestedScrollView scrollContent;
    private CircleImageView ivMascota, ivAvatar;
    private TextView tvEstado, tvMatch, tvFecha, tvMascota, tvMascotaSub,
            tvAdoptante, tvAdoptanteEmail, tvAdoptanteTelefono, tvAdoptanteDireccion,
            tvRefugio, tvComentario;
    private LinearLayout llRespuestas, llComentario, llAcciones;
    private MaterialButton btnAprobar, btnRechazar;

    public static PanelSolicitudDetalleFragment newInstance(int solicitudId) {
        PanelSolicitudDetalleFragment f = new PanelSolicitudDetalleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, solicitudId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) solicitudId = getArguments().getInt(ARG_ID, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_panel_solicitud_detalle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar         = view.findViewById(R.id.progressBar);
        tvError             = view.findViewById(R.id.tvError);
        scrollContent       = view.findViewById(R.id.scrollContent);
        ivMascota           = view.findViewById(R.id.ivMascota);
        ivAvatar            = view.findViewById(R.id.ivAvatar);
        tvEstado            = view.findViewById(R.id.tvEstado);
        tvMatch             = view.findViewById(R.id.tvMatch);
        tvFecha             = view.findViewById(R.id.tvFecha);
        tvMascota           = view.findViewById(R.id.tvMascota);
        tvMascotaSub        = view.findViewById(R.id.tvMascotaSub);
        tvAdoptante         = view.findViewById(R.id.tvAdoptante);
        tvAdoptanteEmail    = view.findViewById(R.id.tvAdoptanteEmail);
        tvAdoptanteTelefono = view.findViewById(R.id.tvAdoptanteTelefono);
        tvAdoptanteDireccion= view.findViewById(R.id.tvAdoptanteDireccion);
        tvRefugio           = view.findViewById(R.id.tvRefugio);
        tvComentario        = view.findViewById(R.id.tvComentario);
        llRespuestas        = view.findViewById(R.id.llRespuestas);
        llComentario        = view.findViewById(R.id.llComentario);
        llAcciones          = view.findViewById(R.id.llAcciones);
        btnAprobar          = view.findViewById(R.id.btnAprobar);
        btnRechazar         = view.findViewById(R.id.btnRechazar);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

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
                    bind(response.body().data);
                    scrollContent.setVisibility(View.VISIBLE);
                } else if (response.code() == 403) {
                    mostrarError("No tenés acceso a esta solicitud.");
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

    private void bind(Solicitud s) {
        Mascota m = s.mascota;
        User a = s.adoptante;

        EstadoUtils.aplicarBadge(tvEstado, s.estado);
        tvMatch.setText(s.puntajeEvaluacion + "% Match");
        tvFecha.setText(FechaUtils.formatoCorto(s.createdAt));

        tvMascota.setText(m != null && m.nombre != null ? m.nombre : "Mascota");
        tvMascotaSub.setText(buildRaza(m));
        String fotoUrl = m != null ? m.fotoUrl : null;
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(this).load(fotoUrl)
                    .placeholder(R.drawable.placeholder_mascota).error(R.drawable.placeholder_mascota).into(ivMascota);
        } else {
            ivMascota.setImageResource(R.drawable.placeholder_mascota);
        }

        tvAdoptante.setText(a != null && a.name != null ? a.name : "Adoptante");
        tvAdoptanteEmail.setText(a != null && a.email != null ? a.email : "Sin correo");
        tvAdoptanteTelefono.setText(a != null && a.telefono != null && !a.telefono.isEmpty()
                ? a.telefono : "Sin teléfono");
        if (a != null && a.direccion != null && !a.direccion.isEmpty()) {
            tvAdoptanteDireccion.setVisibility(View.VISIBLE);
            tvAdoptanteDireccion.setText(a.direccion);
        } else {
            tvAdoptanteDireccion.setVisibility(View.GONE);
        }
        String avatarUrl = a != null ? a.fotoPerfil : null;
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl)
                    .placeholder(R.drawable.ic_person).error(R.drawable.ic_person).into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person);
        }

        Refugio refugio = s.refugio != null ? s.refugio : (m != null ? m.refugio : null);
        tvRefugio.setText(refugio != null && refugio.nombre != null ? refugio.nombre : "—");

        construirRespuestas(s.respuestasFormulario);

        if (s.comentario != null && !s.comentario.isEmpty()) {
            llComentario.setVisibility(View.VISIBLE);
            tvComentario.setText(s.comentario);
        } else {
            llComentario.setVisibility(View.GONE);
        }

        String estado = s.estado == null ? "" : s.estado.toLowerCase();
        boolean accionable = estado.equals("pendiente") || estado.equals("en_revision");
        llAcciones.setVisibility(accionable ? View.VISIBLE : View.GONE);
        btnAprobar.setOnClickListener(v -> confirmar(s, "aprobada"));
        btnRechazar.setOnClickListener(v -> confirmar(s, "rechazada"));
    }

    private void construirRespuestas(Solicitud.RespuestasFormulario r) {
        llRespuestas.removeAllViews();
        if (r == null) {
            addRespuesta("Sin respuestas", "El adoptante no completó el formulario.");
            return;
        }
        if (r.tipoVivienda != null && !r.tipoVivienda.isEmpty())
            addRespuesta("Tipo de vivienda", capitalize(r.tipoVivienda));
        addRespuesta("¿Tiene patio?", r.tienePatio ? "Sí" : "No");
        addRespuesta("¿Tiene otros animales?", r.otrosAnimales ? "Sí" : "No");
        if (r.horasEnCasa != null)
            addRespuesta("Horas en casa al día", r.horasEnCasa + " h");
        if (r.experiencia != null && !r.experiencia.isEmpty())
            addRespuesta("Experiencia con mascotas", r.experiencia);
        if (r.compromiso != null && !r.compromiso.isEmpty())
            addRespuesta("Compromiso", r.compromiso);

        if (llRespuestas.getChildCount() == 0) {
            addRespuesta("Sin respuestas", "El adoptante no completó el formulario.");
        }
    }

    private void addRespuesta(String label, String valor) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (llRespuestas.getChildCount() > 0) lp.topMargin = dp(14);
        col.setLayoutParams(lp);

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(0xFF51443C);
        tvLabel.setTextSize(12);
        tvLabel.setAllCaps(true);
        tvLabel.setLetterSpacing(0.04f);
        col.addView(tvLabel);

        TextView tvValor = new TextView(requireContext());
        tvValor.setText(valor);
        tvValor.setTextColor(0xFF3E2723);
        tvValor.setTextSize(15);
        tvValor.setPadding(0, dp(2), 0, 0);
        col.addView(tvValor);

        llRespuestas.addView(col);
    }

    private void confirmar(Solicitud s, String nuevoEstado) {
        String accion = nuevoEstado.equals("aprobada") ? "aprobar" : "rechazar";
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Comentario para el adoptante (opcional)");
        input.setPadding(dp(20), dp(8), dp(20), dp(8));
        new AlertDialog.Builder(requireContext())
                .setTitle("¿" + capitalize(accion) + " solicitud?")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sí", (d, w) ->
                        cambiarEstado(s, nuevoEstado, input.getText().toString().trim()))
                .show();
    }

    private void cambiarEstado(Solicitud s, String nuevoEstado, String comentario) {
        Map<String, Object> body = new HashMap<>();
        body.put("estado", nuevoEstado);
        if (!comentario.isEmpty()) body.put("comentario", comentario);

        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);

        ApiClient.getService().updateEstadoSolicitud(s.id, body)
                .enqueue(new Callback<SingleResponse<Solicitud>>() {
                    @Override
                    public void onResponse(@NonNull Call<SingleResponse<Solicitud>> call,
                                           @NonNull Response<SingleResponse<Solicitud>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    nuevoEstado.equals("aprobada") ? "Solicitud aprobada" : "Solicitud rechazada",
                                    Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            btnAprobar.setEnabled(true);
                            btnRechazar.setEnabled(true);
                            String msg = response.code() == 403
                                    ? "No tenés permiso sobre esta solicitud."
                                    : "No se pudo actualizar la solicitud.";
                            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SingleResponse<Solicitud>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        btnAprobar.setEnabled(true);
                        btnRechazar.setEnabled(true);
                        Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
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
        if (s == null || s.isEmpty()) return s;
        s = s.replace("_", " ");
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
