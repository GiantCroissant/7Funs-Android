package com.giantcroissant.sevenfuns.app

import okhttp3.Response
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
            @Header("Authorization")
            authorization: String,
            @Path("id")
            id: Int
    ): Observable<JsonModel.MyFavoriteRecipesResult>

    //
    @GET("/api/categories")
    fun getCategories(): Observable<List<JsonModel.CategoryJsonObject>>

    @GET("/api/categories/{id}")
    fun getCategoryById(
            @Path("id")
            id: Int
    ): Observable<JsonModel.CategoryJsonObject>

    @GET("/api/subcategories/{id}")
    fun getSubCategoryById(
            @Path("id")
            id: Int
    ): Observable<JsonModel.SubCategoryJsonObject>

    //
    @GET("/api/tags/{id}")
    fun getTagById(
            @Path("id")
            id: Int
    ): Observable<JsonModel.TagJsonObject>

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

    @GET("/api/messages/{id}/comments")
    fun getSpecificMessageComment(
            @Path("id")
            id: Int
    ): Observable<List<JsonModel.MessageWithCommentJsonObject>>

    @POST("/api/messages")
    fun createMessage(
            @Header("Authorization")
            authorization: String,
            @Body
            messageCreate: JsonModel.MessageCreate
    ): Observable<JsonModel.MessageCreateResultJsonObject>

    @POST("/api/messages/{id}/comments")
    fun createMessageComment(
            @Header("Authorization")
            authorization: String,
            @Path("id")
            id: Int,
            @Body
            messageCommentCreate: JsonModel.MessageCommentCreate
    ): Observable<JsonModel.MessageCommentCreateResultJsonObject>

    @GET("/api/recipe_videos")
    fun getVideos(
            @Query("page")
            page: Int
    ): Observable<List<JsonModel.VideoJson>>

    @GET("/api/recipe_videos/overview")
    fun getVideoOverviews(): Observable<List<JsonModel.Overview>>

    @GET("/api/recipe_videos")
    fun getVideosByIdList(
            @Query("ids[]")
            ids: List<Int>
    ): Observable<List<JsonModel.VideoJson>>

    @POST("/api/login")
    fun login(
            @Body
            loginData: JsonModel.LoginJsonObject
    ): Observable<JsonModel.LoginResultJsonObject>

    @POST("/api/auth/facebook/token")
    fun loginViaFbId(
            @Body
            loginData: JsonModel.LoginFbJsonObject
    ): Observable<JsonModel.LoginResultJsonObject>

    @POST("/api/signup")
    fun register(
            @Body
            loginData: JsonModel.RegisterJsonObject
    ): Observable<JsonModel.LoginResultJsonObject>

    @GET("/recipe/image/")
    @Streaming
    fun downloadRecipesImage(): Observable<Response>
}