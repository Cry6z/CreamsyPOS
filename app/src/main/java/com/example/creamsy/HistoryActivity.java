package com.example.creamsy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.HistoryActionListener {

    private RecyclerView recyclerViewHistory;
    private LinearLayout layoutEmptyState;
    private TextView tvTransactionCount, tvTotalRevenue;
    private Button btnBack, btnClearHistory;
    private SupabaseRepository repository;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize repository
        repository = new SupabaseRepository(this);

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup button listeners
        setupButtonListeners();

        // Load data
        loadHistoryData();
    }

    private void initViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        btnBack = findViewById(R.id.btnBack);
        btnClearHistory = findViewById(R.id.btnClearHistory);
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, new ArrayList<>(), this);
        recyclerViewHistory.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Semua Riwayat")
                    .setMessage("Apakah Anda yakin ingin menghapus semua riwayat transaksi? Tindakan ini tidak dapat dibatalkan.")
                    .setPositiveButton("Hapus Semua", (dialog, which) -> {
                        // For now, just show a message - full implementation would need clear all transactions API
                        Toast.makeText(this, "Fitur hapus semua riwayat dalam pengembangan", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void loadHistoryData() {
        repository.getAllTransactions().thenAccept(transactions -> {
            runOnUiThread(() -> {
                try {
                    // Ensure transactions is not null
                    List<Transaction> safeTransactions = transactions;
                    if (safeTransactions == null) {
                        safeTransactions = new ArrayList<>();
                    }
                    
                    // Update RecyclerView
                    adapter.updateData(safeTransactions);
                    
                    // Update statistics
                    updateStatistics(safeTransactions);
                    
                    // Show/hide empty state
                    if (safeTransactions.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        recyclerViewHistory.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        recyclerViewHistory.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error displaying history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                // Show empty state on error
                layoutEmptyState.setVisibility(View.VISIBLE);
                recyclerViewHistory.setVisibility(View.GONE);
                Toast.makeText(this, "Error loading history: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }

    private void updateStatistics(List<Transaction> transactions) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        
        int transactionCount = transactions.size();
        double totalRevenue = 0;
        
        for (Transaction transaction : transactions) {
            if (transaction != null) {
                totalRevenue += transaction.getTotal();
            }
        }
        
        tvTransactionCount.setText(String.valueOf(transactionCount));
        
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        tvTotalRevenue.setText(nf.format(totalRevenue));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData();
    }

    // HistoryAdapter.HistoryActionListener implementation
    @Override
    public void onDeleteTransaction(Transaction transaction) {
        // For now, just show a message - full implementation would need delete transaction API
        Toast.makeText(this, "Fitur hapus transaksi dalam pengembangan", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTransactionDeleted() {
        loadHistoryData();
    }
}
