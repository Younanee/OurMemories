package com.kotlin.ourmemories.view.splash.presenter

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.kotlin.ourmemories.DB.DBManagerMemory
import com.kotlin.ourmemories.DB.MemoryData
import com.kotlin.ourmemories.R
import com.kotlin.ourmemories.view.MainActivity
import com.kotlin.ourmemories.data.source.autologin.AutoLoginRepository
import com.kotlin.ourmemories.data.source.autologin.UserProfile
import com.kotlin.ourmemories.manager.PManager
import com.kotlin.ourmemories.view.login.LoginActivity
import com.kotlin.ourmemories.view.splash.SplashActivity
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton
import java.io.IOException

/**
 * Created by kimmingyu on 2017. 11. 2..
 */
class SplashPresenter: SplashContract.Presenter {
    lateinit override var profileData: AutoLoginRepository
    lateinit override var activity:SplashActivity

    var token:String? = null

    override val mHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    val userId:String by lazy {
        PManager.getUserId()
    }
    val isLogin:String by lazy {
        PManager.getUserIsLogin()
    }

    init {
        PManager.init()
    }

    // 서버 전송시 callBack 받는 부분
    private val requestProfileCallback:Callback = object :Callback{
        // 실패 했을 경우
        override fun onFailure(call: Call?, e: IOException?) {
            activity.runOnUiThread{
                activity.alert(activity.resources.getString(R.string.error_message_network), "Login"){
                    yesButton { activity.finish() }
                }.show()
            }
        }
        // 성공 했을 경우
        override fun onResponse(call: Call?, response: Response?) {
            val responseData:String = response?.body()!!.string()
            //현재 보안을 위해서 우선은 공유저장소에 들어있는 값(현재 스마트폰의 저장 정보)과 서버에 저장되어 있는 값을 비교//
            //만약 다를 시 해커가 우회에서 들어올 수 있으므로 로그인 화면으로 이동하여 다시 정상적으로 토큰등을 발급받게 함.//

            Log.d("hoho", responseData)
            val profileRequest: UserProfile = Gson().fromJson(responseData, UserProfile::class.java)

            val sUserId:String = profileRequest.userProfileResult.userId

            if(sUserId == userId){
                activity.runOnUiThread {
                    val loginAuth = profileRequest.userProfileResult.authLogin
                    if(loginAuth == "1") // 로그인 한 경우
                    {
                        // 메인화면으로 이동전 공유저장소에 내용을 최신 정보로 변경
                        PManager.setUserName(profileRequest.userProfileResult.userName)
                        PManager.setUserProfileImageUrl(profileRequest.userProfileResult.userProfileImageUrl)
                        PManager.setUserEmail(profileRequest.userProfileResult.userEmail)
                        PManager.setUserId(profileRequest.userProfileResult.userId)
                        PManager.setUserIsLogin(profileRequest.userProfileResult.authLogin)
                        PManager.setUserFcmRegId(token!!)

                        DBManagerMemory.init(activity.applicationContext)
                        // 넘어온 메모리애들을 풀어서 데이터 형식으로 만들어 준다음 내부 디비를 완전히 비우고, 다시 저장한다
                        if(profileRequest.userProfileMemoryResult != null) {
                            val item = arrayOfNulls<MemoryData>(profileRequest.userProfileMemoryResult.size)
                            for (i in 0 until profileRequest.userProfileMemoryResult.size) {
                                item[i] = MemoryData(profileRequest.userProfileMemoryResult[i]._id, profileRequest.userProfileMemoryResult[i].memoryTitle, profileRequest.userProfileMemoryResult[i].memoryLatitude.toDouble(),
                                        profileRequest.userProfileMemoryResult[i].memoryLongitude.toDouble(), profileRequest.userProfileMemoryResult[i].memoryNation, profileRequest.userProfileMemoryResult[i].memoryFromDate,
                                        profileRequest.userProfileMemoryResult[i].memoryToDate, profileRequest.userProfileMemoryResult[i].memoryClassification.toInt())
                            }
                            DBManagerMemory.deleteTable()
                            (0 until item.size).forEach { i ->
                                DBManagerMemory.addMemory(item[i]!!)
                            }
                            DBManagerMemory.close()
                        }else{
                            val cursor = DBManagerMemory.getMemoryAllWithCursor()
                            if(cursor.count != 0){
                                DBManagerMemory.deleteTable()
                            }
                            DBManagerMemory.close()
                        }
                        activity.startActivity<MainActivity>()
                        activity.finish()
                    } else if(loginAuth == "0") // 로그아웃한 경우
                    {
                        loginPageIntent()
                    }
                }
            }else
                loginPageIntent()
        }
    }

    // 자동 로그인을 확인하기 위한 스레드 시간 벌기
    override fun autoLogin() {
        activity.runOnUiThread{
            mHandler.postDelayed({
                //최초 앱을 실행 시 로그인이 되어있는지 검사//
                isLoginCheck()
            }, 1500)
        }
    }

    // 공유저장소에 데이터 값이 있으면 바로 서버로 프로필 달라고 하기, 없으면 로그인 페이지
    override fun isLoginCheck() {
        when(userId){
            // 유저가 현재공유저장소에 값이 있는지를 비교
            ""->{
                loginPageIntent()
            }
            else->{
                when(isLogin){
                    "0"-> {
                        loginPageIntent()
                    }
                    "1"-> {
                        token = FirebaseInstanceId.getInstance().token
                        if (token == null){
                            activity.alert(activity.resources.getString(R.string.error_message_network), "Login"){
                                yesButton { activity.finish() }
                            }.show()
                        }else {
                            profileData.getProfile(userId, token!!, requestProfileCallback, activity)
                        }
                    }
                    else-> {
                        loginPageIntent()
                    }
                }

            }
        }
    }

    fun loginPageIntent(){
        activity.startActivity<LoginActivity>()
        activity.finish()
    }



}