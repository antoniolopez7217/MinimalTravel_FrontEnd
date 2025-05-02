package com.example.minimaltravel.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

import com.example.minimaltravel.model.Balance;

public interface BalanceApi {

    @GET("balances")
    Call<List<Balance>> getAllBalances();

}
