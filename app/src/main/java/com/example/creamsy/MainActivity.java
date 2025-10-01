package com.example.creamsy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ProductAdapter.ProductActionListener {

    private RecyclerView recyclerView;
    private Button btnAddProduct, btnCart, btnHistory, btnLogout;
    private DatabaseHelper db;
    private ProductAdapter adapter;
    private SharedPreferences sharedPreferences;
    private TextView tvWelcome;

    private String tempSelectedImageUri = null;
    private ImageView dialogImagePreview; // diset saat dialog dibuat

    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    // Persist permission agar URI tetap bisa diakses
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    tempSelectedImageUri = uri.toString();
                    if (dialogImagePreview != null) {
                        dialogImagePreview.setImageURI(uri);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user is logged in
        sharedPreferences = getSharedPreferences("CreamyLogin", MODE_PRIVATE);
        if (!isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        db = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnCart = findViewById(R.id.btnCart);
        btnHistory = findViewById(R.id.btnHistory);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(this, db.getAllProducts(), this);
        recyclerView.setAdapter(adapter);

        btnAddProduct.setOnClickListener(v -> showAddOrEditDialog(null));
        btnCart.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CartActivity.class));
        });
        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Set welcome message
        String fullName = sharedPreferences.getString("fullName", "User");
        tvWelcome.setText("Selamat datang, " + fullName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
        updateCartButtonCount();
    }

    private void refreshList() {
        List<Product> products = db.getAllProducts();
        adapter.updateData(products);
    }

    private void updateCartButtonCount() {
        int count = db.getCartItemCount();
        btnCart.setText("🛒 Keranjang (" + count + ")");
    }

    private void showAddOrEditDialog(@Nullable Product toEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null, false);

        dialogImagePreview = view.findViewById(R.id.imgPreview);
        TextInputEditText etName = view.findViewById(R.id.etProductName);
        TextInputEditText etPrice = view.findViewById(R.id.etProductPrice);
        TextInputEditText etStock = view.findViewById(R.id.etProductStock);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        if (toEdit != null) {
            builder.setTitle("Edit Produk");
            etName.setText(toEdit.getNama());
            etPrice.setText(String.valueOf(toEdit.getHarga()));
            etStock.setText(String.valueOf(toEdit.getStok()));
            if (!TextUtils.isEmpty(toEdit.getFotoUri())) {
                try {
                    dialogImagePreview.setImageURI(Uri.parse(toEdit.getFotoUri()));
                } catch (Exception ignored) {}
            }
            tempSelectedImageUri = toEdit.getFotoUri();
        } else {
            builder.setTitle("Tambah Produk");
            tempSelectedImageUri = null;
        }

        btnSelectImage.setOnClickListener(v -> {
            try {
                pickImageLauncher.launch(new String[]{"image/*"});
            } catch (Exception e) {
                Toast.makeText(this, "Gagal membuka galeri", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.setView(view).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "0";
            String stockStr = etStock.getText() != null ? etStock.getText().toString().trim() : "0";

            if (name.isEmpty()) {
                etName.setError("Nama wajib");
                return;
            }

            double price;
            int stock;
            try {
                price = Double.parseDouble(priceStr);
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga/Stok tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (toEdit == null) {
                Product p = new Product(name, price, stock, tempSelectedImageUri);
                db.addProduct(p);
                Toast.makeText(this, "Produk ditambahkan", Toast.LENGTH_SHORT).show();
            } else {
                toEdit.setNama(name);
                toEdit.setHarga(price);
                toEdit.setStok(stock);
                toEdit.setFotoUri(tempSelectedImageUri);
                db.updateProduct(toEdit);
                Toast.makeText(this, "Produk diperbarui", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            refreshList();
        });

        dialog.show();
    }

    @Override
    public void onAddToCart(Product product) {
        if (product.getStok() <= 0) {
            Toast.makeText(this, "Stok habis", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Reduce stock first
        if (db.reduceProductStock(product.getId(), 1)) {
            db.addToCart(product.getId(), 1);
            updateCartButtonCount();
            refreshList(); // Refresh to show updated stock
            Toast.makeText(this, "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal mengurangi stok", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEdit(Product product) {
        showAddOrEditDialog(product);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setMessage("Hapus produk ini?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    db.deleteProduct(product);
                    refreshList();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> logout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }
}
