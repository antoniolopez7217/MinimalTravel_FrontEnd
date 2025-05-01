package com.example.minimaltravel.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.adapter.UserAdapter;
import com.example.minimaltravel.api.ApiClient;
import com.example.minimaltravel.api.UserApi;
import com.example.minimaltravel.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersFragment extends Fragment {

    // RecyclerView para mostrar la lista de usuarios
    private RecyclerView recyclerViewUsers;
    // Adaptador que conecta los datos de usuarios con la vista
    private UserAdapter adapter;
    // Lista de usuarios en memoria
    private List<User> userList;
    // Botón flotante para añadir nuevos usuarios
    private FloatingActionButton fabAddUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Permite que el fragmento tenga su propio menú de opciones (si lo necesitas)
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Infla el layout del fragmento de usuarios
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializa el RecyclerView y su adaptador
        initRecyclerView(view);
        // Inicializa el botón flotante para añadir usuarios
        initFab(view);
        // Carga la lista de usuarios desde el backend
        fetchUsers();
    }

    // Configura el RecyclerView y el adaptador de usuarios
    private void initRecyclerView(View view) {
        recyclerViewUsers = view.findViewById(R.id.recycler_view_users);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        userList = new ArrayList<>();

        // Crea el adaptador, pasando la lista de usuarios y el listener de acciones
        adapter = new UserAdapter(userList, new UserAdapter.UserActionListener() {
            @Override
            public void onUserDelete(User user) {
                // Llama a la función para eliminar el usuario seleccionado
                deleteUser(user);
            }

            @Override
            public void onUserUpdate(User user, String newUsername, String newEmail) {
                // Llama a la función para actualizar el usuario con los nuevos datos
                updateUser(user, newUsername, newEmail);
            }
        });
        recyclerViewUsers.setAdapter(adapter);
    }

    // Inicializa el FloatingActionButton para añadir usuarios
    private void initFab(View view) {
        fabAddUser = view.findViewById(R.id.fab_add_user);
        fabAddUser.setOnClickListener(v -> showAddUserDialog());
    }

    // Muestra un diálogo para añadir un nuevo usuario
    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nuevo Usuario");

        // Infla el layout personalizado del diálogo
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_user, null);
        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etEmail = dialogView.findViewById(R.id.et_email);

        builder.setView(dialogView);

        // Configura el botón "Guardar" del diálogo
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            // Validaciones básicas antes de crear el usuario
            if (username.isEmpty()) {
                showToast("El nombre de usuario es obligatorio");
            } else if (!isValidEmail(email)) {
                showToast("El email no es válido");
            } else {
                // Si todo es correcto, crea el usuario
                createNewUser(username, email);
            }
        });

        // Botón "Cancelar" no hace nada especial
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Valida el formato del email (permite vacío o válido)
    private boolean isValidEmail(String email) {
        return email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Crea un nuevo usuario y lo envía al backend
    private void createNewUser(String username, String email) {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        User newUser = new User();
        newUser.setuserName(username);
        newUser.setmail(email);
        // Asigna la fecha de creación actual
        newUser.setCreationDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        // Llama a la API para crear el usuario
        api.createUser(newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchUsers(); // Recarga la lista de usuarios
                    showToast("Usuario creado exitosamente");
                } else {
                    showToast("Error al crear usuario");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    // Obtiene todos los usuarios del backend y actualiza la lista local
    private void fetchUsers() {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        api.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.updateData(new ArrayList<>(userList)); // Actualiza el adaptador
                } else {
                    showToast("Error al cargar usuarios");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    // Elimina un usuario llamando a la API
    private void deleteUser(User user) {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        api.deleteUser(user.getUserId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchUsers(); // Recarga la lista tras eliminar
                    showToast("Usuario eliminado");
                } else {
                    showToast("Error al eliminar usuario");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    // Actualiza los datos de un usuario en el backend
    private void updateUser(User user, String newUsername, String newEmail) {
        if (!isValidEmail(newEmail)) {
            showToast("El email no es válido");
            return;
        }

        UserApi api = ApiClient.getClient().create(UserApi.class);
        user.setuserName(newUsername);
        user.setmail(newEmail);

        api.updateUser(user.getUserId(), user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    fetchUsers(); // Recarga la lista tras actualizar
                    showToast("Usuario actualizado");
                } else {
                    showToast("Error al actualizar usuario");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    // Muestra un mensaje Toast en pantalla
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
