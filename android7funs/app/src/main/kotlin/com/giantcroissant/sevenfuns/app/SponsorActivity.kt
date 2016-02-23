package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sponsor.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by ayo on 2/21/16.
 */
class SponsorActivity : AppCompatActivity() {

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
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Toast.makeText(this@SponsorActivity, "Oh no! " + error, Toast.LENGTH_SHORT).show()
            }
        })

        loadWebPage()
    }

    private fun loadWebPage() {
        if (isConnected()) {
            val url = intent.getStringExtra("sponsor_link")
            web_view.loadUrl(url)

        } else {
            val bar = Snackbar.make(web_view, "網路狀態不穩，請檢查網路連線", Snackbar.LENGTH_INDEFINITE)
            bar.setAction("重試") {
                bar.dismiss()
                loadWebPage()
            }
            bar.show()
        }
    }

    private fun isConnected(): Boolean {
        val manager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
        return manager?.activeNetworkInfo?.isConnected ?: false
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
