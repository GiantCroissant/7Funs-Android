package com.giantcroissant.sevenfuns.app

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by apprentice on 2/3/16.
 */
class PersonalSectionFragment : Fragment() {
    public companion object {
        public fun newInstance(): PersonalSectionFragment {
            val fragment = PersonalSectionFragment().apply {
                val args = Bundle().apply {
                }

                arguments = args
            }

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_personal_section, container, false)

        return view
    }
}