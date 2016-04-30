package com.giantcroissant.sevenfuns.app

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by apprentice on 4/28/16.
 */
data class SponsorParcelable(
    val id: Int,
    val name: String,
    val url: String,
    val image: String,
    val description: String,
    val videos: MutableList<SponsorVideoParcelable>
) : Parcelable {

    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<SponsorParcelable> = object : Parcelable.Creator<SponsorParcelable> {
            override fun createFromParcel(parcelIn: Parcel): SponsorParcelable {
                return SponsorParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<SponsorParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcelIn: Parcel) : this(
        parcelIn.readInt(),
        parcelIn.readString(),
        parcelIn.readString(),
        parcelIn.readString(),
        parcelIn.readString(),
        arrayListOf<SponsorVideoParcelable>().apply {
            parcelIn.readTypedList(this, SponsorVideoParcelable.CREATOR)
        }
//        listOf<SponsorVideoParcelable>().apply {
//            parcelIn.readTypedList(this, SponsorVideoParcelable.CREATOR)
//        }
    ) {}

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(name)
        dest?.writeString(url)
        dest?.writeString(image)
        dest?.writeString(description)
        dest?.writeTypedList(videos)
    }
}