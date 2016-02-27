package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 2/27/16.
 */
data class TaggedRecipesParcelable(val associatedRecipesIds: List<String>) : Parcelable {

    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<TaggedRecipesParcelable> = object : Parcelable.Creator<TaggedRecipesParcelable> {
            override fun createFromParcel(parcelIn: Parcel): TaggedRecipesParcelable {
                return TaggedRecipesParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<TaggedRecipesParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcelIn: Parcel) : this(
            parcelIn.createStringArrayList()) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeStringList(associatedRecipesIds)
    }
}