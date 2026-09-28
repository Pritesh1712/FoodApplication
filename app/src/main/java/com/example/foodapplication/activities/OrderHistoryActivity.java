package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.OrderAdapter;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.Order;
import com.example.foodapplication.models.OrderListResponse;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.OrderManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvOrders;
    private SwipeRefreshLayout swipeRefresh;

    private final List<Order> orderList = new ArrayList<>();
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrders);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        btnBack.setOnClickListener(v -> finish());

        swipeRefresh.setOnRefreshListener(() -> {
            loadOrdersFromBackend();
            swipeRefresh.setRefreshing(false);
        });

        setupRecyclerView();
        loadOrdersFromBackend();
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, orderList, order -> {
            Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailsActivity.class);
            intent.putExtra("order", order);
            startActivity(intent);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrdersFromBackend() {
        ApiClient.getApiService(this).getMyOrders().enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderListResponse> call, @NonNull Response<OrderListResponse> response) {
                orderList.clear();
                orderList.addAll(OrderManager.getInstance(OrderHistoryActivity.this).getLocalOrders());
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Order remoteOrder : response.body().getData()) {
                        boolean exists = false;
                        for (Order local : orderList) {
                            if (local.getId() != null && local.getId().equals(remoteOrder.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            orderList.add(remoteOrder);
                        }
                    }
                }
                orderAdapter.notifyDataSetChanged();
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
        orderAdapter.notifyDataSetChanged();
    }
}
