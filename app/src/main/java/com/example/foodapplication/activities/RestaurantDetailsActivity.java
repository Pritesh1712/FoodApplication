package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.FoodAdapter;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.Food;
import com.example.foodapplication.models.FoodListResponse;
import com.example.foodapplication.models.Restaurant;
import com.example.foodapplication.utils.CartManager;
import com.example.foodapplication.utils.ImageUtils;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.LocalDatabaseManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private ImageView ivHeader;
    private TextView tvRestaurantName, tvRestaurantDesc;
    private ImageButton btnBack;
    private RecyclerView rvMenu;
    private ExtendedFloatingActionButton fabCart;

    private Restaurant restaurant;
    private final List<Food> foodList = new ArrayList<>();
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        ivHeader = findViewById(R.id.ivHeader);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvRestaurantDesc = findViewById(R.id.tvRestaurantDesc);
        btnBack = findViewById(R.id.btnBack);
        rvMenu = findViewById(R.id.rvMenu);
        fabCart = findViewById(R.id.fabCart);

        restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");

        if (restaurant != null) {
            tvRestaurantName.setText(restaurant.getName());
            tvRestaurantDesc.setText(restaurant.getDescription());

            if (restaurant.getImage() != null && !restaurant.getImage().isEmpty()) {
                ImageUtils.loadImage(this, restaurant.getImage(), ivHeader);
            }
        }

        btnBack.setOnClickListener(v -> finish());
        fabCart.setOnClickListener(v -> startActivity(new Intent(RestaurantDetailsActivity.this, CartActivity.class)));

        setupRecyclerView();
        loadFoodMenuFromBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartFab();
    }

    private void updateCartFab() {
        int count = CartManager.getInstance().getItemCount();
        if (count > 0) {
            fabCart.setText("View Cart (" + count + ")");
            fabCart.show();
        } else {
            fabCart.hide();
        }
    }

    private void setupRecyclerView() {
        foodAdapter = new FoodAdapter(this, foodList, food -> {
            if (restaurant != null) {
                CartManager.getInstance().addItem(food, restaurant.getId(), restaurant.getName());
                Toast.makeText(this, food.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                updateCartFab();
            }
        });
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        rvMenu.setAdapter(foodAdapter);
    }

    private void loadFoodMenuFromBackend() {
        if (restaurant == null) return;

        ApiClient.getApiService(this).getFoodsByRestaurant(restaurant.getId()).enqueue(new Callback<FoodListResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodListResponse> call, @NonNull Response<FoodListResponse> response) {
                foodList.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    foodList.addAll(response.body().getData());
                } else {
                    foodList.addAll(LocalDatabaseManager.getInstance(RestaurantDetailsActivity.this).getFoodsForShop(restaurant.getId()));
                }
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<FoodListResponse> call, @NonNull Throwable t) {
                foodList.clear();
                foodList.addAll(LocalDatabaseManager.getInstance(RestaurantDetailsActivity.this).getFoodsForShop(restaurant.getId()));
                foodAdapter.notifyDataSetChanged();
            }
        });
    }
}
