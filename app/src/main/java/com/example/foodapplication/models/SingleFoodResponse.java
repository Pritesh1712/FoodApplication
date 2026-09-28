package com.example.foodapplication.models;

public class SingleFoodResponse {
    private boolean success;
    private String message;
    private Food data;

    public SingleFoodResponse() {
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

    public Food getData() {
        return data;
    }

    public void setData(Food data) {
        this.data = data;
    }
}
