package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import com.giantcroissant.sevenfuns.app.DbModel.VideoOverview
import io.realm.Realm
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory


class VideoSetupService : IntentService("VideoSetupService") {

    val retrofit = Retrofit
        .Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    override fun onHandleIntent(intent: Intent?) {
        fetchVideoOverview()
    }

    private fun fetchVideoOverview() {
        restApiService
            .getVideoOverviews()
            .subscribe { videoJsonList ->
                val realm = Realm.getInstance(this)
                realm.beginTransaction()
                videoJsonList.forEach { videoJson ->
                    if (realm.where(VideoOverview::class.java)
                        .equalTo("id", videoJson.id)
                        .equalTo("updatedAt", videoJson.updatedAt)
                        .findFirst() == null) {
                        realm.copyToRealmOrUpdate(videoJson.toVideoOverview())
                    }
                }
                realm.commitTransaction()
                realm.close()
            }
    }

}

fun JsonModel.Overview.toVideoOverview(): VideoOverview {
    return VideoOverview(this.id, this.updatedAt)
}
