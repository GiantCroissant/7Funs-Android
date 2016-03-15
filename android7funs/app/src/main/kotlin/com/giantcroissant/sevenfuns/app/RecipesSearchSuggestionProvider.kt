package com.giantcroissant.sevenfuns.app

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.BaseColumns
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/25/16.
 */
open class RecipesSearchSuggestionProvider : ContentProvider() {

    var recipesByTagList: List<MiscModel.RecipesByTag> by Delegates.notNull()

    override fun onCreate() : Boolean {
        recipesByTagList = listOf()
        return true
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?) : Cursor? {
        System.out.println("query")

        val q = uri?.lastPathSegment
        System.out.println(q)

        val c = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA))

        if (recipesByTagList.isEmpty()) {
            System.out.println("no cached values")
            // No cached value, should retrieve from remote and cache the result for later use
            val retrofit = Retrofit
                    .Builder()
                    .baseUrl("https://www.7funs.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build()

            val restApiService = retrofit.create(RestApiService::class.java)

            val recipesByTag = restApiService.getCategories()
//                    .observeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
                    .map { x ->
                        x.flatMap { c ->
                            val ids = c.subCategories.map { sc ->
                                sc.id
                            }
                            ids
                        }
                    }
                    .flatMap { x -> Observable.from(x) }
                    .flatMap { x -> Observable.just(x) }
                    .flatMap { x ->
                        restApiService.getSubCategoryById(x)
                    }
                    .map { x ->
                        x.tags.map { sc ->
                            sc.id
                        }
                    }
//                    .map { x ->
//                        x.tags.map { sc ->
//                            sc.id
//                        }
//                        ids
//                    }
//                    .map { x ->
//                        restApiService.getSubCategoryById(x)
//                    }
//                    .map { x ->
//                        x.flatMap { c ->
//                            val ids = c.tags.map { sc ->
//                                sc.id
//                            }
//                            ids
//                        }
//                    }
                    .flatMap { x -> Observable.from(x) }
                    .flatMap { x -> Observable.just(x) }
                    .flatMap { x ->
                        System.out.println("tag id to get: " + x.toString())
                        restApiService.getTagById(x)
                    }
                    .filter { x -> x != null }
                    //.filter { x -> x == null }
                    .map { x ->
                        val recipesIds = x.taggings.map { t -> t.taggableId }
                        MiscModel.RecipesByTag(x.id, x.name, recipesIds)
                    }
                    //.take(5)

            // Subscribe for further processing
            recipesByTag
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
//                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<MiscModel.RecipesByTag>() {
                override fun onCompleted() {
                    System.out.println("Done getting tagging recipes value")

//                    recipesByTagList.forEach { x ->
//                        c.addRow(arrayOf(x.tagId, x.name, x.tagId))
//                    }
                }

                override fun onError(e: Throwable?) {
                    System.out.println(e?.message)
                }

                override fun onNext(recipesByTag: MiscModel.RecipesByTag) {
                    //System.out.println("Keep getting tagging recipes value")
                    System.out.println(recipesByTag.toString())

                    recipesByTagList = recipesByTagList.plusElement(recipesByTag)
                    c.addRow(arrayOf(recipesByTag.tagId, recipesByTag.name, recipesByTag.tagId, recipesByTag.name))

                    //context?.contentResolver?.notifyChange(uri, null)
                }
            })

//            c.addRow(arrayOf(1, "牛肉", 1, "牛肉"))
//            c.addRow(arrayOf(10, "軟體海鮮", 10, "軟體海鮮"))

        } else {
            // Just use cached value
            System.out.println("Has cached value")
            recipesByTagList.forEach { x ->
                c.addRow(arrayOf(x.tagId, x.name, x.tagId, x.name))
            }
        }

        //(1..160).forEach { x -> c.addRow(arrayOf(x, "Food " + x.toString(), x)) }

        if (SearchManager.SUGGEST_URI_PATH_QUERY.equals(q)) {

        } else {
            //return MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA))
        }

        return c
    }

    override fun getType(uri: Uri?): String? {
        return ""
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 1
    }
}