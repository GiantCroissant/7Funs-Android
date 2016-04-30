package com.giantcroissant.sevenfuns.app.QandA

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.giantcroissant.sevenfuns.app.*
import com.giantcroissant.sevenfuns.app.RestAPIService.RestAPIHelper
import kotlinx.android.synthetic.main.fragment_qa_section_overview.*
import kotlinx.android.synthetic.main.fragment_qa_section_overview.view.*
import kotlinx.android.synthetic.main.listview_qa_section_overview.view.*
import org.joda.time.DateTime
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import kotlin.properties.Delegates


/**
 * Created by apprentice on 2/1/16.
 */
class QASectionOverviewFragment : Fragment() {
    val TAG = QASectionOverviewFragment::class.java.name

    companion object {
        const val WRITTEN_MESSAGE: Int = 0

        fun newInstance(): QASectionOverviewFragment {
            val fragment = QASectionOverviewFragment().apply {
                val args = Bundle().apply {
                }
                arguments = args
            }
            return fragment
        }
    }

    var currentPage = 1

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_qa_section_overview, container, false)
        view?.let { v ->
            v.qaSectionOverview.layoutManager = LinearLayoutManager(v.context)
            (activity as? AppCompatActivity)?.let { a ->
                val itemDecoration = DividerItemDecoration(a, DividerItemDecoration.VERTICAL_LIST)
                v.qaSectionOverview.addItemDecoration(itemDecoration)
            }
        }
        configureSwipeContainer(view)
        configureQuestionRecyclerView(view)
        configureAddQuestionButton(view)
        fetchQuestions()
        return view
    }

    private fun configureSwipeContainer(view: View?) {
        view?.swipe_container?.setOnRefreshListener {
            fetchQuestions {
                view.swipe_container?.isRefreshing = false
            }
        }
    }

    private fun configureQuestionRecyclerView(view: View?) {
        val empty = arrayListOf<JsonModel.MessageJsonObject>()
        view?.qaSectionOverview?.adapter = RecyclerAdapter(activity as AppCompatActivity?, empty)
    }

    private fun fetchQuestions(onComplete: () -> Unit = {}) {
        RestAPIHelper.restApiService
            .getMessageQuery(currentPage)
            .map { msgJsonList ->
                msgJsonList.collection.sortedByDescending {
                    json ->
                    DateTime(json.updatedAt)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ jsonList ->
                val recycler = qaSectionOverview.adapter as RecyclerAdapter
                recycler.addAll(jsonList)
                currentPage += 1
                onComplete()

            }, { error ->
                Log.e(TAG, "error = $error")
                Snackbar.make(coordinator_view, "網路狀態不穩", Snackbar.LENGTH_LONG).show()
                onComplete()
            })
    }

    private fun configureAddQuestionButton(view: View?) {
        view?.add_question_button?.setOnClickListener {
            val token = activity.getSharedPreferences("DATA", 0).getString("token", "")
            if (token.isEmpty()) {
                val loginActivity = Intent(activity, LoginActivity::class.java)
                startActivity(loginActivity)

            } else {
                val commentsActivity = Intent(activity, QADetailNewMessageActivity::class.java)
                startActivityForResult(commentsActivity, Companion.WRITTEN_MESSAGE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }

        when (requestCode) {
            WRITTEN_MESSAGE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val newMessageParcelable: NewMessageParcelable = data.extras.getParcelable("message")

                    System.out.println(newMessageParcelable.title)
                    System.out.println(newMessageParcelable.description)

                    (activity as? AppCompatActivity)?.let {
                        val sp: SharedPreferences = it.getSharedPreferences("DATA", 0)
                        val token = sp.getString("token", "")

                        val combinedHeaderToken = "Bearer " + token

                        RestAPIHelper.restApiService
                            .createMessage(
                                combinedHeaderToken,
                                JsonModel.MessageCreate(
                                    newMessageParcelable.title,
                                    newMessageParcelable.description
                                )
                            )
                            .subscribeOn(Schedulers.io())
                            .subscribe(object : Subscriber<JsonModel.MessageCreateResultJsonObject>() {
                                override fun onCompleted() {
                                }

                                override fun onError(e: Throwable?) {
                                    System.out.println(e?.message)
                                }

                                override fun onNext(x: JsonModel.MessageCreateResultJsonObject) {
                                }
                            })

                    }

                }
            }
        }
    }

    class RecyclerAdapter(
        val activity: AppCompatActivity?,
        val messageList: MutableList<JsonModel.MessageJsonObject>
    ) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            var view: View by Delegates.notNull()

            init {
                view = v
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.listview_qa_section_overview, viewGroup, false)
            return ViewHolder(view)
        }

        // Return a pair of boolean and string, false stands for local, true is cloud
//        fun GetImageContextPair(question: JsonModel.MessageJsonObject) : Pair<Boolean, String> {
//            val pair = if (question.user.isAdmin != null && question.user.isAdmin == true) {
//                return Pair(false, "")
//            } else if (!question.user.image.isNullOrBlank()) {
//                val combinedPath = "https://storage.googleapis.com/funs7-1/uploads/user/image/" + question.user.id.toString() + "/square_" + question.user.image
//                return Pair(true, combinedPath)
//            } else if (!question.user.fbId.isNullOrBlank()) {
//                val combinedPath = "https://graph.facebook.com/${question.user.fbId}/picture?type=square&height=80&width=80"
//                return Pair(true, combinedPath)
//            } else {
//                Pair(false, "")
//            }
//
//            return pair
//        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val question = messageList[position]

            val pair = GlideHelper.getImageContextPair(question.user)

            if (pair.first) {
                Glide.with(activity)
                        .load(Uri.parse(pair.second))
                        .asBitmap()
                        .centerCrop()
                        .into(object : BitmapImageViewTarget(viewHolder.view.profile_image) {

                            override fun setResource(resource: Bitmap?) {
                                super.setResource(resource)

                                val circular = RoundedBitmapDrawableFactory.create(activity?.resources, resource)
                                if (circular != null) {
                                    circular.isCircular = true
                                    viewHolder.view.profile_image.setImageDrawable(circular)
                                }
                            }
                        })
            } else {
                Glide.with(activity)
                    .load(R.drawable.profile)
                    .asBitmap()
                    .centerCrop()
                    .into(object : BitmapImageViewTarget(viewHolder.view.profile_image) {

                        override fun setResource(resource: Bitmap?) {
                            super.setResource(resource)

                            val circular = RoundedBitmapDrawableFactory.create(activity?.resources, resource)
                            if (circular != null) {
                                circular.isCircular = true
                                viewHolder.view.profile_image.setImageDrawable(circular)
                            }
                        }
                    })
            }

//            Glide.with(activity)
//                .load(R.drawable.profile)
//                .asBitmap()
//                .centerCrop()
//                .into(object : BitmapImageViewTarget(viewHolder.view.profile_image) {
//
//                    override fun setResource(resource: Bitmap?) {
//                        super.setResource(resource)
//
//                        val circular = RoundedBitmapDrawableFactory.create(activity?.resources, resource)
//                        if (circular != null) {
//                            circular.isCircular = true
//                            viewHolder.view.profile_image.setImageDrawable(circular)
//                        }
//                    }
//                })

            val nameAndTitle = question.user.name + " " + question.title
            val nameLength = question.user.name.length
            viewHolder.view.question_title?.text = nameAndTitle.colorPartial("#E64A19", nameLength)
            viewHolder.view.question_desc?.text = question.description
            viewHolder.view.setOnClickListener {
                val commentsActivity = Intent(activity, QADetailActivity::class.java)
                val hasImageUrl = if (pair.first == true) 1 else 0
                commentsActivity.putExtra(
                    "message",
                    MessageParcelable(
                        question.id,
                        question.title,
                        question.description,
                        question.user.name,
                        hasImageUrl,
                        pair.second

                    )
                )
                activity?.startActivity(commentsActivity)
            }
        }

        override fun getItemCount(): Int {
            return messageList.size
        }

        fun addAll(contents: List<JsonModel.MessageJsonObject>) {
            messageList.addAll(contents)
            messageList.sortByDescending { y -> DateTime(y.updatedAt) }
            notifyDataSetChanged()
        }
    }

}