package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 2/1/16.
 */
data class RecipesParcelable(val id: Int, val title: String) : Parcelable {
    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<RecipesParcelable> = object : Parcelable.Creator<RecipesParcelable> {
            override fun createFromParcel(parcelIn: Parcel): RecipesParcelable {
                return RecipesParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<RecipesParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcelIn: Parcel) : this(
            parcelIn.readInt(),
            parcelIn.readString())

    override fun describeContents() : Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(title)
    }
}
