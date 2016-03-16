package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.bumptech.glide.Glide
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.RestAPIService.RestAPIHelper
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.cardview_recipes_section_overview.view.*
import kotlinx.android.synthetic.main.fragment_recipes_section_overview.view.*
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Subscriber
import rx.schedulers.Schedulers
import kotlin.properties.Delegates

class RecipesSectionOverviewFragment : Fragment() {

    companion object {
        val TAG = RecipesSectionOverviewFragment::class.java.name
    }

    private var realm: Realm by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
        realm.close()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_recipes_section_overview, container, false)

        Log.e(TAG, "onCreateView")
        setupRecyclerView(view?.recipe_fragment_recycler_view!!)
        view?.recipe_fragment_swipe_to_refresh?.setOnRefreshListener {
            val recipeOverviews = Intent(activity, RecipesSetupService::class.java)
            activity.startService(recipeOverviews)
            view.recipe_fragment_swipe_to_refresh?.isRefreshing = false
        }

        setHasOptionsMenu(true)
        return view
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
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
            Log.e(TAG, "data changed")
            recyclerView.adapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = RecyclerAdapter(activity as AppCompatActivity?, results)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val orderItem = menu?.findItem(R.id.action_order)
        val latestItem = orderItem?.subMenu?.findItem(R.id.action_order_latest)
        latestItem?.setOnMenuItemClickListener { x ->
            var results: RealmResults<Recipes>

            results = realm.where(Recipes::class.java)
                .findAllSortedAsync("updatedAt", Sort.DESCENDING)
            results.addChangeListener {
                view?.recipe_fragment_recycler_view?.adapter?.notifyDataSetChanged()
            }

            (view?.recipe_fragment_recycler_view?.adapter as RecyclerAdapter).let {
                it.updateList(results)
            }

            true
        }
        val popularItem = orderItem?.subMenu?.findItem(R.id.action_order_popular)
        popularItem?.setOnMenuItemClickListener { x ->
            var results: RealmResults<Recipes>

            results = realm.where(Recipes::class.java)
                .findAllSortedAsync("hits", Sort.DESCENDING)
            results.addChangeListener {
                view?.recipe_fragment_recycler_view?.adapter?.notifyDataSetChanged()
            }

            (view?.recipe_fragment_recycler_view?.adapter as RecyclerAdapter).let {
                it.updateList(results)
            }

            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.w(TAG, "onDestroyView")
    }

}


class RecyclerAdapter(
    val activity: AppCompatActivity?,
    var recipeList: List<Recipes>)
: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    val TAG = RecyclerAdapter::class.java.name

    fun updateList(recipeList: List<Recipes>) {
        Log.e(TAG, "update list ${recipeList.size}")
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
        viewHolder.view.recipeInstructorText?.text = recipe.chefName
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

        viewHolder.view.collect_button.setOnClickListener {

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            recipe.favorite = !recipe.favorite
            realm.commitTransaction()
            realm.close()

            (activity as? AppCompatActivity)?.let {
                val sp: SharedPreferences = it.getSharedPreferences("DATA", 0)
                val token = sp.getString("token", "")
                if (token.isEmpty()) {
                    val intent = Intent(it, LoginActivity::class.java)
                    it.startActivity(intent)

                } else {
                    val combinedHeaderToken = "Bearer " + token
                    postCollectRecipeActionToServer(combinedHeaderToken, recipe)
                }
            }
        }
    }

    private fun postCollectRecipeActionToServer(header: String, recipe: Recipes) {
        RestAPIHelper.restApiService
            .addRemoveFavorite(header, recipe.id)
            .subscribeOn(Schedulers.io())
            .subscribe(object : Subscriber<JsonModel.MyFavoriteRecipesResult>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                    System.out.println(e?.message)
                }

                override fun onNext(x: JsonModel.MyFavoriteRecipesResult) {
                    Log.e(TAG, "x = " + x)
                }
            })
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }
}
