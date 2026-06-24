package com.example.adoptasv.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.Mascota;
import com.example.adoptasv.Conexion.Modelos.PaginatedResponse;
import com.example.adoptasv.Conexion.Modelos.Refugio;
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.R;
import com.example.adoptasv.Util.MultipartUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Formulario para publicar (POST /mascotas) o editar (PUT /mascotas/{id}) una
 * mascota, con subida de foto opcional (POST /mascotas/{id}/foto).
 */
public class CrearMascotaFragment extends Fragment {

    private static final String ARG_MASCOTA_ID = "mascota_id";

    private static final String[] ESPECIES = {"Perro", "Gato"};
    private static final String[] SEXOS = {"Macho", "Hembra"};
    private static final String[] TAMANOS = {"Sin especificar", "Pequeño", "Mediano", "Grande"};
    private static final String[] TAMANO_VAL = {null, "pequeno", "mediano", "grande"};

    private int mascotaId = -1;

    private EditText etNombre, etEdad, etRaza, etDescripcion, etPersonalidad, etEstadoSalud;
    private Spinner spEspecie, spSexo, spTamano, spRefugio;
    private final List<Refugio> refugios = new ArrayList<>();
    private int refugioSeleccionadoId = -1; // refugio a preseleccionar al editar
    private SwitchMaterial swVacunas, swEsterilizado;
    private LinearLayout llFotos;
    private View tileAddFoto;
    private MaterialButton btnGuardar;
    private ProgressBar progressBar;
    private TextView tvTitulo;

    private static final int MAX_FOTOS = 4;
    private final List<Uri> fotoUris = new ArrayList<>();          // fotos nuevas a subir
    private final List<String> fotosExistentes = new ArrayList<>(); // URLs ya guardadas (al editar)
    private boolean mascotaYaTieneFoto = false;
    private int fotosFallidas = 0;
    private ActivityResultLauncher<String> imagePicker;

    public CrearMascotaFragment() {}

