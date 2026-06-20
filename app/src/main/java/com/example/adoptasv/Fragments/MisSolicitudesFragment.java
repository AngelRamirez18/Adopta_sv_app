package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adoptasv.Adaptadores.SolicitudAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lista las solicitudes de adopción del adoptante autenticado.
 * GET /api/solicitudes/mis-solicitudes — al tocar una abre su detalle.
 */
public class MisSolicitudesFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvError;
    private RecyclerView rvSolicitudes;
    private SolicitudAdapter adapter;

    private final List<Solicitud> masterList = new ArrayList<>();
    private String filtroEstado = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_solicitudes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar   = view.findViewById(R.id.progressBar);
        tvError       = view.findViewById(R.id.tvError);
        rvSolicitudes = view.findViewById(R.id.rvSolicitudes);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        adapter = new SolicitudAdapter(new ArrayList<>(), this::abrirDetalle);
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSolicitudes.setAdapter(adapter);

        ChipGroup cgFiltro = view.findViewById(R.id.cgFiltro);
        cgFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chipPendientes) filtroEstado = "pendiente";
            else if (id == R.id.chipRevision) filtroEstado = "en_revision";
            else if (id == R.id.chipAprobadas) filtroEstado = "aprobada";
            else filtroEstado = null;
            aplicarFiltro();
        });

        cargarSolicitudes();
    }

    private void aplicarFiltro() {
        List<Solicitud> filtradas = new ArrayList<>();
        for (Solicitud s : masterList) {
            if (filtroEstado == null || filtroEstado.equalsIgnoreCase(s.estado)) {
                filtradas.add(s);
            }
        }
        if (filtradas.isEmpty()) {
            adapter.updateData(filtradas);
            mostrarError(masterList.isEmpty()
                    ? "Todavía no enviaste ninguna solicitud de adopción."
                    : "No tenés solicitudes en este estado.");
        } else {
            tvError.setVisibility(View.GONE);
            adapter.updateData(filtradas);
            rvSolicitudes.setVisibility(View.VISIBLE);
        }
    }

    private void cargarSolicitudes() {
        // Verificar sesión antes de llamar
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            mostrarError("Tenés que iniciar sesión.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvSolicitudes.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getMisSolicitudes().enqueue(new Callback<PaginatedResponse<Solicitud>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Solicitud>> call,
                                   @NonNull Response<PaginatedResponse<Solicitud>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Solicitud> lista = response.body().data;
                    masterList.clear();
                    if (lista != null) masterList.addAll(lista);
                    if (!masterList.isEmpty()) {
                        aplicarFiltro();
                    } else {
                        mostrarError("Todavía no enviaste ninguna solicitud de adopción.");
                    }
                } else {
                    mostrarError("No se pudieron cargar tus solicitudes.");
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

    private void abrirDetalle(Solicitud solicitud) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, SolicitudDetalleFragment.newInstance(solicitud.id))
                .addToBackStack(null)
                .commit();
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvSolicitudes.setVisibility(View.GONE);
    }
}
