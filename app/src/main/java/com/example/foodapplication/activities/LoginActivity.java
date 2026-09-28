package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.AuthResponse;
import com.example.foodapplication.models.User;
import com.example.foodapplication.utils.Constants;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.LocalDatabaseManager;
import com.example.foodapplication.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Spinner spinnerRole;
    private Button btnLogin;
    private TextView tvRegister;

    private final String[] roles = {Constants.ROLE_CUSTOMER, Constants.ROLE_RESTAURANT, Constants.ROLE_ADMIN};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnLogin.setOnClickListener(v -> performLogin());

        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String selectedRole = spinnerRole.getSelectedItem().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        btnLogin.setEnabled(false);
        Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();

        ApiClient.getApiService(this).loginUser(credentials).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse authResponse = response.body();
                    User user = authResponse.getUser();
                    String token = authResponse.getToken();

                    SharedPrefManager.getInstance(LoginActivity.this).saveUser(user, token);
                    Toast.makeText(LoginActivity.this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();

                    navigateToDashboard(user.getRole());
                } else {
                    performOfflineFallbackLogin(email, password, selectedRole);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                btnLogin.setEnabled(true);
                performOfflineFallbackLogin(email, password, selectedRole);
            }
        });
    }

    private void performOfflineFallbackLogin(String email, String password, String selectedRole) {
        User offlineUser = LocalDatabaseManager.getInstance(this).authenticateUser(email, password, selectedRole);
        if (offlineUser != null) {
            SharedPrefManager.getInstance(this).saveUser(offlineUser, "offline_token_123");
            Toast.makeText(this, "Welcome " + offlineUser.getName() + " (Offline Mode)", Toast.LENGTH_SHORT).show();
            navigateToDashboard(offlineUser.getRole());
        } else {
            Toast.makeText(this, "Invalid email or password. Please Sign Up first!", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if (Constants.ROLE_ADMIN.equals(role)) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else if (Constants.ROLE_RESTAURANT.equals(role)) {
            intent = new Intent(LoginActivity.this, RestaurantDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
