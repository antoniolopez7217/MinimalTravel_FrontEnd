package com.example.minimaltravel.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.adapter.TaskAdapter;
import com.example.minimaltravel.api.ApiClient;
import com.example.minimaltravel.api.TaskApi;
import com.example.minimaltravel.api.UserApi;
import com.example.minimaltravel.model.Task;
import com.example.minimaltravel.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TasksFragment extends Fragment {

    // RecyclerView para mostrar la lista de tareas
    private RecyclerView recyclerViewTasks;
    // Adaptador para conectar los datos de tareas con la vista
    private TaskAdapter adapter;
    // Lista de tareas en memoria
    private List<Task> taskList;
    // Botón flotante para añadir nuevas tareas
    private FloatingActionButton fabAddTask;
    // Referencia al ítem de menú de filtro
    private MenuItem menuFilterItem;
    // Estado actual del filtro de tareas (por defecto, "Pendiente")
    private String currentFilter = "Pendiente";
    // Lista de usuarios disponibles para asignar tareas
    private List<User> userList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indica que este fragmento tiene su propio menú de opciones
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Infla el layout del fragmento
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializa el RecyclerView y su adaptador
        initRecyclerView(view);
        // Inicializa el botón flotante para añadir tareas
        initFab(view);
        // Carga la lista de usuarios (para asignar tareas)
        fetchUsers();
        // Carga la lista de tareas desde el backend
        fetchTasks();
    }

    // Inicializa el RecyclerView y configura el adaptador
    private void initRecyclerView(View view) {
        recyclerViewTasks = view.findViewById(R.id.recycler_view_tasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskList = new ArrayList<>();
        // Crea el adaptador, pasando la lista de tareas, usuarios y el listener de acciones
        adapter = new TaskAdapter(taskList, userList, new TaskAdapter.TaskActionListener() {
            @Override
            public void onTaskDelete(Task task) {
                // Marca la tarea como "Eliminado" y actualiza en backend
                updateTaskStatusAndRefresh(task, "Eliminado", "Tarea eliminada", "Error al eliminar tarea");
            }
            @Override
            public void onTaskUndo(Task task) {
                // Restaura la tarea a "Pendiente" y actualiza en backend
                updateTaskStatusAndRefresh(task, "Pendiente", "Tarea restaurada", "Error al restaurar tarea");
            }
            @Override
            public void onStatusChange(Task task) {
                // Cambia el estado de la tarea (Pendiente/Completado) y actualiza en backend
                updateTaskStatusAndRefresh(task, task.getStatus(), "Estado actualizado", "Error al actualizar estado");
            }
            @Override
            public void onDescriptionChange(Task task) {
                // Cambia la descripción de la tarea y actualiza en backend
                updateTaskStatusAndRefresh(task, task.getStatus(), "Descripción actualizada", "Error al actualizar descripción");
            }
        });
        recyclerViewTasks.setAdapter(adapter);
    }

    // Inicializa el FloatingActionButton para añadir tareas
    private void initFab(View view) {
        fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> {
            // Si la lista de usuarios está vacía, la carga antes de mostrar el diálogo
            if (userList.isEmpty()) {
                fetchUsersAndShowDialog();
            } else {
                showAddTaskDialog();
            }
        });
    }

    // Carga los usuarios y muestra el diálogo para añadir tarea solo cuando estén listos
    private void fetchUsersAndShowDialog() {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        api.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.updateUserList(userList);
                    showAddTaskDialog(); // Muestra el diálogo después de cargar usuarios
                } else {
                    showToast("Error al cargar usuarios");
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                showToast("Error al cargar usuarios");
            }
        });
    }

    // Carga los usuarios y actualiza el adaptador (para edición y creación de tareas)
    private void fetchUsers() {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        api.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    if (adapter != null) adapter.updateUserList(userList);
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                showToast("Error al cargar usuarios");
            }
        });
    }

    // Infla el menú de opciones y guarda la referencia al ítem de filtro
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        menuFilterItem = menu.findItem(R.id.menu_filter);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Maneja la selección de opciones del menú (por ejemplo, el filtro)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {
            View anchor = requireActivity().findViewById(R.id.toolbar);
            showFilterPopup(anchor); // Muestra el menú de filtro
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Muestra un menú contextual para filtrar las tareas por estado
    private void showFilterPopup(View anchorView) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.setGravity(Gravity.END);
        // Añade las opciones de filtro
        popup.getMenu().add("Todos");
        popup.getMenu().add("Pendiente");
        popup.getMenu().add("Completado");
        popup.getMenu().add("Eliminado");
        popup.setOnMenuItemClickListener(menuItem -> {
            filterTasksByStatus(menuItem.getTitle().toString());
            return true;
        });
        popup.show();
    }

    // Filtra la lista de tareas por el estado seleccionado y actualiza el icono del filtro
    private void filterTasksByStatus(String status) {
        currentFilter = status;
        if (menuFilterItem != null) {
            menuFilterItem.setIcon(!status.equals("Todos") ? R.drawable.ic_filter_list_active : R.drawable.ic_filter_list_default);
        }
        // Filtra la lista de tareas según el estado seleccionado
        List<Task> filteredTasks = status.equals("Todos")
                ? new ArrayList<>(taskList)
                : taskList.stream().filter(task -> task.getStatus().equals(status)).collect(Collectors.toList());
        adapter.updateData(filteredTasks);
    }

    // Muestra el diálogo para añadir una nueva tarea (con Spinner de usuarios)
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nueva Tarea");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
        EditText input = dialogView.findViewById(R.id.et_task_description);
        Spinner spinner = dialogView.findViewById(R.id.spinner_users);

        // Prepara la lista de nombres de usuario para el Spinner
        List<String> userNames = new ArrayList<>();
        userNames.add("Sin asignar"); // Opción por defecto
        for (User user : userList) userNames.add(user.getuserName());

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, userNames);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        builder.setView(dialogView);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String taskDescription = input.getText().toString().trim();
            int selectedPosition = spinner.getSelectedItemPosition();
            // Si selecciona un usuario, obtiene su id y nombre; si no, deja null
            Long assignedUserId = (selectedPosition > 0) ? userList.get(selectedPosition - 1).getUserId() : null;
            String assignedUserName = (selectedPosition > 0) ? userList.get(selectedPosition - 1).getuserName() : null;

            if (!taskDescription.isEmpty()) {
                createNewTask(taskDescription, assignedUserId, assignedUserName);
            } else {
                showToast("La descripción no puede estar vacía");
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Ordena la lista de tareas por estado: Pendiente > Completado > Eliminado
    private void sortTasksByStatus() {
        taskList.sort((task1, task2) -> {
            if (task1.getStatus().equals("Pendiente") && !task2.getStatus().equals("Pendiente")) return -1;
            if (task1.getStatus().equals("Completado") && task2.getStatus().equals("Eliminado")) return -1;
            if (task1.getStatus().equals(task2.getStatus())) return 0;
            return 1;
        });
    }

    // Crea una nueva tarea y la añade usando la API
    private void createNewTask(String description, Long assignedUserId, String assignedUserName) {
        TaskApi api = ApiClient.getClient().create(TaskApi.class);
        Task newTask = new Task();
        newTask.setDescription(description);
        newTask.setStatus("Pendiente");
        newTask.setCreationDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        newTask.setAssignedUserId(assignedUserId);
        newTask.setAssignedUserName(assignedUserName);

        api.createTask(newTask).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchTasks(); // Recarga la lista de tareas
                    showToast("Tarea creada");
                } else {
                    showToast("Error al crear tarea");
                }
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    // Obtiene todas las tareas del backend y actualiza la lista
    private void fetchTasks() {
        TaskApi api = ApiClient.getClient().create(TaskApi.class);
        api.getAllTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.clear();
                    taskList.addAll(response.body());
                    sortTasksByStatus();
                    adapter.updateData(new ArrayList<>(taskList));
                    filterTasksByStatus(currentFilter); // Aplica el filtro actual
                } else {
                    showToast("Error al cargar tareas");
                }
            }
            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    // Actualiza el estado o descripción de una tarea y refresca la lista
    private void updateTaskStatusAndRefresh(Task task, String newStatus, String successMsg, String errorMsg) {
        TaskApi api = ApiClient.getClient().create(TaskApi.class);
        task.setStatus(newStatus);
        api.updateTask(task.getTaskId(), task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    fetchTasks(); // Recarga la lista tras la actualización
                    showToast(successMsg);
                } else {
                    showToast(errorMsg);
                }
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    // Muestra un mensaje Toast en pantalla
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
