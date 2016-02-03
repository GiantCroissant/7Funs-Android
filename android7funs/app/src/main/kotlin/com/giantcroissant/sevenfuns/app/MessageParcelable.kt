package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 2/3/16.
 */
data class MessageParcelable(val id: Int) : Parcelable {
    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<MessageParcelable> = object : Parcelable.Creator<MessageParcelable> {
            override fun createFromParcel(parcelIn: Parcel): MessageParcelable {
                return MessageParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<MessageParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcelIn: Parcel) : this(
            parcelIn.readInt())  {
    }

    override fun describeContents() : Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
    }
}