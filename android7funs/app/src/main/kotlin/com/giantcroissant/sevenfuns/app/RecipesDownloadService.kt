package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.giantcroissant.sevenfuns.app.DbModel.MethodDesc
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.DbModel.RecipesOverview
import io.realm.Realm
import io.realm.RealmList
import io.realm.Sort
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable

class RecipesDownloadService : IntentService("RecipesDownloadService") {
    val maxDownloadAmount = 100

    val retrofit = Retrofit
        .Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    override fun onHandleIntent(intent: Intent) {
        val realm = Realm.getInstance(this)
        val ro = realm.where(RecipesOverview::class.java).findAllSorted("id", Sort.DESCENDING)
        val maxIndex = if (ro.size < maxDownloadAmount) ro.size else maxDownloadAmount
        val recipeIds = ro.subList(0, maxIndex).map { it.id }
        realm.close()

        if (recipeIds.size <= 0) {
            return
        }

        restApiService
            .getRecipesByIdList(recipeIds)
            .flatMap { jsonList ->
                val recipes = jsonList.map { it.toRecipe() }

                val realm = Realm.getInstance(this)
                realm.isAutoRefresh = false
                realm.beginTransaction()
                arrayListOf(recipes).forEach { realm.copyToRealmOrUpdate(it) }
                realm.commitTransaction()
                realm.close()

                // prepare to remove overviews
                var recipeIdList = recipes.map { it.id }
                Observable.just(recipeIdList)
            }
            .flatMap { recipeIdList ->

                val realm = Realm.getInstance(this)
                realm.isAutoRefresh = false
                realm.beginTransaction()
                val overviewList = realm.where(RecipesOverview::class.java).findAll()

                recipeIdList.forEach { recipeId ->
                    overviewList.filter { it.id == recipeId }.first()?.removeFromRealm()
                }
                realm.commitTransaction()
                realm.close()

                Observable.just(0)
            }
            .subscribe({

            }, { error ->
                Log.e("TAG", "error = $error")

            }, {
                Log.d("TAG", "complete")
            })
    }
}

fun JsonModel.RecipesJsonModel.toRecipe(): Recipes {
    var methods = RealmList<MethodDesc>()
    this.method.forEach { methods.add(MethodDesc(it)) }

    val recipe = Recipes(
        this.id,
        this.image ?: "",
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
