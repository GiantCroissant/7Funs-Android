package com.giantcroissant.sevenfuns.app

import com.google.gson.annotations.SerializedName

/**
 * Created by apprentice on 1/26/16.
 */
object JsonModel {
    //
    data class RecipesJsonModel(
            val id: Int,
            val image: String?,
            @SerializedName("chef_name")
            val chefName: String,
            val title: String,
            val description: String,
            val ingredient: String,
            val seasoning: String,
            val method: List<String>,
            val reminder: String,
            val hits: Int,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("creator_id")
            val creatorId: Int,
            @SerializedName("updator_id")
            val updatorId: Int,
            val collected: Int)

    data class RecipesAddRemoveFavorite(
            val mark: String?,
            @SerializedName("markable_id")
            val markableId: String?,
            @SerializedName("markable_type")
            val markableType: String?,
            @SerializedName("marker_id")
            val markerId: Int?,
            @SerializedName("marker_type")
            val markerType: String?,
            @SerializedName("created_at")
            val createdAt: String?,
            val id: Int?)

    //
    data class Overview(
            val id: Int,
            @SerializedName("updated_at")
            val updatedAt: String)

    //
    data class VideoData(
            val title: String,
            val duration: Int,
            @SerializedName("like_count")
            val likeCount: Int,
            @SerializedName("view_count")
            val viewCount: Int,
            val descritpion: String,
            @SerializedName("published_at")
            val publishedAt: String,
            @SerializedName("thumbnail_url")
            val thumbnailUrl: String)

    data class Video(
            val id: Int,
            @SerializedName("recipe_id")
            val recipeId: Int,
            @SerializedName("youtube_video_code")
            val youtubeVideoCode: String,
            val number: Int,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("video_data")
            val videoData: VideoData)

    //
    data class User(
            val id: Int,
            val name: String,
            @SerializedName("fb_id")
            val fbId: String,
            val image: String
    )

    data class MessageWithComment(
            val id: Int,
            val title: String,
            val comment: String,
            @SerializedName("commentable_id")
            val commentableId: Int,
            @SerializedName("commentable_type")
            val commentableType: String,
            @SerializedName("user_id")
            val userId: Int,
            val role: String,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String)

    data class MessageSpecific(
            val id: Int,
            @SerializedName("user_id")
            val userId: Int,
            val title: String?,
            val description: String?,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("comments_count")
            val commentsCount: Int,
            val comments: List<MessageWithComment>)

    data class Message(
            val id: Int,
            @SerializedName("user_id")
            val userId: Int,
            val title: String?,
            val description: String?,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            @SerializedName("comments_count")
            val commentsCount: Int,
            val user: User)

    data class MessageCreate(
            val title: String,
            val description: String)

    data class MessageCreateResult(
            @SerializedName("user_id")
            val userId: Int,
            val title: String,
            val description: String,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            val id: String)

    data class MessageCommentCreate(
            val messageId: Int,
            val comment: String)

    data class MessageCommentCreateResult(
            val comment: String,
            @SerializedName("commentable_id")
            val commentableId: String,
            @SerializedName("commentable_type")
            val commentableType: String,
            @SerializedName("user_id")
            val userId: Int,
            val role: String,
            @SerializedName("created_at")
            val createdAt: String,
            val id: Int)

    data class PaginationDetail(
            val currentPage: Int,
            val base: String,
            val next: Int,
            val isFirstPage: Boolean,
            val isLastPage: Boolean,
            val prev: Int,
            val total: Int,
            val limit: Int)

    data class MessageQuery(
            val collections: List<Message>,
            @SerializedName("pagination")
            val paginationDetail: PaginationDetail
    )

    //
    data class RegisterUser(
            val email: String,
            val name: String,
            val password: String,
            @SerializedName("password_confirmation")
            val passwordConfirmation: String)

    data class Register(val user: RegisterUser)

    data class RegisterResultDataUser(
            val id: Int,
            val email: String,
            @SerializedName("created_at")
            val createdAt: String,
            val name: String)

    data class RegisterResultData(val user: RegisterResultDataUser)

    data class RegisterResult(
            val success: Boolean,
            val info: String,
            val data: RegisterResultData)

    data class Login(
            val email: String,
            val password: String)

    data class LoginFb(
            @SerializedName("access_token")
            val accessToken: String)

    data class LoginResult(val token: String)

    //
    data class MyFavoriteRecipesResult(val id: Int)

    //
    data class Tagging(
            @SerializedName("tag_id")
            val tagId: Int,
            @SerializedName("taggable_type")
            val taggableType: Int,
            @SerializedName("taggable_id")
            val taggableId: Int)

    data class Tag(
            val id: Int,
            val name: String,
            @SerializedName("taggings_count")
            val taggingCount: Int,
            @SerializedName("category_id")
            val categoryId: Int,
            val taggings: List<Tagging>)

    data class SubCategory(
            val id: Int,
            val title: String,
            @SerializedName("parent_id")
            val parentId: Int?,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            val tags: List<Tag>)

    data class Category(
            val id: String,
            val title: String,
            @SerializedName("parent_id")
            val parentId: Int?,
            @SerializedName("created_at")
            val createdAt: String,
            @SerializedName("updated_at")
            val updatedAt: String,
            val subCategories: List<SubCategory>)
}