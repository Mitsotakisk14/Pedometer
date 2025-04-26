package com.ftechs2016.pedometre.api

import retrofit2.Call
import retrofit2.http.GET

interface MusicApiService {
    @GET("v3/a3eced93-703e-4d44-bbeb-b6cf54d7ac5c")
    fun getTrack(): Call<MusicTrack>
}
