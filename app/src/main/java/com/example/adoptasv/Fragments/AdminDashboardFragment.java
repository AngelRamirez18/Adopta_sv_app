package com.example.adoptasv.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.adoptasv.AdminActivity;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.PanelResumen;
import com.example.adoptasv.Conexion.Modelos.User;
import com.example.adoptasv.R;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Inicio del área administrativa: stats del refugio + accesos rápidos.
 */
public class AdminDashboardFragment extends Fragment {

    private TextView tvStatDisponibles, tvStatPendientes, tvStatSeguimientos, tvSubtitulo;
    private View cardUsuarios;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStatDisponibles  = view.findViewById(R.id.tvStatDisponibles);
        tvStatPendientes   = view.findViewById(R.id.tvStatPendientes);
        tvStatSeguimientos = view.findViewById(R.id.tvStatSeguimientos);
        tvSubtitulo        = view.findViewById(R.id.tvSubtitulo);
        cardUsuarios       = view.findViewById(R.id.cardUsuarios);

        view.findViewById(R.id.cardNuevaMascota).setOnClickListener(v ->
                navegar(new CrearMascotaFragment()));
        view.findViewById(R.id.cardMascotas).setOnClickListener(v -> {
            if (getActivity() instanceof AdminActivity)
                ((AdminActivity) getActivity()).setTab(R.id.nav_mascotas);
        });
        view.findViewById(R.id.cardReportes).setOnClickListener(v -> {
            if (getActivity() instanceof AdminActivity)
                ((AdminActivity) getActivity()).setTab(R.id.nav_reportes);
        });
        view.findViewById(R.id.cardRefugio).setOnClickListener(v ->
                navegar(new AdminRefugiosFragment()));
        cardUsuarios.setOnClickListener(v -> navegar(new AdminUsuariosFragment()));

        cargarResumen();
        verificarAdmin();
    }

    private void cargarResumen() {
        ApiClient.getService().getPanelResumen().enqueue(new Callback<PanelResumen>() {
            @Override
            public void onResponse(@NonNull Call<PanelResumen> call, @NonNull Response<PanelResumen> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    PanelResumen r = response.body();
                    if (r.mascotas != null) tvStatDisponibles.setText(String.valueOf(r.mascotas.disponibles));
                    if (r.solicitudes != null) tvStatPendientes.setText(String.valueOf(r.solicitudes.pendientes));
                    tvStatSeguimientos.setText(String.valueOf(r.seguimientosPendientes));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PanelResumen> call, @NonNull Throwable t) {
                // Stats secundarias; el dashboard sigue usable.
            }
        });
    }

    private void verificarAdmin() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        // getPerfil incluye roles + refugio (auth/me no trae refugio).
        ApiClient.getService().getPerfil().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;
                User u = response.body();
                if (response.isSuccessful() && u != null) {
                    boolean esAdmin = false;
                    if (u.roles != null) {
                        for (String rol : u.roles) {
                            if ("admin".equalsIgnoreCase(rol)) { esAdmin = true; break; }
                        }
                    }
                    if (esAdmin) cardUsuarios.setVisibility(View.VISIBLE);

                    // Mostrar de qué refugio se está gestionando el panel.
                    if (u.refugio != null && u.refugio.nombre != null) {
                        tvSubtitulo.setText("Refugio " + u.refugio.nombre);
                    } else if (esAdmin) {
                        tvSubtitulo.setText("Administración general del sistema");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
        });
    }

    private void navegar(Fragment destino) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.adminContainer, destino)
                .addToBackStack(null)
                .commit();
    }
}
