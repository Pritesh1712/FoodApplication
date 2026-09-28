package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.example.foodapplication.utils.Constants;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPrefManager prefManager = SharedPrefManager.getInstance(SplashActivity.this);
            if (prefManager.isLoggedIn()) {
                String role = prefManager.getUserRole();
                Intent intent;
                if (Constants.ROLE_ADMIN.equals(role)) {
                    intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                } else if (Constants.ROLE_RESTAURANT.equals(role)) {
                    intent = new Intent(SplashActivity.this, RestaurantDashboardActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, CustomerHomeActivity.class);
                }
                startActivity(intent);
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 1500);
    }
}
