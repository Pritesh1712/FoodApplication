package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.CartItem;
import com.example.foodapplication.models.Order;
import com.example.foodapplication.models.OrderItem;
import com.example.foodapplication.models.SingleOrderResponse;
import com.example.foodapplication.utils.CartManager;
import com.example.foodapplication.utils.Constants;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.OrderManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etAddress;
    private Button btnPlaceOrder;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbMockPayment;
    private TextView tvSubtotal, tvDeliveryFee, tvTotalToPay;

    private double subtotal = 0;
    private final double deliveryFee = 30.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        etAddress = findViewById(R.id.etAddress);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbMockPayment = findViewById(R.id.rbMockPayment);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotalToPay = findViewById(R.id.tvTotalToPay);

        subtotal = CartManager.getInstance().getSubtotal();
        updateBillSummary();

        btnPlaceOrder.setOnClickListener(v -> performPlaceOrder());
    }

    private void updateBillSummary() {
        tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
        tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.2f", deliveryFee));

        double total = subtotal + deliveryFee;
        tvTotalToPay.setText(String.format(Locale.getDefault(), "₹%.2f", total));
    }

    private void performPlaceOrder() {
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            etAddress.setError("Delivery address is required");
            return;
        }

        String restaurantId = CartManager.getInstance().getCurrentRestaurantId();
        String restaurantName = CartManager.getInstance().getCurrentRestaurantName();
        if (restaurantName == null) restaurantName = "Campus Food Hub";

        if (restaurantId == null || CartManager.getInstance().getCartItems().isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = rbCOD.isChecked() ? "COD" : "MOCK_PAYMENT";

        List<Map<String, Object>> itemsList = new ArrayList<>();
        List<OrderItem> orderItemsList = new ArrayList<>();

        for (CartItem ci : CartManager.getInstance().getCartItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("food", ci.getFood().getId());
            itemMap.put("name", ci.getFood().getName());
            itemMap.put("price", ci.getFood().getPrice());
            itemMap.put("quantity", ci.getQuantity());
            itemsList.add(itemMap);

            orderItemsList.add(new OrderItem(ci.getFood().getId(), ci.getFood().getName(), ci.getFood().getPrice(), ci.getQuantity()));
        }

        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("restaurantId", restaurantId);
        orderPayload.put("items", itemsList);
        orderPayload.put("paymentMethod", paymentMethod);
        orderPayload.put("deliveryAddress", address);

        btnPlaceOrder.setEnabled(false);
        Toast.makeText(this, "Placing order...", Toast.LENGTH_SHORT).show();

        final String finalRestaurantName = restaurantName;

        ApiClient.getApiService(this).createOrder(orderPayload).enqueue(new Callback<SingleOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleOrderResponse> call, @NonNull Response<SingleOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    btnPlaceOrder.setEnabled(true);

                    Order serverOrder = response.body().getData();
                    String orderId = (serverOrder != null && serverOrder.getId() != null) ? serverOrder.getId() : "ORD" + (System.currentTimeMillis() % 100000);
                    double finalTotal = (serverOrder != null && serverOrder.getTotalAmount() > 0) ? serverOrder.getTotalAmount() : (subtotal + deliveryFee);
                    String formattedDate = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(new Date());

                    Order placedOrder = new Order();
                    placedOrder.setId(orderId);
                    placedOrder.setRestaurantName(finalRestaurantName);
                    placedOrder.setRestaurantId(restaurantId);
                    placedOrder.setOrderStatus(Constants.STATUS_PLACED);
                    placedOrder.setCreatedAt("Just now, " + formattedDate);
                    placedOrder.setTotalAmount(finalTotal);
                    placedOrder.setPaymentMethod(paymentMethod);
                    placedOrder.setPaymentStatus(paymentMethod.equals("MOCK_PAYMENT") ? "PAID" : "PENDING");
                    placedOrder.setDeliveryAddress(address);
                    placedOrder.setItems(orderItemsList);

                    OrderManager.getInstance().addOrder(placedOrder);
                    CartManager.getInstance().clearCart();

                    Toast.makeText(CheckoutActivity.this, "Order placed successfully! (" + paymentMethod + ")", Toast.LENGTH_LONG).show();
                    navigateToOrderHistory();
                } else {
                    btnPlaceOrder.setEnabled(true);
                    String errorMsg = "Order placement failed on server";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CheckoutActivity.this, errorMsg + ". Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SingleOrderResponse> call, @NonNull Throwable t) {
                // Offline fallback if server is unreachable
                btnPlaceOrder.setEnabled(true);
                String formattedDate = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(new Date());
                String orderId = "ORD" + (System.currentTimeMillis() % 100000);

                Order placedOrder = new Order();
                placedOrder.setId(orderId);
                placedOrder.setRestaurantName(finalRestaurantName);
                placedOrder.setRestaurantId(restaurantId);
                placedOrder.setOrderStatus(Constants.STATUS_PLACED);
                placedOrder.setCreatedAt("Just now, " + formattedDate);
                placedOrder.setTotalAmount(subtotal + deliveryFee);
                placedOrder.setPaymentMethod(paymentMethod);
                placedOrder.setPaymentStatus(paymentMethod.equals("MOCK_PAYMENT") ? "PAID" : "PENDING");
                placedOrder.setDeliveryAddress(address);
                placedOrder.setItems(orderItemsList);

                OrderManager.getInstance().addOrder(placedOrder);
                CartManager.getInstance().clearCart();

                Toast.makeText(CheckoutActivity.this, "Order placed successfully! (Offline Mode)", Toast.LENGTH_LONG).show();
                navigateToOrderHistory();
            }
        });
    }

    private void navigateToOrderHistory() {
        Intent intent = new Intent(CheckoutActivity.this, OrderHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
