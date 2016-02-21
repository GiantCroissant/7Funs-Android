package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 2/21/16.
 */
data class RegisterAccountParcelable(val name: String, val email: String, val password: String) : Parcelable {
    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<RegisterAccountParcelable> = object : Parcelable.Creator<RegisterAccountParcelable> {
            override fun createFromParcel(parcelIn: Parcel): RegisterAccountParcelable {
                return RegisterAccountParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<RegisterAccountParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcelIn: Parcel) : this(
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString())  {
    }

    override fun describeContents() : Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(email)
        dest?.writeString(password)
    }
}