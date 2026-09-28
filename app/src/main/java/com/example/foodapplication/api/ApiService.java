package com.example.foodapplication.api;

import com.example.foodapplication.models.AuthResponse;
import com.example.foodapplication.models.CategoryListResponse;
import com.example.foodapplication.models.Food;
import com.example.foodapplication.models.FoodListResponse;
import com.example.foodapplication.models.GenericResponse;
import com.example.foodapplication.models.OrderListResponse;
import com.example.foodapplication.models.Restaurant;
import com.example.foodapplication.models.RestaurantListResponse;
import com.example.foodapplication.models.SingleFoodResponse;
import com.example.foodapplication.models.SingleOrderResponse;
import com.example.foodapplication.models.SingleRestaurantResponse;
import com.example.foodapplication.models.User;
import com.example.foodapplication.models.UserListResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("auth/register")
    Call<AuthResponse> registerUser(@Body User user);

    @POST("auth/register")
    Call<AuthResponse> registerUserWithShop(@Body Map<String, Object> registerPayload);

    @POST("auth/login")
    Call<AuthResponse> loginUser(@Body Map<String, String> credentials);

    @GET("auth/me")
    Call<AuthResponse> getMe();

    // Restaurants
    @GET("restaurants")
    Call<RestaurantListResponse> getRestaurants();

    @GET("restaurants/{id}")
    Call<SingleRestaurantResponse> getRestaurantById(@Path("id") String id);

    @GET("restaurants/my/restaurant")
    Call<SingleRestaurantResponse> getMyRestaurant();

    @POST("restaurants")
    Call<SingleRestaurantResponse> createOrUpdateRestaurant(@Body Restaurant restaurant);

    // Food
    @GET("food/restaurant/{restaurantId}")
    Call<FoodListResponse> getFoodsByRestaurant(@Path("restaurantId") String restaurantId);

    @POST("food")
    Call<SingleFoodResponse> addFood(@Body Food food);

    @PUT("food/{id}")
    Call<SingleFoodResponse> updateFood(@Path("id") String id, @Body Food food);

    @DELETE("food/{id}")
    Call<GenericResponse> deleteFood(@Path("id") String id);

    // Categories
    @GET("categories")
    Call<CategoryListResponse> getCategories();

    @GET("categories")
    Call<CategoryListResponse> getCategoriesByRestaurant(@Query("restaurantId") String restaurantId);

    @POST("categories")
    Call<GenericResponse> addCategory(@Body Map<String, String> categoryPayload);

    // Orders
    @POST("orders")
    Call<SingleOrderResponse> createOrder(@Body Map<String, Object> orderPayload);

    @GET("orders/my")
    Call<OrderListResponse> getMyOrders();

    @GET("orders/restaurant/{restaurantId}")
    Call<OrderListResponse> getRestaurantOrders(@Path("restaurantId") String restaurantId);

    @GET("orders/delivery")
    Call<OrderListResponse> getDeliveryOrders();

    @PUT("orders/{id}/status")
    Call<SingleOrderResponse> updateOrderStatus(@Path("id") String id, @Body Map<String, String> statusBody);

    // Admin
    @GET("admin/users")
    Call<UserListResponse> getAdminUsers();

    @DELETE("admin/users/{id}")
    Call<GenericResponse> deleteUser(@Path("id") String id);

    @DELETE("admin/restaurants/{id}")
    Call<GenericResponse> deleteRestaurant(@Path("id") String id);

    @PUT("admin/restaurants/{id}/approve")
    Call<SingleRestaurantResponse> toggleRestaurantStatus(@Path("id") String id);
}
