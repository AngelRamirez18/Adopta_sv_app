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

import com.example.adoptasv.Adaptadores.ReporteAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.Reporte;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.R;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestión de reportes SOS (GET /reportes) con filtro por estado y cambio de
 * estado (PATCH /reportes/{id}/estado): en_atencion → atendido → cerrado.
 */
public class AdminReportesFragment extends Fragment {

    private static final String[] ESTADOS = {"en_atencion", "atendido", "cerrado"};
    private static final String[] ESTADOS_LABEL = {"En atención", "Atendido", "Cerrado"};

    private ProgressBar progressBar;
    private TextView tvError;
    private RecyclerView rvReportes;
    private ReporteAdapter adapter;
    private String filtroEstado = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reportes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvError     = view.findViewById(R.id.tvError);
        rvReportes  = view.findViewById(R.id.rvReportes);

        adapter = new ReporteAdapter(new ArrayList<>(), this::dialogoEstado);
        rvReportes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReportes.setAdapter(adapter);

        ChipGroup cgFiltro = view.findViewById(R.id.cgFiltro);
        cgFiltro.setOnCheckedStateChangeListener((group, ids) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chipNuevos) filtroEstado = "nuevo";
            else if (id == R.id.chipEnAtencion) filtroEstado = "en_atencion";
            else if (id == R.id.chipAtendidos) filtroEstado = "atendido";
            else if (id == R.id.chipCerrados) filtroEstado = "cerrado";
            else filtroEstado = null;
            cargarReportes();
        });

        cargarReportes();
    }

    private void cargarReportes() {
        progressBar.setVisibility(View.VISIBLE);
        rvReportes.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getReportes(filtroEstado).enqueue(new Callback<PaginatedResponse<Reporte>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Reporte>> call,
                                   @NonNull Response<PaginatedResponse<Reporte>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<Reporte> lista = response.body().data;
                    if (!lista.isEmpty()) {
                        adapter.updateData(lista);
                        rvReportes.setVisibility(View.VISIBLE);
                    } else {
                        mostrarError("No hay reportes en este estado.");
                    }
                } else {
                    mostrarError("No se pudieron cargar los reportes.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Reporte>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void dialogoEstado(Reporte r) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Actualizar reporte")
                .setItems(ESTADOS_LABEL, (d, which) -> cambiarEstado(r, ESTADOS[which]))
                .show();
    }

    private void cambiarEstado(Reporte r, String estado) {
        Map<String, String> body = new HashMap<>();
        body.put("estado", estado);
        ApiClient.getService().updateEstadoReporte(r.id, body).enqueue(new Callback<SingleResponse<Reporte>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Reporte>> call,
                                   @NonNull Response<SingleResponse<Reporte>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Reporte actualizado", Toast.LENGTH_SHORT).show();
                    cargarReportes();
                } else if (response.code() == 403) {
                    Toast.makeText(getContext(), "Solo voluntarios/admin pueden cambiar el estado.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo actualizar el reporte.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Reporte>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvReportes.setVisibility(View.GONE);
    }
}
