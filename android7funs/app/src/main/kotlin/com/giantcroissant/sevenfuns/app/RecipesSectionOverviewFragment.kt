package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.cardview_recipes_section_overview.view.*
import kotlin.properties.Delegates

import kotlinx.android.synthetic.main.fragment_recipes_section_overview.view.*

import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import io.realm.Sort
//import kotlinx.android.synthetic.main.fragment_qa_section_overview.view.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by apprentice on 2/1/16.
 */
class RecipesSectionOverviewFragment : Fragment() {
    public companion object {
        public fun newInstance(): RecipesSectionOverviewFragment {
            val fragment = RecipesSectionOverviewFragment().apply {
                val args = Bundle().apply {
                }

                arguments = args
            }

            return fragment
        }
    }

    private var config: RealmConfiguration by Delegates.notNull()

    private var refreshCount: Int = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //System.out.println("RecipesSectionOverviewFragment - onCreateView");

        val view = inflater?.inflate(R.layout.fragment_recipes_section_overview, container, false)

        view?.recipesSectionOverview?.let {
            it.layoutManager = LinearLayoutManager(it.context)
        }

        view?.recipesSectionSwipeContainer?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                System.out.println("it is freshing")
                (activity as? AppCompatActivity)?.let {
                    config = RealmConfiguration.Builder(it.applicationContext).build()
                    val realm = Realm.getInstance(config)
                    val a = it
//                    val query = realm.where(Recipes::class.java).findAllAsync()
//                    query.sort("id")
                    val query = realm.where(Recipes::class.java).findAllSortedAsync("id", Sort.DESCENDING)
                    query.asObservable()
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .observeOn(Schedulers.io())
                            .filter { x -> x.isLoaded }
                            .flatMap { xs -> Observable.from(xs) }
                            //.skip(30 * refreshCount)
                            .take(30 * refreshCount + 30)
                            .buffer(30 * refreshCount + 30)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe { x ->
                                refreshCount = refreshCount + 1
                                view?.recipesSectionOverview?.let {
//                                    it.layoutManager = LinearLayoutManager(it.context)
                                    it.adapter = RecyclerAdapter((activity as? AppCompatActivity), x)
                                    //(it.adapter as? RecyclerAdapter)?.clearAll()
                                    //(it.adapter as? RecyclerAdapter)?.addAll(x)
                                }
                            }
                }

                view?.recipesSectionSwipeContainer?.isRefreshing = false
            }
        })

//        val view = inflater?.inflate(R.layout.fragment_recipes_section_overview, container, false) as? RecyclerView
//
        (activity as? AppCompatActivity)?.let {
            config = RealmConfiguration.Builder(it.applicationContext).build()
            val realm = Realm.getInstance(config)

            val query = realm.where(Recipes::class.java).findAllSortedAsync("id", Sort.DESCENDING)
//            query.sort("id")
            query.asObservable()
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .observeOn(Schedulers.io())
                    .filter { x -> x.isLoaded }
                    .flatMap { xs -> Observable.from(xs) }
                    .buffer(30)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe { x ->
                        //System.out.println("Subscribe to show onto ui")
                        view?.recipesSectionOverview?.let {
                            //System.out.println("swipe ui is found")
                            //it.layoutManager = LinearLayoutManager(it.context)
                            it.adapter = RecyclerAdapter(
                                    (activity as? AppCompatActivity),
                                    x
//                                    listOf(
//                                            JsonModel.RecipesJsonModel(1, "", "", "", "Beef", "", "", listOf("", ""), "", 0, "", "", 0, 0, 0),
//                                            JsonModel.RecipesJsonModel(2, "", "", "", "Soup", "", "", listOf("", ""), "", 3, "", "", 0, 0, 0),
//                                            JsonModel.RecipesJsonModel(3, "", "", "", "Cake", "", "", listOf("", ""), "", 6, "", "", 0, 0, 0)
//                                    )
                            )
                        }
                    }

        }

        return view
    }

    public class RecyclerAdapter(val activity: AppCompatActivity?, val recipesList: List<Recipes>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        public class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            public var view: View by Delegates.notNull()

            init {
                view = v
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) : ViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.cardview_recipes_section_overview, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val r = recipesList[i]

            viewHolder.view?.recipesSectionOverviewCardViewTitle?.text = r.title

            viewHolder.view?.recipesSectionOverviewCardViewDetail?.setOnClickListener { x ->
                (activity as? AppCompatActivity)?.let {
                    val descList = r.methods.map { md -> md.desc }
                    val intent = Intent(x.context, RecipesDetailActivity::class.java)
                    intent?.putExtra("recipes", RecipesParcelable(r.id.toInt(), r.title, r.ingredient, r.seasoning, descList, r.reminder))

                    x.context.startActivity(intent)
                }
            }
        }

        override fun getItemCount() : Int { return recipesList.size }

//        public fun clearAll() {
        //            recipesList.clear()
        //            notifyDataSetChanged()
        //        }
        //
        //        public fun addAll(contents: List<Recipes>) {
        //            recipesList.addAll(contents)
        //            notifyDataSetChanged()
        //        }

//        public fun appendAfter(contents: List<Recipes>) {
//            recipesList.add
//        }
    }
}