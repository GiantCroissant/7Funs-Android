package com.giantcroissant.sevenfuns.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


/**
 * Created by apprentice on 2/21/16.
 */
class QADetailNewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qa_detail_new_message)

        // For quick comment response from this activity
        val intent = Intent()
        intent?.putExtra("message", NewMessageParcelable("Some new message"))

        setResult(Activity.RESULT_OK, intent)
        //finish()
    }
}