package com.giantcroissant.sevenfuns.app

/**
 * Created by apprentice on 1/26/16.
 */
object MiscModel {
    enum class LocationType {
        Local, Remote
    }

    enum class OverviewActionResultType {
        None, Update, Remove
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
        val recipesIds: List<Int>
    )
}