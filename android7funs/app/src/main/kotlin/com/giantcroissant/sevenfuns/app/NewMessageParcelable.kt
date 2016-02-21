package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 2/21/16.
 */
data class NewMessageParcelable(val message: String) : Parcelable {
    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<NewMessageParcelable> = object : Parcelable.Creator<NewMessageParcelable> {
            override fun createFromParcel(parcelIn: Parcel): NewMessageParcelable {
                return NewMessageParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<NewMessageParcelable?> {
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
        dest?.writeString(message)
    }
}