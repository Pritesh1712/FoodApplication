package com.example.foodapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.AuthResponse;
import com.example.foodapplication.models.User;
import com.example.foodapplication.utils.Constants;
import com.example.foodapplication.utils.ImageUtils;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.LocalDatabaseManager;
import com.example.foodapplication.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private LinearLayout layoutShopDetails;
    private EditText etShopName, etShopAddress, etShopDesc;
    private ImageView ivShopImagePreview;
    private Button btnPickShopImage, btnRegister;
    private Spinner spinnerRole;
    private TextView tvLogin;

    private String selectedShopImageUriString = "";
    private ActivityResultLauncher<Intent> galleryLauncher;

    private final String[] roles = {Constants.ROLE_CUSTOMER, Constants.ROLE_RESTAURANT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);

        layoutShopDetails = findViewById(R.id.layoutShopDetails);
        etShopName = findViewById(R.id.etShopName);
        etShopAddress = findViewById(R.id.etShopAddress);
        etShopDesc = findViewById(R.id.etShopDesc);
        ivShopImagePreview = findViewById(R.id.ivShopImagePreview);
        btnPickShopImage = findViewById(R.id.btnPickShopImage);

        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            selectedShopImageUriString = ImageUtils.uriToBase64(RegisterActivity.this, selectedImageUri);
                            ivShopImagePreview.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        btnPickShopImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = roles[position];
                if (Constants.ROLE_RESTAURANT.equals(selected)) {
                    layoutShopDetails.setVisibility(View.VISIBLE);
                } else {
                    layoutShopDetails.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnRegister.setOnClickListener(v -> performRegister());

        tvLogin.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String selectedRole = spinnerRole.getSelectedItem().toString();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        String shopName = etShopName.getText().toString().trim();
        String shopAddress = etShopAddress.getText().toString().trim();
        String shopDesc = etShopDesc.getText().toString().trim();

        if (Constants.ROLE_RESTAURANT.equals(selectedRole)) {
            if (shopName.isEmpty()) {
                etShopName.setError("Shop name is required");
                return;
            }
            if (shopAddress.isEmpty()) {
                etShopAddress.setError("Shop address is required");
                return;
            }
        }

        Map<String, Object> registerPayload = new HashMap<>();
        registerPayload.put("name", name);
        registerPayload.put("email", email);
        registerPayload.put("password", password);
        registerPayload.put("phone", phone);
        registerPayload.put("role", selectedRole);

        if (Constants.ROLE_RESTAURANT.equals(selectedRole)) {
            registerPayload.put("shopName", shopName);
            registerPayload.put("shopAddress", shopAddress);
            registerPayload.put("shopDescription", shopDesc);
            registerPayload.put("shopImage", selectedShopImageUriString);
        }

        btnRegister.setEnabled(false);
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();

        ApiClient.getApiService(this).registerUserWithShop(registerPayload).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse authResponse = response.body();
                    User user = authResponse.getUser();
                    String token = authResponse.getToken();

                    SharedPrefManager.getInstance(RegisterActivity.this).saveUser(user, token);
                    Toast.makeText(RegisterActivity.this, "Account created! Welcome " + user.getName(), Toast.LENGTH_SHORT).show();

                    navigateToDashboard(user.getRole());
                } else {
                    performOfflineFallbackRegister(name, email, password, phone, selectedRole, shopName, shopAddress, shopDesc, selectedShopImageUriString);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                btnRegister.setEnabled(true);
                performOfflineFallbackRegister(name, email, password, phone, selectedRole, shopName, shopAddress, shopDesc, selectedShopImageUriString);
            }
        });
    }

    private void performOfflineFallbackRegister(String name, String email, String password, String phone, String role,
                                                String shopName, String shopAddress, String shopDesc, String shopImage) {
        User offlineUser = LocalDatabaseManager.getInstance(this).registerUser(name, email, password, phone, role, shopName, shopAddress, shopDesc, shopImage);
        if (offlineUser != null) {
            SharedPrefManager.getInstance(this).saveUser(offlineUser, "offline_token_123");
            Toast.makeText(this, "Account created! Welcome " + offlineUser.getName() + " (Offline Mode)", Toast.LENGTH_SHORT).show();
            navigateToDashboard(offlineUser.getRole());
        } else {
            Toast.makeText(this, "Registration failed. User may already exist!", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if (Constants.ROLE_ADMIN.equals(role)) {
            intent = new Intent(RegisterActivity.this, AdminDashboardActivity.class);
        } else if (Constants.ROLE_RESTAURANT.equals(role)) {
            intent = new Intent(RegisterActivity.this, RestaurantDashboardActivity.class);
        } else {
            intent = new Intent(RegisterActivity.this, CustomerHomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
