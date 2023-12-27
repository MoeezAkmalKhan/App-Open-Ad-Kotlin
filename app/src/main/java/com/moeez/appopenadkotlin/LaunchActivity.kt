package com.moeez.appopenadkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.WindowCompat
import com.moeez.appopenadkotlin.databinding.ActivitySplashBinding

class LaunchActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null
    private var secondRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        createTimer(COUNTER_TIMER)

    }

    private fun createTimer(seconds: Long){
        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000){
            override fun onTick(millisecondsFinish: Long) {
                Log.e(TAG, "onTick: $millisecondsFinish" )
                secondRemaining = millisecondsFinish / 1000 + 1
                binding?.timerTv?.text = "$secondRemaining"

            }

            override fun onFinish() {
                Log.e(TAG, "onFinish: " )
                secondRemaining = 0
                binding?.timerTv?.text = ""

                val application = getApplication()
                if (application !is MyApplication) {
                    Log.e(TAG, "onFinish: Fail to cast application to myapplication" )
                    startActivity()
                    return
                }
                application.showAdIfAvailable(this@LaunchActivity,
                    object : MyApplication.OnShowAdCompleteListener{
                        override fun onShowAdComplete() {
                            Log.e(TAG, "onShowAdComplete: " )
                            startActivity()
                            return
                        }
                    })
            }
        }

        countDownTimer.start()

    }

    private fun startActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object{
        private const val TAG = "Launch_Activity_Tag"
        private const val COUNTER_TIMER: Long = 5
//        if your ad is not showing make COUNTER_TIMER 8 or 10
    }

}