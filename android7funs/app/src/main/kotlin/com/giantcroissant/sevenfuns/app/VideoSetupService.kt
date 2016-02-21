package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.giantcroissant.sevenfuns.app.DbModel.VideoOverview
import io.realm.Realm
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory

/**
 * Created by ayo on 2/22/16.
 */
class VideoSetupService : IntentService("VideoSetupService") {
    val TAG = VideoSetupService::class.java.name

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
                        Log.e(TAG, "new Video Overview !")
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
