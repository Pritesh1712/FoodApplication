package com.example.foodapplication.utils;

/**
 * Centralized Configuration Constants for Food Express Android Application.
 *
 * BACKEND URL CONFIGURATION GUIDE:
 * 1. Android Emulator: Use "http://10.0.2.2:5000/api/"
 * 2. Physical Android Device (with ADB Reverse): Use "http://127.0.0.1:5000/api/" (Run: adb reverse tcp:5000 tcp:5000)
 * 3. Physical Android Device (over Wi-Fi): Use "http://<YOUR_COMPUTER_LOCAL_IP>:5000/api/"
 * 4. Deployed Production: Use "https://your-production-domain.com/api/"
 *
 * NOTE ON CLEARTEXT HTTP:
 * Cleartext HTTP (http://) is permitted for local development via android:usesCleartextTraffic="true"
 * in AndroidManifest.xml. In production deployments, HTTPS must be used for transport security.
 */
public class Constants {

    // Centralized Base Server URL
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

    // System User Roles
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_RESTAURANT = "RESTAURANT";
    public static final String ROLE_DELIVERY = "DELIVERY";
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
