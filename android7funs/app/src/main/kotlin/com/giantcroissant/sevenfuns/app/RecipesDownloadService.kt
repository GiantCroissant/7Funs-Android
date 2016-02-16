package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import android.os.Looper
import android.util.Log
import com.giantcroissant.sevenfuns.app.DbModel.MethodDesc
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.DbModel.RecipesOverview
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.concurrent.currentThread
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/2/16.
 */
class RecipesDownloadService : IntentService("RecipesDownloadService") {

    val thread = currentThread

    val retrofit = Retrofit
            .Builder()
            .baseUrl("https://www.7funs.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    private var config: RealmConfiguration by Delegates.notNull()
    //private var realm: Realm by Delegates.notNull()

    override fun onHandleIntent(intent: Intent) {
        val config = RealmConfiguration.Builder(this).build()
        val realm = Realm.getInstance(config)
        //realm.isAutoRefresh = false

        System.out.println("RecipesDownloadService - onHandleIntent")
        //prepareRecipesFetchingSetup()

//        val remoteDataStream = restApiService.getRecipesByIdList()
//                .flatMap { jos -> Observable.from(jos) }
//                .flatMap { jo ->
//                    Observable.just(MiscModel.IntermediateOverview(
//                            jo.id.toString(), jo.updatedAt, MiscModel.LocationType.Remote, MiscModel.OverviewActionResultType.None))
//                }

        val query = realm.where(RecipesOverview::class.java).findAll()
        // Sort by id first
        query.sort("id")
        System.out.println("define local stream")
        val localDataStream = query.asObservable()
                .filter { rs -> rs.isLoaded }
                .flatMap { rs -> Observable.from(rs) }
                //.delay(1, TimeUnit.SECONDS)
                .flatMap { r -> Observable.just(MiscModel.IntermediateOverview(r.id, r.updatedAt, MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.None)) }
                //.delay(30, TimeUnit.SECONDS)
                //.window(10, TimeUnit.SECONDS, 30)
                .take(30)
                .buffer(30)
                //.buffer(10, TimeUnit.SECONDS, 30)
                //.delay(2, TimeUnit.MINUTES)
                .map { x -> x.map { intermediateOverview -> intermediateOverview.id.toInt() } }
                .flatMap { x -> restApiService.getRecipesByIdList(x) }
                //.subscribeOn( Schedulers.newThread() )
                //.subscribeOn()
                //.observeOn( Schedulers.newThread() )
//                .subscribeOn( Schedulers.from)
                .flatMap { x -> Observable.from(x) }
                .flatMap { x ->
//                    Log.w("Thread", " Thread is " + Thread.currentThread())

                    Log.w("Thread", "rest : " + Thread.currentThread().getName())

                    val rList = RealmList<MethodDesc>()
                    x.method.forEach { m -> rList.add(MethodDesc(m)) }
                    val r = Recipes(x.id.toString(), "", x.updatedAt, x.chefName, x.title, x.description, x.ingredient, x.seasoning, rList, x.reminder)
                    Observable.just(r)
                }
                .buffer(30)
                //.delay(5, TimeUnit.SECONDS)




//        localDataStream.subscribe { x ->
//            x.forEach { y -> System.out.println(y.toString()) }
//            //System.out.println(x)
//        }
//        localDataStream.subscribe { x -> x.forEach { y -> System.out.println(y) } }
        localDataStream
                .subscribe { x ->
//                    val q = realm.where(RecipesOverview::class.java)
//                    val accQuery = x.fold(q, { acc, r -> acc.equalTo("id", r.id).or() })

                    Log.w("Thread", "ream : " + Thread.currentThread().name)
            val re = Realm.getInstance(config)
            re.beginTransaction()


            val recipes = arrayListOf(x)
            recipes.forEach { r -> re.copyToRealmOrUpdate(r) }
            // Update or create for Recipes Overview
            //                    x.forEach { ro -> realm.copyToRealmOrUpdate(ro) }
            //                    // Clear Recipes
            //                    pair.second.clear()

            re.commitTransaction()

            //val removeQuery = realm.where(RecipesOverview::class.java).findAll()
            //removeQuery
        }

    }
}