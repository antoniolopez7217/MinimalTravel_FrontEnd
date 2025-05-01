package com.example.minimaltravel.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.model.Transaction;


import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private TransactionActionListener actionListener;

    public interface TransactionActionListener {
        void onTransactionDelete(Transaction transactions);
    }

    public TransactionAdapter(List<Transaction> transactions, TransactionAdapter.TransactionActionListener actionListener) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.textTransactionDescription.setText(transaction.getDescription());
        holder.textTransactionCategory.setText(
                new Object() {
                    String withIcon(String category) {
                        switch (category) {
                            case "Comida": return "🍔 Comida";
                            case "Transporte": return "🚌 Transporte";
                            case "Ocio": return "🎉 Ocio";
                            case "Ropa": return "👕 Ropa";
                            case "Cultura": return "🎬 Cultura";
                            case "Alojamiento": return "🏨 Alojamiento";
                            case "Compras": return "🛒 Compras";
                            case "Actividades": return "🏕️ Actividades";
                            case "Otros": return "🧩 Otros";
                            default: return category;
                        }
                    }
                }.withIcon(transaction.getCategory()));
        holder.textTransactionCreditor.setText(String.format("%s pagó  (📅%s)",
                transaction.getCreditorUserName(),
                transaction.getCreationDate()));

        List<Transaction.Participant> participants = transaction.getParticipants();
        List<String> participantNames = new ArrayList<>();
        if (participants != null) {
            for (Transaction.Participant p : participants) {
                if (p.getUserName() != null) {
                    participantNames.add(p.getUserName());
                }
            }
        }

        String participantNamesString = String.join(", ", participantNames);
        String message = "Participantes: " + participantNamesString;
        holder.textTransactionParticipants.setText(message);
        holder.textTransactionAmounts.setText(String.format("%s€  ", transaction.getAmount()));

        // Configurar clic en el botón de opciones (menú contextual)
       // holder.buttonMoreOptions.setOnClickListener(v ->
        //        showPopupMenu(v, user, position));

        holder.buttonAction.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar el gasto ?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Sí", (dialog, which) -> {
                        if (actionListener != null) {
                            actionListener.onTransactionDelete(transaction);
                        }
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = new ArrayList<>(newTransactions);
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textTransactionDescription, textTransactionCreditor, textTransactionCategory, textTransactionParticipants, textTransactionAmounts;
        ImageButton buttonAction, buttonMoreOptions;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            textTransactionDescription = itemView.findViewById(R.id.text_transaction_description);
            textTransactionCategory = itemView.findViewById(R.id.text_transaction_category);
            textTransactionCreditor = itemView.findViewById(R.id.text_transaction_creditor);
            textTransactionParticipants = itemView.findViewById(R.id.text_transaction_participants);
            textTransactionAmounts = itemView.findViewById(R.id.text_transaction_amount);
            buttonAction = itemView.findViewById(R.id.button_action_transaction);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }
    }
}
