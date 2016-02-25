package com.giantcroissant.sevenfuns.app

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View

/**
 * Created by ayo on 2/24/16.
 */
class YourCustomBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        val translationY = Math.min(0f, dependency!!.translationY - dependency.height)
        child!!.translationY = translationY
        return true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        // we only want to trigger the change
        // only when the changes is from a snackbar
        return dependency is Snackbar.SnackbarLayout
    }
}