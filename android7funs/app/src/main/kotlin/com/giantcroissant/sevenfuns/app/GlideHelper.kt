package com.giantcroissant.sevenfuns.app

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget

/**
 * Created by ayo on 2/24/16.
 */
object GlideHelper {

    fun setCircularImage(context: Context, imageId: Int, targetImageView: ImageView) {
        Glide.with(context)
            .load(imageId)
            .asBitmap()
            .centerCrop()
            .into(object : BitmapImageViewTarget(targetImageView) {
                override fun setResource(resource: Bitmap?) {
                    super.setResource(resource)
                    RoundedBitmapDrawableFactory.create(context.resources, resource).let {
                        it.isCircular = true
                        targetImageView.setImageDrawable(it)
                    }
                }
            })
    }

}