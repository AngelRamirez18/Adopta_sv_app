package com.example.adoptasv;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.adoptasv.Fragments.AdoptarFragment;
import com.example.adoptasv.Fragments.HomeFragment;
import com.example.adoptasv.Fragments.HubFragment;
import com.example.adoptasv.Fragments.PerfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Status bar transparente con iconos oscuros
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        setContentView(R.layout.activity_home);

        bottomNav = findViewById(R.id.bottomNav);

        // Fragment inicial
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_favorites) {
                fragment = new AdoptarFragment();
            } else if (id == R.id.nav_messages) {
                fragment = new HubFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new PerfilFragment();
            }

            if (fragment != null) {
                // Al cambiar de tab limpiamos la pila de sub-pantallas abiertas
                getSupportFragmentManager().popBackStack(null,
                        androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}