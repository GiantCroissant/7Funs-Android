package com.giantcroissant.sevenfuns.app.RestAPIService

import android.app.IntentService
import android.content.Intent
import com.giantcroissant.sevenfuns.app.DbModel.VideoOverview
import com.giantcroissant.sevenfuns.app.JsonModel
import io.realm.Realm


class VideoSetupService : IntentService("VideoSetupService") {

    override fun onHandleIntent(intent: Intent?) {
        fetchVideoOverview()
    }

    private fun fetchVideoOverview() {
        RestAPIHelper.restApiService
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
