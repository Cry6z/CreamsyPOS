package com.example.creamsy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTotal;
    private Button btnCheckout, btnBack, btnClearCart;
    private DatabaseHelper db;
    private CartAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewCart);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);
        btnClearCart = findViewById(R.id.btnClearCart);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartAdapter(db.getCartItems());
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnClearCart.setOnClickListener(v -> {
            // Restore stock for all items before clearing cart
            List<CartItem> cartItems = db.getCartItems();
            for (CartItem item : cartItems) {
                db.restoreProductStock(item.getProductId(), item.getQuantity());
            }
            db.clearCart();
            refreshList();
            Toast.makeText(this, "Keranjang dikosongkan", Toast.LENGTH_SHORT).show();
        });
        btnCheckout.setOnClickListener(v -> {
            double total = calculateTotal(adapter.getItems());
            if (total <= 0) {
                Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            db.addTransaction(total);
            db.clearCart();
            refreshList();
            Toast.makeText(this, "Transaksi berhasil", Toast.LENGTH_SHORT).show();
            finish();
        });

        updateTotal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        adapter.setItems(db.getCartItems());
        adapter.notifyDataSetChanged();
        updateTotal();
    }

    private void updateTotal() {
        double total = calculateTotal(adapter.getItems());
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        tvTotal.setText(nf.format(total));
    }

    private double calculateTotal(List<CartItem> items) {
        double sum = 0;
        if (items != null) {
            for (CartItem item : items) {
                sum += item.getTotalPrice();
            }
        }
        return sum;
    }

    class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
        private List<CartItem> items;

        CartAdapter(List<CartItem> items) {
            this.items = items;
        }

        public void setItems(List<CartItem> newItems) {
            this.items = newItems;
        }

        public List<CartItem> getItems() {
            return items;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            CartItem item = items.get(position);
            holder.tvName.setText(item.getProductName());
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            holder.tvPrice.setText(nf.format(item.getProductPrice()));
            holder.tvQty.setText(String.valueOf(item.getQuantity()));
            holder.tvSubtotal.setText("Subtotal: " + nf.format(item.getTotalPrice()));

            if (item.getProductImageUri() != null && !item.getProductImageUri().isEmpty()) {
                try {
                    holder.img.setImageURI(Uri.parse(item.getProductImageUri()));
                } catch (Exception e) {
                    holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.btnMinus.setOnClickListener(v -> {
                int q = item.getQuantity();
                if (q > 1) {
                    // Restore 1 stock when reducing quantity
                    db.restoreProductStock(item.getProductId(), 1);
                    db.updateCartItemQuantity(item.getId(), q - 1);
                    refreshList();
                }
            });
            holder.btnPlus.setOnClickListener(v -> {
                // Reduce 1 stock when increasing quantity
                if (db.reduceProductStock(item.getProductId(), 1)) {
                    db.updateCartItemQuantity(item.getId(), item.getQuantity() + 1);
                    refreshList();
                } else {
                    Toast.makeText(CartActivity.this, "Stok tidak mencukupi", Toast.LENGTH_SHORT).show();
                }
            });
            holder.btnRemove.setOnClickListener(v -> {
                // Restore all stock for this item when removing from cart
                db.restoreProductStock(item.getProductId(), item.getQuantity());
                db.removeFromCart(item.getId());
                refreshList();
                Toast.makeText(CartActivity.this, "Item dihapus dari keranjang", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        class CartViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView tvName, tvPrice, tvSubtotal, tvQty;
            Button btnMinus, btnPlus, btnRemove;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.imgCartItem);
                tvName = itemView.findViewById(R.id.tvCartName);
                tvPrice = itemView.findViewById(R.id.tvCartPrice);
                tvSubtotal = itemView.findViewById(R.id.tvCartSubtotal);
                tvQty = itemView.findViewById(R.id.tvQty);
                btnMinus = itemView.findViewById(R.id.btnMinus);
                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}
