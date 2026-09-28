package com.example.foodapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodapplication.models.CartItem;
import com.example.foodapplication.models.Food;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final String PREF_CART = "CartManagerPrefs";
    private static final String KEY_CART_ITEMS = "cart_items_json";
    private static final String KEY_RES_ID = "current_restaurant_id";
    private static final String KEY_RES_NAME = "current_restaurant_name";

    private static CartManager instance;
    private static Context appContext;
    private List<CartItem> cartItems;
    private String currentRestaurantId;
    private String currentRestaurantName;
    private final Gson gson;

    private CartManager(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        gson = new Gson();
        loadCartFromPrefs();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager(null);
        }
        return instance;
    }

    public boolean addItem(Food food, String restaurantId, String restaurantName) {
        if (currentRestaurantId != null && !currentRestaurantId.equals(restaurantId) && !cartItems.isEmpty()) {
            cartItems.clear();
        }

        currentRestaurantId = restaurantId;
        currentRestaurantName = restaurantName;

        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getFood().getId().equals(food.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            cartItems.add(new CartItem(food, 1));
        }

        saveCartToPrefs();
        return true;
    }

    public void updateQuantity(String foodId, int newQuantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getFood().getId().equals(foodId)) {
                if (newQuantity <= 0) {
                    cartItems.remove(i);
                } else {
                    cartItems.get(i).setQuantity(newQuantity);
                }
                break;
            }
        }

        if (cartItems.isEmpty()) {
            currentRestaurantId = null;
            currentRestaurantName = null;
        }

        saveCartToPrefs();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    public String getCurrentRestaurantId() {
        return currentRestaurantId;
    }

    public String getCurrentRestaurantName() {
        return currentRestaurantName;
    }

    public void clearCart() {
        cartItems.clear();
        currentRestaurantId = null;
        currentRestaurantName = null;
        saveCartToPrefs();
    }

    private void saveCartToPrefs() {
        if (appContext == null) return;
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_CART, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CART_ITEMS, gson.toJson(cartItems));
        editor.putString(KEY_RES_ID, currentRestaurantId);
        editor.putString(KEY_RES_NAME, currentRestaurantName);
        editor.apply();
    }

    private void loadCartFromPrefs() {
        if (appContext == null) {
            cartItems = new ArrayList<>();
            return;
        }
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_CART, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CART_ITEMS, null);
        currentRestaurantId = prefs.getString(KEY_RES_ID, null);
        currentRestaurantName = prefs.getString(KEY_RES_NAME, null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
            cartItems = gson.fromJson(json, type);
            if (cartItems == null) cartItems = new ArrayList<>();
        } else {
            cartItems = new ArrayList<>();
        }
    }
}
