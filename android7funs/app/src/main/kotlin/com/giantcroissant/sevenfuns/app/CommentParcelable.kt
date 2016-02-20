package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 2/20/16.
 */
data class CommentParcelable(val comment: String) : Parcelable {
    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<CommentParcelable> = object : Parcelable.Creator<CommentParcelable> {
            override fun createFromParcel(parcelIn: Parcel): CommentParcelable {
                return CommentParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<CommentParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcelIn: Parcel) : this(
            parcelIn.readString())  {
    }

    override fun describeContents() : Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(comment)
    }
}