package com.example.foodapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Food implements Serializable {
    @SerializedName("_id")
    private String id;
    private String name;
    private String description;
    private double price;
    private String image;
    private String categoryId;
    private String categoryName;
    private String restaurantId;
    private boolean isAvailable;

    public Food() {
    }

    public Food(String id, String name, String description, double price, String image, String restaurantId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.restaurantId = restaurantId;
        this.isAvailable = true;
    }

    public Food(String id, String name, String description, double price, String image, String categoryName, String restaurantId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.categoryName = categoryName;
        this.restaurantId = restaurantId;
        this.isAvailable = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean matchesCategory(String selectedCategory) {
        if (selectedCategory == null || "All".equalsIgnoreCase(selectedCategory)) return true;
        String target = normalizeCatName(selectedCategory);

        String catNameNorm = categoryName != null ? normalizeCatName(categoryName) : "";
        String descNorm = description != null ? normalizeCatName(description) : "";
        String titleNorm = name != null ? normalizeCatName(name) : "";

        return catNameNorm.contains(target) || descNorm.contains(target) || titleNorm.contains(target);
    }

    private String normalizeCatName(String cat) {
        if (cat == null) return "";
        String lower = cat.trim().toLowerCase();
        if (lower.contains("starter")) return "starter";
        if (lower.contains("main")) return "main";
        if (lower.contains("sweet") || lower.contains("dessert")) return "dessert";
        if (lower.contains("pizza")) return "pizza";
        if (lower.contains("burger")) return "burger";
        return lower;
    }
}
