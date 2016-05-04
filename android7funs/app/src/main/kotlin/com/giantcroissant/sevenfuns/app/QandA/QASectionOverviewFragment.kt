package com.giantcroissant.sevenfuns.app.QandA

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_qa_section_overview.view.*
import kotlinx.android.synthetic.main.listview_qa_section_overview.view.*
import org.joda.time.DateTime
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * Created by apprentice on 2/1/16.
 */
class QASectionOverviewFragment : Fragment() {
    val TAG = QASectionOverviewFragment::class.java.name
    val INITIAL_PAGE_NUMBER: Int = 1

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


    var mLayoutManager = LinearLayoutManager(activity)
    var mIsLoading = false
    var mScrollListener : RecyclerView.OnScrollListener? = null
    var currentPage = INITIAL_PAGE_NUMBER

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

    override fun onDestroyView() {
        view?.qaSectionOverview?.removeOnScrollListener(mScrollListener)
        super.onDestroyView()
    }

    private fun configureSwipeContainer(view: View?) {
        view?.swipe_container?.setOnRefreshListener {
            currentPage = INITIAL_PAGE_NUMBER
            val recyclerAdapter = view?.qaSectionOverview?.adapter as? RecyclerAdapter
            recyclerAdapter?.clearAll()
            recyclerAdapter?.hasFooter = true

            mIsLoading = true
            fetchQuestions {
                mIsLoading = false
                view.swipe_container?.isRefreshing = false
            }
        }
    }

    private fun configureQuestionRecyclerView(view: View?) {
        val empty = arrayListOf<JsonModel.MessageJsonObject>()
        mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // return if currentPage is lastPage
                val recyclerAdapter = view?.qaSectionOverview?.adapter as? RecyclerAdapter
                if (!recyclerAdapter?.hasFooter!!) {
                    return
                }

                if (!mIsLoading) {
                    val visibleItemCount = mLayoutManager.childCount;
                    val totalItemCount = mLayoutManager.itemCount;
                    val pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        Log.e("...", "Last Item Wow !");
                        mIsLoading = true
                        fetchQuestions {
                            mIsLoading = false
                        }
                    }
                }
            }
        }

        val questionRecyclerView = view?.qaSectionOverview
        questionRecyclerView?.layoutManager = mLayoutManager
        questionRecyclerView?.adapter = RecyclerAdapter(activity as AppCompatActivity?, empty)
        questionRecyclerView?.addOnScrollListener(mScrollListener)

    }


    private fun fetchQuestions(onComplete: () -> Unit = {}) {
        Log.e("...", "fetchQuestions page = " + currentPage);

        RestAPIHelper.restApiService
            .getMessageQuery(currentPage)
            .map { msgJsonList ->
                if (msgJsonList.paginationDetail.isLastPage) {
                    val recyclerAdapter = view?.qaSectionOverview?.adapter as? RecyclerAdapter
                    recyclerAdapter?.hasFooter = false
                }

                msgJsonList.collection.sortedByDescending {
                    json ->
                    DateTime(json.createdAt)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ jsonList ->

                val recycler = view?.qaSectionOverview?.adapter as? RecyclerAdapter
                recycler?.addAll(jsonList)

                currentPage += 1
                onComplete()

            }, { error ->
                Log.e(TAG, "error = $error")

                Snackbar.make(view?.coordinator_view!!, "網路狀態不穩", Snackbar.LENGTH_LONG).show()

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
                        Log.e("TAG", "token = " + combinedHeaderToken)

                        RestAPIHelper.restApiService
                            .createMessage(
                                combinedHeaderToken,
                                JsonModel.MessageCreate(
                                    newMessageParcelable.title,
                                    newMessageParcelable.description,
                                    newMessageParcelable.remark
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

        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1
        var hasFooter = true

        open class ViewHolder(open val v: View) : RecyclerView.ViewHolder(v) {
            var view: View

            init {
                view = v
            }
        }

        class FooterView(override val v: View) : ViewHolder(v) {
            init {
                view = v
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (hasFooter && position == messageList.size) TYPE_FOOTER else TYPE_ITEM
        }

        override fun getItemCount(): Int {
            return messageList.size + (if (hasFooter) 1 else 0)
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            when (viewType) {
                TYPE_ITEM -> {
                    val view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.listview_qa_section_overview, viewGroup, false)
                    return ViewHolder(view)
                }
                TYPE_FOOTER -> {
                    val view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.item_question_list_loading_footer, viewGroup, false)
                    return FooterView(view)
                }
            }
            return ViewHolder(View(viewGroup.context))
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            if (hasFooter && position == messageList.size) {

            } else {
                displayQuestion(viewHolder, position)
            }
        }

        private fun displayQuestion(viewHolder: ViewHolder, position: Int) {
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

        fun addAll(contents: List<JsonModel.MessageJsonObject>) {

            val filtered = contents.filter { it ->
                !messageList.contains(it)
            }

            messageList.addAll(filtered)
            messageList.sortByDescending { y -> DateTime(y.createdAt) }
            notifyDataSetChanged()
        }

        fun clearAll() {
            messageList.clear()
            notifyDataSetChanged()
        }
    }

}

