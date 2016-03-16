package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import com.giantcroissant.sevenfuns.app.DbModel.RecipesOverview
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import org.joda.time.DateTime
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable
import rx.Subscriber
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/1/16.
 */
class RecipesSetupService : IntentService("RecipesSetupService") {

    val retrofit = Retrofit
        .Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    private var config: RealmConfiguration by Delegates.notNull()
    private var realm: Realm by Delegates.notNull()

    override fun onHandleIntent(intent: Intent) {
        config = RealmConfiguration.Builder(this).build()
        realm = Realm.getInstance(config)

        System.out.println("RecipesSetupService - onHandleIntent")

        // Quick fix for removing old recipes overview to not stuck at retrieving recipes
        val q = realm.where(RecipesOverview::class.java).findAll()
        realm.beginTransaction()
        q.clear()
        realm.commitTransaction()
        //realm.close()

        prepareRecipesFetchingSetup()
    }

    fun prepareRecipesFetchingSetup() {
        updateRemovePair()
            .map { intermediateContext ->
                System.out.println("should update recipe overview size: " + intermediateContext.needToUpdate.size.toString())
                val needToUpdateRecipesOverviews = intermediateContext.needToUpdate.map { intermediateOverview ->
                    RecipesOverview(intermediateOverview.id, intermediateOverview.updatedAt)
                }
                val query = realm.where(Recipes::class.java)
                //System.out.println("second pair size: " + pair.second.size.toString())
                val clearRecipeContext = if (intermediateContext.needToRemove.size > 0) {
                    val accQuery = intermediateContext.needToRemove.fold(query, { acc, intermediateOverview ->
                        acc.equalTo("id", intermediateOverview.id).or()
                    })
                    val needToRemovedRecipes = accQuery.findAll()
                    MiscModel.ClearRecipeContext(needToRemovedRecipes, true)
                } else {
                    val needToRemovedRecipes = query.findAll()
                    MiscModel.ClearRecipeContext(needToRemovedRecipes, false)
                }
                Pair(needToUpdateRecipesOverviews, clearRecipeContext)
            }
//        // This returns a pair of need-to-update-list and need-to-remove-list according to the rule
//        updateRemovePair()
//            .map { pair ->
//
////                System.out.println("map pair")
////                System.out.println(pair)
//
//                System.out.println("should update recipe overview size: " + pair.first.size.toString())
//
//                val needToUpdateRecipesOverviews = pair.first.map { intermediateOverview ->
//                    RecipesOverview(intermediateOverview.id, intermediateOverview.updatedAt)
//                }
//
////                System.out.println("Recipes overview to update: " + needToUpdateRecipesOverviews.toString())
////                System.out.println(needToUpdateRecipesOverviews)
//
//                val query = realm.where(Recipes::class.java)
//                System.out.println("second pair size: " + pair.second.size.toString())
//                val clearRecipeContext = if (pair.second.size > 0) {
//                    val accQuery = pair.second.fold(query, { acc, intermediateOverview ->
//                        acc.equalTo("id", intermediateOverview.id).or()
//                    })
//                    val needToRemovedRecipes = accQuery.findAll()
//                    MiscModel.ClearRecipeContext(needToRemovedRecipes, true)
//                } else {
//                    val needToRemovedRecipes = query.findAll()
//                    MiscModel.ClearRecipeContext(needToRemovedRecipes, false)
//                }
//                Pair(needToUpdateRecipesOverviews, clearRecipeContext)
//            }
            .subscribe(object : Subscriber<Pair<List<RecipesOverview>, MiscModel.ClearRecipeContext>>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                    System.out.println(e?.message)
                }

                override fun onNext(pair: Pair<List<RecipesOverview>, MiscModel.ClearRecipeContext>) {
                    realm.beginTransaction()

                    // Update or create for Recipes Overview
                    pair.first.forEach { ro -> realm.copyToRealmOrUpdate(ro) }
                    // Clear Recipes
                    if (pair.second.shouldClear) {
                        pair.second.result.clear()
                    }

                    realm.commitTransaction()
                }
            })
    }

