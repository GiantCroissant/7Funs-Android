package com.giantcroissant.sevenfuns.app.DbModel

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
//import io.realm.annotations.RealmClass

/**
 * Created by apprentice on 1/27/16.
 */
public open class Recipes(
        @PrimaryKey
        public open var id: String = "",
        public open var image: String = "",
        public open var updatedAt: String = "",
        public open var chefName: String = "",
        public open var title: String = "",
        public open var description: String = "",
        public open var ingredient: String = "",
        public open var seasoning: String = "",
        public open var methods: RealmList<MethodDesc> = RealmList(),
        public open var reminder: String = "") : RealmObject() {
}