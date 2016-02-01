package com.giantcroissant.sevenfuns.app

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by apprentice on 2/1/16.
 */
class SponsorSectionFragment : Fragment() {
    public companion object {
        public fun newInstance(): SponsorSectionFragment {
            val fragment = SponsorSectionFragment().apply {
                val args = Bundle().apply {
                }

                arguments = args
            }

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_sponsor_section, container, false)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.let {
            it.supportFragmentManager.beginTransaction().replace(R.id.sponsorSectionFragmentContainer, SponsorSectionOverviewFragment.newInstance()).commit()
        }
    }
}