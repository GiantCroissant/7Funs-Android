package com.giantcroissant.sevenfuns.app

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.RestAPIService.RestAPIHelper
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.cardview_recipes_section_overview.view.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/25/16.
 */
class SearchActivity : AppCompatActivity() {

    companion object {
        val TAG = SearchActivity::class.java.name
    }
    private var realm: Realm by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        realm = Realm.getDefaultInstance()

        System.out.println("SearchActivity - onCreate")

        recipe_recycler_view?.let {
            it.layoutManager = LinearLayoutManager(it.context)
            it.adapter = RecyclerAdapter(this, listOf<Recipes>())
        }

        val query = intent.getStringExtra(SearchManager.QUERY)
        Log.e(TAG, "query = " + query)
        supportActionBar?.title = query

        if (Intent.ACTION_SEARCH.equals(intent.action)) {
            val query = intent.getStringExtra(SearchManager.QUERY)

            //val realm = Realm.getInstance(this)

            System.out.println(query)

            val results = realm.where(Recipes::class.java)
                    //.equalTo("favorite", true)
                    .contains("title", query)
                    .or()
                    .contains("chefName", query)
                    .findAllSortedAsync("id", Sort.DESCENDING)

            results.addChangeListener {
                recipe_recycler_view?.adapter?.notifyDataSetChanged()
            }

            (recipe_recycler_view?.adapter as RecyclerAdapter).let {
                it.updateList(results)
            }


            //realm.close()
            //doMySearch(query);
        } else if (Intent.ACTION_VIEW.equals(intent.action)) {
//            val data = intent.data.toString()
//            System.out.println(data)
            val uri = intent.dataString

            //savedInstanceState?

            val queryPair = uri.split('/')
            val tagId = queryPair.last()
            supportActionBar?.title = queryPair.first()

            RestAPIHelper.restApiService.getTagById(tagId.toInt())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ tagJsonObject ->
                    //tagJsonObject.taggings

                    val query = realm.where(Recipes::class.java)
                    val accQuery = tagJsonObject.taggings.fold(query, { acc, taggingJsonObject ->
                        acc.equalTo("id", taggingJsonObject.taggableId).or()
                    })

                    val results = accQuery.findAllSortedAsync("id", Sort.DESCENDING)

                    results.addChangeListener {
                        recipe_recycler_view?.adapter?.notifyDataSetChanged()
                    }

                    (recipe_recycler_view?.adapter as RecyclerAdapter).let {
                        it.updateList(results)
                    }

                }, { error ->
                    System.out.println(error.toString())
                })


            //            queryPair.last { id ->
//                //id.toInt()
//            }

            System.out.println(uri)
            System.out.println(tagId)

            //realm.close()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


////    override fun onResume() {
////        super.onResume()
////        if (query != null && !query.isEmpty()) {
////            queryRecipesWithSearch(query)
////
////        } else {
////            queryRecipes()
////        }
////    }
//
//    private fun queryRecipesWithSearch(search: String) {
//        var results: RealmResults<Recipes>
////        if (arguments.getString("type") == "collection") {
////            results = realm.where(Recipes::class.java)
////                    .equalTo("favorite", true)
////                    .contains("title", search)
////                    .findAllSortedAsync("id", Sort.DESCENDING)
////
////        } else {
//            results = realm.where(Recipes::class.java)
//                    .contains("title", search)
//                    .findAllSortedAsync("id", Sort.DESCENDING)
////        }
//
//        results.addChangeListener {
//            recipe_recycler_view?.adapter?.notifyDataSetChanged()
//        }
//
//        (recipe_recycler_view?.adapter as RecipesSectionOverviewFragment.RecyclerAdapter).let {
//            it.updateList(results)
//        }
//
//        val bar = Snackbar.make(
//                recipe_coordinator_view,
//                "目前顯示搜尋結果：$search",
//                Snackbar.LENGTH_INDEFINITE
//        )
////        bar.setAction("返回全部") {
////            query = ""
////            (activity as? MainActivity)?.let {
////                it.query = ""
////            }
////            bar.dismiss()
////            queryRecipes()
////        }
//        bar.show()
//    }
//
//    private fun queryRecipes() {
//        var results: RealmResults<Recipes>
////        if (arguments.getString("type") == "collection") {
////            results = realm.where(Recipes::class.java)
////                    .equalTo("favorite", true)
////                    .findAllSortedAsync("id", Sort.DESCENDING)
////
////        } else {
//            results = realm.where(Recipes::class.java)
//                    .findAllSortedAsync("id", Sort.DESCENDING)
////        }
//        results.addChangeListener {
//            recipe_recycler_view?.adapter?.notifyDataSetChanged()
//        }
//
//        (recipe_recycler_view?.adapter as RecipesSectionOverviewFragment.RecyclerAdapter).let {
//            it.updateList(results)
//        }
//    }
//
    class RecyclerAdapter(val activity: AppCompatActivity?, var recipeList: List<Recipes>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
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

            viewHolder.view.recipeInstructorText?.text = recipe.chefName
            viewHolder.view.recipe_title?.text = recipe.title
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
                                recipe.image,
                                recipe.title,
                                recipe.ingredient,
                                recipe.seasoning,
                                descList,
                                recipe.reminder
                        )
                )
                this.activity?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return recipeList.size
        }

    }
//
//        val TAG = RecyclerAdapter::class.java.name
//
//        fun updateList(recipeList: List<Recipes>) {
//            Log.d(TAG, "update list ${recipeList.size}")
//            this.recipeList = recipeList
//            this.notifyDataSetChanged()
//        }
//
//        class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
//            var view: View by Delegates.notNull()
//
//            init {
//                view = v
//            }
//        }
//
//        override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
//            val view = LayoutInflater.from(viewGroup.context)
//                    .inflate(R.layout.cardview_recipes_section_overview, viewGroup, false)
//            return ViewHolder(view)
//        }
//
//        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//            val recipe = recipeList[position]
//            val baseUrl = "https://commondatastorage.googleapis.com/funs7-1/uploads/recipe/image/"
//            val imageUrl = baseUrl + recipe.id + "/" + recipe.image
//
//            Glide.with(activity?.applicationContext)
//                    .load(imageUrl)
//                    .placeholder(R.drawable.food_default)
//                    .centerCrop()
//                    .into(viewHolder.view.recipe_image)
//
//            viewHolder.view.fav_icon.visibility = if (recipe.favorite) View.VISIBLE else View.INVISIBLE
//            viewHolder.view.recipe_title?.text = recipe.title
//            viewHolder.view.recipe_hits_text?.text = "${recipe.collected} 人收藏，${recipe.hits} 人看過"
//            viewHolder.view.detail_button?.setOnClickListener {
//                val descList = recipe.methods.map { md -> md.desc }
//                val intent = Intent(this.activity, RecipesDetailActivity::class.java)
//                intent.putExtra(
//                        "recipes",
//                        RecipesParcelable(
//                                recipe.id,
//                                recipe.title,
//                                recipe.ingredient,
//                                recipe.seasoning,
//                                descList,
//                                recipe.reminder
//                        )
//                )
//                this.activity?.startActivity(intent)
//            }
//        }
//
//        override fun getItemCount(): Int {
//            return recipeList.size
//        }
//    }
}
