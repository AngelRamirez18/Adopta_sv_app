package com.example.adoptasv.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.Seguimiento;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.R;
import com.example.adoptasv.Util.MultipartUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Formulario de seguimiento post-adopción. Solo accesible si el adoptante tiene
 * solicitudes aprobadas. POST /api/seguimientos + foto opcional multipart.
 */
public class SeguimientoFragment extends Fragment {

    private static final String[] ESTADOS_MASCOTA = {"Excelente", "Buena", "Regular"};

    private ProgressBar progressBar;
    private TextView tvError;
    private NestedScrollView scrollContent;
    private Spinner spSolicitud, spEstado;
    private EditText etComentario;
    private MaterialButton btnSeleccionarFoto, btnEnviar;
    private CardView cvPreview;
    private ImageView ivPreview;

    private final List<Solicitud> aprobadas = new ArrayList<>();
    private Uri fotoSeleccionada = null;

    private ActivityResultLauncher<String> imagePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seguimiento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar        = view.findViewById(R.id.progressBar);
        tvError            = view.findViewById(R.id.tvError);
        scrollContent      = view.findViewById(R.id.scrollContent);
        spSolicitud        = view.findViewById(R.id.spSolicitud);
        spEstado           = view.findViewById(R.id.spEstado);
        etComentario       = view.findViewById(R.id.etComentario);
        btnSeleccionarFoto = view.findViewById(R.id.btnSeleccionarFoto);
        btnEnviar          = view.findViewById(R.id.btnEnviar);
        cvPreview          = view.findViewById(R.id.cvPreview);
        ivPreview          = view.findViewById(R.id.ivPreview);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        spEstado.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, ESTADOS_MASCOTA));

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        fotoSeleccionada = uri;
                        cvPreview.setVisibility(View.VISIBLE);
                        Glide.with(this).load(uri).centerCrop().into(ivPreview);
                    }
                });

        btnSeleccionarFoto.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnEnviar.setOnClickListener(v -> enviarSeguimiento());

        cargarSolicitudesAprobadas();
    }

    private void cargarSolicitudesAprobadas() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            mostrarError("Tenés que iniciar sesión.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getMisSolicitudes().enqueue(new Callback<PaginatedResponse<Solicitud>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Solicitud>> call,
                                   @NonNull Response<PaginatedResponse<Solicitud>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    aprobadas.clear();
                    for (Solicitud s : response.body().data) {
                        if ("aprobada".equalsIgnoreCase(s.estado)) aprobadas.add(s);
                    }
                    if (aprobadas.isEmpty()) {
                        mostrarError("Solo podés enviar seguimientos de adopciones aprobadas.\nTodavía no tenés ninguna.");
                    } else {
                        poblarSpinnerSolicitudes();
                        scrollContent.setVisibility(View.VISIBLE);
                    }
                } else {
                    mostrarError("No se pudieron cargar tus adopciones.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Solicitud>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void poblarSpinnerSolicitudes() {
        List<String> nombres = new ArrayList<>();
        for (Solicitud s : aprobadas) {
            String nombre = s.mascota != null && s.mascota.nombre != null ? s.mascota.nombre : "Mascota";
            nombres.add(nombre + " (#" + s.id + ")");
        }
        spSolicitud.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombres));
    }

    private void enviarSeguimiento() {
        int pos = spSolicitud.getSelectedItemPosition();
        if (pos < 0 || pos >= aprobadas.size()) {
            Toast.makeText(getContext(), "Seleccioná una adopción.", Toast.LENGTH_SHORT).show();
            return;
        }
        Solicitud solicitud = aprobadas.get(pos);
        String comentario = etComentario.getText().toString().trim();
        String estadoMascota = (String) spEstado.getSelectedItem();

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando…");

        Map<String, Object> body = new HashMap<>();
        body.put("solicitud_id", solicitud.id);
        if (!comentario.isEmpty()) body.put("comentario", comentario);
        body.put("estado_mascota", estadoMascota);

        ApiClient.getService().createSeguimiento(body).enqueue(new Callback<SingleResponse<Seguimiento>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Seguimiento>> call,
                                   @NonNull Response<SingleResponse<Seguimiento>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Seguimiento creado = response.body().data;
                    if (fotoSeleccionada != null) {
                        subirFoto(creado.id);
                    } else {
                        finalizarOk();
                    }
                } else {
                    restaurarBoton();
                    String msg = response.code() == 422
                            ? "La adopción no está aprobada o ya enviaste este seguimiento."
                            : "No se pudo enviar el seguimiento.";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Seguimiento>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                restaurarBoton();
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subirFoto(int seguimientoId) {
        btnEnviar.setText("Subiendo foto…");
        MultipartBody.Part part = MultipartUtils.fotoPart(requireContext(), fotoSeleccionada, "foto");
        if (part == null) {
            Toast.makeText(getContext(), "No se pudo leer la foto, pero el seguimiento se guardó.",
                    Toast.LENGTH_LONG).show();
            finalizarOk();
            return;
        }
        ApiClient.getService().uploadFotoSeguimiento(seguimientoId, part)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, String>> call,
                                           @NonNull Response<Map<String, String>> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "El seguimiento se guardó, pero la foto no se pudo subir.",
                                    Toast.LENGTH_LONG).show();
                        }
                        finalizarOk();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "El seguimiento se guardó, pero la foto no se pudo subir.",
                                Toast.LENGTH_LONG).show();
                        finalizarOk();
                    }
                });
    }

    private void finalizarOk() {
        Toast.makeText(getContext(), "¡Seguimiento enviado!", Toast.LENGTH_LONG).show();
        getParentFragmentManager().popBackStack();
    }

    private void restaurarBoton() {
        btnEnviar.setEnabled(true);
        btnEnviar.setText("Enviar seguimiento");
    }

    private void mostrarError(String msg) {
        scrollContent.setVisibility(View.GONE);
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
