package com.giantcroissant.sevenfuns.app

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.github.salomonbrys.kotson.fromJson
import com.google.android.youtube.player.internal.i
import com.google.android.youtube.player.internal.v
import com.google.gson.Gson
import kotlinx.android.synthetic.main.cardview_sponsor_section_overview.view.*
import kotlinx.android.synthetic.main.fragment_sponsor_section_overview.*
import kotlinx.android.synthetic.main.fragment_sponsor_section_overview.view.*
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/1/16.
 */
class SponsorSectionOverviewFragment : Fragment() {

    companion object {
        fun newInstance(): SponsorSectionOverviewFragment {
            val fragment = SponsorSectionOverviewFragment().apply {
                val args = Bundle().apply {
                }
                arguments = args
            }
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_sponsor_section_overview, container, false)

        (activity as? AppCompatActivity)?.let {
            val sponsorData = Observable.create(Observable.OnSubscribe<kotlin.String> { t ->
                try {
                    val inputStream = it.assets.open("sponsor-data.json")
                    val inputStreamReader = InputStreamReader(inputStream)
                    val sb = StringBuilder()
                    val br = BufferedReader(inputStreamReader)
                    var read = br.readLine()
                    while (read != null) {
                        sb.append(read)
                        read = br.readLine()
                    }
                    t?.onNext(sb.toString())
                    t?.onCompleted()
                } catch(e: Exception) {
                    t?.onError(e)
                }
            })

            sponsorData
                //.observeOn(Schedulers.io())
                .map { dataString -> Gson().fromJson<JsonModel.SponsorCollectionJsonObject>(dataString) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<JsonModel.SponsorCollectionJsonObject>() {
                    override fun onNext(x: JsonModel.SponsorCollectionJsonObject) {
                        view?.let {
                            it.sponsorSectionOverview.layoutManager = LinearLayoutManager(it.context)
                            it.sponsorSectionOverview.adapter = RecyclerAdapter(activity as AppCompatActivity, x.sponsors)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        System.out.println(e?.message)
                    }

                    override fun onCompleted() {
                    }
                })
        }
        return view
    }

    class RecyclerAdapter(
        val activity: AppCompatActivity,
        val sponsorList: List<JsonModel.SponsorJsonObject>

    ) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            var view: View by Delegates.notNull()
            init {
                view = v
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_sponsor_section_overview, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sponsor = sponsorList[position]
            holder.view.sponsorSectionOverviewCardViewTitle?.text = sponsor.name
            val imagePath = "file:///android_asset/sponsors/" + sponsor.image + ".png"

            Glide.with(activity?.applicationContext)
                .load(Uri.parse(imagePath))
                .centerCrop()
                .into(holder.view.sponsorSectionOverviewCardViewImage)

            holder.view.setOnClickListener {
                SponsorActivity.navigate(activity, sponsor)
            }
        }

        override fun getItemCount(): Int {
            return sponsorList.size
        }
    }

}