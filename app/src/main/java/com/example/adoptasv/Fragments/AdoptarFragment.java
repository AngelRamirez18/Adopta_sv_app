package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adoptasv.Adaptadores.MascotaListaAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.R;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Catálogo de adopción en lista con filtros de especie/sexo/tamaño (servidor)
 * y búsqueda por nombre (local sobre la lista cargada).
 */
public class AdoptarFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvError;
    private RecyclerView rvMascotas;
    private EditText etBuscar;
    private MascotaListaAdapter adapter;

    private final List<Mascota> masterList = new ArrayList<>();

    private String filtroEspecie = null;
    private String filtroSexo = null;
    private String filtroTamano = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adoptar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvError     = view.findViewById(R.id.tvError);
        rvMascotas  = view.findViewById(R.id.rvMascotas);
        etBuscar    = view.findViewById(R.id.etBuscar);

        adapter = new MascotaListaAdapter(new ArrayList<>(), this::abrirDetalle);
        rvMascotas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMascotas.setAdapter(adapter);

        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fab =
                view.findViewById(R.id.fabAdoptar);
        fab.setOnClickListener(v -> rvMascotas.smoothScrollToPosition(0));

        configurarFiltros(view);
        configurarBusqueda();

        cargarMascotas();
    }

    private void configurarFiltros(View view) {
        ChipGroup cgEspecie = view.findViewById(R.id.cgEspecie);
        ChipGroup cgSexo    = view.findViewById(R.id.cgSexo);
        ChipGroup cgTamano  = view.findViewById(R.id.cgTamano);

        cgEspecie.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chipEspPerro) filtroEspecie = "perro";
            else if (id == R.id.chipEspGato) filtroEspecie = "gato";
            else filtroEspecie = null;
            cargarMascotas();
        });

        cgSexo.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chipSexoMacho) filtroSexo = "macho";
            else if (id == R.id.chipSexoHembra) filtroSexo = "hembra";
            else filtroSexo = null;
            cargarMascotas();
        });

        cgTamano.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chipTamPequeno) filtroTamano = "pequeno";
            else if (id == R.id.chipTamMediano) filtroTamano = "mediano";
            else if (id == R.id.chipTamGrande) filtroTamano = "grande";
            else filtroTamano = null;
            cargarMascotas();
        });
    }

    private void configurarBusqueda() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { aplicarBusqueda(); }
        });
    }

    private void cargarMascotas() {
        progressBar.setVisibility(View.VISIBLE);
        rvMascotas.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService()
                .getMascotas(filtroEspecie, filtroSexo, filtroTamano, null, 1)
                .enqueue(new Callback<PaginatedResponse<Mascota>>() {
                    @Override
                    public void onResponse(@NonNull Call<PaginatedResponse<Mascota>> call,
                                           @NonNull Response<PaginatedResponse<Mascota>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null) {
                            masterList.clear();
                            masterList.addAll(response.body().data);
                            aplicarBusqueda();
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

    /** Filtra localmente la lista cargada por el texto de búsqueda. */
    private void aplicarBusqueda() {
        String query = etBuscar.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<Mascota> filtradas = new ArrayList<>();
        for (Mascota m : masterList) {
            if (query.isEmpty() || (m.nombre != null
                    && m.nombre.toLowerCase(Locale.ROOT).contains(query))) {
                filtradas.add(m);
            }
        }

        if (filtradas.isEmpty()) {
            adapter.updateData(filtradas);
            mostrarError(masterList.isEmpty()
                    ? "Sin mascotas disponibles con esos filtros."
                    : "Ninguna mascota coincide con tu búsqueda.");
        } else {
            tvError.setVisibility(View.GONE);
            adapter.updateData(filtradas);
            rvMascotas.setVisibility(View.VISIBLE);
        }
    }

    private void abrirDetalle(Mascota mascota) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, DetallesMascotaFragment.newInstance(mascota.id))
                .addToBackStack(null)
                .commit();
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvMascotas.setVisibility(View.GONE);
    }
}
