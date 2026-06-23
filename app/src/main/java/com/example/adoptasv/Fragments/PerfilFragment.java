package com.example.adoptasv.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.adoptasv.MainActivity;
import com.example.adoptasv.R;
import com.example.adoptasv.Conexion.ApiClient;
import com.example.adoptasv.Conexion.Modelos.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private CircleImageView ivAvatar;
    private TextView tvNombre, tvEmail, tvStatMascotas, tvStatRol, tvStatEstado;
    private LinearLayout llCerrarSesion, llEditarPerfil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        ivAvatar        = view.findViewById(R.id.ivAvatar);
        tvNombre        = view.findViewById(R.id.tvNombre);
        tvEmail         = view.findViewById(R.id.tvEmail);
        tvStatMascotas  = view.findViewById(R.id.tvStatMascotas);
        tvStatRol       = view.findViewById(R.id.tvStatRol);
        tvStatEstado    = view.findViewById(R.id.tvStatEstado);
        llCerrarSesion  = view.findViewById(R.id.llCerrarSesion);
        llEditarPerfil  = view.findViewById(R.id.llEditarPerfil);

        cargarPerfil();

        llCerrarSesion.setOnClickListener(v -> cerrarSesion());
        llEditarPerfil.setOnClickListener(v ->
                Toast.makeText(getContext(), "Editar perfil — próximamente", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void cargarPerfil() {
        ApiClient.getService().getPerfil().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    bindUser(user);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (!isAdded()) return;
                // Mostrar datos de Firebase como fallback
                com.google.firebase.auth.FirebaseUser fbUser =
                        FirebaseAuth.getInstance().getCurrentUser();
                if (fbUser != null) {
                    tvNombre.setText(fbUser.getDisplayName());
                    tvEmail.setText(fbUser.getEmail());
                    if (fbUser.getPhotoUrl() != null) {
                        Glide.with(requireContext())
                                .load(fbUser.getPhotoUrl())
                                .into(ivAvatar);
                    }
                }
            }
        });
    }

    private void bindUser(User user) {
        tvNombre.setText(user.name);
        tvEmail.setText(user.email);

        // Stats
        tvStatRol.setText(user.roles != null && !user.roles.isEmpty()
                ? capitalize(user.roles.get(0)) : "—");
        tvStatEstado.setText(user.estadoCuenta != null
                ? capitalize(user.estadoCuenta) : "—");
        tvStatMascotas.setText(user.refugio != null ? "1" : "0");

        // Foto
        if (user.fotoPerfil != null && !user.fotoPerfil.isEmpty()) {
            Glide.with(requireContext())
                    .load(user.fotoPerfil)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .into(ivAvatar);
        } else {
            // Fallback a foto de Google si existe
            com.google.firebase.auth.FirebaseUser fbUser =
                    FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null && fbUser.getPhotoUrl() != null) {
                Glide.with(requireContext())
                        .load(fbUser.getPhotoUrl())
                        .into(ivAvatar);
            }
        }
    }

    private void cerrarSesion() {
        // 1. Llamar a la API para invalidar sesión
        ApiClient.getService().logout().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
        });

        // 2. Cierre local de Firebase
        FirebaseAuth.getInstance().signOut();

        // 3. Cierre de sesión de Google para permitir elegir cuenta al volver
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            navegarAlLogin();
        });
    }

    private void navegarAlLogin() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}