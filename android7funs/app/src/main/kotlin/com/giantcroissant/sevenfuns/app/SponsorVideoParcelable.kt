package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 4/28/16.
 */
data class SponsorVideoParcelable(
        val id: Int,
        val youtubeVideoCode: String,
        val videoTitle: String,
        val videoDuration: Int,
        val videoLikeCount: Int,
        val videoViewCount: Int,
        val thumbnailUrl: String
) : Parcelable {

    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<SponsorVideoParcelable> = object : Parcelable.Creator<SponsorVideoParcelable> {
            override fun createFromParcel(parcelIn: Parcel): SponsorVideoParcelable {
                return SponsorVideoParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<SponsorVideoParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcelIn: Parcel) : this(
            parcelIn.readInt(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readInt(),
            parcelIn.readInt(),
            parcelIn.readInt(),
            parcelIn.readString()
    ) {}

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(youtubeVideoCode)
        dest?.writeString(videoTitle)
        dest?.writeInt(videoDuration)
        dest?.writeInt(videoLikeCount)
        dest?.writeInt(videoViewCount)
        dest?.writeString(thumbnailUrl)
    }
}