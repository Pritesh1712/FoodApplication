package com.example.foodapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.CategoryAdapter;
import com.example.foodapplication.adapters.ManageFoodAdapter;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.Category;
import com.example.foodapplication.models.CategoryListResponse;
import com.example.foodapplication.models.Food;
import com.example.foodapplication.models.FoodListResponse;
import com.example.foodapplication.models.GenericResponse;
import com.example.foodapplication.models.SingleFoodResponse;
import com.example.foodapplication.utils.CategoryManager;
import com.example.foodapplication.utils.ImageUtils;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.LocalDatabaseManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageMenuActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnAddCategory;
    private RecyclerView rvCategories, rvMenu;
    private SwipeRefreshLayout swipeRefresh;
    private ExtendedFloatingActionButton fabAddFood;

    private String restaurantId;
    private final List<Category> categoryList = new ArrayList<>();
    private final List<Food> foodList = new ArrayList<>();
    private final List<Food> filteredFoodList = new ArrayList<>();

    private CategoryAdapter categoryAdapter;
    private ManageFoodAdapter foodAdapter;
    private String selectedCategoryName = "All";

    private String selectedProductImageUriString = "";
    private ImageView dialogImagePreviewRef = null;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menu);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnBack = findViewById(R.id.btnBack);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        rvCategories = findViewById(R.id.rvCategories);
        rvMenu = findViewById(R.id.rvMenu);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabAddFood = findViewById(R.id.fabAddFood);

        restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId == null) restaurantId = "shop_1";

        btnBack.setOnClickListener(v -> finish());

        swipeRefresh.setOnRefreshListener(() -> {
            loadCategories();
            loadMenu();
            swipeRefresh.setRefreshing(false);
        });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            selectedProductImageUriString = ImageUtils.uriToBase64(ManageMenuActivity.this, selectedImageUri);
                            if (dialogImagePreviewRef != null) {
                                dialogImagePreviewRef.setImageURI(selectedImageUri);
                            }
                        }
                    }
                }
        );

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        fabAddFood.setOnClickListener(v -> showAddFoodDialog());

        setupRecyclerViews();
        loadCategories();
        loadMenu();
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this, categoryList, category -> {
            selectedCategoryName = category.getName();
            filterMenuByCategory(selectedCategoryName);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        foodAdapter = new ManageFoodAdapter(this, filteredFoodList, new ManageFoodAdapter.OnFoodActionListener() {
            @Override
            public void onFoodEditClick(Food food) {
                showEditFoodDialog(food);
            }

            @Override
            public void onFoodDeleteClick(Food food) {
                deleteFoodItem(food);
            }
        });
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        rvMenu.setAdapter(foodAdapter);
    }

    private void loadCategories() {
        ApiClient.getApiService(this).getCategoriesByRestaurant(restaurantId).enqueue(new Callback<CategoryListResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryListResponse> call, @NonNull Response<CategoryListResponse> response) {
                categoryList.clear();
                categoryList.add(new Category("0", "All", ""));
                categoryList.addAll(CategoryManager.getInstance(ManageMenuActivity.this).getLocalCategoriesForRestaurant(restaurantId));
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
                loadDefaultCategories();
            }
        });
    }

    private void loadDefaultCategories() {
        categoryList.clear();
        categoryList.add(new Category("0", "All", ""));
        categoryList.addAll(CategoryManager.getInstance(this).getLocalCategoriesForRestaurant(restaurantId));
        categoryAdapter.notifyDataSetChanged();
    }

    private void loadMenu() {
        ApiClient.getApiService(this).getFoodsByRestaurant(restaurantId).enqueue(new Callback<FoodListResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodListResponse> call, @NonNull Response<FoodListResponse> response) {
                foodList.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    foodList.addAll(response.body().getData());
                } else {
                    foodList.addAll(LocalDatabaseManager.getInstance(ManageMenuActivity.this).getFoodsForShop(restaurantId));
                }
                filterMenuByCategory(selectedCategoryName);
            }

            @Override
            public void onFailure(@NonNull Call<FoodListResponse> call, @NonNull Throwable t) {
                foodList.clear();
                foodList.addAll(LocalDatabaseManager.getInstance(ManageMenuActivity.this).getFoodsForShop(restaurantId));
                filterMenuByCategory(selectedCategoryName);
            }
        });
    }

    private void filterMenuByCategory(String catName) {
        filteredFoodList.clear();
        if ("All".equalsIgnoreCase(catName) || catName == null) {
            filteredFoodList.addAll(foodList);
        } else {
            for (Food f : foodList) {
                if (f.getDescription() != null && f.getDescription().toLowerCase().contains(catName.toLowerCase()) ||
                        f.getName().toLowerCase().contains(catName.toLowerCase())) {
                    filteredFoodList.add(f);
                }
            }
            if (filteredFoodList.isEmpty()) {
                filteredFoodList.addAll(foodList);
            }
        }
        foodAdapter.notifyDataSetChanged();
    }

    private void showAddCategoryDialog() {
        selectedProductImageUriString = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category for Shop");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etCategoryName = new EditText(this);
        etCategoryName.setHint("Category Name (e.g. Starters, Main Course, Sweets)");
        layout.addView(etCategoryName);

        LinearLayout photoLayout = new LinearLayout(this);
        photoLayout.setOrientation(LinearLayout.HORIZONTAL);
        photoLayout.setPadding(0, 16, 0, 16);

        dialogImagePreviewRef = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(120, 120);
        dialogImagePreviewRef.setLayoutParams(imgParams);
        dialogImagePreviewRef.setScaleType(ImageView.ScaleType.CENTER_CROP);
        dialogImagePreviewRef.setImageResource(android.R.drawable.ic_menu_gallery);
        photoLayout.addView(dialogImagePreviewRef);

        Button btnPickImage = new Button(this);
        btnPickImage.setText("Choose Category Photo from Gallery");
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });
        photoLayout.addView(btnPickImage);

        layout.addView(photoLayout);
        builder.setView(layout);

        builder.setPositiveButton("Create Category", (dialog, which) -> {
            String catName = etCategoryName.getText().toString().trim();
            if (catName.isEmpty()) {
                Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            String catImg = (selectedProductImageUriString != null && !selectedProductImageUriString.isEmpty()) ?
                    selectedProductImageUriString : "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300";

            Category newCat = new Category(String.valueOf(System.currentTimeMillis()), catName, catImg, restaurantId);

            CategoryManager.getInstance(this).addCategory(newCat);
            categoryList.add(newCat);
            categoryAdapter.notifyDataSetChanged();

            Map<String, String> payload = new HashMap<>();
            payload.put("name", catName);
            payload.put("image", catImg);
            payload.put("restaurantId", restaurantId);

            ApiClient.getApiService(this).addCategory(payload).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                    Toast.makeText(ManageMenuActivity.this, "Category '" + catName + "' created for this shop!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                    Toast.makeText(ManageMenuActivity.this, "Category '" + catName + "' created for this shop!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddFoodDialog() {
        selectedProductImageUriString = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product to Menu");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etName = new EditText(this);
        etName.setHint("Product Name (e.g. Paneer Tikka)");
        layout.addView(etName);

        TextView tvCategoryLabel = new TextView(this);
        tvCategoryLabel.setText("Select Category:");
        tvCategoryLabel.setPadding(0, 16, 0, 8);
        layout.addView(tvCategoryLabel);

        final Spinner spinnerCategory = new Spinner(this);
        List<String> catNames = new ArrayList<>();
        for (Category c : categoryList) {
            if (!"All".equalsIgnoreCase(c.getName())) {
                catNames.add(c.getName());
            }
        }
        if (catNames.isEmpty()) {
            catNames.add("Starters");
            catNames.add("Main Course");
            catNames.add("Sweets & Desserts");
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, catNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);
        layout.addView(spinnerCategory);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Product Description");
        layout.addView(etDesc);

        final EditText etPrice = new EditText(this);
        etPrice.setHint("Price (₹)");
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        // Photo Gallery Selector
        LinearLayout photoLayout = new LinearLayout(this);
        photoLayout.setOrientation(LinearLayout.HORIZONTAL);
        photoLayout.setPadding(0, 16, 0, 16);

        dialogImagePreviewRef = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(120, 120);
        dialogImagePreviewRef.setLayoutParams(imgParams);
        dialogImagePreviewRef.setScaleType(ImageView.ScaleType.CENTER_CROP);
        dialogImagePreviewRef.setImageResource(android.R.drawable.ic_menu_gallery);
        photoLayout.addView(dialogImagePreviewRef);

        Button btnPickImage = new Button(this);
        btnPickImage.setText("Choose Photo from Gallery");
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });
        photoLayout.addView(btnPickImage);

        layout.addView(photoLayout);

        builder.setView(layout);

        builder.setPositiveButton("Add Product", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String selectedCat = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Main Course";
            String desc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Product name and price are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            String fullDesc = desc + " [" + selectedCat + "]";
            Food newFood = new Food("f_" + System.currentTimeMillis(), name, fullDesc, price, selectedProductImageUriString, restaurantId);

            LocalDatabaseManager.getInstance(this).addFood(newFood);
            foodList.add(newFood);
            filterMenuByCategory(selectedCategoryName);
            Toast.makeText(ManageMenuActivity.this, "Product '" + name + "' added!", Toast.LENGTH_SHORT).show();

            ApiClient.getApiService(this).addFood(newFood).enqueue(new Callback<SingleFoodResponse>() {
                @Override
                public void onResponse(@NonNull Call<SingleFoodResponse> call, @NonNull Response<SingleFoodResponse> response) {}

                @Override
                public void onFailure(@NonNull Call<SingleFoodResponse> call, @NonNull Throwable t) {}
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditFoodDialog(Food food) {
        if (food == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Product (Price & Description)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        TextView tvNameHeader = new TextView(this);
        tvNameHeader.setText("Product: " + food.getName());
        tvNameHeader.setTextSize(16);
        tvNameHeader.setTextColor(getResources().getColor(R.color.primary));
        tvNameHeader.setPadding(0, 8, 0, 16);
        layout.addView(tvNameHeader);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Description");
        etDesc.setText(food.getDescription());
        layout.addView(etDesc);

        final EditText etPrice = new EditText(this);
        etPrice.setHint("Price (₹)");
        etPrice.setText(String.valueOf(food.getPrice()));
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String updatedDesc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (priceStr.isEmpty()) {
                Toast.makeText(this, "Price cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            double updatedPrice = Double.parseDouble(priceStr);
            food.setDescription(updatedDesc);
            food.setPrice(updatedPrice);

            LocalDatabaseManager.getInstance(this).updateFood(food);
            filterMenuByCategory(selectedCategoryName);
            Toast.makeText(ManageMenuActivity.this, "Product updated!", Toast.LENGTH_SHORT).show();

            ApiClient.getApiService(this).updateFood(food.getId(), food).enqueue(new Callback<SingleFoodResponse>() {
                @Override
                public void onResponse(@NonNull Call<SingleFoodResponse> call, @NonNull Response<SingleFoodResponse> response) {}

                @Override
                public void onFailure(@NonNull Call<SingleFoodResponse> call, @NonNull Throwable t) {}
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteFoodItem(Food food) {
        LocalDatabaseManager.getInstance(this).deleteFood(food.getId());
        foodList.remove(food);
        filterMenuByCategory(selectedCategoryName);
        Toast.makeText(ManageMenuActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();

        ApiClient.getApiService(this).deleteFood(food.getId()).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {}

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {}
        });
    }
}
