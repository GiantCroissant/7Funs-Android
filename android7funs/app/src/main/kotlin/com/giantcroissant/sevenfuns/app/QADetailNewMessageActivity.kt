package com.giantcroissant.sevenfuns.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

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
            val intent = android.content.Intent()
            intent?.putExtra("message", com.giantcroissant.sevenfuns.app.NewMessageParcelable(newMessageTitle, newMessageDescription))

            setResult(android.app.Activity.RESULT_OK, intent)
            finish()
        }
    }
}