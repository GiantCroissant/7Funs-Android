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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.cardview_instructor_section_overview.view.*
import kotlinx.android.synthetic.main.fragment_instructor_section_overview.view.*
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/1/16.
 */
class InstructorSectionOverviewFragment : Fragment() {

    companion object {
        fun newInstance(): InstructorSectionOverviewFragment {
            val fragment = InstructorSectionOverviewFragment().apply {
                val args = Bundle().apply {
                }
                arguments = args
            }
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_instructor_section_overview, container, false)

        (activity as? AppCompatActivity)?.let {
            val sponsorData = Observable.create(Observable.OnSubscribe<kotlin.String> { t ->
                try {
                    val inputStream = it.assets.open("instructor-data.json")
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
                .map { dataString -> Gson().fromJson<JsonModel.InstructorCollectionJsonObject>(dataString) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<JsonModel.InstructorCollectionJsonObject>() {
                    override fun onNext(x: JsonModel.InstructorCollectionJsonObject) {
                        view?.let { v ->
                            v.instructorSectionOverview.layoutManager = LinearLayoutManager(v.context)
                            v.instructorSectionOverview.adapter = InstructorSectionOverviewFragment.RecyclerAdapter((activity as? AppCompatActivity), x.instructors)
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

    class RecyclerAdapter(val activity: AppCompatActivity?, val instructorList: List<JsonModel.InstructorJsonObject>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            var view: View by Delegates.notNull()

            init {
                view = v
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_instructor_section_overview, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val r = instructorList[i]

            viewHolder.view.instructorSectionOverviewCardViewTitle?.text = r.name
            val imagePath = "file:///android_asset/instructors/" + r.image + ".png"

            Glide.with(activity?.applicationContext)
                .load(Uri.parse(imagePath))
                .centerCrop()
                .fitCenter()
                .into(viewHolder.view.instructorSectionOverviewCardViewImage)

            viewHolder.view.instructorSectionOverviewCardViewExpand?.setOnClickListener { x ->
                //                (activity as? AppCompatActivity)?.let {
                //                    val intent = Intent(x.context, RecipesDetailActivity::class.java)
                //                    intent?.putExtra("recipes", RecipesParcelable(r.id, r.title))
                //
                //                    x.context.startActivity(intent)
                //                }
            }
        }

        override fun getItemCount(): Int {
            return instructorList.size
        }
    }
}