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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ProductAdapter.ProductActionListener {

    private RecyclerView recyclerView;
    private Button btnAddProduct, btnCart, btnHistory, btnLogout;
    private SupabaseRepository repository;
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

        repository = new SupabaseRepository(this);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnCart = findViewById(R.id.btnCart);
        btnHistory = findViewById(R.id.btnHistory);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with empty list first, then load data
        adapter = new ProductAdapter(this, new ArrayList<>(), this);
        loadProducts();
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
        loadProducts();
    }
    
    private void loadProducts() {
        repository.getAllProducts().thenAccept(products -> {
            runOnUiThread(() -> {
                adapter.updateData(products);
            });
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error loading products: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }

    private void updateCartButtonCount() {
        repository.getCartItems().thenAccept(cartItems -> {
            runOnUiThread(() -> {
                btnCart.setText("🛒 Keranjang (" + cartItems.size() + ")");
            });
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                btnCart.setText("🛒 Keranjang (0)");
            });
            return null;
        });
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
                repository.addProduct(p).thenAccept(product -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Produk ditambahkan", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshList();
                    });
                }).exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error adding product: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
            } else {
                toEdit.setNama(name);
                toEdit.setHarga(price);
                toEdit.setStok(stock);
                toEdit.setFotoUri(tempSelectedImageUri);
                repository.updateProduct(toEdit).thenAccept(product -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Produk diperbarui", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshList();
                    });
                }).exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error updating product: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
            }
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
        repository.reduceProductStock(product.getId(), 1).thenAccept(success -> {
            if (success) {
                repository.addToCart(product.getId(), 1).thenAccept(result -> {
                    runOnUiThread(() -> {
                        updateCartButtonCount();
                        refreshList(); // Refresh to show updated stock
                        Toast.makeText(MainActivity.this, "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                    });
                }).exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error adding to cart: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Gagal mengurangi stok", Toast.LENGTH_SHORT).show();
                });
            }
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error reducing stock: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return null;
        });
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
                    repository.deleteProduct(product.getId()).thenAccept(success -> {
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(MainActivity.this, "Produk dihapus", Toast.LENGTH_SHORT).show();
                                refreshList();
                            } else {
                                Toast.makeText(MainActivity.this, "Gagal menghapus produk", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).exceptionally(throwable -> {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Error deleting product: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        return null;
                    });
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
