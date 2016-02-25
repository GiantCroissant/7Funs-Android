package com.giantcroissant.sevenfuns.app

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.google.gson.Gson
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.cardview_recipes_section_overview.view.*
import kotlinx.android.synthetic.main.fragment_recipes_section_overview.*
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
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.getString("query")?.let {
            query = it
        }
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

        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        //        searchItem.isVisible = showSearch
        val manager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setSearchableInfo(manager.getSearchableInfo(activity.componentName))
        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onResume() {
        super.onResume()
        if (query != null && !query.isEmpty()) {
            queryRecipesWithSearch(query)

        } else {
            queryRecipes()
        }
    }

    private fun queryRecipesWithSearch(search: String) {
        var results: RealmResults<Recipes>
        if (arguments.getString("type") == "collection") {
            results = realm.where(Recipes::class.java)
                .equalTo("favorite", true)
                .contains("title", search)
                .findAllSortedAsync("id", Sort.DESCENDING)

        } else {
            results = realm.where(Recipes::class.java)
                .contains("title", search)
                .findAllSortedAsync("id", Sort.DESCENDING)
        }

        results.addChangeListener {
            recipe_recycler_view?.adapter?.notifyDataSetChanged()
        }

        (recipe_recycler_view?.adapter as RecyclerAdapter).let {
            it.updateList(results)
        }

        val bar = Snackbar.make(
            recipe_coordinator_view,
            "目前顯示搜尋結果：$search",
            Snackbar.LENGTH_INDEFINITE
        )
        bar.setAction("返回全部") {
            query = ""
            (activity as? MainActivity)?.let {
                it.query = ""
            }
            bar.dismiss()
            queryRecipes()
        }
        bar.show()
    }

    private fun queryRecipes() {
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
            recipe_recycler_view?.adapter?.notifyDataSetChanged()
        }

        (recipe_recycler_view?.adapter as RecyclerAdapter).let {
            it.updateList(results)
        }
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
                    val sp: SharedPreferences = a.getSharedPreferences("DATA", 0)
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
//
//class CustomBehavior(
//    context: Context,
//    attrs: AttributeSet
//) : CoordinatorLayout.Behavior<View>(context, attrs) {
//
//    var height = 0f
//
//    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
//        Log.e("TAG", "onDependentViewChanged dependency")
//
//        val translationY = Math.min(0f, dependency!!.translationY - dependency.height)
//        child!!.translationY = translationY
//        height = translationY
//        return true
//    }
//
//    override fun onDependentViewRemoved(parent: CoordinatorLayout?, child: View?, dependency: View?) {
////        val translationY = Math.min(0f, dependency!!.translationY - dependency.height)
////        child!!.translationY = -height
//        onDependentViewChanged(parent, child, dependency)
//    }
//
//    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
//        // we only want to trigger the change
//        // only when the changes is from a snackbar
//        return dependency is Snackbar.SnackbarLayout
//    }
//}