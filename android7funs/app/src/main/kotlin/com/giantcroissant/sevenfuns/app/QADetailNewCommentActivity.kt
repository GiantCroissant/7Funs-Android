package com.giantcroissant.sevenfuns.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_qa_detail_new_comment.*

/**
 * Created by apprentice on 2/20/16.
 */
class QADetailNewCommentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail_new_comment)

        // For quick comment response from this activity
        qaNewCommentButton.setOnClickListener { x ->
            val newComment = qaNewCommentText?.text.toString()
            val intent = Intent()
            intent?.putExtra("comment", CommentParcelable(newComment))

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}