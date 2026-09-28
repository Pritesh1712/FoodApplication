package com.example.foodapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Restaurant implements Serializable {
    @SerializedName("_id")
    private String id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String image;
    private double rating;
    private boolean isApproved;
    private boolean isActive;

    public Restaurant() {
    }

    public Restaurant(String id, String name, String description, String address, String phone, String image, double rating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.image = image;
        this.rating = rating;
        this.isApproved = true;
        this.isActive = true;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
