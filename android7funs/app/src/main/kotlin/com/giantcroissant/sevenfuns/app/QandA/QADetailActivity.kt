package com.giantcroissant.sevenfuns.app.QandA

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.giantcroissant.sevenfuns.app.*
import com.giantcroissant.sevenfuns.app.RestAPIService.RestAPIHelper
import kotlinx.android.synthetic.main.activity_qa_detail.*
import kotlinx.android.synthetic.main.item_question_comment.view.*
import kotlinx.android.synthetic.main.item_question_header_view.view.*
import kotlinx.android.synthetic.main.toolbar.*
import org.joda.time.DateTime
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * Created by apprentice on 2/2/16.
 */
class QADetailActivity : AppCompatActivity() {
    val TAG = QADetailActivity::class.java.name

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
        supportActionBar?.title = question.title

        qaDetailAddFab.setOnClickListener { x ->
            val sp: SharedPreferences = getSharedPreferences("DATA", 0)
            val token = sp.getString("token", "")
            if (token.isEmpty()) {
                val intent = Intent(this, LoginActivity::class.java)
                this.startActivity(intent)

            } else {
                val intent = Intent(applicationContext, QADetailNewCommentActivity::class.java)
                startActivityForResult(intent, WRITTEN_COMMENT)
            }
        }

        comment_list.layoutManager = LinearLayoutManager(baseContext)
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST)
        comment_list.addItemDecoration(itemDecoration)
        comment_list.adapter = RecyclerAdapter(this, question)

        comment_swipe_container.setOnRefreshListener {
            fetchComments() {
                comment_swipe_container.isRefreshing = false
            }
        }
        fetchComments()
    }

    private fun fetchComments(onFinish: () -> Unit = {}) {
        RestAPIHelper.restApiService
            .getSpecificMessageComment(messageId)
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
            .subscribe({ commentJsonList ->
                val adapter = comment_list.adapter as RecyclerAdapter
                adapter.updateList(commentJsonList)
                onFinish()

            }, { error ->
                Log.e(TAG, "error = $error")
                onFinish()
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

                    val remark = "android " + Build.VERSION.RELEASE + " ; version = " + packageManager.getPackageInfo(packageName, 0).versionName
                    RestAPIHelper.restApiService
                        .createMessageComment(
                            "Bearer " + token,
                            messageId,
                            JsonModel.MessageCommentCreate(
                                messageId,
                                commentParcelable.comment,
                                remark
                            )
                        )
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            Log.e(TAG, "result = $result")
                            Snackbar.make(comment_coordinator_view, "留言成功", Snackbar.LENGTH_LONG).show()
                            fetchComments()

                        }, { error ->
                            Log.e(TAG, "error = $error")
                            Snackbar.make(comment_coordinator_view, "留言失敗", Snackbar.LENGTH_LONG).show()
                        })
                }
            }
        }
    }

    class RecyclerAdapter(
        val context: Context,
        val question: MessageParcelable,
        val comments: MutableList<JsonModel.MessageWithCommentJsonObject> = arrayListOf()
    ) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

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

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) TYPE_HEADER else TYPE_ITEM
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
                        .inflate(R.layout.item_question_comment, parent, false)
                    return ViewHolder(view)
                }
            }
            return ViewHolder(View(context))
        }

        override fun getItemCount(): Int {
            return comments.size + 1 // headerView
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            if (position == 0) {
                val header = viewHolder as HeaderView
                if (question.hasImageUrl == 1) {
                    GlideHelper.setCircularImageFromUrl(context, question.imageUrl, viewHolder.view.item_user_image)
                } else {
                    GlideHelper.setCircularImage(context, R.drawable.profile, viewHolder.view.item_user_image)
                }
                displayHeader(header.view)

            } else {
                val comment = comments[position - 1] // headerView
                val pair = GlideHelper.getImageContextPair(comment.user)
                if (pair.first) {
                    GlideHelper.setCircularImageFromUrl(context, pair.second, viewHolder.view.item_comment_user_image)
                } else {
                    GlideHelper.setCircularImage(context, R.drawable.profile, viewHolder.view.item_comment_user_image)
                }
                displayComment(viewHolder.view, comment)
            }
        }

        private fun displayComment(view: View, comment: JsonModel.MessageWithCommentJsonObject) {
            val combine = comment.user.name + " " + comment.comment
            val length = comment.user.name.length
            view.item_comment_title.text = combine.colorPartial("#E64A19", length)
        }

        private fun displayHeader(view: View) {
            view.item_user_name.text = question.userName
            view.item_question_title?.text = question.title
            view.item_question_desc?.text = question.description
        }

        fun updateList(contents: List<JsonModel.MessageWithCommentJsonObject>) {
            comments.clear()
            comments.addAll(contents)
            comments.sortByDescending { y -> DateTime(y.updatedAt) }
            notifyDataSetChanged()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
