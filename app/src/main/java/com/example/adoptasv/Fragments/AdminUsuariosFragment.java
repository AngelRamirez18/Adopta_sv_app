package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adoptasv.Adaptadores.UsuarioAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.UsuarioAdmin;
import com.example.adoptasv.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Administración de usuarios (solo admin): lista GET /users + cambio de rol
 * PATCH /users/{id}/role (adoptante / voluntario / admin).
 */
public class AdminUsuariosFragment extends Fragment {

    private static final String[] ROLES = {"adoptante", "voluntario", "admin"};

    private ProgressBar progressBar;
    private TextView tvError;
    private RecyclerView rvUsuarios;
    private UsuarioAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_usuarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvError     = view.findViewById(R.id.tvError);
        rvUsuarios  = view.findViewById(R.id.rvUsuarios);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        adapter = new UsuarioAdapter(new ArrayList<>(), this::dialogoRol);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsuarios.setAdapter(adapter);

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        progressBar.setVisibility(View.VISIBLE);
        rvUsuarios.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getUsers().enqueue(new Callback<PaginatedResponse<UsuarioAdmin>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<UsuarioAdmin>> call,
                                   @NonNull Response<PaginatedResponse<UsuarioAdmin>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.code() == 403) {
                    mostrarError("Solo un administrador puede ver los usuarios.");
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<UsuarioAdmin> lista = response.body().data;
                    if (!lista.isEmpty()) {
                        adapter.updateData(lista);
                        rvUsuarios.setVisibility(View.VISIBLE);
                    } else {
                        mostrarError("No hay usuarios registrados.");
                    }
                } else {
                    mostrarError("No se pudieron cargar los usuarios.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<UsuarioAdmin>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void dialogoRol(UsuarioAdmin u) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Rol de " + (u.name != null ? u.name : "usuario"))
                .setItems(ROLES, (d, which) -> cambiarRol(u, ROLES[which]))
                .show();
    }

    private void cambiarRol(UsuarioAdmin u, String rol) {
        Map<String, String> body = new HashMap<>();
        body.put("role", rol);
        ApiClient.getService().updateRole(u.id, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Rol actualizado a " + rol, Toast.LENGTH_SHORT).show();
                    cargarUsuarios();
                } else {
                    Toast.makeText(getContext(), "No se pudo cambiar el rol.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvUsuarios.setVisibility(View.GONE);
    }
}
