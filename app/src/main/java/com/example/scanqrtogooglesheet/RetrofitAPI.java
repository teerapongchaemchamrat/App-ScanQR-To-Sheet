package com.example.scanqrtogooglesheet;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitAPI {
    @GET("exec")
    Call<List<Model_item>> getGoogleSheet(@Query("partNo") String Part_No);

    @GET("exec")
    Call<List<Model_emp>> getEmployeeById(@Query("empID") String EmpolyeeID);
}
