package com.giantcroissant.sevenfuns.app.DbModel

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by ayo on 2/22/16.
 */
open class VideoOverview(
    @PrimaryKey
    open var id: Int = 0,
    open var updatedAt: String = ""

) : RealmObject() {
}