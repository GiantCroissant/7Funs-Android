package com.giantcroissant.sevenfuns.app

import android.app.Application
import android.content.Intent
import android.util.Log
import com.facebook.FacebookSdk
import com.giantcroissant.sevenfuns.app.RestAPIService.VideoDownloadService
import com.giantcroissant.sevenfuns.app.RestAPIService.VideoSetupService
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import net.danlew.android.joda.JodaTimeAndroid
import rx.Observable
import java.util.concurrent.TimeUnit


/**
 * Created by apprentice on 1/27/16.
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FacebookSdk.sdkInitialize(applicationContext)
        JodaTimeAndroid.init(this)

//        startService(Intent(this, RecipesSetupServiceEx::class.java))

        startService(Intent(this, RecipesSetupService::class.java))
        startService(Intent(this, VideoSetupService::class.java))

        Observable.interval(5, TimeUnit.SECONDS).subscribe {
            val downloadRecipes = Intent(this, RecipesDownloadService::class.java)
            startService(downloadRecipes)

            val downloadVideos = Intent(this, VideoDownloadService::class.java)
            startService(downloadVideos)
        }
    }

}