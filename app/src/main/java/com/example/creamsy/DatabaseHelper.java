package com.example.creamsy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "creamsy.db";
    private static final int DATABASE_VERSION = 2;

    // Tabel Produk
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAMA = "nama";
    private static final String COLUMN_HARGA = "harga";
    private static final String COLUMN_STOK = "stok";
    private static final String COLUMN_FOTO_URI = "foto_uri";

    // Tabel Keranjang
    private static final String TABLE_CART = "cart";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_QUANTITY = "quantity";

    // Tabel Transaksi
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "transaction_id";
    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_DATE = "date";

    // Tabel Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME = "full_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Products table
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAMA + " TEXT NOT NULL,"
                + COLUMN_HARGA + " REAL NOT NULL,"
                + COLUMN_STOK + " INTEGER NOT NULL,"
                + COLUMN_FOTO_URI + " TEXT"
                + ")";
        db.execSQL(createProductsTable);

        // Create Cart table
        String createCartTable = "CREATE TABLE " + TABLE_CART + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PRODUCT_ID + " INTEGER,"
                + COLUMN_QUANTITY + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(createCartTable);

        // Create Transactions table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TOTAL + " REAL NOT NULL,"
                + COLUMN_DATE + " TEXT NOT NULL"
                + ")";
        db.execSQL(createTransactionsTable);

        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_FULL_NAME + " TEXT NOT NULL"
                + ")";
        db.execSQL(createUsersTable);

        // Insert default admin user
        ContentValues adminUser = new ContentValues();
        adminUser.put(COLUMN_USERNAME, "admin");
        adminUser.put(COLUMN_PASSWORD, "admin123");
        adminUser.put(COLUMN_FULL_NAME, "Administrator");
        db.insert(TABLE_USERS, null, adminUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // CRUD Operations untuk Products
    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, product.getNama());
        values.put(COLUMN_HARGA, product.getHarga());
        values.put(COLUMN_STOK, product.getStok());
        values.put(COLUMN_FOTO_URI, product.getFotoUri());

        long id = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return id;
    }

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(0));
                product.setNama(cursor.getString(1));
                product.setHarga(cursor.getDouble(2));
                product.setStok(cursor.getInt(3));
                product.setFotoUri(cursor.getString(4));
                productList.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return productList;
    }

    public Product getProduct(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, new String[]{COLUMN_ID, COLUMN_NAMA, COLUMN_HARGA, COLUMN_STOK, COLUMN_FOTO_URI},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Product product = new Product(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getInt(3), cursor.getString(4));
        cursor.close();
        db.close();
        return product;
    }

    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, product.getNama());
        values.put(COLUMN_HARGA, product.getHarga());
        values.put(COLUMN_STOK, product.getStok());
        values.put(COLUMN_FOTO_URI, product.getFotoUri());

        int result = db.update(TABLE_PRODUCTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(product.getId())});
        db.close();
        return result;
    }

    public void deleteProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS, COLUMN_ID + " = ?", new String[]{String.valueOf(product.getId())});
        db.close();
    }

    // Cart Operations
    public long addToCart(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check if product already in cart
        Cursor cursor = db.query(TABLE_CART, null, COLUMN_PRODUCT_ID + "=?", 
                new String[]{String.valueOf(productId)}, null, null, null);
        
        if (cursor.getCount() > 0) {
            // Update quantity
            cursor.moveToFirst();
            int currentQuantity = cursor.getInt(2); // quantity is at index 2
            ContentValues values = new ContentValues();
            values.put(COLUMN_QUANTITY, currentQuantity + quantity);
            cursor.close();
            db.update(TABLE_CART, values, COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(productId)});
            db.close();
            return 1;
        } else {
            // Add new item
            ContentValues values = new ContentValues();
            values.put(COLUMN_PRODUCT_ID, productId);
            values.put(COLUMN_QUANTITY, quantity);
            cursor.close();
            long id = db.insert(TABLE_CART, null, values);
            db.close();
            return id;
        }
    }

    public List<CartItem> getCartItems() {
        List<CartItem> cartItems = new ArrayList<>();
        String query = "SELECT c." + COLUMN_ID + ", c." + COLUMN_PRODUCT_ID + ", c." + COLUMN_QUANTITY + 
                      ", p." + COLUMN_NAMA + ", p." + COLUMN_HARGA + ", p." + COLUMN_FOTO_URI +
                      " FROM " + TABLE_CART + " c INNER JOIN " + TABLE_PRODUCTS + " p ON c." + COLUMN_PRODUCT_ID + " = p." + COLUMN_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setId(cursor.getInt(0));
                item.setProductId(cursor.getInt(1));
                item.setQuantity(cursor.getInt(2));
                item.setProductName(cursor.getString(3));
                item.setProductPrice(cursor.getDouble(4));
                item.setProductImageUri(cursor.getString(5));
                cartItems.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return cartItems;
    }

    public void updateCartItemQuantity(int cartId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUANTITY, quantity);
        db.update(TABLE_CART, values, COLUMN_ID + "=?", new String[]{String.valueOf(cartId)});
        db.close();
    }

    public void removeFromCart(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_ID + "=?", new String[]{String.valueOf(cartId)});
        db.close();
    }

    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, null, null);
        db.close();
    }

    // Transaction Operations
    public long addTransaction(double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TOTAL, total);
        values.put(COLUMN_DATE, System.currentTimeMillis());

        long id = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return id;
    }

    public int getCartItemCount() {
        String countQuery = "SELECT * FROM " + TABLE_CART;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    // History/Transaction History Operations
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY " + COLUMN_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(cursor.getInt(0)); // COLUMN_TRANSACTION_ID
                transaction.setTotal(cursor.getDouble(1)); // COLUMN_TOTAL
                transaction.setDate(cursor.getLong(2)); // COLUMN_DATE
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactionList;
    }

    public Transaction getTransaction(int transactionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, 
                new String[]{COLUMN_TRANSACTION_ID, COLUMN_TOTAL, COLUMN_DATE},
                COLUMN_TRANSACTION_ID + "=?", 
                new String[]{String.valueOf(transactionId)}, 
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Transaction transaction = new Transaction(
                    cursor.getInt(0), // COLUMN_TRANSACTION_ID
                    cursor.getDouble(1), // COLUMN_TOTAL
                    cursor.getLong(2) // COLUMN_DATE
            );
            cursor.close();
            db.close();
            return transaction;
        }
        
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    public void deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COLUMN_TRANSACTION_ID + " = ?", 
                new String[]{String.valueOf(transactionId)});
        db.close();
    }

    public void clearAllTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, null, null);
        db.close();
    }

    public int getTransactionCount() {
        String countQuery = "SELECT * FROM " + TABLE_TRANSACTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public double getTotalRevenue() {
        double totalRevenue = 0;
        String query = "SELECT SUM(" + COLUMN_TOTAL + ") FROM " + TABLE_TRANSACTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            totalRevenue = cursor.getDouble(0);
        }
        
        cursor.close();
        db.close();
        return totalRevenue;
    }

    // User Authentication Operations
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_FULL_NAME},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?", 
                new String[]{username, password}, 
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0), // COLUMN_ID
                    cursor.getString(1), // COLUMN_USERNAME
                    cursor.getString(2), // COLUMN_PASSWORD
                    cursor.getString(3) // COLUMN_FULL_NAME
            );
            cursor.close();
            db.close();
            return user;
        }
        
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_FULL_NAME, user.getFullName());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?", 
                new String[]{username}, 
                null, null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Method to reduce product stock
    public boolean reduceProductStock(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // First check current stock
        Cursor cursor = db.query(TABLE_PRODUCTS, new String[]{COLUMN_STOK},
                COLUMN_ID + "=?", new String[]{String.valueOf(productId)}, 
                null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            int currentStock = cursor.getInt(0);
            cursor.close();
            
            if (currentStock >= quantity) {
                // Reduce stock
                ContentValues values = new ContentValues();
                values.put(COLUMN_STOK, currentStock - quantity);
                int result = db.update(TABLE_PRODUCTS, values, COLUMN_ID + " = ?", 
                        new String[]{String.valueOf(productId)});
                db.close();
                return result > 0;
            }
        }
        
        if (cursor != null) cursor.close();
        db.close();
        return false;
    }

    // Method to restore product stock (for cart removal)
    public void restoreProductStock(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Get current stock
        Cursor cursor = db.query(TABLE_PRODUCTS, new String[]{COLUMN_STOK},
                COLUMN_ID + "=?", new String[]{String.valueOf(productId)}, 
                null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            int currentStock = cursor.getInt(0);
            cursor.close();
            
            // Restore stock
            ContentValues values = new ContentValues();
            values.put(COLUMN_STOK, currentStock + quantity);
            db.update(TABLE_PRODUCTS, values, COLUMN_ID + " = ?", 
                    new String[]{String.valueOf(productId)});
        }
        
        if (cursor != null) cursor.close();
        db.close();
    }
}
