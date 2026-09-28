package com.example.foodapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodapplication.models.Category;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {

    private static final String PREF_CAT = "CategoryManagerPrefs";
    private static final String KEY_CATS = "local_categories_json";

    private static CategoryManager instance;
    private static Context appContext;
    private final List<Category> localCategories;
    private final Gson gson;

    private CategoryManager(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        gson = new Gson();
        localCategories = loadCategoriesFromPrefs();
        if (localCategories.isEmpty()) {
            initDefaults();
        }
    }

    public static synchronized CategoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryManager(context);
        }
        return instance;
    }

    public static synchronized CategoryManager getInstance() {
        if (instance == null) {
            instance = new CategoryManager(null);
        }
        return instance;
    }

    private void initDefaults() {
        localCategories.add(new Category("1", "Starters", "https://images.unsplash.com/photo-1541544741938-0af808871cc0?w=300", null));
        localCategories.add(new Category("2", "Main Course", "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=300", null));
        localCategories.add(new Category("3", "Sweets & Desserts", "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=300", null));
        saveCategoriesToPrefs();
    }

    public void addCategory(Category category) {
        if (category == null || category.getName() == null) return;
        for (Category c : localCategories) {
            if (category.getName().equalsIgnoreCase(c.getName())) {
                if (category.getRestaurantId() != null && category.getRestaurantId().equals(c.getRestaurantId())) {
                    return;
                }
            }
        }
        localCategories.add(category);
        saveCategoriesToPrefs();
    }

    public List<Category> getLocalCategoriesForRestaurant(String targetRestId) {
        List<Category> result = new ArrayList<>();
        for (Category c : localCategories) {
            if (c.getRestaurantId() == null || (targetRestId != null && targetRestId.equals(c.getRestaurantId()))) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Category> getLocalCategories() {
        return localCategories;
    }

    private void saveCategoriesToPrefs() {
        if (appContext == null) return;
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_CAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CATS, gson.toJson(localCategories));
        editor.apply();
    }

    private List<Category> loadCategoriesFromPrefs() {
        if (appContext == null) return new ArrayList<>();
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_CAT, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CATS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Category>>() {}.getType();
            List<Category> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
