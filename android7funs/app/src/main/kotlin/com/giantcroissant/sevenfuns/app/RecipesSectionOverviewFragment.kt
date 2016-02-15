package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.google.android.youtube.player.internal.i
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.cardview_recipes_section_overview.view.*
import kotlinx.android.synthetic.main.fragment_recipes_section_overview.view.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import kotlin.properties.Delegates

class RecipesSectionOverviewFragment : Fragment() {

    companion object {
        fun newInstance(): RecipesSectionOverviewFragment {
            val fragment = RecipesSectionOverviewFragment().apply {
                val args = Bundle().apply {
                }
                arguments = args
            }
            return fragment
        }
    }

    private var realm: Realm by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getInstance(this.context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_recipes_section_overview, container, false)
        view?.recipesSectionOverview?.let {
            it.layoutManager = LinearLayoutManager(it.context)
            it.adapter = RecyclerAdapter((activity as? AppCompatActivity), listOf<Recipes>())
        }

        view?.recipesSectionSwipeContainer?.setOnRefreshListener {
            view.recipesSectionSwipeContainer?.isRefreshing = false
        }

        val query = realm.where(Recipes::class.java).findAllSortedAsync("id", Sort.DESCENDING)
        query.asObservable()
            .filter { x -> x.isLoaded }
            .flatMap { xs -> Observable.from(xs) }
            .buffer(30)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { x ->
                view?.recipesSectionOverview?.adapter = RecyclerAdapter((activity as? AppCompatActivity), x)
            }
        return view
    }

    class RecyclerAdapter(val activity: AppCompatActivity?, val recipeList: List<Recipes>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

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
                .centerCrop()
                .into(viewHolder.view.recipesSectionOverviewCardViewImage)

            viewHolder.view.recipesSectionOverviewCardViewTitle?.text = recipe.title

            viewHolder.view.recipesSectionOverviewCardViewDetail?.setOnClickListener {
                val descList = recipe.methods.map { md -> md.desc }
                val intent = Intent(this.activity, RecipesDetailActivity::class.java)
                intent.putExtra("recipes", RecipesParcelable(recipe.id,
                    recipe.title,
                    recipe.ingredient,
                    recipe.seasoning,
                    descList, recipe.reminder)
                )
                this.activity?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return recipeList.size
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}