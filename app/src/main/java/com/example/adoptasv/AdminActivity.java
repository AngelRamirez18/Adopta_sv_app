package com.example.adoptasv;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.adoptasv.Fragments.AdminDashboardFragment;
import com.example.adoptasv.Fragments.AdminMascotasFragment;
import com.example.adoptasv.Fragments.AdminReportesFragment;
import com.example.adoptasv.Fragments.PanelFragment;
import com.example.adoptasv.Fragments.PerfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Contenedor del área administrativa (voluntario/admin). Bottom nav propio:
 * Inicio (dashboard) · Mascotas · Solicitudes · Reportes · Perfil.
 */
public class AdminActivity extends AppCompatActivity {

    public static final String CONTAINER_NAME = "adminContainer";

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_admin);

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                fragment = new AdminDashboardFragment();
            } else if (id == R.id.nav_mascotas) {
                fragment = new AdminMascotasFragment();
            } else if (id == R.id.nav_solicitudes) {
                fragment = new PanelFragment();
            } else if (id == R.id.nav_reportes) {
                fragment = new AdminReportesFragment();
            } else if (id == R.id.nav_perfil) {
                fragment = new PerfilFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager().popBackStack(null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    /** Permite a los fragments (ej. accesos del dashboard) cambiar de tab. */
    public void setTab(int menuId) {
        bottomNav.setSelectedItemId(menuId);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.adminContainer, fragment)
                .commit();
    }
}
