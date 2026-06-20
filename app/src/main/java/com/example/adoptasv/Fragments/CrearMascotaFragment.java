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
import com.example.adoptasv.Conexion.Modelos.SingleResponse;
import com.example.adoptasv.R;
import com.example.adoptasv.Util.MultipartUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashMap;
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
    private Spinner spEspecie, spSexo, spTamano;
    private SwitchMaterial swVacunas, swEsterilizado;
    private ImageView ivPreview;
    private LinearLayout llAddFoto;
    private MaterialButton btnGuardar;
    private ProgressBar progressBar;
    private TextView tvTitulo;

    private Uri fotoUri = null;
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
        swVacunas      = view.findViewById(R.id.swVacunas);
        swEsterilizado = view.findViewById(R.id.swEsterilizado);
        ivPreview      = view.findViewById(R.id.ivPreview);
        llAddFoto      = view.findViewById(R.id.llAddFoto);
        btnGuardar     = view.findViewById(R.id.btnGuardar);
        progressBar    = view.findViewById(R.id.progressBar);
        tvTitulo       = view.findViewById(R.id.tvTitulo);

        spEspecie.setAdapter(spinnerAdapter(ESPECIES));
        spSexo.setAdapter(spinnerAdapter(SEXOS));
        spTamano.setAdapter(spinnerAdapter(TAMANOS));

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                fotoUri = uri;
                llAddFoto.setVisibility(View.GONE);
                Glide.with(this).load(uri).centerCrop().into(ivPreview);
            }
        });
        View.OnClickListener pickFoto = v -> imagePicker.launch("image/*");
        ivPreview.setOnClickListener(pickFoto);
        llAddFoto.setOnClickListener(pickFoto);

        btnGuardar.setOnClickListener(v -> guardar());

        if (mascotaId > 0) {
            tvTitulo.setText("Editar mascota");
            btnGuardar.setText("Guardar cambios");
            cargarMascota();
        }
    }

    private ArrayAdapter<String> spinnerAdapter(String[] items) {
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
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

        if (m.fotoUrl != null && !m.fotoUrl.isEmpty()) {
            llAddFoto.setVisibility(View.GONE);
            Glide.with(this).load(m.fotoUrl).centerCrop().into(ivPreview);
        }
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

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
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
                    if (fotoUri != null && id > 0) {
                        subirFoto(id);
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

    private void subirFoto(int id) {
        btnGuardar.setText("Subiendo foto…");
        MultipartBody.Part part = MultipartUtils.fotoPart(requireContext(), fotoUri, "foto");
        if (part == null) { finalizarOk(); return; }
        ApiClient.getService().uploadFotoMascota(id, part).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> c, @NonNull Response<Map<String, String>> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful()) {
                    Toast.makeText(getContext(), "La mascota se guardó, pero la foto no se subió.",
                            Toast.LENGTH_LONG).show();
                }
                finalizarOk();
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> c, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "La mascota se guardó, pero la foto no se subió.",
                        Toast.LENGTH_LONG).show();
                finalizarOk();
            }
        });
    }

    private void finalizarOk() {
        Toast.makeText(getContext(),
                mascotaId > 0 ? "Mascota actualizada" : "¡Mascota publicada!", Toast.LENGTH_LONG).show();
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
