package com.example.minimaltravel.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.adapter.BalanceAdapter;
import com.example.minimaltravel.api.ApiClient;
import com.example.minimaltravel.api.BalanceApi;
import com.example.minimaltravel.api.TransactionApi;
import com.example.minimaltravel.model.Balance;
import com.example.minimaltravel.model.Transaction;


import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BalancesFragment extends Fragment {

    private RecyclerView recyclerViewBalances;
    private BalanceAdapter adapter;
    private List<Balance> balanceList;

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
        return inflater.inflate(R.layout.fragment_balance, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
        fetchBalances();
    }

    private void initRecyclerView(View view) {
        recyclerViewBalances = view.findViewById(R.id.recycler_view_balances);
        recyclerViewBalances.setLayoutManager(new LinearLayoutManager(requireContext()));
        balanceList = new ArrayList<>();

        adapter = new BalanceAdapter(balanceList, new BalanceAdapter.BalanceActionListener() {
            @Override
            public void onBalanceSettle(Balance balance) {
                settleBalance(balance);
            }
        });
        recyclerViewBalances.setAdapter(adapter);
    }

    private void settleBalance(Balance balance) {

        List<Transaction.Participant> participants = new ArrayList<>();
        Transaction.Participant participant = new Transaction.Participant();
        participant.setUserId(balance.getCreditorUserId());
        participant.setAmount(balance.getAmount());
        participants.add(participant);

        Transaction transactionRequest = new Transaction();
        transactionRequest.setDescription("Pago deudas");
        transactionRequest.setAmount(balance.getAmount());
        transactionRequest.setCategory("Liquidación deudas");
        transactionRequest.setCreditorUserId(balance.getDebtorUserId());
        transactionRequest.setParticipants(participants);
        transactionRequest.setCreationDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        TransactionApi api = ApiClient.getClient().create(TransactionApi.class);
        api.createTransaction(transactionRequest).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showToast("Deuda liquidada exitosamente");
                    fetchBalances();
                } else {
                    showToast("Error al liquidar deuda");
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }


    private void fetchBalances() {
        BalanceApi api = ApiClient.getClient().create(BalanceApi.class);
        api.getAllBalances().enqueue(new Callback<List<Balance>>() {
            @Override
            public void onResponse(Call<List<Balance>> call, Response<List<Balance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    balanceList.clear();
                    balanceList.addAll(response.body());
                    adapter.updateData(new ArrayList<>(balanceList)); // Actualiza el adaptador
                } else {
                    showToast("Error al cargar usuarios");
                }
            }

            @Override
            public void onFailure(Call<List<Balance>> call, Throwable t) {
                showToast("Error de conexión");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}


