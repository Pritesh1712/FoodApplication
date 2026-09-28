package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.GenericResponse;
import com.example.foodapplication.models.Restaurant;
import com.example.foodapplication.models.RestaurantListResponse;
import com.example.foodapplication.models.UserListResponse;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.LocalDatabaseManager;
import com.example.foodapplication.utils.OrderManager;
import com.example.foodapplication.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private ImageButton btnLogout;
    private Button btnViewUsers, btnViewShops;
    private TextView tvUsersCount, tvRestaurantsCount, tvOrdersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnLogout = findViewById(R.id.btnLogout);
        btnViewUsers = findViewById(R.id.btnViewUsers);
        btnViewShops = findViewById(R.id.btnViewShops);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvRestaurantsCount = findViewById(R.id.tvRestaurantsCount);
        tvOrdersCount = findViewById(R.id.tvOrdersCount);

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnViewUsers.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class)));
        btnViewShops.setOnClickListener(v -> showManageShopsDialog());

        loadAdminStats();
    }

    private void loadAdminStats() {
        tvRestaurantsCount.setText(String.valueOf(LocalDatabaseManager.getInstance(this).getShopsList().size()));
        tvUsersCount.setText(String.valueOf(LocalDatabaseManager.getInstance(this).getUsersList().size()));
        tvOrdersCount.setText(String.valueOf(OrderManager.getInstance(this).getLocalOrders().size()));

        ApiClient.getApiService(this).getRestaurants().enqueue(new Callback<RestaurantListResponse>() {
            @Override
            public void onResponse(@NonNull Call<RestaurantListResponse> call, @NonNull Response<RestaurantListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    tvRestaurantsCount.setText(String.valueOf(response.body().getData().size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestaurantListResponse> call, @NonNull Throwable t) {}
        });

        ApiClient.getApiService(this).getAdminUsers().enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserListResponse> call, @NonNull Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    tvUsersCount.setText(String.valueOf(response.body().getData().size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserListResponse> call, @NonNull Throwable t) {}
        });
    }

    private void showManageShopsDialog() {
        ApiClient.getApiService(this).getRestaurants().enqueue(new Callback<RestaurantListResponse>() {
            @Override
            public void onResponse(@NonNull Call<RestaurantListResponse> call, @NonNull Response<RestaurantListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    showShopsListDialog(response.body().getData());
                } else {
                    showShopsListDialog(LocalDatabaseManager.getInstance(AdminDashboardActivity.this).getShopsList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestaurantListResponse> call, @NonNull Throwable t) {
                showShopsListDialog(LocalDatabaseManager.getInstance(AdminDashboardActivity.this).getShopsList());
            }
        });
    }

    private void showShopsListDialog(List<Restaurant> shops) {
        if (shops == null || shops.isEmpty()) {
            Toast.makeText(this, "No shops available", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> shopNames = new ArrayList<>();
        for (Restaurant r : shops) {
            shopNames.add(r.getName() + " (" + r.getAddress() + ")");
        }

        String[] items = shopNames.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registered Shops (" + shops.size() + ")");
        builder.setItems(items, (dialog, which) -> {
            Restaurant selectedShop = shops.get(which);
            confirmDeleteShop(selectedShop);
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmDeleteShop(Restaurant shop) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete " + shop.getName() + "?");
        builder.setMessage("CAUTION: Deleting this shop will automatically delete ALL products, custom categories, and orders created for " + shop.getName() + "!");

        builder.setPositiveButton("Delete Everything", (dialog, which) -> {
            LocalDatabaseManager.getInstance(this).deleteShop(shop.getId());
            Toast.makeText(AdminDashboardActivity.this, "Shop " + shop.getName() + " and all products deleted!", Toast.LENGTH_LONG).show();
            loadAdminStats();

            ApiClient.getApiService(this).deleteRestaurant(shop.getId()).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {}

                @Override
                public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {}
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
