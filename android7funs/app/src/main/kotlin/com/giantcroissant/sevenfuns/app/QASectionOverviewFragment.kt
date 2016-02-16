package com.giantcroissant.sevenfuns.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_qa_section_overview.view.*
import kotlinx.android.synthetic.main.listview_qa_section_overview.view.*
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import kotlin.properties.Delegates

import org.joda.time.DateTime


/**
 * Created by apprentice on 2/1/16.
 */
class QASectionOverviewFragment : Fragment() {
    public companion object {
        public fun newInstance(): QASectionOverviewFragment {
            val fragment = QASectionOverviewFragment().apply {
                val args = Bundle().apply {
                }

                arguments = args
            }

            return fragment
        }
    }

    val retrofit = Retrofit
            .Builder()
            .baseUrl("https://www.7funs.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    var maxPage = 0
    var currentPage = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_qa_section_overview, container, false)

        // Avoid java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.support.v7.widget.RecyclerView$LayoutManager.canScrollVertically()' on a null object reference
        view?.let { v ->
            v.qaSectionOverview.layoutManager = LinearLayoutManager(v.context)
        }

        view?.qaSectionSwipeContainer?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                (activity as? AppCompatActivity)?.let {
                    currentPage = currentPage + 1
                    val messageQuery = restApiService.getMessageQuery(currentPage)
                    messageQuery
                            .flatMap { x -> Observable.just(x) }
                            .map { x ->
                                //x.collection
                                x.collection.sortedByDescending { y -> DateTime(y.updatedAt) }
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(object : Subscriber<List<JsonModel.MessageJsonObject>>() {
                                override fun onCompleted() {}
                                override fun onError(e: Throwable?) {
                                    System.out.println(e?.message)
                                }
                                override fun onNext(x: List<JsonModel.MessageJsonObject>) {
                                    view?.let { v ->
                                        //v.qaSectionOverview.layoutManager = LinearLayoutManager(v.context)
                                        //v.qaSectionOverview.adapter = RecyclerAdapter((activity as? AppCompatActivity), x)
                                        (v.qaSectionOverview.adapter as? RecyclerAdapter)?.addAll(x)
                                    }
                                }
                            })
                }

                view?.qaSectionSwipeContainer?.isRefreshing = false
            }
        })

        (activity as? AppCompatActivity)?.let {
            currentPage = currentPage + 1
            val messageQuery = restApiService.getMessageQuery(currentPage)
            messageQuery
                    //.flatMap { x -> Observable.just(x) }
                    .map { x ->
                        x.collection.sortedByDescending { y -> DateTime(y.updatedAt) }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : Subscriber<List<JsonModel.MessageJsonObject>>() {
                        override fun onCompleted() {}
                        override fun onError(e: Throwable?) {
                            System.out.println(e?.message)
                        }
                        override fun onNext(x: List<JsonModel.MessageJsonObject>) {
                            view?.let { v ->
//                                v.qaSectionOverview.layoutManager = LinearLayoutManager(v.context)
                                v.qaSectionOverview.adapter = RecyclerAdapter((activity as? AppCompatActivity), x.toArrayList())

                                v.qaSectionOverview.addOnItemTouchListener(
                                        RecyclerItemClickListener(v.qaSectionOverview.context, object : RecyclerItemClickListener.OnItemClickListener {
                                            override fun onItemClick(v: View, position: Int) {

                                                System.out.println("Selected recipes id: " + v.id.toString())

                                                val id = x[position].id

                                                //v.id

                                                val intent = Intent(v.context, QADetailActivity::class.java)
                                                intent?.putExtra("message", MessageParcelable(id))
                                                v.context.startActivity(intent)
                                            }
                                        }))
                            }
                        }
                    })
        }

        return view
    }

    public class RecyclerItemClickListener(val c: Context, val l: OnItemClickListener) : RecyclerView.OnItemTouchListener {
        public interface OnItemClickListener {
            fun onItemClick(view: View, position: Int)
        }

        var listener: OnItemClickListener by Delegates.notNull()
        var gestureDetector: GestureDetector by Delegates.notNull()

        init {
            listener = l
            gestureDetector = GestureDetector(c, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent) : Boolean {
                    return true
                }
            })
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent) : Boolean {
            val childView = rv.qaSectionOverview.findChildViewUnder(e.x, e.y)
            val callClicked = (childView != null) && (listener != null) && gestureDetector.onTouchEvent(e)

            if (callClicked) {
                listener.onItemClick(childView, rv.getChildAdapterPosition(childView))
            }

            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

        }
    }

    public class RecyclerAdapter(val activity: AppCompatActivity?, val messageList: MutableList<JsonModel.MessageJsonObject>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        public class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
            public var view: View by Delegates.notNull()

            init {
                view = v
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) : ViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.listview_qa_section_overview, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val r = messageList[i]

            viewHolder.view?.qaSectionOverviewTitle?.text = r.title
            viewHolder.view?.qaSectionOverviewDescription?.text = r.description
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

        override fun getItemCount() : Int { return messageList.size }

        public fun clearAll() {
            messageList.clear()
            notifyDataSetChanged()
        }

        public fun addAll(contents: List<JsonModel.MessageJsonObject>) {
            messageList.addAll(contents)
            messageList.sortByDescending { y -> DateTime(y.updatedAt) }
            notifyDataSetChanged()
        }
    }

}