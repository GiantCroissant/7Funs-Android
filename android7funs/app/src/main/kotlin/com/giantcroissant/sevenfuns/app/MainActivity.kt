package com.giantcroissant.sevenfuns.app

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.facebook.appevents.AppEventsLogger
import com.giantcroissant.sevenfuns.app.QandA.QASectionFragment
import com.giantcroissant.sevenfuns.app.R.string.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*


class MainActivity : AppCompatActivity() {
    val CURRENT_SECTION = "CURRENT_SECTION"
    var current_section = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            current_section = savedInstanceState.getInt(CURRENT_SECTION);

        } else {
            current_section = R.id.navigationItemRecipesSection
        }

        setContentView(R.layout.activity_main)

//        // Clean out cached token for testing
//        val sp: SharedPreferences =  getSharedPreferences("DATA", 0)
//        sp.edit().remove("token").commit()

        //
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as? SearchManager
//        search?.setSearchableInfo(searchManager?.getSearchableInfo(componentName))

        navigationView?.setNavigationItemSelectedListener {

            it.isChecked = true
            drawerLayout?.closeDrawers()

            var fragment = Fragment()
            when (it.itemId) {
                R.id.navigationItemRecipesSection -> {
                    supportActionBar?.title = getString(navigation_item_recipes_section)
                    current_section = R.id.navigationItemRecipesSection
                    fragment = RecipesSectionOverviewFragment()
                    val bundle = Bundle()
                    bundle.putString("type", "recipe")
                    fragment.arguments = bundle
                }
                R.id.navigationItemPersonalSection -> {
                    supportActionBar?.title = getString(navigation_item_personal_section)
                    current_section = R.id.navigationItemPersonalSection
                    fragment = RecipesSectionOverviewFragment()
                    val bundle = Bundle()
                    bundle.putString("type", "collection")
                    fragment.arguments = bundle
                }
                R.id.navigationItemInstructorSection -> {
                    supportActionBar?.title = getString(navigation_item_instructor_section)
                    current_section = R.id.navigationItemInstructorSection
                    fragment = InstructorSectionFragment.newInstance()
                }
                R.id.navigationItemQASection -> {
                    supportActionBar?.title = getString(navigation_item_qa_section)
                    current_section = R.id.navigationItemQASection
                    fragment = QASectionFragment.newInstance()
                }
                R.id.navigationItemSponsorSection -> {
                    supportActionBar?.title = getString(navigation_item_sponsor_section)
                    current_section = R.id.navigationItemSponsorSection
                    fragment = SponsorSectionFragment.newInstance()
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView.setCheckedItem(current_section)
        navigationView.menu.performIdentifierAction(current_section, 0)

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    override fun onPause() {
        super.onPause()

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as? SearchManager
        val search = menu.findItem(R.id.action_search).actionView as? SearchView
        search?.setSearchableInfo(searchManager?.getSearchableInfo(componentName))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout?.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(CURRENT_SECTION, current_section);

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        if (savedInstanceState != null) {
            current_section = savedInstanceState.getInt(CURRENT_SECTION);

        } else {
            current_section = R.id.navigationItemRecipesSection
        }
    }

}

