package com.giantcroissant.sevenfuns.app.QandA

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
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
        supportActionBar?.title = "回覆問題"

        create_comment_button.setOnClickListener { x ->
            val newComment = new_comment_text?.text.toString()

            val intent = Intent()
            intent?.putExtra("comment", CommentParcelable(newComment))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    override fun onSupportNavigateUp(): Boolean {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        finish()
        return true
    }
}