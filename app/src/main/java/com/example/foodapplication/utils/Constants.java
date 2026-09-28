package com.example.foodapplication.utils;

public class Constants {
    // Default Base URL for Android Local Network / Emulator
    public static final String BASE_URL = "http://127.0.0.1:5000/api/";
    public static final String SOCKET_URL = "http://127.0.0.1:5000";

    // Shared Preferences Keys
    public static final String PREF_NAME = "FoodAppPrefs";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // User Roles
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_RESTAURANT = "RESTAURANT";
    public static final String ROLE_ADMIN = "ADMIN";

    // Order Statuses
    public static final String STATUS_PLACED = "PLACED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_PREPARING = "PREPARING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_PICKED_UP = "PICKED_UP";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}
