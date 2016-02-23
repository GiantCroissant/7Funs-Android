package com.giantcroissant.sevenfuns.app.QandA

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

        create_question_button.setOnClickListener { x ->
            val newMessageTitle = question_title_text?.text.toString()
            val newMessageDescription = question_desc_text?.text.toString()

            val intent = Intent()
            intent?.putExtra("message", NewMessageParcelable(newMessageTitle, newMessageDescription))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}