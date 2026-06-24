package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adoptasv.Adaptadores.AdminMascotaAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.R;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lista de mascotas del refugio (GET /panel/mascotas) con filtro por estado,
 * FAB para crear y edición / cambio de estado por item.
 */
public class AdminMascotasFragment extends Fragment {

    private static final String[] ESTADOS = {"disponible", "en_proceso", "adoptada"};
    private static final String[] ESTADOS_LABEL = {"Disponible", "En proceso", "Adoptada"};

    private ProgressBar progressBar;
    private TextView tvError;
    private RecyclerView rvMascotas;
    private AdminMascotaAdapter adapter;
    private String filtroEstado = null;

    // refugio_id del usuario; la API real exige enviarlo en GET /panel/mascotas.
    // Se resuelve una sola vez con getPerfil y se cachea para los refrescos.
    private Integer refugioId = null;
    private boolean perfilCargado = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_mascotas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvError     = view.findViewById(R.id.tvError);
        rvMascotas  = view.findViewById(R.id.rvMascotas);

        adapter = new AdminMascotaAdapter(new ArrayList<>(), new AdminMascotaAdapter.OnItemClickListener() {
            @Override public void onEditar(Mascota m) { abrirEditor(m.id); }
            @Override public void onCambiarEstado(Mascota m) { dialogoEstado(m); }
        });
        rvMascotas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMascotas.setAdapter(adapter);

        ChipGroup cgFiltro = view.findViewById(R.id.cgFiltro);
        cgFiltro.setOnCheckedStateChangeListener((group, ids) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chipDisponibles) filtroEstado = "disponible";
            else if (id == R.id.chipEnProceso) filtroEstado = "en_proceso";
            else if (id == R.id.chipAdoptadas) filtroEstado = "adoptada";
            else filtroEstado = null;
            asegurarRefugioYCargar();
        });

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabNueva);
        fab.setOnClickListener(v -> abrirEditor(-1));

        asegurarRefugioYCargar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver del editor refrescamos; el perfil ya está resuelto (no re-consultar).
        if (perfilCargado) cargarMascotas();
    }

    /**
     * Resuelve el refugio del usuario (una sola vez) antes de listar, porque la API
     * exige refugio_id. Si no se puede obtener, igual intenta cargar (refugio_id null).
     */
    private void asegurarRefugioYCargar() {
        if (perfilCargado) {
            cargarMascotas();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        rvMascotas.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getPerfil().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                User u = response.body();
                if (response.isSuccessful() && u != null && u.refugio != null) {
                    refugioId = u.refugio.id;
                }
                perfilCargado = true;
                cargarMascotas();
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                perfilCargado = true; // no reintentar en bucle; intentar listar igual
                cargarMascotas();
            }
        });
    }

    private void cargarMascotas() {
        progressBar.setVisibility(View.VISIBLE);
        rvMascotas.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getPanelMascotas(filtroEstado, refugioId).enqueue(new Callback<PaginatedResponse<Mascota>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Mascota>> call,
                                   @NonNull Response<PaginatedResponse<Mascota>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.code() == 403) {
                    mostrarError("No tenés un refugio registrado.");
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<Mascota> lista = response.body().data;
                    if (!lista.isEmpty()) {
                        adapter.updateData(lista);
                        rvMascotas.setVisibility(View.VISIBLE);
                    } else {
                        mostrarError("No hay mascotas en este estado.");
                    }
                } else {
                    mostrarError("No se pudieron cargar las mascotas.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Mascota>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void abrirEditor(int mascotaId) {
        Fragment editor = mascotaId > 0
                ? CrearMascotaFragment.newInstance(mascotaId)
                : new CrearMascotaFragment();
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.adminContainer, editor)
                .addToBackStack(null)
                .commit();
    }

    private void dialogoEstado(Mascota m) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar estado de " + m.nombre)
                .setItems(ESTADOS_LABEL, (d, which) -> cambiarEstado(m, ESTADOS[which]))
                .show();
    }

    private void cambiarEstado(Mascota m, String estado) {
        Map<String, String> body = new HashMap<>();
        body.put("estado", estado);
        ApiClient.getService().updateEstadoMascota(m.id, body).enqueue(new Callback<SingleResponse<Mascota>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Mascota>> call,
                                   @NonNull Response<SingleResponse<Mascota>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Estado actualizado", Toast.LENGTH_SHORT).show();
                    cargarMascotas();
                } else {
                    Toast.makeText(getContext(), "No se pudo actualizar el estado.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Mascota>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvMascotas.setVisibility(View.GONE);
    }
}
