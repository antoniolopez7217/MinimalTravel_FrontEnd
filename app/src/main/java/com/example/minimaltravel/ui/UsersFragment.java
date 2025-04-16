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

    private RecyclerView recyclerViewUsers;
    private UserAdapter adapter;
    private List<User> userList;
    private FloatingActionButton fabAddUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Permite que el fragmento tenga su propio menú de opciones
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Infla el layout del fragmento
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
        initFab(view);
        fetchUsers();
    }

    private void initRecyclerView(View view) {
        recyclerViewUsers = view.findViewById(R.id.recycler_view_users);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        userList = new ArrayList<>();

        adapter = new UserAdapter(userList, new UserAdapter.UserActionListener() {
            @Override
            public void onUserDelete(User user) {
                deleteUser(user);
            }

            @Override
            public void onUserUpdate(User user, String newUsername, String newEmail) {
                updateUser(user, newUsername, newEmail);
            }
        });
        recyclerViewUsers.setAdapter(adapter);
    }

    // Inicializa el botón flotante para añadir usuarios
    private void initFab(View view) {
        fabAddUser = view.findViewById(R.id.fab_add_user);
        fabAddUser.setOnClickListener(v -> showAddUserDialog());
    }

    // Muestra el diálogo para añadir un nuevo usuario
    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nuevo Usuario");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_user, null);
        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etEmail = dialogView.findViewById(R.id.et_email);

        builder.setView(dialogView);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (username.isEmpty()) {
                showToast("El nombre de usuario es obligatorio");
            } else if (!isValidEmail(email)) {
                showToast("El email no es válido");
            } else {
                createNewUser(username, email);
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private boolean isValidEmail(String email) {
        return email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private void createNewUser(String username, String email) {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        User newUser = new User();
        newUser.setuserName(username);
        newUser.setmail(email);
        newUser.setCreationDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        api.createUser(newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchUsers();
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

    // Obtiene todos los users del backend y actualiza la lista
    private void fetchUsers() {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        api.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.updateData(new ArrayList<>(userList));
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

    private void deleteUser(User user) {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        api.deleteUser(user.getUserId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchUsers();
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
                    fetchUsers();
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}