package com.giantcroissant.sevenfuns.app

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.giantcroissant.sevenfuns.app.QandA.QASectionOverviewFragment
import com.giantcroissant.sevenfuns.app.RestAPIService.RestAPIHelper
import com.github.salomonbrys.kotson.fromJson
import com.google.android.youtube.player.internal.i
import com.google.android.youtube.player.internal.v
import com.google.gson.Gson
import kotlinx.android.synthetic.main.cardview_sponsor_section_overview.view.*
import kotlinx.android.synthetic.main.fragment_qa_section_overview.*
import kotlinx.android.synthetic.main.fragment_sponsor_section_overview.*
import kotlinx.android.synthetic.main.fragment_sponsor_section_overview.view.*
import org.joda.time.DateTime
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
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

    override fun onResume() {
        super.onResume()

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_sponsor_section_overview, container, false)

        (activity as? AppCompatActivity)?.let {
            RestAPIHelper.restApiService
                .getSponsorQuery(currentPage)
                .map { msgJson ->
                    msgJson.collection
                }
                .observeOn((AndroidSchedulers.mainThread()))
                .subscribeOn(Schedulers.io())
                .subscribe({ x ->

//                    System.out.println(x)

                    view?.let {
                        it.sponsorSectionOverview.layoutManager = LinearLayoutManager(it.context)
                        it.sponsorSectionOverview.adapter = RecyclerAdapter(activity as AppCompatActivity, x)
                    }

                }, { error ->
                    Snackbar.make(coordinator_view, "網路狀態不穩", Snackbar.LENGTH_LONG).show()
                })
//            val sponsorData = Observable.create(Observable.OnSubscribe<kotlin.String> { t ->
//                try {
//                    val inputStream = it.assets.open("sponsor-data.json")
//                    val inputStreamReader = InputStreamReader(inputStream)
//                    val sb = StringBuilder()
//                    val br = BufferedReader(inputStreamReader)
//                    var read = br.readLine()
//                    while (read != null) {
//                        sb.append(read)
//                        read = br.readLine()
//                    }
//                    t?.onNext(sb.toString())
//                    t?.onCompleted()
//                } catch(e: Exception) {
//                    t?.onError(e)
//                }
//            })
//
//            sponsorData
//                //.observeOn(Schedulers.io())
//                .map { dataString -> Gson().fromJson<JsonModel.SponsorCollectionJsonObject>(dataString) }
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : Subscriber<JsonModel.SponsorCollectionJsonObject>() {
//                    override fun onNext(x: JsonModel.SponsorCollectionJsonObject) {
//                        view?.let {
//                            it.sponsorSectionOverview.layoutManager = LinearLayoutManager(it.context)
//                            it.sponsorSectionOverview.adapter = RecyclerAdapter(activity as AppCompatActivity, x.sponsors)
//                        }
//                    }
//
//                    override fun onError(e: Throwable?) {
//                        System.out.println(e?.message)
//                    }
//
//                    override fun onCompleted() {
//                    }
//                })
        }
        return view
    }

    var currentPage = 1

    private fun fetchSponsors(onComplete: () -> Unit = {}) {
        RestAPIHelper.restApiService
            .getSponsorQuery(currentPage)
            .map { msgJson ->
                msgJson.collection.sortedBy { json -> json.id }
            }
            .observeOn((AndroidSchedulers.mainThread()))
            .subscribeOn(Schedulers.io())
            .subscribe({ x ->

            }, { error ->
                Snackbar.make(coordinator_view, "網路狀態不穩", Snackbar.LENGTH_LONG).show()
            })
//            .getMessageQuery(currentPage)
//                .map { msgJsonList ->
//                    msgJsonList.collection.sortedByDescending {
//                        json ->
//                        DateTime(json.updatedAt)
//                    }
//                }
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ jsonList ->
//                    val recycler = qaSectionOverview.adapter as QASectionOverviewFragment.RecyclerAdapter
//                    recycler.addAll(jsonList)
//                    currentPage += 1
//                    onComplete()
//
//                }, { error ->
//                    Log.e(TAG, "error = $error")
//                    Snackbar.make(coordinator_view, "網路狀態不穩", Snackbar.LENGTH_LONG).show()
//                    onComplete()
//                })
    }

    class RecyclerAdapter(
        val activity: AppCompatActivity,
        //val sponsorList: List<JsonModel.SponsorJsonObject>
        val sponsorDetailList: List<JsonModel.SponsorDetailJsonObject>

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
            //val sponsor = sponsorList[position]
            val sponsor = sponsorDetailList[position]
            holder.view.sponsorSectionOverviewCardViewTitle?.text = sponsor.name
//            val imagePath = "file:///android_asset/sponsors/" + sponsor.image + ".png"

            val baseUrl = "https://commondatastorage.googleapis.com/funs7-1/uploads/sponsor/image/"
            val imagePath = baseUrl + sponsor.id + "/" + sponsor.image

            Glide.with(activity?.applicationContext)
                .load(Uri.parse(imagePath))
                .centerCrop()
                .into(holder.view.sponsorSectionOverviewCardViewImage)

            holder.view.setOnClickListener {
                System.out.println(sponsor)

                SponsorActivity.navigate(activity, sponsor)
            }
        }

        override fun getItemCount(): Int {
//            return sponsorList.size
            return sponsorDetailList.size
        }
    }

}