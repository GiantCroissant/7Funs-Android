package com.giantcroissant.sevenfuns.app.DbModel

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//import io.realm.annotations.RealmClass

/**
 * Created by apprentice on 1/27/16.
 */
open class Recipes(
    @PrimaryKey
    open var id: Int = 0,
    open var image: String = "",
    open var updatedAt: String = "",
    open var chefName: String = "",
    open var title: String = "",
    open var description: String = "",
    open var ingredient: String = "",
    open var seasoning: String = "",
    open var methods: RealmList<MethodDesc> = RealmList(),
    open var reminder: String = "",
    open var hits: Int = 0,
    open var collected: Int = 0
    ) : RealmObject() {
}