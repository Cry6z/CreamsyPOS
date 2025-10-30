package com.example.creamsy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private DatabaseHelper db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database and shared preferences
        db = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CreamyLogin", MODE_PRIVATE);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMain();
            return;
        }

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Set login button click listener
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void attemptLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate input
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username wajib diisi");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password wajib diisi");
            etPassword.requestFocus();
            return;
        }

        // Authenticate user
        User user = db.authenticateUser(username, password);
        if (user != null) {
            // Login successful
            saveLoginSession(user);
            Toast.makeText(this, "Login berhasil! Selamat datang, " + user.getFullName(), Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            // Login failed
            Toast.makeText(this, "Username atau password salah", Toast.LENGTH_SHORT).show();
            etPassword.setText("");
            etPassword.requestFocus();
        }
    }

    private void saveLoginSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", user.getId());
        editor.putString("username", user.getUsername());
        editor.putString("fullName", user.getFullName());
        editor.apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back from login screen
        moveTaskToBack(true);
    }
}
