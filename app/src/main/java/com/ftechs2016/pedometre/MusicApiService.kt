package com.ftechs2016.pedometre.api

import retrofit2.Call
import retrofit2.http.GET

interface MusicApiService {
    @GET("v3/e993f51d-3ff7-4caa-8da0-ff2103bb34f2")
    fun getTrack(): Call<MusicTrack>
}
