package com.giantcroissant.sevenfuns.app

import android.app.Application
import android.content.Intent
import com.facebook.FacebookSdk
import com.giantcroissant.sevenfuns.app.RestAPIService.VideoDownloadService
import com.giantcroissant.sevenfuns.app.RestAPIService.VideoSetupService
import io.realm.Realm
import io.realm.RealmConfiguration
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

        setupServices()
        setupRealm()
    }

    private fun setupRealm() {
        val config = RealmConfiguration.Builder(this).build()
        Realm.setDefaultConfiguration(config)
    }

    private fun setupServices() {
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