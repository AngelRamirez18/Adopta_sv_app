package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adoptasv.Adaptadores.RefugioAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lista de refugios (GET /refugios) con CRUD: FAB para crear, item → editar,
 * menú de opciones para editar o desactivar (DELETE /refugios/{id}).
 */
public class AdminRefugiosFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvError;
    private RecyclerView rvRefugios;
    private RefugioAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_refugios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvError     = view.findViewById(R.id.tvError);
        rvRefugios  = view.findViewById(R.id.rvRefugios);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        adapter = new RefugioAdapter(new ArrayList<>(), new RefugioAdapter.OnItemClickListener() {
            @Override public void onEditar(Refugio r) { abrirEditor(r.id); }
            @Override public void onOpciones(Refugio r, View anchor) { mostrarMenu(r, anchor); }
        });
        rvRefugios.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRefugios.setAdapter(adapter);

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabNuevo);
        fab.setOnClickListener(v -> abrirEditor(-1));

        cargarRefugios();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarRefugios(); // refrescar al volver del editor
    }

    private void cargarRefugios() {
        progressBar.setVisibility(View.VISIBLE);
        rvRefugios.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        // Solo mostramos el refugio del usuario autenticado. GET /perfil es el único
        // endpoint que devuelve el refugio asociado (auth/me no lo incluye).
        ApiClient.getService().getPerfil().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Refugio propio = response.body().refugio;
                    if (propio != null) {
                        adapter.updateData(Collections.singletonList(propio));
                        rvRefugios.setVisibility(View.VISIBLE);
                    } else {
                        mostrarError("Todavía no tenés un refugio registrado.\nCreá el tuyo con el botón +.");
                    }
                } else {
                    mostrarError("No se pudo cargar tu refugio.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void mostrarMenu(Refugio r, View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add("Editar");
        menu.getMenu().add("Desactivar");
        menu.setOnMenuItemClickListener(item -> {
            if ("Editar".contentEquals(item.getTitle())) {
                abrirEditor(r.id);
            } else {
                confirmarEliminar(r);
            }
            return true;
        });
        menu.show();
    }

    private void confirmarEliminar(Refugio r) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Desactivar refugio")
                .setMessage("¿Querés desactivar \"" + r.nombre + "\"? Sus mascotas seguirán en la base de datos.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Desactivar", (d, w) -> eliminar(r))
                .show();
    }

    private void eliminar(Refugio r) {
        ApiClient.getService().deleteRefugio(r.id).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call,
                                   @NonNull Response<Map<String, String>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Refugio desactivado", Toast.LENGTH_SHORT).show();
                    cargarRefugios();
                } else if (response.code() == 403) {
                    Toast.makeText(getContext(), "No sos dueño de este refugio ni admin.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo desactivar el refugio.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirEditor(int refugioId) {
        Fragment editor = refugioId > 0
                ? CrearRefugioFragment.newInstance(refugioId)
                : new CrearRefugioFragment();
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.adminContainer, editor)
                .addToBackStack(null)
                .commit();
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvRefugios.setVisibility(View.GONE);
    }
}
