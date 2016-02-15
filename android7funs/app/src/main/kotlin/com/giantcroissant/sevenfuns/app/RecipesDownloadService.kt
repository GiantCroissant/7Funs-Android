package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
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
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

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

    override fun onHandleIntent(intent: Intent) {
        val config = RealmConfiguration.Builder(this).build()
        val realm = Realm.getInstance(config)

        val query = realm.where(RecipesOverview::class.java).findAll()
        query.sort("id")

        val localDataStream = query.asObservable()
                .filter { rs -> rs.isLoaded }
                .flatMap { rs -> Observable.from(rs) }
                .flatMap { r -> Observable.just(MiscModel.IntermediateOverview(r.id, r.updatedAt, MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.None)) }
                .buffer(30)
                .map { x -> x.map { intermediateOverview -> intermediateOverview.id.toInt() } }
                .flatMap { x -> restApiService.getRecipesByIdList(x) }
                .flatMap { x -> Observable.from(x) }
                .flatMap { x ->
                    val rList = RealmList<MethodDesc>()
                    x.method.forEach { m -> rList.add(MethodDesc(m)) }
                    val r = Recipes(x.id.toString(), "", x.updatedAt, x.chefName, x.title, x.description, x.ingredient, x.seasoning, rList, x.reminder)
                    Observable.just(r)
                }
                .buffer(30)

        localDataStream.subscribe { x ->
            val realm = Realm.getInstance(config)
            realm.isAutoRefresh = false
            realm.beginTransaction()
            val recipes = arrayListOf(x)
            recipes.forEach { r -> realm.copyToRealmOrUpdate(r) }
            realm.commitTransaction()
        }

    }
}