package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by apprentice on 2/20/16.
 */
class QADetailNewCommentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail_new_comment)

        // For quick comment response from this activity
        val intent = Intent()
        intent?.putExtra("comment", CommentParcelable("Some new comment"))

        setResult(QADetailActivity.WRITTEN_COMMENT, intent)
    }
}