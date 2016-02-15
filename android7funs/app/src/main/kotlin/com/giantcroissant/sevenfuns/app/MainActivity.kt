package com.giantcroissant.sevenfuns.app


import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recipesSetupServiceIntent = Intent(this, RecipesSetupService::class.java)
        val recipesDownloadServiceIntent = Intent(this, RecipesDownloadService::class.java)
        startService(recipesSetupServiceIntent)
        startService(recipesDownloadServiceIntent)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView?.let {
            it.setNavigationItemSelectedListener {
                it.isChecked = true
                drawerLayout?.closeDrawers()

                when (it.itemId) {
                    R.id.navigationItemRecipesSection -> {
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, RecipesSectionFragment.newInstance()).commit()
                    }
                    R.id.navigationItemPersonalSection -> {
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, PersonalSectionFragment.newInstance()).commit()
                    }
                    R.id.navigationItemInstructorSection -> {
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, InstructorSectionFragment.newInstance()).commit()
                    }
                    R.id.navigationItemQASection -> {
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, QASectionFragment.newInstance()).commit()
                    }
                    R.id.navigationItemSponsorSection -> {
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, SponsorSectionFragment.newInstance()).commit()
                    }
                }

                true
            }

            it.setCheckedItem(R.id.navigationItemRecipesSection)
            it.menu.performIdentifierAction(R.id.navigationItemRecipesSection, 0)
        }

        //retrieveRecipesOverview()
        //retrieveMessageQuery()

        //prepareRecipesFetchingSetup()
        //retrieveRemoteRecipes(5)

        //retrieveRemoteRecipesTest(4)

        //Log.d("", realm.path)
        //val localDataStream = realm.where(Recipes::class.java)
        //.isNotNull("id").findAllAsync()//.asObservable()
        //                .filter { rs -> rs.isLoaded }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        //        if (id == R.id.action_settings) {
        //            return true
        //        }

        when (id) {
            android.R.id.home -> drawerLayout?.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    //    //
    //    fun retrieveRecipesOverview() {
    //        System.out.println("MainActivity retrieveRecipesOverview")
    //        restApiService
    //                .getRecipesOverview()
    //                .subscribeOn(Schedulers.io())
    //                .observeOn(Schedulers.io())
    //                .subscribe(object : Subscriber<List<JsonModel.Overview>>() {
    //                    override fun onCompleted() {}
    //                    override fun onError(e: Throwable?) {
    //                        System.out.println(e?.message)
    //                    }
    //                    override fun onNext(overviews: List<JsonModel.Overview>) {
    //                        System.out.println(overviews.toString())
    //                    }
    //                })
    //    }
    //
    //    fun retrieveMessageQuery() {
    //        restApiService
    //                .getMessageQuery()
    //                .subscribeOn(Schedulers.io())
    //                .observeOn(Schedulers.io())
    //                .subscribe(object : Subscriber<JsonModel.MessageQueryJsonObject>() {
    //                    override fun onCompleted() {}
    //                    override fun onError(e: Throwable?) {
    //                        System.out.println(e?.message)
    //                    }
    //                    override fun onNext(overviews: JsonModel.MessageQueryJsonObject) {
    //                        System.out.println(overviews.toString())
    //                    }
    //                })
    //    }
    //
    //    fun retrieveRemoteRecipesTest(amount: Int) {//: Observable<List<JsonModel.Recipes>> {
    //        val sourceList = Observable.from(listOf(
    //                MiscModel.IntermediateOverview("2820", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update),
    //                MiscModel.IntermediateOverview("2821", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update),
    //                MiscModel.IntermediateOverview("2822", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update),
    //                MiscModel.IntermediateOverview("2823", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update),
    //                MiscModel.IntermediateOverview("2824", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update),
    //                MiscModel.IntermediateOverview("2825", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update),
    //                MiscModel.IntermediateOverview("2826", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update),
    //                MiscModel.IntermediateOverview("2827", "", MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.Update)
    //        ))
    //        sourceList
    //            .subscribeOn(Schedulers.io())
    //            .observeOn(Schedulers.io())
    ////            .take(amount)
    //            .buffer(amount)
    //            .map { l -> l.map { item -> item.id.toInt() } }
    //            .flatMap { idList ->
    //                restApiService.getRecipesByIdList(idList)
    //            }
    //            .map { x ->
    //                x.map { item ->
    //                    Recipes(item.id.toString(), item.image ?: "", item.updatedAt, item.chefName, item.title, item.description, item.ingredient, item.seasoning, item.reminder)
    //                }
    //            }
    //            .subscribe { x ->
    //                x.forEach { System.out.println(x) }
    //            }
    //    }
    //
    //    fun retrieveRemoteRecipes(amount: Int) {//: Observable<MiscModel.IntermediateOverview> {
    //        //
    //        val realm = Realm.getInstance(config)
    //        val query = realm.where(RecipesOverview::class.java).findAll()
    //        if (query.count() != 0) {
    //            val localDataStream = query.asObservable()
    //                    .filter { rs -> rs.isLoaded }
    //                    .flatMap { rs -> Observable.from(rs) }
    //                    .flatMap { ro -> Observable.just(ro.id.toInt()) }
    //                    //.subscr
    //                    //.take(amount)
    ////                    .map { ro ->
    ////                        ro.id.toInt()
    ////                    }
    //                    .buffer(amount)
    //                    .flatMap { x ->
    //                        restApiService.getRecipesByIdList(x)
    //                    }
    //                    .map { x ->
    //                        x.map { item ->
    //                            Recipes(item.id.toString(), item.image ?: "", item.updatedAt, item.chefName, item.title, item.description, item.ingredient, item.seasoning, item.reminder)
    //                        }
    //                    }.subscribe(object : Subscriber<List<Recipes>>() {
    //                        override fun onCompleted() {}
    //                        override fun onError(e: Throwable?) {
    //                            System.out.println(e?.message)
    //                        }
    //                        override fun onNext(rs: List<Recipes>) {
    //                            //System.out.println(overviews.toString())
    //                            realm.beginTransaction()
    //
    //                            // Update or create for Recipes Overview
    //                            rs.forEach { r -> realm.copyToRealmOrUpdate(r) }
    //
    //                            realm.commitTransaction()
    //
    //                        }
    //                    })
    //
    ////                    .flatMap { x -> x.isLoaded }
    ////                    .flatMap { x -> Observable.from(x) }
    //        }
    ////        val localDataStream = realm.where(Recipes::class.java).isNotNull("id").findAllAsync().asObservable()
    ////                .filter { rs -> rs.isLoaded }
    ////                .flatMap { rs -> Observable.from(rs) }
    ////                .flatMap { r ->
    ////                    Observable.just(MiscModel.IntermediateOverview(
    ////                            r.id, r.updatedAt, MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.None))
    ////                }
    ////                .take(amount)
    ////
    ////        return localDataStream
    //    }
    //
    //    fun prepareRecipesFetchingSetup() {
    //        // This returns a pair of need-to-update-list and need-to-remove-list according to the rule
    //        updateRemovePair()
    //                // Make everything in background thread
    //                .observeOn(Schedulers.io())
    //                .subscribeOn(Schedulers.io())
    //                // Just map it to what realm db knows
    //                .map { pair ->
    //                    val realm = Realm.getInstance(config)
    //
    //                    val needToUpdateRecipesOverviews = pair.first.map { intermediateOverview -> RecipesOverview(intermediateOverview.id, intermediateOverview.updatedAt) }
    //                    val query = realm.where(Recipes::class.java)
    //                    val accQuery = pair.second.fold(query, { acc, intermediateOverview -> acc.equalTo("id", intermediateOverview.id).or() })
    //                    val needToRemovedRecipes = accQuery.findAll()
    //                    Pair(needToUpdateRecipesOverviews, needToRemovedRecipes)
    //                }
    //                .subscribe(object : Subscriber<Pair<List<RecipesOverview>, RealmResults<Recipes>>>() {
    //                    override fun onCompleted() {}
    //                    override fun onError(e: Throwable?) {
    //                        System.out.println(e?.message)
    //                    }
    //                    override fun onNext(pair: Pair<List<RecipesOverview>, RealmResults<Recipes>>) {
    //                        val realm = Realm.getInstance(config)
    //                        // New or update for the first list, delete all for the second
    //                        realm.beginTransaction()
    //
    //                        // Update or create for Recipes Overview
    //                        pair.first.forEach { ro -> realm.copyToRealmOrUpdate(ro) }
    //                        // Clear Recipes
    //                        pair.second.clear()
    //
    //                        realm.commitTransaction()
    //                    }
    //                })
    //    }
    //
    //    fun updateRemovePair(): Observable<Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>>> {
    //        val realm = Realm.getInstance(config)
    //        //
    //        val remoteDataStream = restApiService.getRecipesOverview()
    //                .flatMap { jos -> Observable.from(jos) }
    //                .flatMap { jo ->
    //                    Observable.just(MiscModel.IntermediateOverview(
    //                            jo.id.toString(), jo.updatedAt, MiscModel.LocationType.Remote, MiscModel.OverviewActionResultType.None))
    //                }
    //
    //
    //        val query = realm.where(Recipes::class.java).findAll()
    //
    //        val combinedStreams = if (query.count() == 0) remoteDataStream else {
    //            val localDataStream = query.asObservable()
    //                    .filter { rs -> rs.isLoaded }
    //                    .flatMap { rs -> Observable.from(rs) }
    //                    .flatMap { r ->
    //                        Observable.just(MiscModel.IntermediateOverview(
    //                                r.id, r.updatedAt, MiscModel.LocationType.Local, MiscModel.OverviewActionResultType.None))
    //                    }
    //
    //            //localDataStream.concatWith(remoteDataStream)
    //            remoteDataStream.concatWith(localDataStream)
    //        }
    //
    //        //val combinedStreams = localDataStream.concatWith(remoteDataStream)
    //
    //        //
    //
    //        // first list for the case to update, second list for the case to remove
    //        val initialPair = Pair(listOf<MiscModel.IntermediateOverview>(), listOf<MiscModel.IntermediateOverview>())
    //        val categorizeToPairList: (accPair: Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>>, intermediateOverview: MiscModel.IntermediateOverview) -> Pair<List<MiscModel.IntermediateOverview>, List<MiscModel.IntermediateOverview>> = { accPair, intermediateOverview ->
    //            val (needToUpdateList, needToRemoveList) = accPair
    //            val sameIdInList = needToUpdateList.find { item -> item.id == intermediateOverview.id  }
    //
    //            // aro should have one action of None, Update, or Remove
    //            val aro: MiscModel.IntermediateOverview = if (sameIdInList == null) {
    //                if (intermediateOverview.locationType == MiscModel.LocationType.Remote) {
    //                    // Local recipes needs to add this for later retrieval
    //                    MiscModel.IntermediateOverview(intermediateOverview.id, intermediateOverview.updatedAt, intermediateOverview.locationType, MiscModel.OverviewActionResultType.Update)
    //                } else if (intermediateOverview.locationType == MiscModel.LocationType.Local) {
    //                    // Remote server no longer has this recipes, should remove from local
    //                    MiscModel.IntermediateOverview(intermediateOverview.id, intermediateOverview.updatedAt, intermediateOverview.locationType, MiscModel.OverviewActionResultType.Remove)
    //                } else {
    //                    // Although in this case, this should never be hit, the type of action, None
    //                    // is still required later
    //                    MiscModel.IntermediateOverview(intermediateOverview.id, intermediateOverview.updatedAt, intermediateOverview.locationType,MiscModel.OverviewActionResultType.None)
    //                }
    //            } else {
    //                // Both found, need to compare which one is the latest
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
    //                    MiscModel.IntermediateOverview(remoteLocalPair.first.id, remoteLocalPair.first.updatedAt, remoteLocalPair.first.locationType, MiscModel.OverviewActionResultType.Update)
    //                } else {
    //                    MiscModel.IntermediateOverview(remoteLocalPair.first.id, remoteLocalPair.first.updatedAt, remoteLocalPair.first.locationType, MiscModel.OverviewActionResultType.None)
    //                }
    //
    //                ior
    //            }
    //
    //            // let's make group by the action
    //            val result = if (aro.overviewActionResultType == MiscModel.OverviewActionResultType.Update) {
    //                Pair(accPair.first.plus(aro), accPair.second)
    //            } else if (aro.overviewActionResultType == MiscModel.OverviewActionResultType.Remove) {
    //                Pair(accPair.first, accPair.second.plus(aro))
    //            } else {
    //                // Should capture the case of None(should have no other case)
    //                Pair(accPair.first, accPair.second)
    //            }
    //
    //            result
    //        }
    //
    //        return combinedStreams.reduce(initialPair, categorizeToPairList)
    //    }
}

