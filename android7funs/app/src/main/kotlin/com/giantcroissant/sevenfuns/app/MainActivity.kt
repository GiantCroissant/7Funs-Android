package com.giantcroissant.sevenfuns.app

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
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

        // Clean out cached token for testing
        val sp: SharedPreferences =  getSharedPreferences("DATA", 0)
        sp.edit().remove("token").commit()

        //
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView?.setNavigationItemSelectedListener {

            it.isChecked = true
            drawerLayout?.closeDrawers()

            var fragment = Fragment()
            when (it.itemId) {
                R.id.navigationItemRecipesSection -> {
                    current_section = R.id.navigationItemRecipesSection
                    fragment = RecipesSectionFragment.newInstance()
                }
                R.id.navigationItemPersonalSection -> {
                    current_section = R.id.navigationItemPersonalSection
                    fragment = PersonalSectionFragment.newInstance()
                }
                R.id.navigationItemInstructorSection -> {
                    current_section = R.id.navigationItemInstructorSection
                    fragment = InstructorSectionFragment.newInstance()
                }
                R.id.navigationItemQASection -> {
                    current_section = R.id.navigationItemQASection
                    fragment = QASectionFragment.newInstance()
                }
                R.id.navigationItemSponsorSection -> {
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
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

