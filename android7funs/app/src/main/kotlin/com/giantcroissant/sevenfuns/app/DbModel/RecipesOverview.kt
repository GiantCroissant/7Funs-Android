package com.giantcroissant.sevenfuns.app.DbModel

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
//import io.realm.annotations.RealmClass

/**
 * Created by apprentice on 1/27/16.
 */
public open class RecipesOverview(
        @PrimaryKey
        public open var id: String = "",
        public open var updatedAt: String = "") : RealmObject() {
}