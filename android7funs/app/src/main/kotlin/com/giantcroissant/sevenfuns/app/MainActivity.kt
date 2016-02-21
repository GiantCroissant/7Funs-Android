package com.giantcroissant.sevenfuns.app

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView?.let {
            it.setNavigationItemSelectedListener {
                it.isChecked = true
                drawerLayout?.closeDrawers()

                when (it.itemId) {
                    R.id.navigationItemRecipesSection -> {
                        supportActionBar?.title = resources.getString(R.string.navigation_item_recipes_section)
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, RecipesSectionFragment.newInstance())
                            .commit()
                    }
                    R.id.navigationItemPersonalSection -> {
                        supportActionBar?.title = resources.getString(R.string.navigation_item_personal_section)
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, PersonalSectionFragment.newInstance())
                            .commit()
                    }
                    R.id.navigationItemInstructorSection -> {
                        supportActionBar?.title = resources.getString(R.string.navigation_item_instructor_section)
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, InstructorSectionFragment.newInstance())
                            .commit()
                    }
                    R.id.navigationItemQASection -> {
                        supportActionBar?.title = resources.getString(R.string.navigation_item_qa_section)
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, QASectionFragment.newInstance())
                            .commit()
                    }
                    R.id.navigationItemSponsorSection -> {
                        supportActionBar?.title = resources.getString(R.string.navigation_item_sponsor_section)
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, SponsorSectionFragment.newInstance())
                            .commit()
                    }
                }

                true
            }

            it.setCheckedItem(R.id.navigationItemRecipesSection)
            it.menu.performIdentifierAction(R.id.navigationItemRecipesSection, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        //        if (id == R.id.action_settings) {
        //            return true
        //        }

        when (id) {
            android.R.id.home -> drawerLayout?.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

}

