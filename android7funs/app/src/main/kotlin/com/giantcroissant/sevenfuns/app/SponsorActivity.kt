package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.bumptech.glide.Glide
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.google.android.youtube.player.YouTubeStandalonePlayer
import kotlinx.android.synthetic.main.activity_sponsor.*
import kotlinx.android.synthetic.main.cardview_sponsor_detail_video.view.*
import kotlinx.android.synthetic.main.cardview_sponsor_section_overview.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlin.properties.Delegates

/**
 * Created by ayo on 2/21/16.
 */
class SponsorActivity : AppCompatActivity() {

    companion object {
//        fun navigate(activity: AppCompatActivity, sponsor: JsonModel.SponsorJsonObject) {
        fun navigate(activity: AppCompatActivity, sponsor: JsonModel.SponsorDetailJsonObject) {

            val intent = Intent(activity, SponsorActivity::class.java)
            val sponsorVideoParcelableList = sponsor.spnosorVideos
                    .filter { x -> x.videoData != null }
                    .map { x ->
                        System.out.println("mapped data: " + x)
                        SponsorVideoParcelable(
                            x.id,
                            x.youtubeVideoCode,
                            x.videoData?.title ?: "Blank",
                            x.videoData?.duration ?: 100,
                            x.videoData?.likeCount ?: 1,
                            x.videoData?.viewCount ?: 1,
                            x.videoData?.thumbnailUrl ?: "https://i.ytimg.com/vi/zLztyHSWOMU/hqdefault.jpg")
                    } as MutableList<SponsorVideoParcelable>

            System.out.println("sponsorVideoParcelableList should be like the following")
            System.out.println(sponsorVideoParcelableList)

            intent.putExtra(
                "sponsor",
                SponsorParcelable(
                    sponsor.id,
                    sponsor.name,
                    sponsor.url,
                    sponsor.image,
                    sponsor.description,
                    sponsorVideoParcelableList
//                            SponsorVideoParcelable(
//                                x.id,
//                                x.youtubeVideoCode,
//                                x.videoData?.title ?: "Blank",
//                                x.videoData?.duration ?: 100,
//                                x.videoData?.likeCount ?: 1,
//                                x.videoData?.viewCount ?: 1,
//                                x.videoData?.thumbnailUrl ?: "https://i.ytimg.com/vi/zLztyHSWOMU/hqdefault.jpg")
//                        }
                )
            )
//            intent.putExtra("sponsor_link", sponsor.urlLink)
//            intent.putExtra("sponsor_link", sponsor.url)
//            intent.putExtra("sponsor_name", sponsor.name)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sponsor)

        val sponsor = intent.getParcelableExtra<SponsorParcelable>("sponsor")

//        val name = intent.getStringExtra("sponsor_name")
        val name = sponsor.name

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

        //loadWebPage()

        val baseUrl = "https://commondatastorage.googleapis.com/funs7-1/uploads/sponsor/image/"
        val sponsorBrandImagePath = baseUrl + sponsor.id + "/" + sponsor.image
        Glide.with(applicationContext)
                .load(Uri.parse(sponsorBrandImagePath))
                .centerCrop()
                .into(sponsorBrandImage)

        sponsorDetailUrlLink?.text = sponsor.name
        sponsorDetailUrlLink?.setOnClickListener { x ->
//            load
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sponsor.url))
            startActivity(browserIntent)
        }

        sponsorDetailDescription?.text = sponsor.description

        System.out.println("sponsor size should be: " + sponsor.videos.size)

        sponsorDetailVideos?.let {
            it.layoutManager = LinearLayoutManager(it.context)
//            it.adapter = SponsorActivity.RecyclerAdapter(this, listOf<SponsorVideoParcelable>())
            it.adapter = RecyclerAdapter(this, sponsor.videos)
        }
    }

    private fun loadWebPage() {
        if (isConnected()) {
            val sponsor = intent.getParcelableExtra<SponsorParcelable>("sponsor")
//            val url = intent.getStringExtra("sponsor_link")
            val url = sponsor.url
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

    class RecyclerAdapter(
        val activity: AppCompatActivity,
        val sponsorVideoParcelables: List<SponsorVideoParcelable>
    ) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            var view: View by Delegates.notNull()
            init {
                view = v
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_sponsor_detail_video, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sponsorVideo = sponsorVideoParcelables[position]

            holder.view.setOnClickListener { x ->
                System.out.println("should open an activity to play movie")
                val key = "AIzaSyCGkt_vNwDOmv8jDQPybsu8u3yyn95NK7o"
                val intent = YouTubeStandalonePlayer.createVideoIntent(activity, key, sponsorVideo.youtubeVideoCode)
                activity?.startActivity(intent)
            }

            holder.view.sponsorDetailCardViewTitle?.text = sponsorVideo.videoTitle

//            activity?.supportFragmentManager
//                    .beginTransaction()
//                    .replace(holder.view.sponsorSectionDetailCardViewYoutube, youtubeFragment)
//                    .commit()
//            sponsorSectionDetailCardViewYoutube
            Glide.with(activity?.applicationContext)
                .load(Uri.parse(sponsorVideo.thumbnailUrl))
                .centerCrop()
                .into(holder.view.sponsorDetailCardViewImage)
        }

        override fun getItemCount(): Int {
            return sponsorVideoParcelables.size
        }
    }
}
