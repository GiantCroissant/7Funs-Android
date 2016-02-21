package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sponsor.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by ayo on 2/21/16.
 */
class SponsorActivity : AppCompatActivity() {

    val TAG = SponsorActivity::class.java.name

    companion object {
        fun navigate(activity: AppCompatActivity, sponsor: JsonModel.SponsorJsonObject) {
            val intent = Intent(activity, SponsorActivity::class.java)
            intent.putExtra("sponsor_link", sponsor.urlLink)
            intent.putExtra("sponsor_name", sponsor.name)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sponsor)

        val url = intent.getStringExtra("sponsor_link")
        val name = intent.getStringExtra("sponsor_name")

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = name

        val settings = web_view.settings
        settings.javaScriptEnabled = true
        settings.builtInZoomControls = true
        settings.setSupportZoom(true)
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        web_view.setWebViewClient(object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Toast.makeText(this@SponsorActivity, "Oh no! " + error, Toast.LENGTH_SHORT).show()
            }
        })

        web_view.loadUrl(url)
    }

}
