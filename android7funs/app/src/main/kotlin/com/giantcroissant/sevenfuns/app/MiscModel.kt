package com.giantcroissant.sevenfuns.app

import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import io.realm.RealmResults

/**
 * Created by apprentice on 1/26/16.
 */
object MiscModel {
    enum class LocationType {
        Local, Remote
    }

    enum class OverviewActionResultType {
        None, Update, Remove, MayNeedToUpdate
    }

    data class IntermediateOverview(
        val id: Int,
        val updatedAt: String,
        val locationType: LocationType,
        val overviewActionResultType: OverviewActionResultType
    )

    //data class ActionResultOverview(val id: String, val updatedAt: String, val locationType: LocationType, val overviewActionResultType: OverviewActionResultType)

    data class RecipesByTag(
        val tagId: Int,
        val name: String,
        val recipesIds: List<Int>
    )

    data class ClearRecipeContext(
        val result: RealmResults<Recipes>,
        val shouldClear: Boolean
    )

    data class IntermediateContext(
        val mayNeedToUpdate: List<IntermediateOverview>,
        val needToUpdate: List<IntermediateOverview>,
        val needToRemove: List<IntermediateOverview>
    )
}