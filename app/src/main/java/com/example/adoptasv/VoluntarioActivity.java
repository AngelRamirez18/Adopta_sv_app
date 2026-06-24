package com.example.adoptasv;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.adoptasv.Fragments.AdminMascotasFragment;
import com.example.adoptasv.Fragments.AdminReportesFragment;
import com.example.adoptasv.Fragments.PanelFragment;
import com.example.adoptasv.Fragments.PerfilFragment;
import com.example.adoptasv.Fragments.VoluntarioDashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Módulo dedicado del voluntario del refugio. Bottom nav propio enfocado en la
 * gestión de su refugio: Inicio (dashboard) · Mascotas · Solicitudes · Reportes · Perfil.
 * Reutiliza los fragments de gestión existentes (que ya filtran por el refugio del
 * voluntario) y conserva el id de contenedor {@code adminContainer} para no romper su
 * navegación interna. A diferencia de {@link AdminActivity}, no expone Usuarios ni el
 * CRUD de refugios.
 */
public class VoluntarioActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        // En modo oscuro usar iconos claros en la barra de estado (no el flag light).
        boolean night = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (!night) uiFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);

        setContentView(R.layout.activity_voluntario);

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            loadFragment(new VoluntarioDashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                fragment = new VoluntarioDashboardFragment();
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

    /** Permite a los accesos del dashboard cambiar de tab. */
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
