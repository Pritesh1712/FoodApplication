package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.CategoryAdapter;
import com.example.foodapplication.adapters.RestaurantAdapter;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.Category;
import com.example.foodapplication.models.CategoryListResponse;
import com.example.foodapplication.models.Food;
import com.example.foodapplication.models.Restaurant;
import com.example.foodapplication.models.RestaurantListResponse;
import com.example.foodapplication.models.User;
import com.example.foodapplication.utils.CartManager;
import com.example.foodapplication.utils.CategoryManager;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.LocalDatabaseManager;
import com.example.foodapplication.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerHomeActivity extends AppCompatActivity {

    private TextView tvWelcomeUser;
    private EditText etSearch;
    private ImageButton btnOrders, btnProfile;
    private RecyclerView rvCategories, rvRestaurants;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton fabCart;

    private final List<Category> categoryList = new ArrayList<>();
    private final List<Restaurant> restaurantList = new ArrayList<>();
    private final List<Restaurant> filteredRestaurantList = new ArrayList<>();

    private CategoryAdapter categoryAdapter;
    private RestaurantAdapter restaurantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        etSearch = findViewById(R.id.etSearch);
        btnOrders = findViewById(R.id.btnOrders);
        btnProfile = findViewById(R.id.btnProfile);
        rvCategories = findViewById(R.id.rvCategories);
        rvRestaurants = findViewById(R.id.rvRestaurants);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        fabCart = findViewById(R.id.fabCart);

        User currentUser = SharedPrefManager.getInstance(this).getUser();
        if (currentUser != null) {
            tvWelcomeUser.setText("Hi, " + currentUser.getName() + " 👋");
        }

        setupRecyclerViews();
        setupListeners();
        loadDataFromBackend();
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

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this, categoryList, category -> filterByCategory(category.getName()));
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        restaurantAdapter = new RestaurantAdapter(this, filteredRestaurantList, restaurant -> {
            Intent intent = new Intent(CustomerHomeActivity.this, RestaurantDetailsActivity.class);
            intent.putExtra("restaurant", restaurant);
            startActivity(intent);
        });
        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        rvRestaurants.setAdapter(restaurantAdapter);
    }

    private void setupListeners() {
        fabCart.setOnClickListener(v -> startActivity(new Intent(CustomerHomeActivity.this, CartActivity.class)));
        btnOrders.setOnClickListener(v -> startActivity(new Intent(CustomerHomeActivity.this, OrderHistoryActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(CustomerHomeActivity.this, ProfileActivity.class)));

        swipeRefresh.setOnRefreshListener(() -> {
            loadDataFromBackend();
            swipeRefresh.setRefreshing(false);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRestaurants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDataFromBackend() {
        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService(this).getCategories().enqueue(new Callback<CategoryListResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryListResponse> call, @NonNull Response<CategoryListResponse> response) {
                categoryList.clear();
                categoryList.add(new Category("0", "All", ""));
                categoryList.addAll(CategoryManager.getInstance(CustomerHomeActivity.this).getLocalCategories());
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Category remote : response.body().getData()) {
                        boolean exists = false;
                        for (Category local : categoryList) {
                            if (local.getName().equalsIgnoreCase(remote.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) categoryList.add(remote);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<CategoryListResponse> call, @NonNull Throwable t) {
                categoryList.clear();
                categoryList.add(new Category("0", "All", ""));
                categoryList.addAll(CategoryManager.getInstance(CustomerHomeActivity.this).getLocalCategories());
                categoryAdapter.notifyDataSetChanged();
            }
        });

        ApiClient.getApiService(this).getRestaurants().enqueue(new Callback<RestaurantListResponse>() {
            @Override
            public void onResponse(@NonNull Call<RestaurantListResponse> call, @NonNull Response<RestaurantListResponse> response) {
                progressBar.setVisibility(View.GONE);
                restaurantList.clear();
                filteredRestaurantList.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    restaurantList.addAll(response.body().getData());
                } else {
                    restaurantList.addAll(LocalDatabaseManager.getInstance(CustomerHomeActivity.this).getShopsList());
                }
                filteredRestaurantList.addAll(restaurantList);
                restaurantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<RestaurantListResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                restaurantList.clear();
                restaurantList.addAll(LocalDatabaseManager.getInstance(CustomerHomeActivity.this).getShopsList());
                filteredRestaurantList.clear();
                filteredRestaurantList.addAll(restaurantList);
                restaurantAdapter.notifyDataSetChanged();
            }
        });
    }

    private void filterByCategory(String selectedCategory) {
        if (selectedCategory == null || "All".equalsIgnoreCase(selectedCategory)) {
            filteredRestaurantList.clear();
            filteredRestaurantList.addAll(restaurantList);
            restaurantAdapter.notifyDataSetChanged();
            return;
        }

        filteredRestaurantList.clear();
        for (Restaurant r : restaurantList) {
            List<Food> foods = LocalDatabaseManager.getInstance(this).getFoodsForShop(r.getId());
            boolean hasCategoryItem = false;
            for (Food f : foods) {
                if (f.matchesCategory(selectedCategory)) {
                    hasCategoryItem = true;
                    break;
                }
            }
            if (hasCategoryItem) {
                filteredRestaurantList.add(r);
            }
        }

        if (filteredRestaurantList.isEmpty()) {
            filteredRestaurantList.addAll(restaurantList);
        }
        restaurantAdapter.notifyDataSetChanged();
    }

    private void filterRestaurants(String query) {
        filteredRestaurantList.clear();
        if (query.trim().isEmpty()) {
            filteredRestaurantList.addAll(restaurantList);
        } else {
            for (Restaurant r : restaurantList) {
                if (r.getName().toLowerCase().contains(query.toLowerCase()) ||
                        (r.getDescription() != null && r.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                    filteredRestaurantList.add(r);
                }
            }
        }
        restaurantAdapter.notifyDataSetChanged();
    }
}
