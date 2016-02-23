package com.giantcroissant.sevenfuns.app.QandA

//import kotlinx.android.synthetic.main.activity_main.*

//import kotlinx.android.synthetic.main.fragment_qa_section_overview.view.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.giantcroissant.sevenfuns.app.*
import com.giantcroissant.sevenfuns.app.RestAPIService.RestAPIHelper
import com.google.android.youtube.player.internal.x
import kotlinx.android.synthetic.main.activity_qa_detail.*
import kotlinx.android.synthetic.main.item_question_header_view.view.*

import kotlinx.android.synthetic.main.listview_qa_detail_item.view.*
import kotlinx.android.synthetic.main.listview_qa_section_overview.view.*
import kotlinx.android.synthetic.main.toolbar.*
import org.joda.time.DateTime
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

//import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/2/16.
 */
class QADetailActivity : AppCompatActivity() {

    companion object {
        const val WRITTEN_COMMENT: Int = 0
    }

    var messageId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail)

        val question = intent.getParcelableExtra<MessageParcelable>("message")
        messageId = question.id


        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        qaDetailAddFab.setOnClickListener { x ->
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

        qaDetailMessageList.layoutManager = LinearLayoutManager(baseContext)

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST)
        qaDetailMessageList.addItemDecoration(itemDecoration)

        qaDetailMessageList.adapter = RecyclerAdapter(this, question)
    }

    override fun onResume() {
        super.onResume()

        val question = intent.getParcelableExtra<MessageParcelable>("message")
        Log.e("TA", "question = $question")

        RestAPIHelper.restApiService
            .getSpecificMessageComment(question.id)
            .flatMap { x -> Observable.just(x) }
            .map { x ->
                x.filter { y ->
                    !y.comment.isEmpty()

                }.sortedByDescending { y ->
                    DateTime(y.updatedAt)
                }
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<List<JsonModel.MessageWithCommentJsonObject>>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                    Log.e("XD", e?.message)
                }

                override fun onNext(x: List<JsonModel.MessageWithCommentJsonObject>) {
                    Log.e("TAT", "onNext = $x")
                    val adapter = qaDetailMessageList.adapter as RecyclerAdapter
                    adapter.addAll(x)

//                    qaDetailMessageList.adapter =
//                        RecyclerAdapter(
//                            (this as? AppCompatActivity),
//                            question,
//                            x.toMutableList()
//                        )
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }

        when (requestCode) {
            WRITTEN_COMMENT -> {
                if (resultCode == RESULT_OK) {
                    val sp: SharedPreferences = getSharedPreferences("DATA", 0)
                    val token = sp.getString("token", "")
                    val commentParcelable: CommentParcelable = data.extras.getParcelable("comment")

                    System.out.println(commentParcelable.comment)

                    RestAPIHelper.restApiService
                        .createMessageComment(
                            "Bearer " + token,
                            messageId,
                            JsonModel.MessageCommentCreate(
                                messageId,
                                commentParcelable.comment
                            )
                        )
                        .subscribeOn(Schedulers.io())
                        .subscribe(object : Subscriber<JsonModel.MessageCommentCreateResultJsonObject>() {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                System.out.println(e?.message)
                            }

                            override fun onNext(x: JsonModel.MessageCommentCreateResultJsonObject) {
                            }
                        })
                }
            }
        }
    }

    class RecyclerAdapter(
        val context: Context,
        val question: MessageParcelable,
        val messageList: MutableList<JsonModel.MessageWithCommentJsonObject> = arrayListOf()
    )

    : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        val TYPE_HEADER = 0
        val TYPE_ITEM = 1

        class HeaderView(override val v: View) : ViewHolder(v) {
            init {
                view = v
            }
        }

        open class ViewHolder(open val v: View) : RecyclerView.ViewHolder(v) {
            var view: View

            init {
                view = v
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            when (viewType) {
                TYPE_HEADER -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_question_header_view, parent, false)
                    return HeaderView(view)
                }
                TYPE_ITEM -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listview_qa_detail_item, parent, false)
                    return ViewHolder(view)
                }
            }
            return null!!
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            if (position == 0) {
                val header = viewHolder as HeaderView
                Glide.with(context)
                    .load(R.drawable.profile)
                    .asBitmap()
                    .centerCrop()
                    .into(object : BitmapImageViewTarget(header.view.item_user_image) {

                        override fun setResource(resource: Bitmap?) {
                            super.setResource(resource)

                            RoundedBitmapDrawableFactory.create(context.resources, resource).let {
                                it.isCircular = true
                                header.view.item_user_image.setImageDrawable(it)
                            }
                        }

                    })
                header.view.item_user_name.text = question.userName
                header.view.item_question_title?.text = question.title
                header.view.item_question_desc?.text = question.description

            } else {
                val comment = messageList[position - 1] // headerView
                viewHolder.view.qaSectionDetailTitle?.text = comment.title
                viewHolder.view.qaSectionDetailComment?.text = comment.comment
            }
        }

        override fun getItemCount(): Int {
            return messageList.size + 1 // headerView
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) TYPE_HEADER else TYPE_ITEM
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}