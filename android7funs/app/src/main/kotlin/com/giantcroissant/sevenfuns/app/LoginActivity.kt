package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_login.*

/**
 * Created by apprentice on 2/3/16.
 */
class LoginActivity : AppCompatActivity() {
    val REQUEST_SIGNUP: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //
        linkToSignup.setOnClickListener { x ->
            val intent = Intent(applicationContext, SignupActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            REQUEST_SIGNUP -> {
                if (resultCode == RESULT_OK) {
                    this.finish()
                }
            }
        }
    }
}