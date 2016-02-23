package com.giantcroissant.sevenfuns.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.annotations.NotNull
import retrofit2.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.RxJavaCallAdapterFactory
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import kotlin.properties.Delegates

/**
 * Created by apprentice on 2/3/16.
 */
class LoginActivity : AppCompatActivity() {

    val retrofit = Retrofit
        .Builder()
        .baseUrl("https://www.7funs.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()

    val restApiService = retrofit.create(RestApiService::class.java)

    val REQUEST_SIGNUP: Int = 0
    var callbackManager: CallbackManager by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loginButton?.setOnClickListener { x ->
            val email = loginEmailText.text.toString()
            val password = loginPasswordText.text.toString()
            restApiService.login(JsonModel.LoginJsonObject(email, password))
                .subscribeOn(Schedulers.io())
                .subscribe(object : Subscriber<JsonModel.LoginResultJsonObject>() {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable?) {
                        System.out.println(e?.message)
                    }

                    override fun onNext(loginResult: JsonModel.LoginResultJsonObject) {
                        val sp: SharedPreferences = getSharedPreferences("DATA", 0)
                        sp.edit().putString("token", loginResult.token).commit()
                        finish()
                    }
                })
        }

        callbackManager = com.facebook.CallbackManager.Factory.create()
        loginFbButton?.setReadPermissions("public_profile email")
        loginFbButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                System.out.println("fb login ok")

                val accessToken = loginResult.accessToken.token

                restApiService.loginViaFbId(JsonModel.LoginFbJsonObject(accessToken))
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : Subscriber<JsonModel.LoginResultJsonObject>() {
                        override fun onCompleted() {
                        }

                        override fun onError(e: Throwable?) {
                            System.out.println(e?.message)
                        }

                        override fun onNext(x: JsonModel.LoginResultJsonObject) {
                            Log.e("TAG", "x.token = ${x.token}")

                            val sp: SharedPreferences = applicationContext.getSharedPreferences("DATA", 0)
                            val result = sp.edit().putString("token", x.token).commit()
                            Log.e("TAG", "result = $result")

                            val currentToken = sp.getString("token", "DEFAULT")
                            Log.e("TAG", "currentToken = $currentToken")

                            finish()
                        }
                    })
            }

            override fun onCancel() {

            }

            override fun onError(e: FacebookException) {
                System.out.println(e.cause.toString())
            }
        })


        //        loginFbButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
        //            override fun onSuccess(loginResult: LoginResult) {
        //                val accessToken = loginResult.accessToken.token
        //
        //                System.out.println("fb access token " + accessToken)
        //
        ////                restApiService.loginViaFbId(JsonModel.LoginFbJsonObject(accessToken))
        ////                    .subscribeOn(Schedulers.io())
        ////                    .subscribe(object : Subscriber<JsonModel.LoginResultJsonObject>() {
        ////                        override fun onCompleted() {
        ////                        }
        ////
        ////                        override fun onError(e: Throwable?) {
        ////                            System.out.println(e?.message)
        ////                        }
        ////
        ////                        override fun onNext(x: JsonModel.LoginResultJsonObject) {
        ////                            val sp: SharedPreferences = getSharedPreferences("DATA", 0)
        ////                            sp.edit().putString("token", x.token).commit()
        ////                            finish()
        ////                        }
        ////                    })
        //            }
        //
        //            override fun onCancel() {
        //
        //            }
        //
        //            override fun onError(e: FacebookException) {
        //                System.out.println(e.message)
        //            }
        //        })

        //        loginButton?.setOnClickListener { x ->
        //            val retrofit = Retrofit
        //                    .Builder()
        //                    .baseUrl("https://www.7funs.com")
        //                    .addConverterFactory(GsonConverterFactory.create())
        //                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        //                    .build()
        //
        //            val restApiService = retrofit.create(RestApiService::class.java)
        //
        //            val email = loginEmailText.text.toString()
        //            val password = loginPasswordText.text.toString()
        //
        //            System.out.println(email)
        //            System.out.println(password)
        //
        //            restApiService.login(JsonModel.LoginJsonObject(email, password))
        //                .map {x ->
        //                    System.out.println(x.token)
        //                    x.token
        //                }
        ////                .observeOn(Schedulers.io())
        //                .observeOn(AndroidSchedulers.mainThread())
        ////                .subscribeOn(AndroidSchedulers.mainThread())
        //                .subscribe(object : Subscriber<String>() {
        //                    override fun onCompleted() {}
        //                    override fun onError(e: Throwable?) {
        //                        System.out.println("some error occur")
        //                        System.out.println(e?.message)
        //                    }
        //                    override fun onNext(loginResult: String) {
        ////                        val sp: SharedPreferences = getSharedPreferences("DATA", 0)
        ////                        sp.edit().putString("token", loginResult.token).commit()
        ////                        finish()
        //                    }
        //                })
        ////                    .observeOn(Schedulers.io())
        ////                    .observeOn(AndroidSchedulers.mainThread())
        ////                    .subscribeOn(AndroidSchedulers.mainThread())
        ////                    .subscribe(object : Subscriber<JsonModel.LoginResultJsonObject>() {
        ////                        override fun onCompleted() {}
        ////                        override fun onError(e: Throwable?) {
        ////                            System.out.println("some error occur")
        ////                            System.out.println(e?.message)
        ////                        }
        ////                        override fun onNext(loginResult: JsonModel.LoginResultJsonObject) {
        ////                            val sp: SharedPreferences = getSharedPreferences("DATA", 0)
        ////                            sp.edit().putString("token", loginResult.token).commit()
        ////                            finish()
        ////                        }
        ////                    })
        //        }

        //
        linkToSignup?.setOnClickListener { x ->
            val intent = Intent(applicationContext, SignupActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //val callbackManager = com.facebook.CallbackManager.Factory.create()
        callbackManager.onActivityResult(requestCode, resultCode, data)
        System.out.println("LoginActivity::onActivityResult")
        when (requestCode) {
            REQUEST_SIGNUP -> {
                if (resultCode == RESULT_OK) {
                    this.finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}