    public static CrearMascotaFragment newInstance(int mascotaId) {
        CrearMascotaFragment f = new CrearMascotaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MASCOTA_ID, mascotaId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mascotaId = getArguments().getInt(ARG_MASCOTA_ID, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_mascota, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNombre       = view.findViewById(R.id.etNombre);
        etEdad         = view.findViewById(R.id.etEdad);
        etRaza         = view.findViewById(R.id.etRaza);
        etDescripcion  = view.findViewById(R.id.etDescripcion);
        etPersonalidad = view.findViewById(R.id.etPersonalidad);
        etEstadoSalud  = view.findViewById(R.id.etEstadoSalud);
        spEspecie      = view.findViewById(R.id.spEspecie);
        spSexo         = view.findViewById(R.id.spSexo);
        spTamano       = view.findViewById(R.id.spTamano);
        spRefugio      = view.findViewById(R.id.spRefugio);
        swVacunas      = view.findViewById(R.id.swVacunas);
        swEsterilizado = view.findViewById(R.id.swEsterilizado);
        llFotos        = view.findViewById(R.id.llFotos);
        tileAddFoto    = view.findViewById(R.id.tileAddFoto);
        btnGuardar     = view.findViewById(R.id.btnGuardar);
        progressBar    = view.findViewById(R.id.progressBar);
        tvTitulo       = view.findViewById(R.id.tvTitulo);

        spEspecie.setAdapter(spinnerAdapter(ESPECIES));
        spSexo.setAdapter(spinnerAdapter(SEXOS));
        spTamano.setAdapter(spinnerAdapter(TAMANOS));

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(), uris -> {
                    if (uris == null || uris.isEmpty()) return;
                    int libres = MAX_FOTOS - (fotoUris.size() + fotosExistentes.size());
                    for (Uri uri : uris) {
                        if (libres <= 0) {
                            Toast.makeText(getContext(), "Máximo " + MAX_FOTOS + " fotos.",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                        fotoUris.add(uri);
                        libres--;
                    }
                    renderThumbs();
                });
        tileAddFoto.setOnClickListener(v -> imagePicker.launch("image/*"));
        renderThumbs();

        btnGuardar.setOnClickListener(v -> guardar());

        cargarRefugios();

        if (mascotaId > 0) {
            tvTitulo.setText("Editar mascota");
            btnGuardar.setText("Guardar cambios");
            cargarMascota();
        }
    }

    private ArrayAdapter<String> spinnerAdapter(String[] items) {
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
    }

    private void cargarRefugios() {
        ApiClient.getService().getRefugios().enqueue(new Callback<PaginatedResponse<Refugio>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Refugio>> call,
                                   @NonNull Response<PaginatedResponse<Refugio>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    refugios.clear();
                    refugios.addAll(response.body().data);
                    String[] nombres = new String[refugios.size()];
                    for (int i = 0; i < refugios.size(); i++) {
                        nombres[i] = refugios.get(i).nombre;
                    }
                    spRefugio.setAdapter(spinnerAdapter(nombres));
                    aplicarSeleccionRefugio();
                } else {
                    Toast.makeText(getContext(),
                            "No se pudieron cargar los refugios (HTTP " + response.code() + ").",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Refugio>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "No se pudieron cargar los refugios: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Selecciona en el spinner el refugio cuyo id coincide con el de la mascota editada. */
    private void aplicarSeleccionRefugio() {
        if (refugioSeleccionadoId <= 0 || refugios.isEmpty()) return;
        for (int i = 0; i < refugios.size(); i++) {
            if (refugios.get(i).id == refugioSeleccionadoId) {
                spRefugio.setSelection(i);
                return;
            }
        }
    }

    private void cargarMascota() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService().getMascota(mascotaId).enqueue(new Callback<SingleResponse<Mascota>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Mascota>> call,
                                   @NonNull Response<SingleResponse<Mascota>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    rellenar(response.body().data);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Mascota>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No se pudo cargar la mascota.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rellenar(Mascota m) {
        etNombre.setText(m.nombre);
        if (m.edadMeses != null) etEdad.setText(String.valueOf(m.edadMeses));
        etRaza.setText(m.raza);
        etDescripcion.setText(m.descripcion);
        etPersonalidad.setText(m.personalidad);
        etEstadoSalud.setText(m.estadoSalud);
        spEspecie.setSelection("gato".equalsIgnoreCase(m.especie) ? 1 : 0);
        spSexo.setSelection("hembra".equalsIgnoreCase(m.sexo) ? 1 : 0);
        spTamano.setSelection(indiceTamano(m.tamano));
        swVacunas.setChecked(m.vacunas);
        swEsterilizado.setChecked(m.esterilizado);

        if (m.refugio != null) {
            refugioSeleccionadoId = m.refugio.id;
            aplicarSeleccionRefugio();
        }

        fotosExistentes.clear();
        if (m.fotoUrl != null && !m.fotoUrl.isEmpty()) {
            mascotaYaTieneFoto = true;
            fotosExistentes.add(m.fotoUrl);
        }
        if (m.fotosExtra != null) {
            for (String url : m.fotosExtra) {
                if (url != null && !url.isEmpty()) fotosExistentes.add(url);
            }
        }
        renderThumbs();
    }

    /** Pinta las miniaturas: primero las fotos ya guardadas (solo lectura), luego las nuevas. */
    private void renderThumbs() {
        // Quitar todas las miniaturas previas, conservando el tile de "Añadir".
        for (int i = llFotos.getChildCount() - 1; i >= 0; i--) {
            if (llFotos.getChildAt(i) != tileAddFoto) llFotos.removeViewAt(i);
        }

        int index = 0;
        for (String url : fotosExistentes) {
            View thumb = crearThumb(url, null);
            llFotos.addView(thumb, index++);
        }
        for (Uri uri : fotoUris) {
            View thumb = crearThumb(uri, uri);
            llFotos.addView(thumb, index++);
        }

        int total = fotosExistentes.size() + fotoUris.size();
        tileAddFoto.setVisibility(total >= MAX_FOTOS ? View.GONE : View.VISIBLE);
    }

    /** Crea una miniatura. Si {@code removable} no es null, muestra botón de quitar. */
    private View crearThumb(Object fuente, Uri removable) {
        View thumb = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_foto_thumb, llFotos, false);
        ImageView iv = thumb.findViewById(R.id.ivThumb);
        ImageButton btnRemove = thumb.findViewById(R.id.btnRemove);

        Glide.with(this).load(fuente).centerCrop().into(iv);

        if (removable != null) {
            btnRemove.setVisibility(View.VISIBLE);
            btnRemove.setOnClickListener(v -> {
                fotoUris.remove(removable);
                renderThumbs();
            });
        } else {
            btnRemove.setVisibility(View.GONE);
        }
        return thumb;
    }

    private int indiceTamano(String tamano) {
        if (tamano == null) return 0;
        for (int i = 1; i < TAMANO_VAL.length; i++) {
            if (tamano.equalsIgnoreCase(TAMANO_VAL[i])) return i;
        }
        return 0;
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresá el nombre");
            return;
        }

        if (refugios.isEmpty()) {
            Toast.makeText(getContext(), "Primero registrá un refugio.", Toast.LENGTH_LONG).show();
            return;
        }

        // Al publicar se exige al menos una foto; al editar puede conservar las existentes.
        if (mascotaId <= 0 && fotoUris.isEmpty()) {
            Toast.makeText(getContext(), "Agregá al menos una foto de la mascota.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);

        int posRefugio = spRefugio.getSelectedItemPosition();
        if (posRefugio >= 0 && posRefugio < refugios.size()) {
            body.put("refugio_id", refugios.get(posRefugio).id);
        }
        body.put("especie", spEspecie.getSelectedItemPosition() == 1 ? "gato" : "perro");
        body.put("sexo", spSexo.getSelectedItemPosition() == 1 ? "hembra" : "macho");

        String tamano = TAMANO_VAL[spTamano.getSelectedItemPosition()];
        if (tamano != null) body.put("tamano", tamano);

        putIfNotEmpty(body, "raza", etRaza);
        putIfNotEmpty(body, "descripcion", etDescripcion);
        putIfNotEmpty(body, "personalidad", etPersonalidad);
        putIfNotEmpty(body, "estado_salud", etEstadoSalud);

        String edad = etEdad.getText().toString().trim();
        if (!edad.isEmpty()) {
            try { body.put("edad_meses", Integer.parseInt(edad)); } catch (NumberFormatException ignored) {}
        }
        body.put("vacunas", swVacunas.isChecked());
        body.put("esterilizado", swEsterilizado.isChecked());

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando…");
        progressBar.setVisibility(View.VISIBLE);

        Call<SingleResponse<Mascota>> call = mascotaId > 0
                ? ApiClient.getService().updateMascota(mascotaId, body)
                : ApiClient.getService().createMascota(body);

        call.enqueue(new Callback<SingleResponse<Mascota>>() {
            @Override
            public void onResponse(@NonNull Call<SingleResponse<Mascota>> c,
                                   @NonNull Response<SingleResponse<Mascota>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    int id = response.body().data.id;
                    if (!fotoUris.isEmpty() && id > 0) {
                        subirFotos(id);
                    } else {
                        finalizarOk();
                    }
                } else {
                    restaurar();
                    String msg = response.code() == 403
                            ? "No tenés un refugio registrado o permiso."
                            : "No se pudo guardar la mascota.";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleResponse<Mascota>> c, @NonNull Throwable t) {
                if (!isAdded()) return;
                restaurar();
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subirFotos(int id) {
        fotosFallidas = 0;
        // La primera foto nueva es la principal solo si la mascota aún no tiene una.
        subirFotoIndice(id, 0, !mascotaYaTieneFoto);
    }

    private void subirFotoIndice(int id, int index, boolean primeraEsPrincipal) {
        if (!isAdded()) return;
        if (index >= fotoUris.size()) {
            finalizarOk();
            return;
        }

        btnGuardar.setText("Subiendo fotos (" + (index + 1) + "/" + fotoUris.size() + ")…");
        MultipartBody.Part part = MultipartUtils.fotoPart(requireContext(), fotoUris.get(index), "foto");
        if (part == null) {
            subirFotoIndice(id, index + 1, primeraEsPrincipal);
            return;
        }

        if (primeraEsPrincipal && index == 0) {
            ApiClient.getService().uploadFotoMascota(id, part).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, String>> c, @NonNull Response<Map<String, String>> r) {
                    if (!r.isSuccessful()) avisarFotoFallida();
                    subirFotoIndice(id, index + 1, primeraEsPrincipal);
                }
                @Override
                public void onFailure(@NonNull Call<Map<String, String>> c, @NonNull Throwable t) {
                    avisarFotoFallida();
                    subirFotoIndice(id, index + 1, primeraEsPrincipal);
                }
            });
        } else {
            ApiClient.getService().uploadFotoExtraMascota(id, part).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Object>> c, @NonNull Response<Map<String, Object>> r) {
                    if (!r.isSuccessful()) avisarFotoFallida();
                    subirFotoIndice(id, index + 1, primeraEsPrincipal);
                }
                @Override
                public void onFailure(@NonNull Call<Map<String, Object>> c, @NonNull Throwable t) {
                    avisarFotoFallida();
                    subirFotoIndice(id, index + 1, primeraEsPrincipal);
                }
            });
        }
    }

    private void avisarFotoFallida() {
        fotosFallidas++;
    }

    private void finalizarOk() {
        String base = mascotaId > 0 ? "Mascota actualizada" : "¡Mascota publicada!";
        String msg = fotosFallidas > 0
                ? base + " (" + fotosFallidas + " foto" + (fotosFallidas == 1 ? "" : "s")
                  + " no se pudo subir, podés reintentar editando la mascota)"
                : base;
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        getParentFragmentManager().popBackStack();
    }

    private void restaurar() {
        progressBar.setVisibility(View.GONE);
        btnGuardar.setEnabled(true);
        btnGuardar.setText(mascotaId > 0 ? "Guardar cambios" : "Publicar mascota");
    }

    private void putIfNotEmpty(Map<String, Object> body, String key, EditText et) {
        String val = et.getText().toString().trim();
        if (!val.isEmpty()) body.put(key, val);
    }
}
