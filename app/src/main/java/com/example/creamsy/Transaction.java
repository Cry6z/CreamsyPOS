package com.example.creamsy;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction {
    private int transactionId;
    private double total;
    private long date;
    private String formattedDate;
    private String formattedTotal;

    public Transaction() {
    }

    public Transaction(int transactionId, double total, long date) {
        this.transactionId = transactionId;
        this.total = total;
        this.date = date;
        this.formattedDate = formatDate(date);
        this.formattedTotal = formatCurrency(total);
    }

    public Transaction(double total, long date) {
        this.total = total;
        this.date = date;
        this.formattedDate = formatDate(date);
        this.formattedTotal = formatCurrency(total);
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
        this.formattedTotal = formatCurrency(total);
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
        this.formattedDate = formatDate(date);
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }

    // Helper methods for formatting
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatCurrency(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return nf.format(amount);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", total=" + total +
                ", date=" + date +
                ", formattedDate='" + formattedDate + '\'' +
                ", formattedTotal='" + formattedTotal + '\'' +
                '}';
    }
}
