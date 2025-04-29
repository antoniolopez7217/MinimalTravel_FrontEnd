package com.example.minimaltravel.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private UserActionListener actionListener;

    // Interfaz única para todas las acciones de tarea
    public interface UserActionListener {
        void onUserDelete(User user);
        void onUserUpdate(User user, String newUserName, String newMail);
    }

    public UserAdapter(List<User> users, UserActionListener actionListener) {
        this.users = users != null ? users : new ArrayList<>();
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Configurar datos del usuario
        holder.textUserName.setText(user.getuserName());
        holder.textUserMail.setText(user.getmail());

        // Listeners de clic
        holder.buttonMoreOptions.setOnClickListener(v ->
                showPopupMenu(v, user, position));

        holder.buttonAction.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar al usuario \"" + user.getuserName() + "\"?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Sí", (dialog, which) -> {
                        if (actionListener != null) {
                            actionListener.onUserDelete(user);
                        }
                    })
                    .show();
        });
    }

    private void showPopupMenu(View view, User user, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.user_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit_username) {
                showEditDialog(view.getContext(), user, position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showEditDialog(Context context, User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Usuario");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null);
        EditText etUserName = dialogView.findViewById(R.id.et_username);
        EditText etMail = dialogView.findViewById(R.id.et_email);

        etUserName.setText(user.getuserName());
        etMail.setText(user.getmail());

        builder.setView(dialogView);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newUserName = etUserName.getText().toString().trim();
            String newMail = etMail.getText().toString().trim();
            if (!newUserName.isEmpty()) {
                actionListener.onUserUpdate(user, newUserName, newMail);
            } else {
                Toast.makeText(context, "El usuario es obligatorio", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public void updateData(List<User> newUsers) {
        this.users = new ArrayList<>(newUsers);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textUserMail;
        ImageButton buttonAction, buttonMoreOptions;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.text_user_username);
            textUserMail = itemView.findViewById(R.id.text_user_mail);
            buttonAction = itemView.findViewById(R.id.button_action_user);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }
    }
}