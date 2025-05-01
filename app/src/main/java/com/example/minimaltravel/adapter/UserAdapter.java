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

// Adaptador para mostrar usuarios en un RecyclerView
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    // Lista de usuarios a mostrar
    private List<User> users;
    // Listener para manejar acciones sobre los usuarios
    private UserActionListener actionListener;

    // Interfaz para definir las acciones disponibles sobre los usuarios
    public interface UserActionListener {
        void onUserDelete(User user); // Eliminar usuario
        void onUserUpdate(User user, String newUserName, String newMail); // Actualizar datos del usuario
    }

    // Constructor que recibe la lista inicial de usuarios y el listener de acciones
    public UserAdapter(List<User> users, UserActionListener actionListener) {
        this.users = users != null ? users : new ArrayList<>();
        this.actionListener = actionListener;
    }

    // Crea nuevos ViewHolders para los items de usuario
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    // Vincula los datos de un usuario a su ViewHolder correspondiente
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Mostrar datos del usuario en la interfaz
        holder.textUserName.setText(user.getuserName());
        holder.textUserMail.setText(user.getmail());

        // Configurar clic en el botón de opciones (menú contextual)
        holder.buttonMoreOptions.setOnClickListener(v ->
                showPopupMenu(v, user, position));

        // Configurar clic en el botón de acción principal (eliminar)
        holder.buttonAction.setOnClickListener(v -> {
            // Diálogo de confirmación para eliminar usuario
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar al usuario \"" + user.getuserName() + "\"?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Sí", (dialog, which) -> {
                        if (actionListener != null) {
                            actionListener.onUserDelete(user); // Notificar acción de eliminación
                        }
                    })
                    .show();
        });
    }

    // Muestra el menú contextual con opciones para el usuario
    private void showPopupMenu(View view, User user, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.user_options_menu); // Inflar menú desde recursos XML
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit_username) {
                showEditDialog(view.getContext(), user, position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    // Muestra diálogo para editar los datos del usuario
    private void showEditDialog(Context context, User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Usuario");

        // Inflar layout personalizado del diálogo
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null);
        EditText etUserName = dialogView.findViewById(R.id.et_username);
        EditText etMail = dialogView.findViewById(R.id.et_email);

        // Pre-cargar datos actuales del usuario
        etUserName.setText(user.getuserName());
        etMail.setText(user.getmail());

        builder.setView(dialogView);

        // Configurar acción del botón Guardar
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newUserName = etUserName.getText().toString().trim();
            String newMail = etMail.getText().toString().trim();

            if (!newUserName.isEmpty()) {
                // Notificar actualización solo si hay nombre de usuario válido
                actionListener.onUserUpdate(user, newUserName, newMail);
            } else {
                Toast.makeText(context, "El usuario es obligatorio", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Devuelve la cantidad de usuarios en la lista
    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    // Actualiza la lista de usuarios y refresca la vista
    public void updateData(List<User> newUsers) {
        this.users = new ArrayList<>(newUsers); // Crear nueva instancia para evitar mutaciones
        notifyDataSetChanged(); // Notificar cambios a RecyclerView
    }

    // ViewHolder que contiene las vistas de cada item de usuario
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textUserMail;
        ImageButton buttonAction, buttonMoreOptions;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Enlazar vistas del layout
            textUserName = itemView.findViewById(R.id.text_user_username);
            textUserMail = itemView.findViewById(R.id.text_user_mail);
            buttonAction = itemView.findViewById(R.id.button_action_user);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }
    }
}
