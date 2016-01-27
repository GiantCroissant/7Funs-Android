package com.giantcroissant.sevenfuns.app

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by apprentice on 1/26/16.
 */
object DbModel {
    class MethodDesc(val desc: String) : RealmObject() {}
    class Recipes(
            @PrimaryKey
            val id: String,
            val image: String,
            val updatedAt: String,
            val chefName: String,
            val title: String,
            val description: String,
            val ingredient: String,
            val seasoning: String,
            val methods: RealmList<MethodDesc>,
            val reminder: String) : RealmObject() {}

    class RecipesOverview(val id: String, val updatedAt: String) : RealmObject() {}
}