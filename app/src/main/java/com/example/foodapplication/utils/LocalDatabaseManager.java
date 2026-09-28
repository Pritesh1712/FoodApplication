package com.example.foodapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodapplication.models.Food;
import com.example.foodapplication.models.Restaurant;
import com.example.foodapplication.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalDatabaseManager {

    private static final String PREF_DB = "LocalDatabasePrefs";
    private static final String KEY_USERS = "db_users";
    private static final String KEY_SHOPS = "db_shops";
    private static final String KEY_FOODS = "db_foods";

    private static LocalDatabaseManager instance;
    private static Context appContext;
    private final Gson gson;

    private final List<User> usersList;
    private final List<Restaurant> shopsList;
    private final List<Food> foodsList;

    private LocalDatabaseManager(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        gson = new Gson();

        usersList = loadList(KEY_USERS, new TypeToken<ArrayList<User>>() {}.getType());
        shopsList = loadList(KEY_SHOPS, new TypeToken<ArrayList<Restaurant>>() {}.getType());
        foodsList = loadList(KEY_FOODS, new TypeToken<ArrayList<Food>>() {}.getType());

        if (usersList.isEmpty() && shopsList.isEmpty()) {
            initDefaultOfflineData();
        }
    }

    public static synchronized LocalDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalDatabaseManager(context);
        }
        return instance;
    }

    public static synchronized LocalDatabaseManager getInstance() {
        if (instance == null) {
            instance = new LocalDatabaseManager(null);
        }
        return instance;
    }

    private void initDefaultOfflineData() {
        // Seed default Admin
        usersList.add(new User("admin_1", "admin", "admin@gmail.com", "admin@123", "9999999999", Constants.ROLE_ADMIN));

        // Seed default Shop 1
        User owner1 = new User("owner_1", "Chef Marco", "marco@pizza.com", "123456", "9876543210", Constants.ROLE_RESTAURANT);
        usersList.add(owner1);

        Restaurant shop1 = new Restaurant("shop_1", "Campus Pizza Hub", "Artisanal fresh-baked pizzas & garlic bread", "Student Center, Floor 1", "9876543210", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500", 4.8);
        shopsList.add(shop1);

        foodsList.add(new Food("f1", "Margherita Pizza", "Classic cheese and tomato pizza [Starters]", 249.00, "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?w=500", "shop_1"));
        foodsList.add(new Food("f2", "Farmhouse Pizza", "Loaded with capsicum, onion, and cheese [Main Course]", 349.00, "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?w=500", "shop_1"));
        foodsList.add(new Food("f3", "Cheesy Garlic Bread", "Crispy garlic bread with mozzarella [Starters]", 149.00, "https://images.unsplash.com/photo-1619895092538-128341789043?w=500", "shop_1"));

        // Seed default Shop 2
        User owner2 = new User("owner_2", "Chef Raj", "raj@spice.com", "123456", "9876543211", Constants.ROLE_RESTAURANT);
        usersList.add(owner2);

        Restaurant shop2 = new Restaurant("shop_2", "Spice Junction", "Authentic North Indian thalis & biryani", "Hostel Block C Market", "9876543211", "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=500", 4.6);
        shopsList.add(shop2);

        foodsList.add(new Food("f4", "Paneer Butter Masala", "Rich creamy paneer gravy served fresh [Main Course]", 220.00, "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=500", "shop_2"));
        foodsList.add(new Food("f5", "Gulab Jamun (2 pcs)", "Soft sweet gulab jamuns in syrup [Sweets & Desserts]", 80.00, "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=500", "shop_2"));

        saveAll();
    }

    public User authenticateUser(String email, String password, String role) {
        if (email == null || password == null) return null;
        for (User u : usersList) {
            if (email.trim().equalsIgnoreCase(u.getEmail()) && password.equals(u.getPassword())) {
                return u;
            }
        }
        if ("admin@gmail.com".equalsIgnoreCase(email.trim()) && "admin@123".equals(password)) {
            User admin = new User("admin_1", "admin", "admin@gmail.com", "admin@123", "9999999999", Constants.ROLE_ADMIN);
            usersList.add(admin);
            saveAll();
            return admin;
        }
        return null;
    }

    public User registerUser(String name, String email, String password, String phone, String role,
                             String shopName, String shopAddress, String shopDesc, String shopImage) {
        for (User u : usersList) {
            if (email.trim().equalsIgnoreCase(u.getEmail())) {
                return null;
            }
        }

        String userId = "usr_" + System.currentTimeMillis();
        User newUser = new User(userId, name, email, password, phone, role);
        usersList.add(newUser);

        if (Constants.ROLE_RESTAURANT.equals(role)) {
            String shopId = "shop_" + System.currentTimeMillis();
            String sName = (shopName != null && !shopName.isEmpty()) ? shopName : name + "'s Kitchen";
            String sAddr = (shopAddress != null && !shopAddress.isEmpty()) ? shopAddress : "Campus Food Court";
            String sDesc = (shopDesc != null && !shopDesc.isEmpty()) ? shopDesc : "Quality campus refreshments";
            String sImg = (shopImage != null && !shopImage.isEmpty()) ? shopImage : "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500";

            Restaurant newShop = new Restaurant(shopId, sName, sDesc, sAddr, phone, sImg, 4.8);
            shopsList.add(newShop);
        }

        saveAll();
        return newUser;
    }

    public List<User> getUsersList() {
        return usersList;
    }

    public List<Restaurant> getShopsList() {
        return shopsList;
    }

    public Restaurant getShopByOwnerName(String ownerName) {
        if (ownerName == null || shopsList.isEmpty()) return null;
        for (Restaurant r : shopsList) {
            if (r.getName().toLowerCase().contains(ownerName.toLowerCase())) {
                return r;
            }
        }
        return shopsList.get(0);
    }

    public List<Food> getFoodsForShop(String shopId) {
        List<Food> result = new ArrayList<>();
        if (shopId == null || foodsList.isEmpty()) return foodsList;

        for (Food f : foodsList) {
            if (f.getRestaurantId() != null && f.getRestaurantId().equalsIgnoreCase(shopId)) {
                result.add(f);
            }
        }

        if (result.isEmpty()) {
            return foodsList;
        }

        return result;
    }

    public void addFood(Food food) {
        if (food != null) {
            foodsList.add(food);
            saveAll();
        }
    }

    public void updateFood(Food food) {
        if (food == null || food.getId() == null) return;
        for (int i = 0; i < foodsList.size(); i++) {
            if (food.getId().equals(foodsList.get(i).getId())) {
                foodsList.set(i, food);
                break;
            }
        }
        saveAll();
    }

    public void deleteFood(String foodId) {
        Food toRemove = null;
        for (Food f : foodsList) {
            if (f.getId() != null && f.getId().equals(foodId)) {
                toRemove = f;
                break;
            }
        }
        if (toRemove != null) {
            foodsList.remove(toRemove);
            saveAll();
        }
    }

    public void deleteUser(String userId) {
        User toRemove = null;
        for (User u : usersList) {
            if (u.getId() != null && u.getId().equals(userId)) {
                toRemove = u;
                break;
            }
        }
        if (toRemove != null) {
            usersList.remove(toRemove);
            saveAll();
        }
    }

    public void deleteShop(String shopId) {
        Restaurant toRemoveShop = null;
        for (Restaurant r : shopsList) {
            if (r.getId() != null && r.getId().equals(shopId)) {
                toRemoveShop = r;
                break;
            }
        }
        if (toRemoveShop != null) {
            shopsList.remove(toRemoveShop);

            List<Food> foodsToRemove = new ArrayList<>();
            for (Food f : foodsList) {
                if (f.getRestaurantId() != null && f.getRestaurantId().equals(shopId)) {
                    foodsToRemove.add(f);
                }
            }
            foodsList.removeAll(foodsToRemove);
            saveAll();
        }
    }

    private void saveAll() {
        if (appContext == null) return;
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_DB, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERS, gson.toJson(usersList));
        editor.putString(KEY_SHOPS, gson.toJson(shopsList));
        editor.putString(KEY_FOODS, gson.toJson(foodsList));
        editor.apply();
    }

    private <T> List<T> loadList(String key, Type type) {
        if (appContext == null) return new ArrayList<>();
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_DB, Context.MODE_PRIVATE);
        String json = prefs.getString(key, null);
        if (json != null) {
            List<T> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
