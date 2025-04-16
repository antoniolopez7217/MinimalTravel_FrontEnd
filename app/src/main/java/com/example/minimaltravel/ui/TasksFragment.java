package com.example.minimaltravel.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
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
import com.example.minimaltravel.model.Task;
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

    private RecyclerView recyclerViewTasks;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private FloatingActionButton fabAddTask;
    private MenuItem menuFilterItem;
    private String currentFilter = "Pending";

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
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
        initFab(view);
        fetchTasks();
    }

    // Inicializa el RecyclerView y su adaptador
    private void initRecyclerView(View view) {
        recyclerViewTasks = view.findViewById(R.id.recycler_view_tasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, new TaskAdapter.TaskActionListener() {
            @Override
            public void onTaskDelete(Task task) { updateTaskStatusAndRefresh(task, "Deleted", "Tarea eliminada", "Error al eliminar tarea"); }
            @Override
            public void onTaskUndo(Task task) { updateTaskStatusAndRefresh(task, "Pending", "Tarea restaurada", "Error al restaurar tarea"); }
            @Override
            public void onStatusChange(Task task) { updateTaskStatusAndRefresh(task, task.getStatus(), "Estado actualizado", "Error al actualizar estado"); }
            @Override
            public void onDescriptionChange(Task task) { updateTaskStatusAndRefresh(task, task.getStatus(), "Descripción actualizada", "Error al actualizar descripción"); }
        });
        recyclerViewTasks.setAdapter(adapter);
    }

    // Inicializa el botón flotante para añadir tareas
    private void initFab(View view) {
        fabAddTask = view.findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    // Infla el menú y guarda la referencia al ítem de filtro
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        menuFilterItem = menu.findItem(R.id.menu_filter);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Maneja la selección del ítem de filtro
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {
            View anchor = requireActivity().findViewById(R.id.toolbar);
            showFilterPopup(anchor);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Muestra el menú de filtro alineado a la derecha
    private void showFilterPopup(View anchorView) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.setGravity(Gravity.END);
        popup.getMenu().add("All");
        popup.getMenu().add("Pending");
        popup.getMenu().add("Done");
        popup.getMenu().add("Deleted");
        popup.setOnMenuItemClickListener(menuItem -> {
            filterTasksByStatus(menuItem.getTitle().toString());
            return true;
        });
        popup.show();
    }

    // Filtra las tareas por estado y actualiza el icono del filtro
    private void filterTasksByStatus(String status) {
        currentFilter = status;

        if (menuFilterItem != null) {
            menuFilterItem.setIcon(!status.equals("All") ? R.drawable.ic_filter_list_active : R.drawable.ic_filter_list_default);
        }
        List<Task> filteredTasks = status.equals("All")
                ? new ArrayList<>(taskList)
                : taskList.stream().filter(task -> task.getStatus().equals(status)).collect(Collectors.toList());
        adapter.updateData(filteredTasks);
    }

    // Muestra el diálogo para añadir una nueva tarea
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nueva Tarea");
        final EditText input = new EditText(requireContext());
        input.setHint("Descripción de la tarea");
        builder.setView(input);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String taskDescription = input.getText().toString().trim();
            if (!taskDescription.isEmpty()) {
                createNewTask(taskDescription);
            } else {
                showToast("La descripción no puede estar vacía");
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Ordena las tareas por estado: Pending > Done > Deleted
    private void sortTasksByStatus() {
        taskList.sort((task1, task2) -> {
            if (task1.getStatus().equals("Pending") && !task2.getStatus().equals("Pending")) return -1;
            if (task1.getStatus().equals("Done") && task2.getStatus().equals("Deleted")) return -1;
            if (task1.getStatus().equals(task2.getStatus())) return 0;
            return 1;
        });
    }

    // Crea una nueva tarea y la añade a la lista
    private void createNewTask(String description) {
        TaskApi api = ApiClient.getClient().create(TaskApi.class);
        Task newTask = new Task();
        newTask.setDescription(description);
        newTask.setStatus("Pending");
        newTask.setCreationDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        api.createTask(newTask).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.add(response.body());
                    fetchTasks();
                    sortTasksByStatus();
                    adapter.notifyDataSetChanged();
                    recyclerViewTasks.smoothScrollToPosition(taskList.size() - 1);
                    showToast("Tarea creada");
                } else {
                    showToast("Error al crear tarea");
                }
            }
            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                Log.e("API Error", t.getMessage());
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
                    filterTasksByStatus(currentFilter);
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

    // Actualiza el estado de una tarea y refresca la lista
    private void updateTaskStatusAndRefresh(Task task, String newStatus, String successMsg, String errorMsg) {
        TaskApi api = ApiClient.getClient().create(TaskApi.class);
        task.setStatus(newStatus);
        api.updateTask(task.getTaskId(), task).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    fetchTasks();
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

    // Muestra un Toast con el mensaje proporcionado
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
