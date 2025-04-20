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

    private List<Task> tasks;
    private TaskActionListener actionListener;
    private List<User> userList; // NUEVO: lista de usuarios para el spinner

    // Interfaz única para todas las acciones de tarea
    public interface TaskActionListener {
        void onTaskDelete(Task task);
        void onTaskUndo(Task task);
        void onStatusChange(Task task);
        void onDescriptionChange(Task task);
    }

    // NUEVO: el constructor ahora recibe también la lista de usuarios
    public TaskAdapter(List<Task> tasks, List<User> userList, TaskActionListener actionListener) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.userList = userList != null ? userList : new ArrayList<>();
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.textDescription.setText(task.getDescription());
        holder.textUser.setText(task.getAssignedUserName() != null ? task.getAssignedUserName() : "Sin asignar");

        holder.buttonMoreOptions.setOnClickListener(v -> showPopupMenu(v, task, position));

        holder.checkBoxDone.setOnCheckedChangeListener(null);
        holder.checkBoxDone.setChecked(task.getStatus().equals("Done"));
        holder.checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked ? "Done" : "Pending");
            if (actionListener != null) {
                actionListener.onStatusChange(task);
            }
            notifyItemChanged(position);
        });

        applyStyle(holder, task);
    }

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

    // MODIFICADO: ahora permite editar descripción y usuario asignado
    private void showEditDialog(Context context, Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Tarea");

        // Usa un layout personalizado con EditText y Spinner
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);
        EditText input = dialogView.findViewById(R.id.et_task_description);
        Spinner spinner = dialogView.findViewById(R.id.spinner_users);

        input.setText(task.getDescription());

        // Prepara la lista de nombres de usuario
        List<String> userNames = new ArrayList<>();
        userNames.add("Sin asignar");
        for (User user : userList) userNames.add(user.getuserName());

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, userNames);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        // Selecciona el usuario actual en el Spinner
        int selectedIndex = 0;
        if (task.getAssignedUserId() != null) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getUserId().equals(task.getAssignedUserId())) {
                    selectedIndex = i + 1; // +1 por "Sin asignar"
                    break;
                }
            }
        }
        spinner.setSelection(selectedIndex);

        builder.setView(dialogView);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newDescription = input.getText().toString().trim();
            int selectedPosition = spinner.getSelectedItemPosition();
            Long assignedUserId = (selectedPosition > 0) ? userList.get(selectedPosition - 1).getUserId() : null;
            String assignedUserName = (selectedPosition > 0) ? userList.get(selectedPosition - 1).getuserName() : null;

            if (!newDescription.isEmpty()) {
                task.setDescription(newDescription);
                task.setAssignedUserId(assignedUserId);
                task.setAssignedUserName(assignedUserName);
                notifyItemChanged(position);
                if (actionListener != null) {
                    actionListener.onDescriptionChange(task);
                }
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Unifica el estilo visual según el estado
    private void applyStyle(TaskViewHolder holder, Task task) {
        int typeface;
        int paintFlags;
        int textColor;
        int iconRes;
        View.OnClickListener actionListener;

        switch (task.getStatus()) {
            case "Pending":
                typeface = Typeface.BOLD;
                paintFlags = holder.textDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
                textColor = Color.BLACK;
                iconRes = R.drawable.ic_delete;
                actionListener = v -> this.actionListener.onTaskDelete(task);
                break;
            case "Done":
                typeface = Typeface.ITALIC;
                paintFlags = holder.textDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
                textColor = Color.BLACK;
                iconRes = R.drawable.ic_delete;
                actionListener = v -> this.actionListener.onTaskDelete(task);
                break;
            case "Deleted":
                typeface = Typeface.NORMAL;
                paintFlags = holder.textDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG;
                textColor = Color.RED;
                iconRes = R.drawable.ic_undo;
                actionListener = v -> this.actionListener.onTaskUndo(task);
                break;
            default:
                return;
        }

        holder.textDescription.setTypeface(null, typeface);
        holder.textDescription.setPaintFlags(paintFlags);
        holder.textDescription.setTextColor(textColor);
        holder.buttonAction.setImageResource(iconRes);
        holder.buttonAction.setOnClickListener(actionListener);
        holder.buttonAction.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public void updateData(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    // NUEVO: permite actualizar la lista de usuarios si cambia
    public void updateUserList(List<User> newUsers) {
        this.userList = newUsers != null ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

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
