package com.giantcroissant.sevenfuns.app.RestAPIService

import com.giantcroissant.sevenfuns.app.RestApiService
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory

/**
 * Created by ayo on 2/21/16.
 */
object RestAPIHelper {

    val retrofit = Retrofit.Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

}