package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.R;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard "Hola" — punto de entrada (tab nav_messages) hacia las pantallas
 * sin tab propio: Mis Solicitudes, Mapa, Seguimiento y Panel (voluntario/admin).
 */
public class HubFragment extends Fragment {

    private View cardPanel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hub, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cardSolicitudes).setOnClickListener(v ->
                navegar(new MisSolicitudesFragment()));
        view.findViewById(R.id.cardMapa).setOnClickListener(v ->
                navegar(new MapaFragment()));
        view.findViewById(R.id.cardSeguimiento).setOnClickListener(v ->
                navegar(new SeguimientoFragment()));
        view.findViewById(R.id.cardReportes).setOnClickListener(v ->
                navegar(new MapaFragment()));

        cardPanel = view.findViewById(R.id.cardPanel);
        cardPanel.setOnClickListener(v -> navegar(new PanelFragment()));

        verificarRol();
    }

    private void verificarRol() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        ApiClient.getService().getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().roles != null) {
                    for (String rol : response.body().roles) {
                        if ("voluntario".equalsIgnoreCase(rol) || "admin".equalsIgnoreCase(rol)) {
                            cardPanel.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                // Si falla, el panel queda oculto (rol no confirmado).
            }
        });
    }

    private void navegar(Fragment destino) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, destino)
                .addToBackStack(null)
                .commit();
    }
}
