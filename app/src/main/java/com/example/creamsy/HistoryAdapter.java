package com.example.creamsy;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface HistoryActionListener {
        void onDeleteTransaction(Transaction transaction);
        void onTransactionDeleted(); // Callback to refresh the list
    }

    private final Context context;
    private List<Transaction> transactions;
    private final HistoryActionListener listener;

    public HistoryAdapter(Context context, List<Transaction> transactions, HistoryActionListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        if (transactions == null || position >= transactions.size()) return;
        Transaction transaction = transactions.get(position);
        
        // Format transaction ID with leading zeros
        String formattedId = String.format("%03d", transaction.getTransactionId());
        
        // Create styled text for transaction title
        String fullText = "Transaksi #" + formattedId;
        SpannableString spannableString = new SpannableString(fullText);
        
        // Apply orange color to the ID part
        int startIndex = fullText.indexOf("#") + 1;
        int endIndex = fullText.length();
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF6B35")), 
                               startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        holder.tvTransactionTitle.setText(spannableString);
        
        holder.tvTransactionDate.setText(transaction.getFormattedDate());
        holder.tvTransactionTotal.setText(transaction.getFormattedTotal());

        // Set delete button click listener
        holder.btnDeleteTransaction.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Transaksi")
                    .setMessage("Apakah Anda yakin ingin menghapus transaksi #" + formattedId + "?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        if (listener != null) {
                            listener.onDeleteTransaction(transaction);
                            Toast.makeText(context, "Transaksi #" + formattedId + " telah dihapus", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // Optional: Add click listener for the entire item to show transaction details
        holder.itemView.setOnClickListener(v -> {
            // You can add a detail view here in the future
            Toast.makeText(context, "Transaksi #" + formattedId + " - " + transaction.getFormattedTotal(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionTitle, tvTransactionDate, tvTransactionTotal;
        Button btnDeleteTransaction;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionTotal = itemView.findViewById(R.id.tvTransactionTotal);
            btnDeleteTransaction = itemView.findViewById(R.id.btnDeleteTransaction);
        }
    }
}
