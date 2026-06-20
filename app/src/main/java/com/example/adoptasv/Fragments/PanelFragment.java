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

import com.example.adoptasv.Adaptadores.PanelSolicitudAdapter;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.PanelResumen;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.Conexion.Modelos.Solicitud;
import com.example.adoptasv.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Panel del refugio (voluntario/admin): stats de resumen + solicitudes con
 * acciones Aprobar/Rechazar. GET /api/panel/resumen, GET /api/panel/solicitudes,
 * PATCH /api/solicitudes/{id}/estado.
 */
public class PanelFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvError, tvStatPendientes, tvStatEntrevistas, tvStatSeguimientos;
    private RecyclerView rvSolicitudes;
    private PanelSolicitudAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar        = view.findViewById(R.id.progressBar);
        tvError            = view.findViewById(R.id.tvError);
        tvStatPendientes   = view.findViewById(R.id.tvStatPendientes);
        tvStatEntrevistas  = view.findViewById(R.id.tvStatEntrevistas);
        tvStatSeguimientos = view.findViewById(R.id.tvStatSeguimientos);
        rvSolicitudes      = view.findViewById(R.id.rvSolicitudes);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        adapter = new PanelSolicitudAdapter(new ArrayList<>(), new PanelSolicitudAdapter.OnItemClickListener() {
            @Override public void onAprobar(Solicitud s) { confirmarCambio(s, "aprobada"); }
            @Override public void onRechazar(Solicitud s) { confirmarCambio(s, "rechazada"); }
        });
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSolicitudes.setAdapter(adapter);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            mostrarError("Tenés que iniciar sesión.");
            return;
        }

        cargarResumen();
        cargarSolicitudes();
    }

    private void cargarResumen() {
        ApiClient.getService().getPanelResumen().enqueue(new Callback<PanelResumen>() {
            @Override
            public void onResponse(@NonNull Call<PanelResumen> call, @NonNull Response<PanelResumen> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    PanelResumen r = response.body();
                    if (r.solicitudes != null) {
                        tvStatPendientes.setText(String.valueOf(r.solicitudes.pendientes));
                        tvStatEntrevistas.setText(String.valueOf(r.solicitudes.enRevision));
                    }
                    tvStatSeguimientos.setText(r.seguimientosPendientes + " seguimientos");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PanelResumen> call, @NonNull Throwable t) {
                // Las stats son secundarias; si fallan, el panel sigue usable.
            }
        });
    }

    private void cargarSolicitudes() {
        progressBar.setVisibility(View.VISIBLE);
        rvSolicitudes.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        ApiClient.getService().getPanelSolicitudes(null).enqueue(new Callback<PaginatedResponse<Solicitud>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Solicitud>> call,
                                   @NonNull Response<PaginatedResponse<Solicitud>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.code() == 403) {
                    mostrarError("No tenés un refugio registrado o permiso para el panel.");
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<Solicitud> lista = response.body().data;
                    if (!lista.isEmpty()) {
                        adapter.updateData(lista);
                        rvSolicitudes.setVisibility(View.VISIBLE);
                    } else {
                        mostrarError("Tu refugio aún no tiene solicitudes.");
                    }
                } else {
                    mostrarError("No se pudieron cargar las solicitudes.");
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

    private void confirmarCambio(Solicitud s, String nuevoEstado) {
        String mascota = s.mascota != null && s.mascota.nombre != null ? s.mascota.nombre : "esta mascota";
        String accion = nuevoEstado.equals("aprobada") ? "aprobar" : "rechazar";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar")
                .setMessage("¿Querés " + accion + " la solicitud para " + mascota + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sí", (d, w) -> cambiarEstado(s, nuevoEstado))
                .show();
    }

    private void cambiarEstado(Solicitud s, String nuevoEstado) {
        Map<String, Object> body = new HashMap<>();
        body.put("estado", nuevoEstado);

        ApiClient.getService().updateEstadoSolicitud(s.id, body)
                .enqueue(new Callback<SingleResponse<Solicitud>>() {
                    @Override
                    public void onResponse(@NonNull Call<SingleResponse<Solicitud>> call,
                                           @NonNull Response<SingleResponse<Solicitud>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    nuevoEstado.equals("aprobada") ? "Solicitud aprobada" : "Solicitud rechazada",
                                    Toast.LENGTH_SHORT).show();
                            // Refrescar lista y stats
                            cargarResumen();
                            cargarSolicitudes();
                        } else if (response.code() == 403) {
                            Toast.makeText(getContext(), "No tenés permiso sobre esta solicitud.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "No se pudo actualizar la solicitud.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SingleResponse<Solicitud>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        rvSolicitudes.setVisibility(View.GONE);
    }
}
