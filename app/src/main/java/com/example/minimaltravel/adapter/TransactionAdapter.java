package com.example.minimaltravel.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
        void onTransactionEdit(Transaction transaction);
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
                            case "Actividades": return "🏕️ Actividades";
                            case "Alojamiento": return "🏨 Alojamiento";
                            case "Comida": return "🍔 Comida";
                            case "Compras": return "🛒 Compras";
                            case "Cultura": return "🎬 Cultura";
                            case "Ocio": return "🎉 Ocio";
                            case "Ropa": return "👕 Ropa";
                            case "Transporte": return "🚌 Transporte";
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

        holder.buttonMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.options_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    actionListener.onTransactionEdit(transaction);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Estás seguro de que quieres eliminar el gasto?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Sí", (dialog, which) -> {
                                if (actionListener != null) {
                                    actionListener.onTransactionDelete(transaction);
                                }
                            })
                            .show();
                    return true;
                }
                return false;
            });
            popup.show();
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
        ImageButton buttonMoreOptions;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            textTransactionDescription = itemView.findViewById(R.id.text_transaction_description);
            textTransactionCategory = itemView.findViewById(R.id.text_transaction_category);
            textTransactionCreditor = itemView.findViewById(R.id.text_transaction_creditor);
            textTransactionParticipants = itemView.findViewById(R.id.text_transaction_participants);
            textTransactionAmounts = itemView.findViewById(R.id.text_transaction_amount);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }
    }
}
