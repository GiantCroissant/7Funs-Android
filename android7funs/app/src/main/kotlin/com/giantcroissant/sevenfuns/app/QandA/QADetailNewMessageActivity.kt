package com.giantcroissant.sevenfuns.app.QandA

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.giantcroissant.sevenfuns.app.NewMessageParcelable
import com.giantcroissant.sevenfuns.app.R

import kotlinx.android.synthetic.main.activity_qa_detail_new_message.*


/**
 * Created by apprentice on 2/21/16.
 */
class QADetailNewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail_new_message)

        qaNewMessageButton.setOnClickListener { x ->
            val newMessageTitle = qaNewMessageTitleText?.text.toString()
            val newMessageDescription = qaNewMessageDescriptionText?.text.toString()

            // For quick comment response from this activity
            val intent = Intent()
            intent?.putExtra("message", NewMessageParcelable(newMessageTitle, newMessageDescription))

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}