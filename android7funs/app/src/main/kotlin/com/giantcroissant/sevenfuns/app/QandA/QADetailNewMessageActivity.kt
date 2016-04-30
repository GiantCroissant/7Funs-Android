package com.giantcroissant.sevenfuns.app.QandA

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import com.giantcroissant.sevenfuns.app.NewMessageParcelable
import com.giantcroissant.sevenfuns.app.R
import kotlinx.android.synthetic.main.activity_qa_detail_new_message.*
import kotlinx.android.synthetic.main.toolbar.*


/**
 * Created by apprentice on 2/21/16.
 */
class QADetailNewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail_new_message)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "新增問題"

        create_question_button.setOnClickListener { x ->
            if (question_title_text?.text.isNullOrBlank() || question_desc_text?.text.isNullOrBlank()) {
                Snackbar.make(new_message_coordinator_view, "主題和內容都要填寫", Snackbar.LENGTH_LONG).show()
            } else {
                val platformNote = "from Android"
                val newMessageTitle = question_title_text?.text.toString()
                val newMessageDescription = question_desc_text?.text.toString() + "/n" + platformNote

                val intent = Intent()
                intent?.putExtra("message", NewMessageParcelable(newMessageTitle, newMessageDescription))
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
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