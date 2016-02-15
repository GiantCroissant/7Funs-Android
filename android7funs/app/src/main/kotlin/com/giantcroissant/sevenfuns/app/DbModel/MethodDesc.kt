package com.giantcroissant.sevenfuns.app.DbModel

import io.realm.RealmObject

//import io.realm.annotations.RealmClass

/**
 * Created by apprentice on 1/27/16.
 */

open class MethodDesc(
    open var desc: String = "") : RealmObject() {
}