package com.giantcroissant.sevenfuns.app

import android.app.IntentService
import android.content.Intent
import com.giantcroissant.sevenfuns.app.DbModel.Recipes
import io.realm.Realm
import io.realm.RealmConfiguration
import org.joda.time.DateTime
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable

/**
 * Created by apprentice on 3/7/16.
 */
class RecipesSetupServiceEx : IntentService("RecipesSetupService")  {
    override fun onHandleIntent(intent: Intent) {
        System.out.println("RecipesSetupServiceEx - onHandleIntent")

        val retrofit = Retrofit
                .Builder()
                .baseUrl("https://www.7funs.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()

        val restApiService = retrofit.create(RestApiService::class.java)

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

        val config = RealmConfiguration.Builder(this).build()
        val realm = Realm.getInstance(config)

        val query = realm.where(Recipes::class.java).findAll()
        System.out.println("query Recipes size: " + query.size.toString())
        val localDataStream = Observable.just(realm.copyFromRealm(query))
            .flatMap { x -> Observable.from(x) }
            .flatMap { x ->
                Observable.just(MiscModel.IntermediateOverview(
                        x.id as Int,
                        x.updatedAt,
                        MiscModel.LocationType.Local,
                        MiscModel.OverviewActionResultType.None)
                )
            }
//        val localDataStream = query.asObservable()
//            .filter { x -> x.isLoaded }
//            .flatMap { x -> Observable.from(x) }
//            .flatMap { x ->
//                Observable.just(MiscModel.IntermediateOverview(
//                        x.id as Int,
//                        x.updatedAt,
//                        MiscModel.LocationType.Local,
//                        MiscModel.OverviewActionResultType.None)
//                )
//
//            }

        val combinedStreams = remoteDataStream.concatWith(localDataStream)//.mergeWith(localDataStream)

        val initialPair: Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>> = Pair(listOf<MiscModel.IntermediateOverview>(), listOf<MiscModel.IntermediateOverview>())
        val categorizeToPairList: (accPair: Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>>, intermediateOverview: MiscModel.IntermediateOverview) -> Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>> = { accPair, intermediateOverview ->
            val (needToUpdateList, needToRemoveList) = accPair
//            val sameIdInList = needToUpdateList.find { item -> item.id == intermediateOverview.id }

            System.out.println(needToUpdateList)
            //System.out.println(needToRemoveList)
            Pair(listOf<MiscModel.IntermediateOverview>(), listOf<MiscModel.IntermediateOverview>())
        }

//        localDataStream.reduce { a: MiscModel.IntermediateOverview?, b: MiscModel.IntermediateOverview? ->
//            MiscModel.IntermediateOverview(0, "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.None)
//        }
//        .subscribe({ x ->
//            System.out.println("something")
//        }, { error ->
//            System.out.println(error.message)
//        })

//        localDataStream
        combinedStreams
            .reduce(initialPair, categorizeToPairList)
            .map { x ->
                x.first
            }
            .subscribe({ x ->
                System.out.println("something")
            }, { error ->
                System.out.println(error.message)
            })

//        combinedStreams.reduce { acc: Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>>?, intermediateOverview: MiscModel.IntermediateOverview? ->
//            Pair(listOf<MiscModel.IntermediateOverview>(), listOf<MiscModel.IntermediateOverview>())
//        }

//        combinedStreams.reduce(initialPair, categorizeToPairList)
//            .subscribe({ x ->
//                System.out.println("something")
//            }, { error ->
//
//            })

//        combinedStreams//.reduce(initialPair, categorizeToPairList)
//            .subscribe({ x ->
//                System.out.println("something")
//            })

//        combinedStreams.reduce(initialPair, categorizeToPairList)
////        combinedStreams.reduce(initialPair, categorizeToPairList)
////        //combinedStream
//            .subscribe({ x ->
////            if (x.locationType == MiscModel.LocationType.Local) {
//                System.out.println("something")
//                System.out.println(x)
////            }
//            }, { error ->
//                System.out.println(error)
//            }, {
//                System.out.println("done, cloase realm")
//                realm.close()
//            })
    }
}