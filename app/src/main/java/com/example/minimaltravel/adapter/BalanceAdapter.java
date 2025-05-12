package com.example.minimaltravel.adapter;

import android.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minimaltravel.R;
import com.example.minimaltravel.model.Balance;
import com.example.minimaltravel.model.User;

import java.util.ArrayList;
import java.util.List;

public class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {

    private List<Balance> balances;
    private BalanceActionListener actionListener;

    public interface BalanceActionListener {
        void onBalanceSettle(Balance balance);
    }

    public BalanceAdapter(List<Balance> balances, BalanceActionListener actionListener) {
        this.balances = balances != null ? balances : new ArrayList<>();
        this.actionListener = actionListener;
    }


    @NonNull
    @Override
    public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_balance, parent, false);
        return new BalanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
        Balance balance = balances.get(position);

        holder.textBalanceDescription.setText(
                Html.fromHtml(
                        String.format("<b>%s</b> le debe a <b>%s</b>",
                                balance.getDebtorUserName(),
                                balance.getCreditorUserName()
                        ),
                        Html.FROM_HTML_MODE_LEGACY
                )
        );

        double amount = balance.getAmount();
        String amountText;
        if (amount == (long) amount) {
            amountText = String.format("%d€  ", (long) amount);
        } else {
            amountText = String.format("%.2f€  ", amount);
        }
        holder.textBalanceAmount.setText(amountText);

        holder.buttonMoreOptions.setOnClickListener(v ->
                showPopupMenu(v, balance, position));

    }

    private void showPopupMenu(View view, Balance balance, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.balance_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_settle) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Liquidar deuda")
                        .setMessage("¿Estás seguro de que quieres liquidar la deuda de "
                                + balance.getDebtorUserName()
                                + " con "
                                + balance.getCreditorUserName()
                                + " ("
                                + balance.getAmount()
                                + "€)?"
                        )
                        .setNegativeButton("No", null)
                        .setPositiveButton("Sí", (dialog, which) -> {
                            if (actionListener != null) {
                                actionListener.onBalanceSettle(balance);
                            }
                        })
                        .show();
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return balances != null ? balances.size() : 0;
    }

    public void updateData(List<Balance> newBalances) {
        this.balances = new ArrayList<>(newBalances);
        notifyDataSetChanged();
    }

    static class BalanceViewHolder extends RecyclerView.ViewHolder {
        TextView textBalanceDescription, textBalanceAmount;
        ImageButton buttonMoreOptions;

        public BalanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textBalanceDescription = itemView.findViewById(R.id.text_balance_description);
            textBalanceAmount = itemView.findViewById(R.id.text_balance_amount);
            buttonMoreOptions = itemView.findViewById(R.id.button_more_options);
        }
    }
}
