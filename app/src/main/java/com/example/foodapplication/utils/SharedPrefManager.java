package com.example.foodapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodapplication.models.User;

public class SharedPrefManager {

    private static SharedPrefManager instance;
    private static Context ctx;

    private SharedPrefManager(Context context) {
        ctx = context.getApplicationContext();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(User user, String token) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_TOKEN, token);
        editor.putString(Constants.KEY_USER_ID, user.getId());
        editor.putString(Constants.KEY_USER_NAME, user.getName());
        editor.putString(Constants.KEY_USER_EMAIL, user.getEmail());
        editor.putString(Constants.KEY_USER_ROLE, user.getRole());
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.KEY_TOKEN, null);
    }

    public User getUser() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString(Constants.KEY_USER_ID, null);
        String name = sharedPreferences.getString(Constants.KEY_USER_NAME, null);
        String email = sharedPreferences.getString(Constants.KEY_USER_EMAIL, null);
        String role = sharedPreferences.getString(Constants.KEY_USER_ROLE, null);

        if (id != null) {
            return new User(id, name, email, role);
        }
        return null;
    }

    public String getUserRole() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.KEY_USER_ROLE, Constants.ROLE_CUSTOMER);
    }

    public void logout() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
