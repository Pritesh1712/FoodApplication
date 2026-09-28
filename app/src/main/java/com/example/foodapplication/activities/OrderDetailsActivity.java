package com.example.foodapplication.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.Order;
import com.example.foodapplication.models.OrderItem;
import com.example.foodapplication.models.SingleOrderResponse;
import com.example.foodapplication.utils.Constants;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.OrderManager;
import com.example.foodapplication.utils.SocketManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvOrderId, tvStatus, tvRestaurantName, tvOrderDate, tvItemsList, tvTotalAmount, tvDeliveryAddress;
    private Button btnCancelOrder;

    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnBack = findViewById(R.id.btnBack);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvItemsList = findViewById(R.id.tvItemsList);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        btnBack.setOnClickListener(v -> finish());

        order = (Order) getIntent().getSerializableExtra("order");

        if (order != null) {
            tvOrderId.setText("Order #" + order.getId());
            tvStatus.setText("Status: " + order.getOrderStatus());
            tvRestaurantName.setText(order.getRestaurantName());
            tvOrderDate.setText("Placed on: " + (order.getCreatedAt() != null ? order.getCreatedAt() : "Recently"));
            tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));
            tvDeliveryAddress.setText(order.getDeliveryAddress());

            StringBuilder sb = new StringBuilder();
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    sb.append("• ").append(item.getFoodName() != null ? item.getFoodName() : "Item")
                            .append("  x").append(item.getQuantity())
                            .append("  (₹").append(String.format(Locale.getDefault(), "%.2f", item.getPrice())).append(")\n");
                }
            }
            tvItemsList.setText(sb.toString().trim());

            if (Constants.STATUS_PLACED.equals(order.getOrderStatus()) || Constants.STATUS_ACCEPTED.equals(order.getOrderStatus())) {
                btnCancelOrder.setEnabled(true);
            } else {
                btnCancelOrder.setEnabled(false);
                btnCancelOrder.setAlpha(0.5f);
            }

            // Real-time Socket.IO Connection
            setupSocketRealtimeTracking(order.getId());
        }

        btnCancelOrder.setOnClickListener(v -> cancelOrder());
    }

    private void setupSocketRealtimeTracking(String orderId) {
        SocketManager.getInstance().connect();
        SocketManager.getInstance().joinOrderRoom(orderId);
        SocketManager.getInstance().listenForOrderStatus((updatedOrderId, newStatus) -> runOnUiThread(() -> {
            if (updatedOrderId.equals(orderId)) {
                if (order != null) {
                    order.setOrderStatus(newStatus);
                    OrderManager.getInstance(OrderDetailsActivity.this).updateOrderStatus(order.getId(), newStatus);
                }
                tvStatus.setText("Status: " + newStatus);
                Toast.makeText(OrderDetailsActivity.this, "Order Status Updated: " + newStatus, Toast.LENGTH_LONG).show();
                if (!Constants.STATUS_PLACED.equals(newStatus) && !Constants.STATUS_ACCEPTED.equals(newStatus)) {
                    btnCancelOrder.setEnabled(false);
                    btnCancelOrder.setAlpha(0.5f);
                }
            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.getInstance().disconnect();
    }

    private void cancelOrder() {
        if (order == null) return;

        Map<String, String> body = new HashMap<>();
        body.put("status", Constants.STATUS_CANCELLED);

        btnCancelOrder.setEnabled(false);
        btnCancelOrder.setAlpha(0.5f);

        // Update local object & OrderManager persistently
        order.setOrderStatus(Constants.STATUS_CANCELLED);
        OrderManager.getInstance(this).updateOrderStatus(order.getId(), Constants.STATUS_CANCELLED);
        tvStatus.setText("Status: " + Constants.STATUS_CANCELLED);

        Toast.makeText(this, "Order cancelled successfully", Toast.LENGTH_SHORT).show();

        // Send to backend
        ApiClient.getApiService(this).updateOrderStatus(order.getId(), body).enqueue(new Callback<SingleOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleOrderResponse> call, @NonNull Response<SingleOrderResponse> response) {
                // Already updated locally
            }

            @Override
            public void onFailure(@NonNull Call<SingleOrderResponse> call, @NonNull Throwable t) {
                // Already updated locally
            }
        });
    }
}
