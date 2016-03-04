package com.giantcroissant.sevenfuns.app

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_recipes_detail.*
import kotlinx.android.synthetic.main.cardview_recipes_detail.view.*
import kotlinx.android.synthetic.main.cardview_recipes_section_overview.view.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by apprentice on 2/1/16.
 */
class RecipesDetailActivity : AppCompatActivity() {
    val TAG = RecipesDetailActivity::class.java.name

    var youtubeFragment = YouTubePlayerSupportFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes_detail)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recipe = intent.getParcelableExtra<RecipesParcelable>("recipes")
        supportActionBar?.title = recipe.title

        if (recipe.ingredient.isEmpty()) {
            ingredientCardView.visibility = View.GONE

        } else {
            ingredientCardView?.detail_title?.text = "材料"
            ingredientCardView?.detail_content?.text = recipe.ingredient
        }

        if (recipe.seasoning.isEmpty()) {
            seasoningCardView.visibility = View.GONE

        } else {
            seasoningCardView?.detail_title?.text = "調味料"
            seasoningCardView?.detail_content?.text = recipe.seasoning
        }

        if (recipe.methods.isEmpty()) {
            methodCardView.visibility = View.GONE

        } else {
            methodCardView?.detail_title?.text = getString(R.string.recipe_detail_method)
            val adjustedContent = recipe.methods.reduce { acc, s -> acc + s + "\n\n" }
            val methods = adjustedContent.substring(0, adjustedContent.length - 4)
            methodCardView?.detail_content?.text = methods
        }

        if (recipe.reminder.isEmpty()) {
            reminderCardView.visibility = View.GONE

        } else {
            reminderCardView?.detail_title?.text = "小提醒"
            reminderCardView?.detail_content?.text = recipe.reminder
        }


        configureYouTubePlayer(recipe.image, recipe.id)
    }

    private fun configureYouTubePlayer(recipeImage: String, recipeId: Int) {
        val realm = Realm.getInstance(this)
        val videoCode = realm.where(Video::class.java)
            .equalTo("recipeId", recipeId)
            .findFirst()?.youtubeVideoCode ?: ""
        realm.close()

        if (videoCode.isEmpty()) {
            // No vide code found, use image instead
            Log.e(TAG, "configureYouTubePlayer : videoCode.isEmpty()")

            val baseUrl = "https://commondatastorage.googleapis.com/funs7-1/uploads/recipe/image/"
            val imageUrl = baseUrl + recipeId + "/" + recipeImage

            Glide.with(applicationContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.food_default)
                    .centerCrop()
                    .into(recipe_image)

            return
        } else {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.youtube_container, youtubeFragment)
                    .commit()

            val key = "AIzaSyCGkt_vNwDOmv8jDQPybsu8u3yyn95NK7o"
            youtubeFragment.initialize(key,
                object : YouTubePlayer.OnInitializedListener {

                    override fun onInitializationSuccess(provider: YouTubePlayer.Provider,
                                                         player: YouTubePlayer, wasRestored: Boolean) {
                        if (!wasRestored) {
                            player.loadVideo(videoCode);
                            player.setShowFullscreenButton(false)
                        }
                    }

                    override fun onInitializationFailure(provider: YouTubePlayer.Provider,
                                                         error: YouTubeInitializationResult) {
                        Log.e(TAG, "onInitializationFailure -> $error")
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubeFragment.unregisterForContextMenu(youtube_container)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        when (newConfig?.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                supportActionBar?.show()
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                supportActionBar?.hide()
            }
        }
    }
}
