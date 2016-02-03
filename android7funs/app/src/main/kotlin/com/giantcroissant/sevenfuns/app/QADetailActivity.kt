package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.joda.time.DateTime
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_qa_detail.*

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

        //qaDetailMessageList
        qaDetailAddFab.setOnClickListener { x ->
            System.out.println("hello")
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
        }
    }
}