package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.DeliveryOrderAdapter;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.Order;
import com.example.foodapplication.models.OrderListResponse;
import com.example.foodapplication.models.SingleOrderResponse;
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

public class DeliveryDashboardActivity extends AppCompatActivity {

    private ImageButton btnLogout;
    private RecyclerView rvDeliveries;
    private SwipeRefreshLayout swipeRefresh;

    private final List<Order> orderList = new ArrayList<>();
    private DeliveryOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_dashboard);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnLogout = findViewById(R.id.btnLogout);
        rvDeliveries = findViewById(R.id.rvDeliveries);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(DeliveryDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        swipeRefresh.setOnRefreshListener(() -> {
            loadDeliveryOrders();
            swipeRefresh.setRefreshing(false);
        });

        setupRecyclerView();
        loadDeliveryOrders();
    }

    private void setupRecyclerView() {
        adapter = new DeliveryOrderAdapter(this, orderList, (order, nextStatus) -> performDeliveryAction(order, nextStatus));
        rvDeliveries.setLayoutManager(new LinearLayoutManager(this));
        rvDeliveries.setAdapter(adapter);
    }

    private void loadDeliveryOrders() {
        ApiClient.getApiService(this).getDeliveryOrders().enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderListResponse> call, @NonNull Response<OrderListResponse> response) {
                orderList.clear();
                orderList.addAll(OrderManager.getInstance(DeliveryDashboardActivity.this).getLocalOrders());
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Order remoteOrder : response.body().getData()) {
                        boolean exists = false;
                        for (Order local : orderList) {
                            if (local.getId() != null && local.getId().equals(remoteOrder.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) orderList.add(remoteOrder);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<OrderListResponse> call, @NonNull Throwable t) {
                loadFallbackOrders();
            }
        });
    }

    private void loadFallbackOrders() {
        orderList.clear();
        orderList.addAll(OrderManager.getInstance(this).getLocalOrders());
        adapter.notifyDataSetChanged();
    }

    private void performDeliveryAction(Order order, String nextStatus) {
        order.setOrderStatus(nextStatus);
        OrderManager.getInstance(this).updateOrderStatus(order.getId(), nextStatus);
        adapter.notifyDataSetChanged();
        Toast.makeText(DeliveryDashboardActivity.this, "Order updated to " + nextStatus, Toast.LENGTH_SHORT).show();

        Map<String, String> body = new HashMap<>();
        body.put("status", nextStatus);

        ApiClient.getApiService(this).updateOrderStatus(order.getId(), body).enqueue(new Callback<SingleOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleOrderResponse> call, @NonNull Response<SingleOrderResponse> response) {
                // Updated locally
            }

            @Override
            public void onFailure(@NonNull Call<SingleOrderResponse> call, @NonNull Throwable t) {
                // Updated locally
            }
        });
    }
}
