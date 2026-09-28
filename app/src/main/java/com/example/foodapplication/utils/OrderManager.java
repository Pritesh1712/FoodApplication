package com.example.foodapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodapplication.models.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private static final String PREF_ORDERS = "OrderManagerPrefs";
    private static final String KEY_ORDERS_LIST = "local_orders_json";

    private static OrderManager instance;
    private static Context appContext;
    private final List<Order> localOrders;
    private final Gson gson;

    private OrderManager(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        gson = new Gson();
        localOrders = loadOrdersFromPrefs();
    }

    public static synchronized OrderManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrderManager(context);
        }
        return instance;
    }

    public static synchronized OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager(null);
        }
        return instance;
    }

    public void addOrder(Order order) {
        if (order != null) {
            // Remove duplicate if exists
            for (int i = 0; i < localOrders.size(); i++) {
                if (localOrders.get(i).getId() != null && localOrders.get(i).getId().equals(order.getId())) {
                    localOrders.remove(i);
                    break;
                }
            }
            localOrders.add(0, order); // Prepend so latest placed order appears at top
            saveOrdersToPrefs();
        }
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        if (orderId == null || newStatus == null) return;
        boolean updated = false;
        for (Order o : localOrders) {
            if (orderId.equalsIgnoreCase(o.getId())) {
                o.setOrderStatus(newStatus);
                updated = true;
                break;
            }
        }
        if (updated) {
            saveOrdersToPrefs();
        }
    }

    public List<Order> getLocalOrders() {
        return localOrders;
    }

    private void saveOrdersToPrefs() {
        if (appContext == null) return;
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_ORDERS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(localOrders);
        editor.putString(KEY_ORDERS_LIST, json);
        editor.apply();
    }

    private List<Order> loadOrdersFromPrefs() {
        if (appContext == null) return new ArrayList<>();
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_ORDERS, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ORDERS_LIST, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Order>>() {}.getType();
            List<Order> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
