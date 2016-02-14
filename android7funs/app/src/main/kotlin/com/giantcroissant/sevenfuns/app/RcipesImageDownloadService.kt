package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import io.realm.Realm
import io.realm.RealmConfiguration
import retrofit2.Retrofit
import rx.Observable
import rx.Subscriber

/**
 * Created by apprentice on 2/1/16.
 */
class RcipesImageDownloadService : IntentService("ImageDownloadService") {
    override fun onHandleIntent(intent: Intent) {
        // IntentService is itself context
        val baseImageUrl = "https://commondatastorage.googleapis.com/funs7-1/uploads"

        val retrofit = Retrofit
                .Builder()
                .baseUrl(baseImageUrl)
                .build()

        val restApiService = retrofit.create(RestApiService::class.java)


        val config: RealmConfiguration = RealmConfiguration.Builder(this).build()
        val realm = Realm.getInstance(config)

        val query = realm.where(Recipes::class.java).findAll()
//        val localDataStream = query.asObservable()
//                .filter { rs -> rs.isLoaded }
//                .flatMap { rs -> Observable.from(rs) }
//                .flatMap { r ->
//                    // Should add with id and path to android internal file system
//                    val completedImagePath = r.id + "/" + r.image
//
//                    Observable.just(completedImagePath)
//                }
//                .filter { x ->
//                    val file = getFileStreamPath(x)
//                    !file.exists()
//                }
//                .flatMap { x ->
//
//                }
//                .subscribe(object : Subscriber<String>() {
//                    override fun onCompleted() {}
//                    override fun onError(e: Throwable?) {
//                        System.out.println(e?.message)
//                    }
//                    override fun onNext(overviews: String) {
//                        //System.out.println(overviews.toString())
//                    }
//                })

    }
}