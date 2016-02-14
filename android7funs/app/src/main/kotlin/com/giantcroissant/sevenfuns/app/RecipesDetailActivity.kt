package com.giantcroissant.sevenfuns.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import kotlinx.android.synthetic.main.activity_recipes_detail.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.cardview_recipes_detail.view.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by apprentice on 2/1/16.
 */
class RecipesDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes_detail)

        setSupportActionBar(toolbar)
        supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar.setDisplayHomeAsUpEnabled(true)

//        val recipesParcelable = intent.getParcelableExtra<RecipesParcelable>("recipes")
//
//        //
//        supportActionBar?.title = recipesParcelable.title
//
//        //
//        val ytpsf = YouTubePlayerSupportFragment()
//        ytpsf.initialize("AIzaSyAocAvXaWG5w8WszO2N8pvPewgga74QmtA", object : YouTubePlayer.OnInitializedListener {
//            override fun onInitializationSuccess(provider: YouTubePlayer.Provider, player: YouTubePlayer, wasRestored: Boolean) {
//                if (!wasRestored) {
//                    player.cueVideo("Er1cDWIJ1z4");
//                }
//            }
//
//            override fun onInitializationFailure(provider: YouTubePlayer.Provider, error: YouTubeInitializationResult) {
//                System.out.println(error.name)
//            }
//        })
//
//        supportFragmentManager.beginTransaction().replace(R.id.youtubePlayerFragmentContainer, ytpsf).commit()
//
//        //
//
//        ingredientCardView?.recipesDetailTitle?.text = "Ingredient"
//        ingredientCardView?.recipesDetailContent?.text = recipesParcelable.ingredient
//
//        seasoningCardView?.recipesDetailTitle?.text = "Seasoning"
//        seasoningCardView?.recipesDetailContent?.text = recipesParcelable.seasoning
//
//        methodCardView?.recipesDetailTitle?.text = "Method"
//        val adjustedContent = recipesParcelable.methods.reduce { acc, s -> acc + s + "\n"  }
//        methodCardView?.recipesDetailContent?.text = adjustedContent
//
//        reminderCardView?.recipesDetailTitle?.text = "Reminder"
//        reminderCardView?.recipesDetailContent?.text = recipesParcelable.reminder
    }
}
