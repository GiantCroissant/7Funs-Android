package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.DateTime
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_qa_detail.*
import kotlinx.android.synthetic.main.listview_qa_detail_item.view.*
//import kotlinx.android.synthetic.main.fragment_qa_section_overview.view.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable

//import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/2/16.
 */
class QADetailActivity : AppCompatActivity() {

    val retrofit = Retrofit
        .Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    companion object {
        const val WRITTEN_MESSAGE: Int = 0
        const val WRITTEN_COMMENT: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail)

        //
        val messageParcelable = intent.getParcelableExtra<MessageParcelable>("message")

        val messageWithComment = restApiService.getSpecificMessageComment(messageParcelable.id)
        messageWithComment
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Subscriber<List<JsonModel.MessageWithCommentJsonObject>>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                    System.out.println(e?.message)
                }

                override fun onNext(x: List<JsonModel.MessageWithCommentJsonObject>) {
                }
            })

        //
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        drawerLayout?.setOnClickListener { x ->
            this.onBackPressed()
        }

        //qaDetailMessageList
        qaDetailAddFab.setOnClickListener { x ->
            //System.out.println("hello")

            val sp: SharedPreferences = getSharedPreferences("DATA", 0)
            val token = sp.getString("token", "")
            if (token.isEmpty()) {
                System.out.println("No cached token")

                val intent = Intent(this, LoginActivity::class.java)
                this.startActivity(intent)
            } else {
                System.out.println("Have cached token: " + token)

                // Do something with token
                val intent = Intent(applicationContext, QADetailNewCommentActivity::class.java)
                startActivityForResult(intent, WRITTEN_COMMENT)
            }
        }

        //
        qaDetailMessageList.layoutManager = LinearLayoutManager(baseContext)

        //
        val retrofit = Retrofit
            .Builder()
            .baseUrl("https://www.7funs.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()

        val restApiService = retrofit.create(RestApiService::class.java)
        val commentsResponse = restApiService.getSpecificMessageComment(messageParcelable.id)
        commentsResponse
            .flatMap { x -> Observable.just(x) }
            .map { x ->
                x.sortedByDescending { y -> DateTime(y.updatedAt) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Subscriber<List<JsonModel.MessageWithCommentJsonObject>>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                    System.out.println(e?.message)
                }

                override fun onNext(x: List<JsonModel.MessageWithCommentJsonObject>) {
                    System.out.println(x)
                    qaDetailMessageList.adapter = RecyclerAdapter((this as? AppCompatActivity), x.toMutableList())
                    //                        view?.let { v ->
                    //                            //v.qaSectionOverview.layoutManager = LinearLayoutManager(v.context)
                    //                            //v.qaSectionOverview.adapter = RecyclerAdapter((activity as? AppCompatActivity), x)
                    //                            (v.qaSectionOverview.adapter as? QASectionOverviewFragment.RecyclerAdapter)?.addAll(x)
                    //                        }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            WRITTEN_MESSAGE -> {
                if (resultCode == RESULT_OK) {
                    val newMessageParcelable : NewMessageParcelable = data.extras.getParcelable("message")

                    System.out.println(newMessageParcelable.message)
                }
            }

            WRITTEN_COMMENT -> {
                if (resultCode == RESULT_OK) {
                    //this.finish()

                    //data?.putExtra("message", MessageParcelable(id))
                    val commentParcelable : CommentParcelable = data.extras.getParcelable("comment")

                    System.out.println(commentParcelable.comment)
//                    restApiService.createMessageComment()
                }
            }
        }
    }

    class RecyclerAdapter(val activity: AppCompatActivity?, val messageList: MutableList<JsonModel.MessageWithCommentJsonObject>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        public class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            //public var view: View by Delegates.notNull()
            var view: View

            init {
                view = v
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.listview_qa_detail_item, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val r = messageList[i]

            viewHolder.view.qaSectionDetailTitle?.text = r.title
            viewHolder.view.qaSectionDetailComment?.text = r.comment
            //
            //            viewHolder.view?.instructorSectionOverviewCardViewExpand?.setOnClickListener { x ->
            //                //                (activity as? AppCompatActivity)?.let {
            //                //                    val intent = Intent(x.context, RecipesDetailActivity::class.java)
            //                //                    intent?.putExtra("recipes", RecipesParcelable(r.id, r.title))
            //                //
            //                //                    x.context.startActivity(intent)
            //                //                }
            //            }
        }

        override fun getItemCount(): Int {
            return messageList.size
        }

        fun clearAll() {
            messageList.clear()
            notifyDataSetChanged()
        }

        fun addAll(contents: List<JsonModel.MessageWithCommentJsonObject>) {
            messageList.addAll(contents)
            messageList.sortByDescending { y -> DateTime(y.updatedAt) }
            notifyDataSetChanged()
        }
    }

}