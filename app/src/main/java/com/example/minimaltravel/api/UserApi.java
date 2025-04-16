package com.example.minimaltravel.api;

import com.example.minimaltravel.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface UserApi {

    @GET("users")
    Call<List<User>> getAllUsers();

    @POST("users")
    Call<User> createUser(@Body User task);

    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") Long id, @Body User task);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") Long id);
}