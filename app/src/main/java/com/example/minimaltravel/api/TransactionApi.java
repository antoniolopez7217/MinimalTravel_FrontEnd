package com.example.minimaltravel.api;

import com.example.minimaltravel.model.Transaction;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TransactionApi {

    // Obtener todas las transacciones
    @GET("transactions")
    Call<List<Transaction>> getAllTransactions();

    // Crear nueva transacción
    @POST("transactions")
    Call<Transaction> createTransaction(@Body Transaction transaction);

    // Obtener transacción por ID
    @GET("transactions/{id}")
    Call<Transaction> getTransactionById(@Path("id") Long id);

    // Eliminar transacción
    @DELETE("transactions/{id}")
    Call<Void> deleteTransaction(@Path("id") Long id);

}
