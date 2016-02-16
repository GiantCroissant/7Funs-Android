package com.giantcroissant.sevenfuns.app

import android.app.Application
import android.content.Intent
import com.facebook.FacebookSdk
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

        val recipeOverviews = Intent(this, RecipesSetupService::class.java)
        startService(recipeOverviews)

        Observable.interval(5, TimeUnit.SECONDS).subscribe {
            val downloadRecipes = Intent(this, RecipesDownloadService::class.java)
            startService(downloadRecipes)
        }
    }

}