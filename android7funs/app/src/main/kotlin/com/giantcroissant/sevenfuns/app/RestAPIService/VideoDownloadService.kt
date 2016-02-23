package com.giantcroissant.sevenfuns.app.RestAPIService

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.giantcroissant.sevenfuns.app.DbModel.VideoOverview
import com.giantcroissant.sevenfuns.app.Video
import com.giantcroissant.sevenfuns.app.toVideo
import io.realm.Realm
import io.realm.Sort
import rx.Observable

class VideoDownloadService : IntentService("VideoDownloadService") {
    val TAG = VideoDownloadService::class.java.name

    val maxDownloadAmount = 100

    override fun onHandleIntent(intent: Intent) {
        fetchVideos()
    }

    private fun fetchVideos() {
        val realm = Realm.getInstance(this)
        val ro = realm.where(VideoOverview::class.java).findAllSorted("id", Sort.DESCENDING)
        val maxIndex = if (ro.size < maxDownloadAmount) ro.size else maxDownloadAmount
        val videoIds = ro.subList(0, maxIndex).map { it.id }
        realm.close()

        if (videoIds.isEmpty()) {
            Log.d(TAG, "Video - nothing to pull")
            return
        }

        RestAPIHelper.restApiService
            .getVideosByIdList(videoIds)
            .flatMap { jsonList ->
                val videos = jsonList.map { json ->
                    json.toVideo()
                }

                val realm = Realm.getInstance(this)
                realm.beginTransaction()
                videos.forEach { realm.copyToRealmOrUpdate(it) }
                realm.commitTransaction()

                val videoResults = realm.where(Video::class.java).findAll()
                Log.d(TAG, "videoResults count = ${videoResults.count()}")
                realm.close()

                val overviewIds = videos.map { it.id }
                Observable.just(overviewIds)
            }
            .subscribe({ overviewIds ->
                val realm = Realm.getInstance(this)
                realm.beginTransaction()
                overviewIds.forEach { overviewId ->
                    realm.where(VideoOverview::class.java)
                        .equalTo("id", overviewId)
                        .findFirst()?.removeFromRealm()
                }
                realm.commitTransaction()
                realm.close()

            }, { error ->
                Log.e(TAG, "getVideosByIdList -> $error")
            })
    }

}
