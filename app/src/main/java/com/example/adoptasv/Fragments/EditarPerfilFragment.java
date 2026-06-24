package com.example.adoptasv.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.R;
import com.example.adoptasv.Util.MultipartUtils;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Edición del perfil propio (los 3 roles): PUT /api/perfil para nombre/teléfono/dirección
 * y POST /api/perfil/foto para la foto. Reemplazó el placeholder "próximamente".
 */
public class EditarPerfilFragment extends Fragment {

    private CircleImageView ivAvatar;
    private EditText etNombre, etTelefono, etDireccion;
    private MaterialButton btnGuardar;
    private ProgressBar progressBar;

    private Uri fotoUri;                          // foto nueva seleccionada (null = no cambia)
    private ActivityResultLauncher<String> imagePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar    = view.findViewById(R.id.ivAvatar);
        etNombre    = view.findViewById(R.id.etNombre);
        etTelefono  = view.findViewById(R.id.etTelefono);
        etDireccion = view.findViewById(R.id.etDireccion);
        btnGuardar  = view.findViewById(R.id.btnGuardar);
        progressBar = view.findViewById(R.id.progressBar);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri == null) return;
                    fotoUri = uri;
                    Glide.with(this).load(uri).into(ivAvatar);
                });
        ivAvatar.setOnClickListener(v -> imagePicker.launch("image/*"));

        btnGuardar.setOnClickListener(v -> guardar());

        cargarPerfil();
    }

    /** Prellena el formulario con los datos actuales del perfil. */
    private void cargarPerfil() {
        ApiClient.getService().getPerfil().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                User u = response.body();
                if (response.isSuccessful() && u != null) {
                    etNombre.setText(u.name);
                    etTelefono.setText(u.telefono);
                    etDireccion.setText(u.direccion);
                    if (u.fotoPerfil != null && !u.fotoPerfil.isEmpty()) {
                        Glide.with(EditarPerfilFragment.this).load(u.fotoPerfil)
                                .placeholder(android.R.drawable.ic_menu_myplaces).into(ivAvatar);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                // El formulario sigue editable aunque falle la precarga.
            }
        });
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresá tu nombre");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", nombre);
        body.put("telefono", etTelefono.getText().toString().trim());
        body.put("direccion", etDireccion.getText().toString().trim());

        setCargando(true);
        ApiClient.getService().updatePerfil(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    // Si hay foto nueva, subirla; si no, terminar.
                    if (fotoUri != null) subirFoto();
                    else finalizar();
                } else {
                    setCargando(false);
                    Toast.makeText(getContext(), "No se pudo guardar el perfil.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setCargando(false);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subirFoto() {
        MultipartBody.Part part = MultipartUtils.fotoPart(requireContext(), fotoUri, "foto");
        if (part == null) { finalizar(); return; }

        ApiClient.getService().uploadFotoPerfil(part).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call,
                                   @NonNull Response<Map<String, String>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Perfil guardado, pero la foto no se pudo subir.", Toast.LENGTH_LONG).show();
                }
                finalizar();
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Perfil guardado, pero la foto no se pudo subir.", Toast.LENGTH_LONG).show();
                finalizar();
            }
        });
    }

    private void finalizar() {
        if (!isAdded()) return;
        setCargando(false);
        Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void setCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando);
        btnGuardar.setText(cargando ? "Guardando…" : "Guardar cambios");
    }
}
