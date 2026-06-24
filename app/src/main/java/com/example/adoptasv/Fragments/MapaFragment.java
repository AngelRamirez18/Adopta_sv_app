package com.example.adoptasv.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.Reporte;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.R;
import com.example.adoptasv.Util.EstadoUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mapa con refugios (casa café) y reportes SOS (alerta naranja).
 * GET /api/mapa/refugios + GET /api/mapa/reportes — FAB para crear reporte SOS.
 */
public class MapaFragment extends Fragment {

    // Centro aproximado de El Salvador (San Salvador) como fallback sin ubicación
    private static final LatLng SV_DEFAULT = new LatLng(13.6929, -89.2182);
    private static final double RADIO_KM = 50;

    private MapView mapView;
    private ProgressBar progressBar;
    private GoogleMap map;

    private FusedLocationProviderClient fusedLocation;
    private Double userLat = null, userLng = null;

    private final List<Marker> refugioMarkers = new ArrayList<>();

    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar  = view.findViewById(R.id.progressBar);
        mapView      = view.findViewById(R.id.mapView);
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext());

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> obtenerUbicacionYcargar());

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabReporte);
        fab.setOnClickListener(v -> intentarCrearReporte());

        EditText etBuscar = view.findViewById(R.id.etBuscar);
        etBuscar.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                filtrarRefugios(s.toString());
            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            map = googleMap;
            map.getUiSettings().setZoomControlsEnabled(true);
            map.setOnMarkerClickListener(this::onMarkerClick);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(SV_DEFAULT, 11f));

            if (tienePermisoUbicacion()) {
                habilitarMiUbicacion();
                obtenerUbicacionYcargar();
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    // ── Ubicación ─────────────────────────────────────────

    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void habilitarMiUbicacion() {
        if (map != null && tienePermisoUbicacion()) {
            try { map.setMyLocationEnabled(true); } catch (SecurityException ignored) {}
        }
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionYcargar() {
        if (tienePermisoUbicacion()) {
            habilitarMiUbicacion();
            fusedLocation.getLastLocation().addOnSuccessListener(location -> {
                if (!isAdded()) return;
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    if (map != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(userLat, userLng), 13f));
                    }
                }
                cargarMapa();
            }).addOnFailureListener(e -> { if (isAdded()) cargarMapa(); });
        } else {
            // Sin permiso: cargar sin coordenadas (todos los registros, sin distancia)
            cargarMapa();
        }
    }

    // ── Carga de datos ────────────────────────────────────

    private void cargarMapa() {
        progressBar.setVisibility(View.VISIBLE);
        cargarRefugios();
        cargarReportes();
    }

    private void cargarRefugios() {
        ApiClient.getService().getRefugiosCercanos(userLat, userLng, RADIO_KM)
                .enqueue(new Callback<List<Refugio>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Refugio>> call,
                                           @NonNull Response<List<Refugio>> response) {
                        if (!isAdded() || map == null) return;
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            BitmapDescriptor icono = bitmapFromVector(R.drawable.ic_home_marker);
                            refugioMarkers.clear();
                            for (Refugio r : response.body()) {
                                Marker mk = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(r.getLat(), r.getLng()))
                                        .title(r.nombre)
                                        .icon(icono));
                                if (mk != null) {
                                    mk.setTag(r);
                                    refugioMarkers.add(mk);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Refugio>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No se pudieron cargar los refugios.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarReportes() {
        ApiClient.getService().getReportesCercanos(userLat, userLng, RADIO_KM)
                .enqueue(new Callback<List<Reporte>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Reporte>> call,
                                           @NonNull Response<List<Reporte>> response) {
                        if (!isAdded() || map == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            BitmapDescriptor icono = bitmapFromVector(R.drawable.ic_alert);
                            for (Reporte r : response.body()) {
                                Marker mk = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(r.latitud, r.longitud))
                                        .title("Reporte SOS")
                                        .icon(icono));
                                if (mk != null) mk.setTag(r);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Reporte>> call, @NonNull Throwable t) {
                        // Silencioso: el mapa sigue usable aunque fallen los reportes
                    }
                });
    }

    /** Muestra solo los refugios cuyo nombre contenga el texto buscado. */
    private void filtrarRefugios(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (Marker mk : refugioMarkers) {
            String titulo = mk.getTitle() != null ? mk.getTitle().toLowerCase(Locale.ROOT) : "";
            mk.setVisible(q.isEmpty() || titulo.contains(q));
        }
    }

    // ── Marker click → BottomSheet ────────────────────────

    private boolean onMarkerClick(Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof Refugio) {
            mostrarBottomSheetRefugio((Refugio) tag);
        } else if (tag instanceof Reporte) {
            mostrarBottomSheetReporte((Reporte) tag);
        }
        return true;
    }

    private void mostrarBottomSheetRefugio(Refugio r) {
        View sheet = getLayoutInflater().inflate(R.layout.bottomsheet_marker, null);
        ((ImageView) sheet.findViewById(R.id.ivIcono)).setImageResource(R.drawable.ic_home_marker);
        ((TextView) sheet.findViewById(R.id.tvTitulo)).setText(r.nombre);
        ((TextView) sheet.findViewById(R.id.tvDescripcion)).setText(
                r.direccion != null ? r.direccion : "Sin dirección");
        mostrarDistancia(sheet, r.distanciaKm);
        abrirSheet(sheet);
    }

    private void mostrarBottomSheetReporte(Reporte r) {
        View sheet = getLayoutInflater().inflate(R.layout.bottomsheet_marker, null);
        ((ImageView) sheet.findViewById(R.id.ivIcono)).setImageResource(R.drawable.ic_alert);
        ((TextView) sheet.findViewById(R.id.tvTitulo)).setText("Reporte SOS");
        ((TextView) sheet.findViewById(R.id.tvDescripcion)).setText(
                r.descripcion != null ? r.descripcion : "Animal en situación de calle");
        mostrarDistancia(sheet, r.distanciaKm);
        TextView tvEstado = sheet.findViewById(R.id.tvEstado);
        tvEstado.setVisibility(View.VISIBLE);
        EstadoUtils.aplicarBadge(tvEstado, r.estadoReporte);
        abrirSheet(sheet);
    }

    private void mostrarDistancia(View sheet, Double distanciaKm) {
        TextView tvDist = sheet.findViewById(R.id.tvDistancia);
        if (distanciaKm != null) {
            tvDist.setVisibility(View.VISIBLE);
            tvDist.setText(String.format(java.util.Locale.US, "%.1f km", distanciaKm));
        } else {
            tvDist.setVisibility(View.GONE);
        }
    }

    private void abrirSheet(View sheet) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(sheet);
        dialog.show();
    }

    // ── Crear reporte SOS ─────────────────────────────────

    private void intentarCrearReporte() {
        if (!tienePermisoUbicacion()) {
            Toast.makeText(getContext(),
                    "Necesitamos tu ubicación para crear el reporte.", Toast.LENGTH_LONG).show();
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        obtenerUbicacionActualParaReporte();
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionActualParaReporte() {
        fusedLocation.getLastLocation().addOnSuccessListener(location -> {
            if (!isAdded()) return;
            Double lat = location != null ? location.getLatitude() : userLat;
            Double lng = location != null ? location.getLongitude() : userLng;
            mostrarDialogoReporte(lat, lng);
        }).addOnFailureListener(e -> {
            if (isAdded()) mostrarDialogoReporte(userLat, userLng);
        });
    }

    private void mostrarDialogoReporte(Double lat, Double lng) {
        View form = getLayoutInflater().inflate(R.layout.dialog_crear_reporte, null);
        EditText etDescripcion = form.findViewById(R.id.etDescripcion);
        EditText etReferencia = form.findViewById(R.id.etReferencia);
        com.google.android.material.chip.ChipGroup cgTipo = form.findViewById(R.id.cgTipo);
        TextView tvUbic = form.findViewById(R.id.tvUbicacionEstado);
        tvUbic.setText(lat != null && lng != null
                ? "Se usará tu ubicación actual ✓"
                : "Sin ubicación disponible (el reporte se creará sin mapa).");

        new AlertDialog.Builder(requireContext())
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar Reporte", (d, w) -> {
                    String desc = etDescripcion.getText().toString().trim();
                    if (desc.isEmpty()) {
                        Toast.makeText(getContext(), "Describí la situación del animal.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String descFinal = tipoSeleccionado(cgTipo) + desc;
                    enviarReporte(descFinal, etReferencia.getText().toString().trim(), lat, lng);
                })
                .show();
    }

    private String tipoSeleccionado(com.google.android.material.chip.ChipGroup cgTipo) {
        int id = cgTipo.getCheckedChipId();
        if (id == R.id.chipExtraviado) return "[Extraviado] ";
        if (id == R.id.chipHerido) return "[Herido] ";
        if (id == R.id.chipOtro) return "[Otro] ";
        return "";
    }

    private void enviarReporte(String descripcion, String referencia, Double lat, Double lng) {
        Map<String, Object> body = new HashMap<>();
        body.put("descripcion", descripcion);
        if (lat != null && lng != null) {
            body.put("latitud", lat);
            body.put("longitud", lng);
        }
        if (!referencia.isEmpty()) body.put("direccion_referencia", referencia);

        ApiClient.getService().createReporte(body).enqueue(new Callback<SingleResponse<Reporte>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Reporte>> call,
                                   @NonNull Response<SingleResponse<Reporte>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Reporte enviado! Gracias por ayudar.",
                            Toast.LENGTH_LONG).show();
                    Reporte creado = response.body() != null ? response.body().data : null;
                    if (creado != null && map != null && lat != null && lng != null) {
                        Marker mk = map.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title("Reporte SOS")
                                .icon(bitmapFromVector(R.drawable.ic_alert)));
                        if (mk != null) mk.setTag(creado);
                    }
                } else {
                    Toast.makeText(getContext(), "No se pudo enviar el reporte.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Reporte>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Helper: vector → BitmapDescriptor ─────────────────

    private BitmapDescriptor bitmapFromVector(int resId) {
        Drawable d = ContextCompat.getDrawable(requireContext(), resId);
        if (d == null) return BitmapDescriptorFactory.defaultMarker();
        int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : 96;
        int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : 96;
        d.setBounds(0, 0, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        d.draw(new Canvas(bmp));
        return BitmapDescriptorFactory.fromBitmap(bmp);
    }

    // ── Ciclo de vida del MapView ─────────────────────────

    @Override public void onStart()  { super.onStart();  if (mapView != null) mapView.onStart();  }
    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause()  { if (mapView != null) mapView.onPause();  super.onPause();  }
    @Override public void onStop()   { if (mapView != null) mapView.onStop();   super.onStop();   }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
