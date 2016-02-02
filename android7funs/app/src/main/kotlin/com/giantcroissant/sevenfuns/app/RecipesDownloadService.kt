package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.DbModel.RecipesOverview
import io.realm.Realm
import io.realm.RealmConfiguration
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/2/16.
 */
class RecipesDownloadService : IntentService("RecipesDownloadService") {
    val retrofit = Retrofit
            .Builder()
            .baseUrl("https://www.7funs.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    private var config: RealmConfiguration by Delegates.notNull()
    private var realm: Realm by Delegates.notNull()

    override fun onHandleIntent(intent: Intent) {
        config = RealmConfiguration.Builder(this).build()
        realm = Realm.getInstance(config)
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
                .flatMap { r -> Observable.just(MiscModel.IntermediateOverview(r.id, r.updatedAt, MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.None)) }
                .buffer(30)
                .map { x -> x.map { intermediateOverview -> intermediateOverview.id.toInt() } }
                .flatMap { x -> restApiService.getRecipesByIdList(x) }
                .flatMap { x -> Observable.from(x) }
                .flatMap { x ->
                    val r = Recipes(x.id.toString(), "", x.updatedAt, x.chefName, x.title, x.description, x.ingredient, x.seasoning, x.reminder)
                    Observable.just(r)
                }
                .buffer(30)


//        localDataStream.subscribe { x ->
//            x.forEach { y -> System.out.println(y.toString()) }
//            //System.out.println(x)
//        }
//        localDataStream.subscribe { x -> x.forEach { y -> System.out.println(y) } }
        localDataStream.subscribe { x ->
//                    val q = realm.where(RecipesOverview::class.java)
//                    val accQuery = x.fold(q, { acc, r -> acc.equalTo("id", r.id).or() })

                    val re = Realm.getInstance(config)

                    re.beginTransaction()

                    x.forEach { r -> re.copyToRealmOrUpdate(r) }
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