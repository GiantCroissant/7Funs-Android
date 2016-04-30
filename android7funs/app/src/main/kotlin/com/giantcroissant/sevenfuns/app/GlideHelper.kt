package com.giantcroissant.sevenfuns.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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

    fun setCircularImageFromUrl(context: Context, url: String, targetImageView: ImageView) {
        Glide.with(context)
            .load(Uri.parse(url))
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

    fun getImageContextPair(user: JsonModel.UserJsonObject) : Pair<Boolean, String> {
        val pair = if (user.isAdmin != null && user.isAdmin == true) {
            return Pair(false, "")
        } else if (!user.image.isNullOrBlank()) {
            val combinedPath = "https://storage.googleapis.com/funs7-1/uploads/user/image/" + user.id.toString() + "/square_" + user.image
            return Pair(true, combinedPath)
        } else if (!user.fbId.isNullOrBlank()) {
            val combinedPath = "https://graph.facebook.com/${user.fbId}/picture?type=square&height=80&width=80"
            return Pair(true, combinedPath)
        } else {
            Pair(false, "")
        }

        return pair
    }
}