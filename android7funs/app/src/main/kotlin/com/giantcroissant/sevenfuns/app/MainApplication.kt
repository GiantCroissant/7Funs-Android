package com.giantcroissant.sevenfuns.app

import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid

//import io.realm.Realm
//import io.realm.RealmConfiguration

/**
 * Created by apprentice on 1/27/16.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        JodaTimeAndroid.init(this)
//        val config = RealmConfiguration.Builder(applicationContext).build()
//        Realm.setDefaultConfiguration(config)
    }
}