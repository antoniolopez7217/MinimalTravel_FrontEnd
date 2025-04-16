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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private TaskActionListener actionListener;

    // Interfaz única para todas las acciones de tarea
    public interface TaskActionListener {
        void onTaskDelete(Task task);
        void onTaskUndo(Task task);
        void onStatusChange(Task task);
        void onDescriptionChange(Task task);
    }

    public TaskAdapter(List<Task> tasks, TaskActionListener actionListener) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
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
        holder.textStatus.setText(task.getStatus());

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

    private void showEditDialog(Context context, Task task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Task");

        final EditText input = new EditText(context);
        input.setText(task.getDescription());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newDescription = input.getText().toString().trim();
            if (!newDescription.isEmpty()) {
                task.setDescription(newDescription);
                notifyItemChanged(position);
                if (actionListener != null) {
                    actionListener.onDescriptionChange(task);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
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

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textDescription, textStatus;
        ImageButton buttonAction, buttonMoreOptions;
        CheckBox checkBoxDone;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.text_task_description);
            textStatus = itemView.findViewById(R.id.text_task_status);
            buttonAction = itemView.findViewById(R.id.button_action_task);
            checkBoxDone = itemView.findViewById(R.id.checkbox_task_done);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }
    }
}
