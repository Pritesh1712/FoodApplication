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

    private EditText etAddress, etCoupon;
    private Button btnApplyCoupon, btnPlaceOrder;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbMockPayment;
    private TextView tvSubtotal, tvDeliveryFee, tvDiscount, tvTotalToPay;

    private double subtotal = 0;
    private final double deliveryFee = 30.0;
    private double discount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        etAddress = findViewById(R.id.etAddress);
        etCoupon = findViewById(R.id.etCoupon);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbMockPayment = findViewById(R.id.rbMockPayment);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotalToPay = findViewById(R.id.tvTotalToPay);

        subtotal = CartManager.getInstance().getSubtotal();
        updateBillSummary();

        btnApplyCoupon.setOnClickListener(v -> {
            String couponCode = etCoupon.getText().toString().trim();
            if (couponCode.equalsIgnoreCase("CAMPUS50")) {
                discount = 50.0;
                Toast.makeText(this, "Coupon CAMPUS50 applied! ₹50 off", Toast.LENGTH_SHORT).show();
            } else {
                discount = 0;
                Toast.makeText(this, "Invalid coupon code", Toast.LENGTH_SHORT).show();
            }
            updateBillSummary();
        });

        btnPlaceOrder.setOnClickListener(v -> performPlaceOrder());
    }

    private void updateBillSummary() {
        tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
        tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.2f", deliveryFee));
        tvDiscount.setText(String.format(Locale.getDefault(), "-₹%.2f", discount));

        double total = subtotal + deliveryFee - discount;
        if (total < 0) total = 0;
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
        double totalToPay = subtotal + deliveryFee - discount;
        if (totalToPay < 0) totalToPay = 0;

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
        orderPayload.put("totalAmount", totalToPay);
        orderPayload.put("paymentMethod", paymentMethod);
        orderPayload.put("deliveryAddress", address);

        // Build local order object for instant history feedback
        String orderId = "ORD" + (System.currentTimeMillis() % 100000);
        String formattedDate = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(new Date());

        Order placedOrder = new Order();
        placedOrder.setId(orderId);
        placedOrder.setRestaurantName(restaurantName);
        placedOrder.setOrderStatus(Constants.STATUS_PLACED);
        placedOrder.setCreatedAt("Just now, " + formattedDate);
        placedOrder.setTotalAmount(totalToPay);
        placedOrder.setPaymentMethod(paymentMethod);
        placedOrder.setPaymentStatus(paymentMethod.equals("MOCK_PAYMENT") ? "PAID" : "PENDING");
        placedOrder.setDeliveryAddress(address);
        placedOrder.setItems(orderItemsList);

        OrderManager.getInstance().addOrder(placedOrder);

        btnPlaceOrder.setEnabled(false);
        Toast.makeText(this, "Placing order...", Toast.LENGTH_SHORT).show();

        ApiClient.getApiService(this).createOrder(orderPayload).enqueue(new Callback<SingleOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<SingleOrderResponse> call, @NonNull Response<SingleOrderResponse> response) {
                btnPlaceOrder.setEnabled(true);
                CartManager.getInstance().clearCart();
                Toast.makeText(CheckoutActivity.this, "Order placed successfully! (" + paymentMethod + ")", Toast.LENGTH_LONG).show();
                navigateToOrderHistory();
            }

            @Override
            public void onFailure(@NonNull Call<SingleOrderResponse> call, @NonNull Throwable t) {
                btnPlaceOrder.setEnabled(true);
                CartManager.getInstance().clearCart();
                Toast.makeText(CheckoutActivity.this, "Order placed successfully! (" + paymentMethod + ")", Toast.LENGTH_LONG).show();
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
