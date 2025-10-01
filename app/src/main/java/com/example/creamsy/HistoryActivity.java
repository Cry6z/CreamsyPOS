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
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.HistoryActionListener {

    private RecyclerView recyclerViewHistory;
    private LinearLayout layoutEmptyState;
    private TextView tvTransactionCount, tvTotalRevenue;
    private Button btnBack, btnClearHistory;
    private DatabaseHelper db;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize database
        db = new DatabaseHelper(this);

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
        adapter = new HistoryAdapter(this, db.getAllTransactions(), this);
        recyclerViewHistory.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearHistory.setOnClickListener(v -> {
            List<Transaction> transactions = db.getAllTransactions();
            if (transactions.isEmpty()) {
                Toast.makeText(this, "Tidak ada riwayat untuk dihapus", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Hapus Semua Riwayat")
                    .setMessage("Apakah Anda yakin ingin menghapus semua riwayat transaksi? Tindakan ini tidak dapat dibatalkan.")
                    .setPositiveButton("Hapus Semua", (dialog, which) -> {
                        db.clearAllTransactions();
                        loadHistoryData();
                        Toast.makeText(this, "Semua riwayat transaksi telah dihapus", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void loadHistoryData() {
        List<Transaction> transactions = db.getAllTransactions();
        
        // Update statistics
        updateStatistics();
        
        // Update RecyclerView
        adapter.updateData(transactions);
        
        // Show/hide empty state
        if (transactions.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        int transactionCount = db.getTransactionCount();
        double totalRevenue = db.getTotalRevenue();
        
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
        db.deleteTransaction(transaction.getTransactionId());
        onTransactionDeleted();
    }

    @Override
    public void onTransactionDeleted() {
        loadHistoryData();
    }
}
