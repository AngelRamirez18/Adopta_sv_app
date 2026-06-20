package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adoptasv.Adaptadores.MascotaAdapter;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvMascotas;
    private ProgressBar progressBar;
    private TextView tvError;
    private MascotaAdapter adapter;
    private String filtroEspecie = null;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvMascotas  = view.findViewById(R.id.rvMascotas);
        progressBar = view.findViewById(R.id.progressBar);
        tvError     = view.findViewById(R.id.tvError);

        adapter = new MascotaAdapter(new ArrayList<>(), this::abrirDetalle);
        rvMascotas.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvMascotas.setAdapter(adapter);

        // Chips de filtro
        Chip chipTodos  = view.findViewById(R.id.chipTodos);
        Chip chipPerros = view.findViewById(R.id.chipPerros);
        Chip chipGatos  = view.findViewById(R.id.chipGatos);

        chipTodos.setOnClickListener(v -> {
            filtroEspecie = null;
            cargarMascotas();
        });
        chipPerros.setOnClickListener(v -> {
            filtroEspecie = "perro";
            cargarMascotas();
        });
        chipGatos.setOnClickListener(v -> {
            filtroEspecie = "gato";
            cargarMascotas();
        });

        cargarMascotas();
        return view;
    }

    private void cargarMascotas() {
        progressBar.setVisibility(View.VISIBLE);
        rvMascotas.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService()
                .getMascotas(filtroEspecie, null, null, null, 1)
                .enqueue(new Callback<PaginatedResponse<Mascota>>() {
                    @Override
                    public void onResponse(Call<PaginatedResponse<Mascota>> call,
                                           Response<PaginatedResponse<Mascota>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Mascota> lista = response.body().data;
                            int total = response.body().meta != null ? response.body().meta.total : -1;

                            if (lista != null && !lista.isEmpty()) {
                                adapter.updateData(lista);
                                rvMascotas.setVisibility(View.VISIBLE);
                            } else {
                                mostrarError("Sin mascotas disponibles");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PaginatedResponse<Mascota>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        mostrarError("Error de conexión: " + t.getMessage());
                    }
                });
    }

    private void abrirDetalle(Mascota mascota) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out)
                .replace(R.id.fragmentContainer,
                        DetallesMascotaFragment.newInstance(mascota.id))
                .addToBackStack(null)
                .commit();
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvMascotas.setVisibility(View.GONE);
    }
}