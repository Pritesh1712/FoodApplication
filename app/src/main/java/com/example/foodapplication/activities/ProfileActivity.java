package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.foodapplication.R;
import com.example.foodapplication.models.User;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.SharedPrefManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvName, tvEmail, tvRole;
    private Button btnLogout;
    private RadioGroup rgTheme;
    private RadioButton rbSystem, rbLight, rbDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnBack = findViewById(R.id.btnBack);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        btnLogout = findViewById(R.id.btnLogout);
        rgTheme = findViewById(R.id.rgTheme);
        rbSystem = findViewById(R.id.rbSystem);
        rbLight = findViewById(R.id.rbLight);
        rbDark = findViewById(R.id.rbDark);

        btnBack.setOnClickListener(v -> finish());

        User user = SharedPrefManager.getInstance(this).getUser();
        if (user != null) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            tvRole.setText("Role: " + user.getRole());
        }

        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            rbDark.setChecked(true);
        } else if (currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            rbLight.setChecked(true);
        } else {
            rbSystem.setChecked(true);
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.rbDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
