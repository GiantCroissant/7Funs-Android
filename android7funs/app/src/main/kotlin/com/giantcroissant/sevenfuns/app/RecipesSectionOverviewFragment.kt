package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.google.gson.Gson
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.cardview_recipes_section_overview.view.*
import kotlinx.android.synthetic.main.fragment_recipes_section_overview.view.*
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import kotlin.properties.Delegates

class RecipesSectionOverviewFragment : Fragment() {
    val TAG = RecipesSectionOverviewFragment::class.java.name

    private var realm: Realm by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getInstance(activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_recipes_section_overview, container, false)
        view?.recipe_recycler_view?.let {
            it.layoutManager = LinearLayoutManager(it.context)
            it.adapter = RecyclerAdapter((activity as? AppCompatActivity), listOf<Recipes>())
        }

        view?.recipe_swipe_to_refresh?.setOnRefreshListener {
            val recipeOverviews = Intent(activity, RecipesSetupService::class.java)
            activity.startService(recipeOverviews)
            view.recipe_swipe_to_refresh?.isRefreshing = false
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val recipeAdapter = view?.recipe_recycler_view?.adapter as RecyclerAdapter

        var results: RealmResults<Recipes>
        if (arguments.getString("type") == "collection") {
            results = realm.where(Recipes::class.java)
                .equalTo("favorite", true)
                .findAllSortedAsync("id", Sort.DESCENDING)

        } else {
            results = realm.where(Recipes::class.java)
                .findAllSortedAsync("id", Sort.DESCENDING)
        }
        results.addChangeListener {
            Log.d(TAG, "results.size = ${results.size}")
            recipeAdapter?.notifyDataSetChanged()
        }
        recipeAdapter?.updateList(results)
    }

    override fun onPause() {
        super.onPause()
        var results: RealmResults<Recipes>

        if (arguments.getString("type") == "collection") {
            results = realm.where(Recipes::class.java)
                .equalTo("favorite", true)
                .findAllSortedAsync("id", Sort.DESCENDING)

        } else {
            results = realm.where(Recipes::class.java).findAllSortedAsync("id", Sort.DESCENDING)
        }

        results.removeChangeListener { }
    }

    class RecyclerAdapter(
        val activity: AppCompatActivity?,
        var recipeList: List<Recipes>)
    : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        val TAG = RecyclerAdapter::class.java.name

        fun updateList(recipeList: List<Recipes>) {
            Log.d(TAG, "update list ${recipeList.size}")
            this.recipeList = recipeList
            this.notifyDataSetChanged()
        }

        class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            var view: View by Delegates.notNull()

            init {
                view = v
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.cardview_recipes_section_overview, viewGroup, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val recipe = recipeList[position]
            val baseUrl = "https://commondatastorage.googleapis.com/funs7-1/uploads/recipe/image/"
            val imageUrl = baseUrl + recipe.id + "/" + recipe.image

            Glide.with(activity?.applicationContext)
                .load(imageUrl)
                .placeholder(R.drawable.food_default)
                .centerCrop()
                .dontAnimate()
                .into(viewHolder.view.recipe_image)

            viewHolder.view.fav_icon.visibility = if (recipe.favorite) View.VISIBLE else View.INVISIBLE
            viewHolder.view.recipe_title?.text = recipe.title
            viewHolder.view.recipe_hits_text?.text = "${recipe.collected} 人收藏，${recipe.hits} 人看過"
            viewHolder.view.detail_button?.setOnClickListener {
                val descList = recipe.methods.map { md -> md.desc }
                val intent = Intent(this.activity, RecipesDetailActivity::class.java)
                intent.putExtra(
                    "recipes",
                    RecipesParcelable(
                        recipe.id,
                        recipe.title,
                        recipe.ingredient,
                        recipe.seasoning,
                        descList,
                        recipe.reminder
                    )
                )
                this.activity?.startActivity(intent)
            }

            viewHolder.view.collect_button.setOnClickListener {
                val realm = Realm.getInstance(activity)
                realm.beginTransaction()
                recipe.favorite = !recipe.favorite
                Log.d(TAG, "recipe favorite = ${recipe.favorite}")
                realm.commitTransaction()
                realm.close()

                val retrofit = Retrofit
                        .Builder()
                        .baseUrl("https://www.7funs.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build()

                val restApiService = retrofit.create(RestApiService::class.java)

                (activity as? AppCompatActivity)?.let { a ->
                    val sp: SharedPreferences =  a.getSharedPreferences("DATA", 0)
                    val token = sp.getString("token", "")

                    if (token.isEmpty()) {
                        System.out.println("No cached token")

                        val intent = Intent(a, LoginActivity::class.java)
                        a.startActivity(intent)
                    } else {
                        System.out.println("Have cached token: " + token)

                        // Do something with token
                        //val intent = Intent(a.applicationContext, QADetailNewMessageActivity::class.java)
                        //a.startActivityForResult(intent, QASectionOverviewFragment.WRITTEN_MESSAGE)


                        val combinedHeaderToken = "Bearer " + token

                        restApiService.addRemoveFavorite(combinedHeaderToken, recipe.id)
                            .subscribeOn(Schedulers.io())
                            .subscribe(object : Subscriber<JsonModel.MyFavoriteRecipesResult>() {
                                override fun onCompleted() {
                                }

                                override fun onError(e: Throwable?) {
                                    System.out.println(e?.message)
                                }

                                override fun onNext(x: JsonModel.MyFavoriteRecipesResult) {
                                }
                            })
//                        RestAPIHelper.restApiService
//                            .addRemoveFavorite(recipe.id)
//                            .subscribeOn(Schedulers.io())
//                            .subscribe({ json ->
//                                // MyFavoriteRecipesResult
//                                Log.e(TAG, "json: $json")
//
//                            }, { error ->
//                                Log.e(TAG, "error $error")
//                            })

                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return recipeList.size
        }
    }

    // Get category and tag for search use?
    fun fetchTags() {
        val retrofit = Retrofit
            .Builder()
            .baseUrl("https://www.7funs.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()

        val restApiService = retrofit.create(RestApiService::class.java)

        val recipesByTag = restApiService.getCategories()
            .observeOn(Schedulers.io())
            .map { x ->
                x.flatMap { c ->
                    val ids = c.subCategories.map { sc ->
                        sc.id
                    }
                    ids
                }
            }
            .flatMap { x -> Observable.from(x) }
            .flatMap { x -> Observable.just(x) }
            .flatMap { x -> restApiService.getTagById(x) }
            .map { x ->
                val recipesIds = x.taggings.map { t -> t.taggableId }
                MiscModel.RecipesByTag(x.id, recipesIds)
            }

        // Subscribe for further processing
        recipesByTag.subscribe(object : Subscriber<MiscModel.RecipesByTag>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                System.out.println(e?.message)
            }

            override fun onNext(recipesByTag: MiscModel.RecipesByTag) {
            }
        })
    }
}