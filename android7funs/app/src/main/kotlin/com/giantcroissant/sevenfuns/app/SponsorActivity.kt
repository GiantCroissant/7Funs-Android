package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.youtube.player.YouTubeStandalonePlayer
import kotlinx.android.synthetic.main.activity_sponsor.*
import kotlinx.android.synthetic.main.cardview_sponsor_detail_video.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlin.properties.Delegates

/**
 * Created by ayo on 2/21/16.
 */
class SponsorActivity : AppCompatActivity() {

    companion object {
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

            intent.putExtra(
                "sponsor",
                SponsorParcelable(
                    sponsor.id,
                    sponsor.name,
                    sponsor.url,
                    sponsor.image,
                    sponsor.description,
                    sponsorVideoParcelableList
                )
            )
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sponsor)
        val sponsor = intent.getParcelableExtra<SponsorParcelable>("sponsor")
        val name = sponsor.name
        val url = sponsor.url
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = name

        val baseUrl = "https://commondatastorage.googleapis.com/funs7-1/uploads/sponsor/image/"
        val sponsorBrandImagePath = baseUrl + sponsor.id + "/" + sponsor.image
        Glide.with(applicationContext)
                .load(Uri.parse(sponsorBrandImagePath))
                .centerCrop()
                .into(sponsorBrandImage)

        sponsorDetailUrlLink?.text = sponsor.name
        sponsorDetailUrlLink?.setOnClickListener { openBrowser(url) }
        sponsorBrandImage?.setOnClickListener { openBrowser(url) }

        // for better UX
        sponsorDetailVideos?.isFocusable = false
        sponsorDetailVideos.isNestedScrollingEnabled = false;

        sponsorDetailDescription?.text = sponsor.description
        sponsorDetailVideos?.let {
            it.layoutManager = LinearLayoutManager(it.context)
            it.adapter = RecyclerAdapter(this, sponsor.videos)
        }
    }

    fun openBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
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
                val intent = YouTubeStandalonePlayer.createVideoIntent(
                    activity,
                    key,
                    sponsorVideo.youtubeVideoCode
                )
                activity?.startActivity(intent)
            }

            holder.view.sponsorDetailCardViewTitle?.text = sponsorVideo.videoTitle

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
