package com.example.foodapplication.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodapplication.R;
import com.example.foodapplication.adapters.UserAdapter;
import com.example.foodapplication.api.ApiClient;
import com.example.foodapplication.models.GenericResponse;
import com.example.foodapplication.models.User;
import com.example.foodapplication.models.UserListResponse;
import com.example.foodapplication.utils.InsetUtils;
import com.example.foodapplication.utils.LocalDatabaseManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUsersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvUsers;
    private SwipeRefreshLayout swipeRefresh;

    private final List<User> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        InsetUtils.applySystemBarInsets(findViewById(R.id.mainRoot));

        btnBack = findViewById(R.id.btnBack);
        rvUsers = findViewById(R.id.rvUsers);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        btnBack.setOnClickListener(v -> finish());

        swipeRefresh.setOnRefreshListener(() -> {
            loadUsersFromBackend();
            swipeRefresh.setRefreshing(false);
        });

        setupRecyclerView();
        loadUsersFromBackend();
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this, userList, this::confirmDeleteUser);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void loadUsersFromBackend() {
        ApiClient.getApiService(this).getAdminUsers().enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserListResponse> call, @NonNull Response<UserListResponse> response) {
                userList.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    userList.addAll(response.body().getData());
                } else {
                    userList.addAll(LocalDatabaseManager.getInstance(ManageUsersActivity.this).getUsersList());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<UserListResponse> call, @NonNull Throwable t) {
                userList.clear();
                userList.addAll(LocalDatabaseManager.getInstance(ManageUsersActivity.this).getUsersList());
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void confirmDeleteUser(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete User " + user.getName() + "?");
        builder.setMessage("Are you sure you want to delete user account " + user.getEmail() + "?");

        builder.setPositiveButton("Delete User", (dialog, which) -> {
            LocalDatabaseManager.getInstance(this).deleteUser(user.getId());
            userList.remove(user);
            adapter.notifyDataSetChanged();
            Toast.makeText(ManageUsersActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();

            ApiClient.getApiService(this).deleteUser(user.getId()).enqueue(new Callback<GenericResponse>() {
                @Override
                public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {}

                @Override
                public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {}
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