//    fun updateRemovePair(): Observable<Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>>> {
    fun updateRemovePair(): Observable<MiscModel.IntermediateContext> {
        //        val realm = Realm.getInstance(config)

        //
        val remoteDataStream = restApiService.getRecipesOverview()
            .flatMap { jos -> Observable.from(jos) }
            .flatMap { jo ->
                //System.out.println(jo.id.toString())
                Observable.just(
                    MiscModel.IntermediateOverview(
                        jo.id,
                        jo.updatedAt,
                        MiscModel.LocationType.Remote,
                        MiscModel.OverviewActionResultType.None)
                )
            }


        //System.out.println("query realm for recipes")
        val query = realm.where(Recipes::class.java).findAll()

        val combinedStreams = if (query.count() == 0) {
            System.out.println("use only remote stream")

            remoteDataStream
        } else {
            System.out.println("define local stream")

            val localDataStream = Observable.just(realm.copyFromRealm(query))//query.asObservable()
                //.filter { rs -> rs.isLoaded }
                .flatMap { rs -> Observable.from(rs) }
                .flatMap { r ->
                    Observable.just(MiscModel.IntermediateOverview(
                        r.id as Int,
                        r.updatedAt,
                        MiscModel.LocationType.Local,
                        MiscModel.OverviewActionResultType.None)
                    )
                }

//            localDataStream.concatWith(remoteDataStream)

            //System.out.println("remote concat with local")

//            System.out.println("concat remote with local stream")

            remoteDataStream.concatWith(localDataStream)
        }

        System.out.println("combined count: " + combinedStreams.count())

        //
        //System.out.println("have combined stream")

        // first list for the case to update, second list for the case to remove
//        val initialPair = Pair(listOf<MiscModel.IntermediateOverview>(), listOf<MiscModel.IntermediateOverview>())
//        val categorizeToPairList: (accPair: Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>>, intermediateOverview: MiscModel.IntermediateOverview) -> Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>> = { accPair, intermediateOverview ->
//            val (needToUpdateList, needToRemoveList) = accPair
//            val sameIdInList = needToUpdateList.find { item -> item.id == intermediateOverview.id }
//
//            // aro should have one action of None, Update, or Remove
//            val aro: MiscModel.IntermediateOverview = if (sameIdInList == null) {
//                if (intermediateOverview.locationType == MiscModel.LocationType.Remote) {
//                    //if (intermediateOverview.id == 2863) {
//                    //    System.out.println("id 2863 is found in remote")
//                    //}
//                    //System.out.println("Need to add: " + intermediateOverview.id.toString())
//                    // Local recipes needs to add this for later retrieval
//                    MiscModel.IntermediateOverview(intermediateOverview.id, intermediateOverview.updatedAt, intermediateOverview.locationType, MiscModel.OverviewActionResultType.Update)
//                } else if (intermediateOverview.locationType == MiscModel.LocationType.Local) {
//                    // Remote server no longer has this recipes, should remove from local
//                    MiscModel.IntermediateOverview(intermediateOverview.id, intermediateOverview.updatedAt, intermediateOverview.locationType, MiscModel.OverviewActionResultType.Remove)
//                } else {
//                    // Although in this case, this should never be hit, the type of action, None
//                    // is still required later
//                    System.out.println("Hit the case never should be for id: " + intermediateOverview.id.toString())
//                    MiscModel.IntermediateOverview(intermediateOverview.id, intermediateOverview.updatedAt, intermediateOverview.locationType, MiscModel.OverviewActionResultType.None)
//                }
//            } else {
//                // Both found, need to compare which one is the latest
//                System.out.println("local has id: " + sameIdInList.id)
//
//                // First make remote and local in a pair form, where remote comes first then local second
//                val remoteLocalPair = if (sameIdInList.locationType == MiscModel.LocationType.Remote) {
//                    Pair(sameIdInList, intermediateOverview)
//                } else {
//                    Pair(intermediateOverview, sameIdInList)
//                }
//
//                // Normally, only remote could have later time than local cached value
//                val remoteTime = DateTime(remoteLocalPair.first.updatedAt)
//                val localTime = DateTime(remoteLocalPair.second.updatedAt)
//                val remoteTimeLater: Boolean = remoteTime.isAfter(localTime)
//                val ior = if (remoteTimeLater) {
//                    //System.out.println("later remote time: " + remoteTime.toString() + " local time: " + localTime.toString())
//                    MiscModel.IntermediateOverview(remoteLocalPair.first.id, remoteLocalPair.first.updatedAt, remoteLocalPair.first.locationType, MiscModel.OverviewActionResultType.Update)
//                } else {
//                    //System.out.println("not later remote time: " + remoteTime.toString() + " local time: " + localTime.toString())
//                    MiscModel.IntermediateOverview(remoteLocalPair.first.id, remoteLocalPair.first.updatedAt, remoteLocalPair.first.locationType, MiscModel.OverviewActionResultType.None)
//                }
//
//                ior
//            }
//
//            // let's make group by the action
//            val result = if (aro.overviewActionResultType == MiscModel.OverviewActionResultType.Update) {
//                System.out.println("id: " + aro.id.toString() + " should update")
//                Pair(accPair.first.plus(aro), accPair.second)
//            } else if (aro.overviewActionResultType == MiscModel.OverviewActionResultType.Remove) {
//                System.out.println("id: " + aro.id.toString() + " should be removed")
//                Pair(accPair.first, accPair.second.plus(aro))
//            } else {
//                // Should capture the case of None(should have no other case)
//                System.out.println("id: " + aro.id.toString() + " should do nothing")
//                Pair(accPair.first, accPair.second)
//            }
//
//            result
//        }
//
//        System.out.println("returning reduced pair")
//        return combinedStreams.reduce(initialPair, categorizeToPairList)


        val formUpdateRemoveList: (ic: MiscModel.IntermediateContext, intermediateOverview: MiscModel.IntermediateOverview) -> MiscModel.IntermediateContext = { ic, intermediateOverview ->
            val latestIC = if (intermediateOverview.locationType == MiscModel.LocationType.Remote) {
//                System.out.println("may need to update for id: " + intermediateOverview.id.toString())
                MiscModel.IntermediateContext(ic.mayNeedToUpdate, ic.needToUpdate.plus(intermediateOverview), ic.needToRemove)
            } else if (intermediateOverview.locationType == MiscModel.LocationType.Local) {
                val foundInNeedToUpdate = ic.needToUpdate.find { item -> item.id == intermediateOverview.id }

                val theIC = if (foundInNeedToUpdate != null) {
//                    System.out.println("need to update for id: " + intermediateOverview.id.toString())
                    val remoteTime = DateTime(foundInNeedToUpdate.updatedAt)
                    val localTime = DateTime(intermediateOverview.updatedAt)
                    val remoteTimeLater: Boolean = remoteTime.isAfter(localTime)

                    val tempIC = if (remoteTimeLater) {
                        MiscModel.IntermediateContext(ic.mayNeedToUpdate, ic.needToUpdate, ic.needToRemove)
                    } else {
                        val filteredList = ic.needToUpdate.filter { x -> x.id != intermediateOverview.id }
                        MiscModel.IntermediateContext(ic.mayNeedToUpdate, filteredList, ic.needToRemove)
                    }
                    tempIC
                } else {
//                    System.out.println("need to remove for id: " + intermediateOverview.id.toString())
                    MiscModel.IntermediateContext(ic.mayNeedToUpdate, ic.needToUpdate, ic.needToRemove.plus(intermediateOverview))
                }
                theIC
            } else {
                MiscModel.IntermediateContext(ic.mayNeedToUpdate, ic.needToUpdate, ic.needToRemove)
            }

            latestIC
        }

        return combinedStreams.reduce(
                MiscModel.IntermediateContext(listOf<MiscModel.IntermediateOverview>(), listOf<MiscModel.IntermediateOverview>(), listOf<MiscModel.IntermediateOverview>()),
                formUpdateRemoveList)
    }
}