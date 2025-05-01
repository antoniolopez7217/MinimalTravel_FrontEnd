package com.example.minimaltravel.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.model.Task;
import com.example.minimaltravel.model.User;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // Lista de tareas que se mostrarán en el RecyclerView
    private List<Task> tasks;
    // Listener para manejar acciones sobre las tareas (borrar, deshacer, cambiar estado, etc.)
    private TaskActionListener actionListener;
    // Lista de usuarios, usada para el spinner al editar una tarea
    private List<User> userList; // NUEVO: lista de usuarios para el spinner

    // Interfaz para definir las acciones que se pueden realizar sobre una tarea
    public interface TaskActionListener {
        void onTaskDelete(Task task);
        void onTaskUndo(Task task);
        void onStatusChange(Task task);
        void onDescriptionChange(Task task);
    }

    // Constructor del adaptador, recibe la lista de tareas, usuarios y el listener de acciones
    public TaskAdapter(List<Task> tasks, List<User> userList, TaskActionListener actionListener) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.userList = userList != null ? userList : new ArrayList<>();
        this.actionListener = actionListener;
    }

    // Crea un nuevo ViewHolder para cada item de la lista
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout de cada tarea
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    // Asocia los datos de una tarea a su ViewHolder correspondiente
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        // Muestra la descripción y el nombre del usuario asignado (o "Sin asignar" si es null)
        holder.textDescription.setText(task.getDescription());
        holder.textUser.setText(task.getAssignedUserName() != null ? task.getAssignedUserName() : "Sin asignar");

        // Configura el botón de opciones adicionales (menú contextual)
        holder.buttonMoreOptions.setOnClickListener(v -> showPopupMenu(v, task, position));

        // Configura el checkbox para marcar la tarea como completada o pendiente
        holder.checkBoxDone.setOnCheckedChangeListener(null); // Evita disparar el listener anterior
        holder.checkBoxDone.setChecked(task.getStatus().equals("Completado"));
        holder.checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked ? "Completado" : "Pendiente");
            if (actionListener != null) {
                actionListener.onStatusChange(task);
            }
            notifyItemChanged(position); // Actualiza la vista de este item
        });

        // Aplica el estilo visual según el estado de la tarea
        applyStyle(holder, task);
    }

    // Muestra el menú contextual (popup) para editar la tarea
    private void showPopupMenu(View view, Task task, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.task_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit) {
                showEditDialog(view.getContext(), task, position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    // Muestra un diálogo para editar la descripción y el usuario asignado de la tarea
    private void showEditDialog(Context context, Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Tarea");

        // Infla el layout personalizado del diálogo (con EditText y Spinner)
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);
        EditText input = dialogView.findViewById(R.id.et_task_description);
        Spinner spinner = dialogView.findViewById(R.id.spinner_users);

        // Pre-carga la descripción actual
        input.setText(task.getDescription());

        // Prepara la lista de nombres de usuario para el spinner
        List<String> userNames = new ArrayList<>();
        userNames.add("Sin asignar");
        for (User user : userList) userNames.add(user.getuserName());

        // Configura el spinner con los nombres de usuario
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, userNames);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        // Selecciona el usuario actualmente asignado en el spinner
        int selectedIndex = 0;
        if (task.getAssignedUserId() != null) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getUserId().equals(task.getAssignedUserId())) {
                    selectedIndex = i + 1; // +1 porque "Sin asignar" es el primer elemento
                    break;
                }
            }
        }
        spinner.setSelection(selectedIndex);

        builder.setView(dialogView);

        // Acción al pulsar "Guardar" en el diálogo
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newDescription = input.getText().toString().trim();
            int selectedPosition = spinner.getSelectedItemPosition();
            // Si no es "Sin asignar", obtiene el id y nombre del usuario seleccionado
            Long assignedUserId = (selectedPosition > 0) ? userList.get(selectedPosition - 1).getUserId() : null;
            String assignedUserName = (selectedPosition > 0) ? userList.get(selectedPosition - 1).getuserName() : null;

            if (!newDescription.isEmpty()) {
                // Actualiza la tarea con los nuevos datos
                task.setDescription(newDescription);
                task.setAssignedUserId(assignedUserId);
                task.setAssignedUserName(assignedUserName);
                notifyItemChanged(position); // Actualiza la vista
                if (actionListener != null) {
                    actionListener.onDescriptionChange(task);
                }
            }
        });

        // Acción al pulsar "Cancelar" (no hace nada)
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Aplica el estilo visual (negrita, tachado, color, icono) según el estado de la tarea
    private void applyStyle(TaskViewHolder holder, Task task) {
        int typeface;
        int paintFlags;
        int textColor;
        int iconRes;
        View.OnClickListener actionListener;

        switch (task.getStatus()) {
            case "Pendiente":
                typeface = Typeface.BOLD;
                paintFlags = holder.textDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
                textColor = Color.BLACK;
                iconRes = R.drawable.ic_delete;
                actionListener = v -> this.actionListener.onTaskDelete(task);
                break;
            case "Completado":
                typeface = Typeface.ITALIC;
                paintFlags = holder.textDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
                textColor = Color.BLACK;
                iconRes = R.drawable.ic_delete;
                actionListener = v -> this.actionListener.onTaskDelete(task);
                break;
            case "Eliminado":
                typeface = Typeface.NORMAL;
                paintFlags = holder.textDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG;
                textColor = Color.RED;
                iconRes = R.drawable.ic_undo;
                actionListener = v -> this.actionListener.onTaskUndo(task);
                break;
            default:
                return;
        }

        // Aplica los estilos calculados
        holder.textDescription.setTypeface(null, typeface);
        holder.textDescription.setPaintFlags(paintFlags);
        holder.textDescription.setTextColor(textColor);
        holder.buttonAction.setImageResource(iconRes);
        holder.buttonAction.setOnClickListener(actionListener);
        holder.buttonAction.setVisibility(View.VISIBLE);
    }

    // Devuelve el número de tareas en la lista
    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    // Permite actualizar la lista de tareas y refresca la vista
    public void updateData(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    // Permite actualizar la lista de usuarios (por ejemplo, si cambian los usuarios disponibles)
    public void updateUserList(List<User> newUsers) {
        this.userList = newUsers != null ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ViewHolder que contiene las vistas de cada item de tarea
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textDescription, textUser;
        ImageButton buttonAction, buttonMoreOptions;
        CheckBox checkBoxDone;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.text_task_description);
            textUser = itemView.findViewById(R.id.text_task_user);
            buttonAction = itemView.findViewById(R.id.button_action_task);
            checkBoxDone = itemView.findViewById(R.id.checkbox_task_done);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }
    }
}