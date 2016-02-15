package com.giantcroissant.sevenfuns.app.DbModel

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by apprentice on 1/27/16.
 */
open class RecipesOverview(
    @PrimaryKey
    open var id: Int = 0,
    open var updatedAt: String = ""

) : RealmObject() {

}