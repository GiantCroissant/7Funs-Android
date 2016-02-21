package com.giantcroissant.sevenfuns.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Subscriber
import rx.schedulers.Schedulers

/**
 * Created by apprentice on 2/3/16.
 */
class SignupActivity : AppCompatActivity() {
    val retrofit = Retrofit
            .Builder()
            .baseUrl("https://www.7funs.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signupCreateAccount.setOnClickListener { x ->
            val name = signupInputName?.text.toString()
            val email = signupInputEmail?.text.toString()
            val password = signupInputPassword?.text.toString()

            restApiService.register(JsonModel.RegisterJsonObject(email, name, password, password))
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<JsonModel.LoginResultJsonObject>() {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable?) {
                        System.out.println(e?.message)
                    }

                    override fun onNext(x: JsonModel.LoginResultJsonObject) {
                        val sp: SharedPreferences = getSharedPreferences("DATA", 0)
                        sp.edit().putString("token", x.token).commit()
                        val intent = Intent()
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                })
        }
    }
}