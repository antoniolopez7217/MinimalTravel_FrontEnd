package com.example.minimaltravel.api;

import com.example.minimaltravel.model.Task;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface TaskApi {

    @GET("tasks")
    Call<List<Task>> getAllTasks();

    @POST("tasks")
    Call<Task> createTask(@Body Task task);

    @PUT("tasks/{id}")
    Call<Task> updateTask(@Path("id") Long id, @Body Task task);

    @DELETE("tasks/{id}")
    Call<Void> deleteTask(@Path("id") Long id);
}