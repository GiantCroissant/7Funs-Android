package com.giantcroissant.sevenfuns.app

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

/**
 * Created by ayo on 2/24/16.
 */

fun String.colorPartial(color: String, length: Int): CharSequence? {
    val nameAndTitle = SpannableString(this)
    nameAndTitle.setSpan(
        ForegroundColorSpan(Color.parseColor(color)),
        0,
        length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return nameAndTitle
}
