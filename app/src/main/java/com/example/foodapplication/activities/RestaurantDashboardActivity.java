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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.RestaurantOrderAdapter;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.Order;
import com.example.foodapplication.models.OrderListResponse;
import com.example.foodapplication.models.SingleOrderResponse;
import com.example.foodapplication.models.SingleRestaurantResponse;
import com.example.foodapplication.utils.Constants;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.OrderManager;
import com.example.foodapplication.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDashboardActivity extends AppCompatActivity {

    private TextView tvRestaurantTitle, tvAddress;
    private ImageButton btnLogout;
    private Button btnManageMenu, btnViewHistory;
    private RecyclerView rvIncomingOrders;
    private SwipeRefreshLayout swipeRefresh;

    private String restaurantId = "my";
    private String shopName = "";
    private final List<Order> allOrdersList = new ArrayList<>();
    private final List<Order> activeOrdersList = new ArrayList<>();
    private RestaurantOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_dashboard);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        tvRestaurantTitle = findViewById(R.id.tvRestaurantTitle);
        tvAddress = findViewById(R.id.tvAddress);
        btnLogout = findViewById(R.id.btnLogout);
        btnManageMenu = findViewById(R.id.btnManageMenu);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        rvIncomingOrders = findViewById(R.id.rvIncomingOrders);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(RestaurantDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnManageMenu.setOnClickListener(v -> {
            Intent intent = new Intent(RestaurantDashboardActivity.this, ManageMenuActivity.class);
            intent.putExtra("restaurantId", restaurantId);
            startActivity(intent);
        });

        btnViewHistory.setOnClickListener(v -> showOrderHistoryDialog());

        swipeRefresh.setOnRefreshListener(() -> {
            loadRestaurantInfoAndOrders();
            swipeRefresh.setRefreshing(false);
        });

        setupRecyclerView();
        loadRestaurantInfoAndOrders();
    }

    private void setupRecyclerView() {
        adapter = new RestaurantOrderAdapter(this, activeOrdersList, this::updateStatus);
        rvIncomingOrders.setLayoutManager(new LinearLayoutManager(this));
        rvIncomingOrders.setAdapter(adapter);
    }

    private void loadRestaurantInfoAndOrders() {
        ApiClient.getApiService(this).getMyRestaurant().enqueue(new Callback<SingleRestaurantResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleRestaurantResponse> call, @NonNull Response<SingleRestaurantResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    shopName = response.body().getData().getName();
                    tvRestaurantTitle.setText(shopName);
                    tvAddress.setText(response.body().getData().getAddress());
                    restaurantId = response.body().getData().getId();
                } else {
                    shopName = SharedPrefManager.getInstance(RestaurantDashboardActivity.this).getUser() != null ?
                            SharedPrefManager.getInstance(RestaurantDashboardActivity.this).getUser().getName() + "'s Kitchen" : "My Shop";
                    tvRestaurantTitle.setText(shopName);
                    tvAddress.setText("Campus Food Court");
                }
                fetchShopOrders();
            }

            @Override
            public void onFailure(@NonNull Call<SingleRestaurantResponse> call, @NonNull Throwable t) {
                shopName = SharedPrefManager.getInstance(RestaurantDashboardActivity.this).getUser() != null ?
                        SharedPrefManager.getInstance(RestaurantDashboardActivity.this).getUser().getName() + "'s Kitchen" : "My Shop";
                tvRestaurantTitle.setText(shopName);
                tvAddress.setText("Campus Food Court");
                fetchShopOrders();
            }
        });
    }

    private void fetchShopOrders() {
        ApiClient.getApiService(this).getRestaurantOrders("my").enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderListResponse> call, @NonNull Response<OrderListResponse> response) {
                allOrdersList.clear();

                // Add matching local orders for this shop ONLY
                List<Order> localOrders = OrderManager.getInstance(RestaurantDashboardActivity.this).getLocalOrders();
                for (Order local : localOrders) {
                    if (isOrderForThisShop(local)) {
                        allOrdersList.add(local);
                    }
                }

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Order remoteOrder : response.body().getData()) {
                        boolean exists = false;
                        for (Order local : allOrdersList) {
                            if (local.getId() != null && local.getId().equals(remoteOrder.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) allOrdersList.add(remoteOrder);
                    }
                }
                filterActiveOrders();
            }

            @Override
            public void onFailure(@NonNull Call<OrderListResponse> call, @NonNull Throwable t) {
                loadFallbackOrders();
            }
        });
    }

    private boolean isOrderForThisShop(Order order) {
        if (order == null) return false;
        if (order.getRestaurantId() != null && order.getRestaurantId().equals(restaurantId)) {
            return true;
        }
        if (order.getRestaurantName() != null && shopName != null && !shopName.isEmpty()) {
            return order.getRestaurantName().equalsIgnoreCase(shopName);
        }
        return false;
    }

    private void filterActiveOrders() {
        activeOrdersList.clear();
        for (Order o : allOrdersList) {
            if (!Constants.STATUS_DELIVERED.equalsIgnoreCase(o.getOrderStatus()) &&
                    !Constants.STATUS_CANCELLED.equalsIgnoreCase(o.getOrderStatus())) {
                activeOrdersList.add(o);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadFallbackOrders() {
        allOrdersList.clear();
        List<Order> localOrders = OrderManager.getInstance(this).getLocalOrders();
        for (Order local : localOrders) {
            if (isOrderForThisShop(local)) {
                allOrdersList.add(local);
            }
        }
        filterActiveOrders();
    }

    private void showOrderHistoryDialog() {
        List<Order> historyOrders = new ArrayList<>();
        for (Order o : allOrdersList) {
            if (Constants.STATUS_DELIVERED.equalsIgnoreCase(o.getOrderStatus()) ||
                    Constants.STATUS_CANCELLED.equalsIgnoreCase(o.getOrderStatus())) {
                historyOrders.add(o);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shop Order History (" + historyOrders.size() + " Past Orders)");

        if (historyOrders.isEmpty()) {
            builder.setMessage("No past completed or cancelled orders for this shop yet.");
        } else {
            RecyclerView rvHistory = new RecyclerView(this);
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            RestaurantOrderAdapter historyAdapter = new RestaurantOrderAdapter(this, historyOrders, (order, nextStatus) -> {});
            rvHistory.setAdapter(historyAdapter);
            builder.setView(rvHistory);
        }

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateStatus(Order order, String nextStatus) {
        order.setOrderStatus(nextStatus);
        OrderManager.getInstance(this).updateOrderStatus(order.getId(), nextStatus);
        filterActiveOrders();
        Toast.makeText(RestaurantDashboardActivity.this, "Order status updated to " + nextStatus, Toast.LENGTH_SHORT).show();

        Map<String, String> body = new HashMap<>();
        body.put("status", nextStatus);

        ApiClient.getApiService(this).updateOrderStatus(order.getId(), body).enqueue(new Callback<SingleOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleOrderResponse> call, @NonNull Response<SingleOrderResponse> response) {}

            @Override
            public void onFailure(@NonNull Call<SingleOrderResponse> call, @NonNull Throwable t) {}
        });
    }
}
