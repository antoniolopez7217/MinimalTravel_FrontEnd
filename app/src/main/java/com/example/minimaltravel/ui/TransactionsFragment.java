package com.example.minimaltravel.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.adapter.TransactionAdapter;
import com.example.minimaltravel.api.ApiClient;
import com.example.minimaltravel.api.TransactionApi;
import com.example.minimaltravel.model.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
   // private FloatingActionButton fabAddTransaction;

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
       // initFab(view);
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



