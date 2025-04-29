package com.example.minimaltravel.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.minimaltravel.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new TasksFragment(), "Tareas");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            String title = "";

            int itemId = item.getItemId(); // Obtener el ID del ítem seleccionado

            if (itemId == R.id.nav_tasks) {
                fragment = new TasksFragment();
                title = "Tareas";
            } else if (itemId == R.id.nav_users) {
                fragment = new UsersFragment();
                title = "Usuarios";
            } else if (itemId == R.id.nav_transactions) {
                fragment = new TransactionsFragment();
                title = "Transacciones";
            } else if (itemId == R.id.nav_debts) {
                fragment = new DebtsFragment();
                title = "Deudas";
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                toolbar.setTitle(title);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, String title) {
        // Actualizar título del Toolbar
        toolbar.setTitle(title);

        // Transacción de fragmentos
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
