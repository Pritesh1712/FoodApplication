package com.example.foodapplication.models;

public class SingleRestaurantResponse {
    private boolean success;
    private String message;
    private Restaurant data;

    public SingleRestaurantResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Restaurant getData() {
        return data;
    }

    public void setData(Restaurant data) {
        this.data = data;
    }
}
