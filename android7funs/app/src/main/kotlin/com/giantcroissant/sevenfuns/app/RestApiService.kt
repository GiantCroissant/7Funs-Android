package com.giantcroissant.sevenfuns.app

import retrofit2.http.*
import rx.Observable

/**
 * Created by apprentice on 1/26/16.
 */
interface RestApiService {
    @GET("/api/recipes")
    fun getRecipes(
            @Query("page")
            page: Int
    ): Observable<List<JsonModel.RecipesJsonModel>>

    @GET("/api/recipes/{id}")
    fun getRecipesById(
            @Path("id")
            id: Int
    ): Observable<JsonModel.RecipesJsonModel>

    @GET("/api/recipes/overview")
    fun getRecipesOverview(): Observable<List<JsonModel.Overview>>

    @GET("/api/recipes")
    fun getRecipesByIdList(
            @Query("ids[]")
            ids: List<Int>
    ): Observable<List<JsonModel.RecipesJsonModel>>

    @POST("/api/recipes/{id}/switch_favorite")
    fun addRemoveFavorite(
            @Path("id")
            id: Int
    ): Observable<List<JsonModel.MyFavoriteRecipesResult>>

    //
    @GET("/api/categories")
    fun getCategories(): Observable<List<JsonModel.Category>>

    @GET("/api/categories/{id}")
    fun getCategoryById(
            @Path("id")
            id: Int
    ): Observable<JsonModel.Category>

    //
    @GET("/api/messages")
    fun getMessageQuery(
            @Query("page")
            page: Int
    ): Observable<JsonModel.MessageQueryJsonObject>

    @GET("/api/messages/{id}")
    fun getMessageById(
            @Path("id")
            id: Int
    ): Observable<JsonModel.MessageSpecificJsonObject>

    @GET("/api/messages/{id}")
    fun getSpecificMessageComment(
            @Path("id")
            id: Int
    ): Observable<List<JsonModel.MessageWithCommentJsonObject>>

    @POST("/api/messages")
    fun createMessage(
            @Body
            messageCreate: JsonModel.MessageCreate
    ): Observable<JsonModel.MessageCreateResultJsonObject>

    @POST("/api/messages/{id}/comments")
    fun createMessageComment(
            @Path("id")
            id: Int,
            @Body
            messageCommentCreate: JsonModel.MessageCommentCreate
    ): Observable<JsonModel.MessageCommentCreateResultJsonObject>

    @GET("/api/recipe_videos")
    fun getVideos(
            @Query("page")
            page: Int
    ): Observable<List<JsonModel.Video>>

    @GET("/api/recipe_videos/overview")
    fun getVideoOverviews(): Observable<List<JsonModel.Overview>>

    @GET("/api/recipe_videos")
    fun getVideosByIdList(
            @Query("ids[]")
            ids: List<Int>
    ): Observable<List<JsonModel.Video>>

    @POST("/api/login")
    fun login(
            @Body
            loginData: JsonModel.Login
    ): Observable<JsonModel.LoginResultJsonObject>

    @POST("/api/auth/facebook/token")
    fun loginViaFbId(
            @Body
            loginData: JsonModel.LoginFb
    ): Observable<JsonModel.LoginResultJsonObject>

    @POST("/api/signup")
    fun register(
            @Body
            loginData: JsonModel.Register
    ): Observable<JsonModel.RegisterResult>
}