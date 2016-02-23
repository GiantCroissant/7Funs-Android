package com.giantcroissant.sevenfuns.app.QandA

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.giantcroissant.sevenfuns.app.CommentParcelable
import com.giantcroissant.sevenfuns.app.R
import kotlinx.android.synthetic.main.activity_qa_detail_new_comment.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by apprentice on 2/20/16.
 */
class QADetailNewCommentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail_new_comment)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // For quick comment response from this activity
        qaNewCommentButton.setOnClickListener { x ->
            val newComment = qaNewCommentText?.text.toString()
            val intent = Intent()
            intent?.putExtra("comment", CommentParcelable(newComment))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}