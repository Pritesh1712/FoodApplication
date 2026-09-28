package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.CartAdapter;
import com.example.foodapplication.models.CartItem;
import com.example.foodapplication.utils.CartManager;
import com.example.foodapplication.utils.InsetUtils;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvRestaurantTitle, tvSubtotal, tvEmptyCart;
    private RecyclerView rvCartItems;
    private Button btnCheckout;
    private View bottomSummaryBar;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnBack = findViewById(R.id.btnBack);
        tvRestaurantTitle = findViewById(R.id.tvRestaurantTitle);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        rvCartItems = findViewById(R.id.rvCartItems);
        btnCheckout = findViewById(R.id.btnCheckout);
        bottomSummaryBar = findViewById(R.id.bottomSummaryBar);

        btnBack.setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
            }
        });

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCart();
    }

    private void setupRecyclerView() {
        cartItemList = CartManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(this, cartItemList, (cartItem, newQuantity) -> {
            CartManager.getInstance().updateQuantity(cartItem.getFood().getId(), newQuantity);
            refreshCart();
        });
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void refreshCart() {
        cartItemList = CartManager.getInstance().getCartItems();
        if (cartItemList.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            bottomSummaryBar.setVisibility(View.GONE);
            tvRestaurantTitle.setText("Your Cart");
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            bottomSummaryBar.setVisibility(View.VISIBLE);

            String resName = CartManager.getInstance().getCurrentRestaurantName();
            if (resName != null) {
                tvRestaurantTitle.setText(resName);
            }

            tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", CartManager.getInstance().getSubtotal()));
            cartAdapter.notifyDataSetChanged();
        }
    }
}
