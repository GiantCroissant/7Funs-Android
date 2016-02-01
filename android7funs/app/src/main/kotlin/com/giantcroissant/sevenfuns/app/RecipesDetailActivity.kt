package com.giantcroissant.sevenfuns.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment

/**
 * Created by apprentice on 2/1/16.
 */
class RecipesDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes_detail)

        val ytpsf = YouTubePlayerSupportFragment()
        ytpsf.initialize("AIzaSyAocAvXaWG5w8WszO2N8pvPewgga74QmtA", object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(provider: YouTubePlayer.Provider, player: YouTubePlayer, wasRestored: Boolean) {
                if (!wasRestored) {
                    player.cueVideo("Er1cDWIJ1z4");
                }
            }

            override fun onInitializationFailure(provider: YouTubePlayer.Provider, error: YouTubeInitializationResult) {
                System.out.println(error.name)
            }
        })

        supportFragmentManager.beginTransaction().replace(R.id.youtubePlayerFragmentContainer, ytpsf).commit()
    }
}
