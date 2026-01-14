package com.example.scalelit.network

import com.example.skalelit.model.AppData
import retrofit2.http.GET


interface ApiService {

    @GET("status")
    suspend fun fetchStatus(): AppData
}