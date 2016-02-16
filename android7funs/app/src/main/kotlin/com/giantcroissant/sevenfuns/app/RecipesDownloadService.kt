package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.giantcroissant.sevenfuns.app.DbModel.MethodDesc
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.DbModel.RecipesOverview
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.Sort
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable

class RecipesDownloadService : IntentService("RecipesDownloadService") {

    val retrofit = Retrofit
        .Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    override fun onHandleIntent(intent: Intent) {
//        val config = RealmConfiguration.Builder(this).build()
//        val realm = Realm.getInstance(config)
//        //realm.isAutoRefresh = false
//
//        System.out.println("RecipesDownloadService - onHandleIntent")
//        //prepareRecipesFetchingSetup()
//
////        val remoteDataStream = restApiService.getRecipesByIdList()
////                .flatMap { jos -> Observable.from(jos) }
////                .flatMap { jo ->
////                    Observable.just(MiscModel.IntermediateOverview(
////                            jo.id.toString(), jo.updatedAt, MiscModel.LocationType.Remote, MiscModel.OverviewActionResultType.None))
////                }
//
//        val query = realm.where(RecipesOverview::class.java).findAll()
//        // Sort by id first
//        query.sort("id")
//        System.out.println("define local stream")
//        val localDataStream = query.asObservable()
//                .filter { rs -> rs.isLoaded }
//                .flatMap { rs -> Observable.from(rs) }
//                //.delay(1, TimeUnit.SECONDS)
//                .flatMap { r -> Observable.just(MiscModel.IntermediateOverview(r.id, r.updatedAt, MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.None)) }
//                //.delay(30, TimeUnit.SECONDS)
//                //.window(10, TimeUnit.SECONDS, 30)
//                .take(30)
//                .buffer(30)
//                //.buffer(10, TimeUnit.SECONDS, 30)
//                //.delay(2, TimeUnit.MINUTES)
//                .map { x -> x.map { intermediateOverview -> intermediateOverview.id.toInt() } }
//                .flatMap { x -> restApiService.getRecipesByIdList(x) }
//                //.subscribeOn( Schedulers.newThread() )
//                //.subscribeOn()
//                //.observeOn( Schedulers.newThread() )
////                .subscribeOn( Schedulers.from)
//                .flatMap { x -> Observable.from(x) }
//                .flatMap { x ->
////                    Log.w("Thread", " Thread is " + Thread.currentThread())
//
//                    Log.w("Thread", "rest : " + Thread.currentThread().getName())
//
//                    val rList = RealmList<MethodDesc>()
//                    x.method.forEach { m -> rList.add(MethodDesc(m)) }
//                    val r = Recipes(x.id.toString(), "", x.updatedAt, x.chefName, x.title, x.description, x.ingredient, x.seasoning, rList, x.reminder)
//                    Observable.just(r)
//                }
//                .buffer(30)
//                //.delay(5, TimeUnit.SECONDS)

        Log.d("Hello", "XDD")

        val realm = Realm.getInstance(this)
        val ro = realm.where(RecipesOverview::class.java).findAllSorted("id", Sort.DESCENDING)
        val maxIndex = if (ro.size < 30) ro.size else 30;
        val recipeIds = ro.subList(0, maxIndex).map { it.id }
        realm.close()

        if (recipeIds.size <= 0) {
            Log.d("TAG", "nothing to pull !!")
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
        this.reminder
    )
    return recipe
}
