package com.example.minimaltravel.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.adapter.TransactionAdapter;
import com.example.minimaltravel.api.ApiClient;
import com.example.minimaltravel.api.TransactionApi;
import com.example.minimaltravel.api.UserApi;
import com.example.minimaltravel.model.Transaction;
import com.example.minimaltravel.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private FloatingActionButton fabAddTransaction;
    private List<User> userList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
        initFab(view);
        fetchTransactions();
    }

    private void initRecyclerView(View view) {
        recyclerViewTransactions = view.findViewById(R.id.recycler_view_transactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionList = new ArrayList<>();

        adapter = new TransactionAdapter(transactionList, new TransactionAdapter.TransactionActionListener() {
            @Override
            public void onTransactionDelete(Transaction transaction) {
                deleteTransaction(transaction);
            }

//            @Override
//            public void onUserUpdate(User user, String newUsername, String newEmail) {
//                // Llama a la función para actualizar el usuario con los nuevos datos
//                updateUser(user, newUsername, newEmail);
//            }
        });
        recyclerViewTransactions.setAdapter(adapter);
    }

    private void initFab(View view) {
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);
        fabAddTransaction.setOnClickListener(v -> fetchUsersAndShowDialog());
    }

    private void fetchUsersAndShowDialog() {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        api.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    showAddTransactionDialog(); // Solo muestra el diálogo cuando userList ya está llena
                } else {
                    showToast("Error al cargar usuarios");
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                showToast("Error de conexión al cargar usuarios");
            }
        });
    }


    private void createNewTransaction(String description, double amount, String category,
                                      Long creditorUserId, List<Long> participantUserIds) {
        // 1. Calcular el importe por participante
        double amountPerParticipant = amount / participantUserIds.size();

        // 2. Crear lista de participantes para el request
        List<Transaction.Participant> participants = new ArrayList<>();
        for (Long userId : participantUserIds) {
                Transaction.Participant participant = new Transaction.Participant();
                participant.setUserId(userId);
                participant.setAmount(amountPerParticipant); // Importe que debe cada uno
                participants.add(participant);
        }

        // 3. Crear objeto Transaction con los datos
        Transaction transactionRequest = new Transaction();
        transactionRequest.setDescription(description);
        transactionRequest.setAmount(amount);
        transactionRequest.setCategory(category);
        transactionRequest.setCreditorUserId(creditorUserId);
        transactionRequest.setParticipants(participants);
        transactionRequest.setCreationDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        // 4. Llamar a la API
        TransactionApi api = ApiClient.getClient().create(TransactionApi.class);
        api.createTransaction(transactionRequest).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showToast("Gasto creado exitosamente");
                    fetchTransactions(); // Actualizar la lista
                } else {
                    showToast("Error al crear gasto");
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    private void showAddTransactionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nuevo gasto");

        // Infla el layout personalizado
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        // Referencias a los campos
        EditText etDescription = dialogView.findViewById(R.id.et_transaction_description);
        EditText etAmount = dialogView.findViewById(R.id.et_transaction_amount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_transaction_category);
        Spinner spinnerCreditor = dialogView.findViewById(R.id.spinner_creditor_user);
        LinearLayout layoutParticipants = dialogView.findViewById(R.id.layout_participants_checkboxes);

        // --- 1. Rellenar spinner de categorías (ejemplo) ---
        String[] categories = {"🍔 Comida", "🚌 Transporte", "🎉 Ocio","🎬 Cultura","👕 Ropa","🏨 Alojamiento", "🛒 Compras", "🏕️ Actividades", "🧩 Otros"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // --- 2. Rellenar spinner de usuarios (pagador) ---
        List<String> userNames = new ArrayList<>();
        for (User user : userList) userNames.add(user.getuserName());
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, userNames);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCreditor.setAdapter(userAdapter);

        // --- 3. Rellenar checkboxes de participantes ---
        layoutParticipants.removeAllViews();
        for (User user : userList) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(user.getuserName());
            checkBox.setTag(user.getUserId());
            layoutParticipants.addView(checkBox);
        }

        // --- 4. Configurar el botón Guardar ---
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String description = etDescription.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            int creditorPosition = spinnerCreditor.getSelectedItemPosition();
            Long creditorUserId = userList.get(creditorPosition).getUserId();

            // Recoger participantes seleccionados
            List<Long> selectedUserIds = new ArrayList<>();
            for (int i = 0; i < layoutParticipants.getChildCount(); i++) {
                View child = layoutParticipants.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    if (cb.isChecked()) {
                        selectedUserIds.add((Long) cb.getTag());
                    }
                }
            }

            // Validaciones básicas
            if (description.isEmpty() || amountStr.isEmpty() || selectedUserIds.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos y selecciona al menos un participante", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);

                // Validación: el importe debe ser positivo
                if (amount <= 0) {
                    showToast("El importe debe ser mayor que cero");
                    return;
                }

                // Llamar al método createNewTransaction
                createNewTransaction(
                        description,
                        amount,
                        category.replaceAll("^[^a-zA-ZáéíóúÁÉÍÓÚñÑ]+\\s*", ""),
                        creditorUserId,
                        selectedUserIds
                );

            } catch (NumberFormatException e) {
                showToast("Formato de importe inválido");
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void fetchTransactions() {
        TransactionApi api = ApiClient.getClient().create(TransactionApi.class);
        api.getAllTransactions().enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    transactionList.clear();
                    transactionList.addAll(response.body());
                    adapter.updateData(new ArrayList<>(transactionList));
                } else {
                    showToast("Error al cargar gastos");
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    private void deleteTransaction(Transaction transaction) {
        TransactionApi api = ApiClient.getClient().create(TransactionApi.class);
        api.deleteTransaction(transaction.getTransactionId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchTransactions();
                    showToast("Gasto eliminado");
                } else {
                    showToast("Error al eliminar gasto");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

        private void showToast(String message) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }



