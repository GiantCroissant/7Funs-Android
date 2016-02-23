package com.giantcroissant.sevenfuns.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.giantcroissant.sevenfuns.app.RestAPIService.RestAPIHelper
import com.google.android.youtube.player.internal.m
import com.google.android.youtube.player.internal.v
import com.google.android.youtube.player.internal.x
import com.google.android.youtube.player.internal.y
import kotlinx.android.synthetic.main.fragment_qa_section_overview.*
import kotlinx.android.synthetic.main.fragment_qa_section_overview.view.*
import kotlinx.android.synthetic.main.listview_qa_section_overview.view.*
import org.joda.time.DateTime
import rx.Observable
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

        view?.qaSectionSwipeContainer?.setOnRefreshListener({
            (activity as? AppCompatActivity)?.let {
                currentPage += 1

                RestAPIHelper.restApiService
                    .getMessageQuery(currentPage)
                    .flatMap { x -> Observable.just(x) }
                    .map { x ->
                        //x.collection
                        x.collection.filter { y -> !y.title.isEmpty() }.sortedByDescending { y -> DateTime(y.updatedAt) }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : Subscriber<List<JsonModel.MessageJsonObject>>() {
                        override fun onCompleted() {
                        }

                        override fun onError(e: Throwable?) {
                            System.out.println(e?.message)
                        }

                        override fun onNext(x: List<JsonModel.MessageJsonObject>) {
                            view.let { v ->
                                (v.qaSectionOverview.adapter as? RecyclerAdapter)?.addAll(x)
                            }
                        }
                    })
            }

            view.qaSectionSwipeContainer?.isRefreshing = false
        })

        //        (activity as? AppCompatActivity)?.let {
        //            view?.add_question_button?.setOnClickListener { x ->
        //                val sp: SharedPreferences = it.getSharedPreferences("DATA", 0)
        //                val token = sp.getString("token", "")
        //                if (token.isEmpty()) {
        //                    System.out.println("No cached token")
        //
        //                    val intent = Intent(it, LoginActivity::class.java)
        //                    this.startActivity(intent)
        //                } else {
        //                    System.out.println("Have cached token: " + token)
        //
        //                    // Do something with token
        //                    val intent = Intent(it.applicationContext, QADetailNewMessageActivity::class.java)
        //                    startActivityForResult(intent, QASectionOverviewFragment.WRITTEN_MESSAGE)
        //                }
        //            }
        //
        //            currentPage += 1
        //
        //
        //            RestAPIHelper.restApiService
        //                .getMessageQuery(currentPage)
        //                .map { x ->
        //                    x.collection.sortedByDescending { y -> DateTime(y.updatedAt) }
        //                }
        //                .observeOn(AndroidSchedulers.mainThread())
        //                .subscribeOn(Schedulers.io())
        //                .subscribe(object : Subscriber<List<JsonModel.MessageJsonObject>>() {
        //                    override fun onCompleted() {
        //                    }
        //
        //                    override fun onError(e: Throwable?) {
        //                        System.out.println(e?.message)
        //                    }
        //
        //                    override fun onNext(x: List<JsonModel.MessageJsonObject>) {
        //                        view?.let { v ->
        //                            v.qaSectionOverview.adapter = RecyclerAdapter((activity as? AppCompatActivity), x.toCollection(arrayListOf<JsonModel.MessageJsonObject>()))
        //
        //                            v.qaSectionOverview.addOnItemTouchListener(
        //                                RecyclerItemClickListener(v.qaSectionOverview.context, object : RecyclerItemClickListener.OnItemClickListener {
        //                                    override fun onItemClick(v: View, position: Int) {
        //
        //                                        System.out.println("Selected recipes id: " + v.id.toString())
        //
        //                                        val id = x[position].id
        //                                        val title = x[position].title
        //                                        val description = x[position].description
        //
        //                                        //v.id
        //
        //                                        val intent = Intent(v.context, QADetailActivity::class.java)
        //                                        intent?.putExtra("message", MessageParcelable(id, title, description))
        //                                        v.context.startActivity(intent)
        //                                    }
        //                                }))
        //                        }
        //                    }
        //                })
        //        }


        configureQuestionRecyclerView(view)
        configureAddQuestionButton(view)
        fetchQuestions()

        return view
    }

    private fun configureQuestionRecyclerView(view: View?) {
        val empty = arrayListOf<JsonModel.MessageJsonObject>()
        view?.qaSectionOverview?.adapter = RecyclerAdapter(activity as AppCompatActivity?, empty)
    }

    private fun fetchQuestions() {
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

            }, { error ->
                Log.e(TAG, "error = $error")
            })

        //            .subscribe(object : Subscriber<List<JsonModel.MessageJsonObject>>() {
        //                override fun onCompleted() {
        //                }
        //
        //                override fun onError(e: Throwable?) {
        //                    System.out.println(e?.message)
        //                }
        //
        //                override fun onNext(x: List<JsonModel.MessageJsonObject>) {
        //                    view?.let { v ->
        //                        v.qaSectionOverview.adapter = RecyclerAdapter((activity as? AppCompatActivity), x.toCollection(arrayListOf<JsonModel.MessageJsonObject>()))
        //
        //                        v.qaSectionOverview.addOnItemTouchListener(
        //                            RecyclerItemClickListener(v.qaSectionOverview.context, object : RecyclerItemClickListener.OnItemClickListener {
        //                                override fun onItemClick(v: View, position: Int) {
        //
        //                                    System.out.println("Selected recipes id: " + v.id.toString())
        //
        //                                    val id = x[position].id
        //                                    val title = x[position].title
        //                                    val description = x[position].description
        //
        //                                    //v.id
        //
        //                                    val intent = Intent(v.context, QADetailActivity::class.java)
        //                                    intent?.putExtra("message", MessageParcelable(id, title, description))
        //                                    v.context.startActivity(intent)
        //                                }
        //                            }))
        //                    }
        //                }
        //            })
    }

    private fun configureAddQuestionButton(view: View?) {
        view?.add_question_button?.setOnClickListener {
            val token = activity.applicationContext.getSharedPreferences("DATA", 0).getString("token", "")
            Log.w(TAG, "token = $token")

            if (token.isEmpty()) {
                val loginActivity = Intent(activity, LoginActivity::class.java)
                startActivity(loginActivity)

            } else {
                val commentsActivity = Intent(activity, QADetailNewMessageActivity::class.java)
                startActivityForResult(commentsActivity, QASectionOverviewFragment.WRITTEN_MESSAGE)
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

    //    class RecyclerItemClickListener(
    //        val c: Context,
    //        val l: OnItemClickListener
    //    ) : RecyclerView.OnItemTouchListener {
    //
    //        interface OnItemClickListener {
    //            fun onItemClick(view: View, position: Int)
    //        }
    //
    //        var listener: OnItemClickListener by Delegates.notNull()
    //        var gestureDetector: GestureDetector by Delegates.notNull()
    //
    //        init {
    //            listener = l
    //            gestureDetector = GestureDetector(c, object : GestureDetector.SimpleOnGestureListener() {
    //                override fun onSingleTapUp(e: MotionEvent): Boolean {
    //                    return true
    //                }
    //            })
    //        }
    //
    //        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    //        }
    //
    //        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
    //            val childView = rv.qaSectionOverview.findChildViewUnder(e.x, e.y)
    //            val callClicked = (childView != null) && (listener != null) && gestureDetector.onTouchEvent(e)
    //            if (callClicked) {
    //                listener.onItemClick(childView, rv.getChildAdapterPosition(childView))
    //            }
    //            return false
    //        }
    //
    //        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    //        }
    //    }

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
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.listview_qa_section_overview, viewGroup, false)
            view.setOnClickListener {
                val question = messageList[position]
                val commentsActivity = Intent(activity, QADetailActivity::class.java)
                commentsActivity.putExtra(
                    "message",
                    MessageParcelable(
                        question.id,
                        question.title,
                        question.description
                    )
                )
                activity?.startActivity(commentsActivity)
            }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val r = messageList[i]
            viewHolder.view.qaSectionOverviewTitle?.text = r.title
            viewHolder.view.qaSectionOverviewDescription?.text = r.description
        }

        override fun getItemCount(): Int {
            return messageList.size
        }

        fun clearAll() {
            messageList.clear()
            notifyDataSetChanged()
        }

        fun addAll(contents: List<JsonModel.MessageJsonObject>) {
            messageList.addAll(contents)
            messageList.sortByDescending { y -> DateTime(y.updatedAt) }
            notifyDataSetChanged()
        }
    }

}