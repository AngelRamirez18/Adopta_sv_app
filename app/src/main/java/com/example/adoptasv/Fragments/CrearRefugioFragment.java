package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.R;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Formulario para registrar (POST /refugios) o editar (PUT /refugios/{id}) un
 * refugio. Al crear el primero, el backend asigna el rol voluntario al usuario.
 */
public class CrearRefugioFragment extends Fragment {

    private static final String ARG_REFUGIO_ID = "refugio_id";

    private int refugioId = -1;

    private TextView tvTitulo;
    private EditText etNombre, etDireccion, etTelefono, etCorreo, etDescripcion, etLatitud, etLongitud;
    private MaterialButton btnGuardar;
    private ProgressBar progressBar;

    public CrearRefugioFragment() {}

    public static CrearRefugioFragment newInstance(int refugioId) {
        CrearRefugioFragment f = new CrearRefugioFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_REFUGIO_ID, refugioId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) refugioId = getArguments().getInt(ARG_REFUGIO_ID, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_refugio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitulo      = view.findViewById(R.id.tvTitulo);
        etNombre      = view.findViewById(R.id.etNombre);
        etDireccion   = view.findViewById(R.id.etDireccion);
        etTelefono    = view.findViewById(R.id.etTelefono);
        etCorreo      = view.findViewById(R.id.etCorreo);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        etLatitud     = view.findViewById(R.id.etLatitud);
        etLongitud    = view.findViewById(R.id.etLongitud);
        btnGuardar    = view.findViewById(R.id.btnGuardar);
        progressBar   = view.findViewById(R.id.progressBar);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnGuardar.setOnClickListener(v -> guardar());

        if (refugioId > 0) {
            tvTitulo.setText("Editar refugio");
            btnGuardar.setText("Guardar cambios");
            cargarRefugio();
        }
    }

    private void cargarRefugio() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService().getRefugio(refugioId).enqueue(new Callback<SingleResponse<Refugio>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Refugio>> call,
                                   @NonNull Response<SingleResponse<Refugio>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    rellenar(response.body().data);
                } else {
                    Toast.makeText(getContext(), "No se pudo cargar el refugio.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Refugio>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void rellenar(Refugio r) {
        etNombre.setText(r.nombre);
        etDireccion.setText(r.direccion);
        etTelefono.setText(r.telefono);
        etCorreo.setText(r.correo);
        etDescripcion.setText(r.descripcion);
        double lat = r.getLat(), lng = r.getLng();
        if (lat != 0) etLatitud.setText(String.valueOf(lat));
        if (lng != 0) etLongitud.setText(String.valueOf(lng));
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresá el nombre");
            return;
        }
        if (direccion.isEmpty()) {
            etDireccion.setError("Ingresá la dirección");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("direccion", direccion);
        putIfNotEmpty(body, "telefono", etTelefono);
        putIfNotEmpty(body, "correo", etCorreo);
        putIfNotEmpty(body, "descripcion", etDescripcion);
        putDoubleIfValid(body, "latitud", etLatitud);
        putDoubleIfValid(body, "longitud", etLongitud);

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando…");
        progressBar.setVisibility(View.VISIBLE);

        Call<SingleResponse<Refugio>> call = refugioId > 0
                ? ApiClient.getService().updateRefugio(refugioId, body)
                : ApiClient.getService().createRefugio(body);

        call.enqueue(new Callback<SingleResponse<Refugio>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Refugio>> c,
                                   @NonNull Response<SingleResponse<Refugio>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            refugioId > 0 ? "Refugio actualizado" : "¡Refugio registrado!",
                            Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    restaurar();
                    String msg;
                    if (response.code() == 422) {
                        msg = "Ya tenés un refugio registrado.";
                    } else if (response.code() == 403) {
                        msg = "No tenés permiso sobre este refugio (rol/guard).";
                    } else {
                        msg = "No se pudo guardar el refugio (HTTP " + response.code() + ").";
                    }
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Refugio>> c, @NonNull Throwable t) {
                if (!isAdded()) return;
                restaurar();
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void restaurar() {
        progressBar.setVisibility(View.GONE);
        btnGuardar.setEnabled(true);
        btnGuardar.setText(refugioId > 0 ? "Guardar cambios" : "Registrar refugio");
    }

    private void putIfNotEmpty(Map<String, Object> body, String key, EditText et) {
        String val = et.getText().toString().trim();
        if (!val.isEmpty()) body.put(key, val);
    }

    private void putDoubleIfValid(Map<String, Object> body, String key, EditText et) {
        String val = et.getText().toString().trim();
        if (val.isEmpty()) return;
        try {
            body.put(key, Double.parseDouble(val));
        } catch (NumberFormatException ignored) {
            // Coordenada inválida: simplemente no se envía.
        }
    }
}
