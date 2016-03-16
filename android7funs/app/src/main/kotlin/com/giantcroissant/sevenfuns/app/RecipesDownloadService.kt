package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.giantcroissant.sevenfuns.app.DbModel.MethodDesc
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.DbModel.RecipesOverview
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.Sort
import io.realm.annotations.PrimaryKey
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable
import rx.Subscriber

class RecipesDownloadService : IntentService("RecipesDownloadService") {
    val TAG = RecipesDownloadService::class.java.name

    val maxDownloadAmount = 100

    val retrofit = Retrofit
        .Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    override fun onHandleIntent(intent: Intent) {
        fetchRecipes()
    }

    private fun fetchRecipes() {
        val realm = Realm.getDefaultInstance()
        val ro = realm.where(RecipesOverview::class.java).findAllSorted("id", Sort.DESCENDING)
        val maxIndex = if (ro.size < maxDownloadAmount) ro.size else maxDownloadAmount
        val recipeIds = ro.subList(0, maxIndex).map { it.id }
        System.out.println("recipes overview size(starting): " + ro.size.toString())
        System.out.println(recipeIds)
        realm.close()

        if (recipeIds.size <= 0) {
            Log.d(TAG, "nothing to pull - recipe")
            return
        }

        restApiService.getRecipesByIdList(recipeIds)
            .flatMap { jsonList ->
                //System.out.println(jsonList)
                val recipes = jsonList.map { it.toRecipe() }

                val realm = Realm.getInstance(this)
                realm.beginTransaction()
                recipes.forEach { realm.copyToRealmOrUpdate(it) }
                realm.commitTransaction()
                realm.close()

                var overviewIds = recipes.map { it.id }

                Observable.just(overviewIds)
            }
            //            .subscribe(object : Subscriber<List<Int>>() {
            //                override fun onCompleted() {
            //                }
            //
            //                override fun onError(e: Throwable?) {
            //                    System.out.println(e?.message)
            //                }
            //
            //                override fun onNext(recipeIdList: List<Int>) {
            //                    val realm = Realm.getInstance(self)
            //                    realm.beginTransaction()
            //                    recipeIdList.forEach { recipeId ->
            //                        realm.where(RecipesOverview::class.java)
            //                                .equalTo("id", recipeId)
            //                                .findFirst()?.removeFromRealm()
            //                    }
            //                    realm.commitTransaction()
            //                    realm.close()
            //                }
            //            })
            .subscribe({ recipeIdList ->
                Log.d(TAG, recipeIdList.toString())


                val realm = Realm.getInstance(this)
                realm.beginTransaction()
                recipeIdList.forEach { recipeId ->
                    realm.where(RecipesOverview::class.java)
                        .equalTo("id", recipeId)
                        .findFirst()?.removeFromRealm()
                }
                realm.commitTransaction()

                val ro1 = realm.where(RecipesOverview::class.java).findAllSorted("id", Sort.DESCENDING)
                System.out.println("recipes overview size: " + ro1.size.toString())

                realm.close()

            }, { error ->
                Log.e(TAG, "getRecipesByIdList -> $error")
            }, {
                Log.d(TAG, "completed")
            })
    }
}

fun JsonModel.VideoJson.toVideo(): Video {
    val data = if (videoData == null) VideoData() else VideoData(
        videoData.title,
        videoData.duration,
        videoData.likeCount,
        videoData.viewCount,
        videoData.description,
        videoData.publishedAt,
        videoData.thumbnailUrl
    )

    return Video(
        id,
        recipeId,
        youtubeVideoCode,
        number,
        createdAt,
        updatedAt,
        data
    )
}


open class Video(
    @PrimaryKey
    open var id: Int = 0,
    open var recipeId: Int = 0,
    open var youtubeVideoCode: String = "",
    open var number: Int = 0, // Type, 1, 2, 3
    open var createdAt: String = "",
    open var updatedAt: String = "",
    open var videoData: VideoData? = null

) : RealmObject() {}


open class VideoData(
    open var title: String = "",
    open var duration: Int = 0,
    open var likeCount: Int = 0,
    open var viewCount: Int = 0,
    open var description: String = "",
    open var publishedAt: String = "",
    open var thumbnailUrl: String = ""
) : RealmObject() {}

fun JsonModel.RecipesJsonModel.toRecipe(): Recipes {
    var methods = RealmList<MethodDesc>()
    this.method.forEach { methods.add(MethodDesc(it)) }

    val image : String = if (this.image.isNullOrEmpty()) "" else this.image?.toString()

    val recipe = Recipes(
        this.id,
        image,
        //this.image ?: "",
        this.updatedAt,
        this.chefName,
        this.title,
        this.description,
        this.ingredient,
        this.seasoning,
        methods,
        this.reminder,
        this.hits,
        this.collected
    )
    return recipe
}
