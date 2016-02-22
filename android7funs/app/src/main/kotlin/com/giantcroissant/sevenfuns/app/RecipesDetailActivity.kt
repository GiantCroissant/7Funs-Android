package com.giantcroissant.sevenfuns.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_recipes_detail.*
import kotlinx.android.synthetic.main.cardview_recipes_detail.view.*
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

        val realm = Realm.getInstance(this)
        val videoCode = realm.where(Video::class.java)
            .equalTo("recipeId", recipe.id)
            .findFirst()?.youtubeVideoCode ?: ""
        realm.close()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.youtube_container, youtubeFragment)
            .commit()

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
            methodCardView?.detail_title?.text = "做法"
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


        youtubeFragment.initialize("AIzaSyAocAvXaWG5w8WszO2N8pvPewgga74QmtA",
            object : YouTubePlayer.OnInitializedListener {

            override fun onInitializationSuccess(provider: YouTubePlayer.Provider,
                                                 player: YouTubePlayer, wasRestored: Boolean) {
                if (!wasRestored) {
                    player.loadVideo(videoCode);
                }
            }

            override fun onInitializationFailure(provider: YouTubePlayer.Provider,
                                                 error: YouTubeInitializationResult) {
                Log.e(TAG, "$error")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubeFragment.unregisterForContextMenu(youtube_container)
    }
}
