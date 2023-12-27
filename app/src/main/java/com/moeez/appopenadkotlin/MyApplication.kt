package com.moeez.appopenadkotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.Date

class MyApplication : Application(), ActivityLifecycleCallbacks, LifecycleObserver {

    private lateinit var appOpenAdManager: AppOpenAdManager
    private lateinit var currentActivity: Activity

    override fun onCreate() {
        super.onCreate()
        app = this
        registerActivityLifecycleCallbacks(this)
        MobileAds.initialize(this)

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(""))
            .build())


        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onMoveToForeground(){
        appOpenAdManager.showAdIfAvailable(currentActivity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        Log.e(TAG, "onActivityStarted: " )
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }

    private inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        private var loadTime: Long = 0

        private fun loadAd(context: Context) {

            if (isLoadingAd || isAdAvailable()) {
                return
            }
            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(context, getString(R.string.testAppOpenAdId), request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object  : AppOpenAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.e(TAG, "onAdFailedToLoad: ${adError.message}" )
                    }

                    override fun onAdLoaded(ad: AppOpenAd) {
                        super.onAdLoaded(ad)
                        Log.e(TAG, "onAdLoaded: " )
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time

                        Log.d(TAG, "adLoaded... ")
                        Toast.makeText(this@MyApplication, "Ad loaded...", Toast.LENGTH_SHORT).show()
                    }
                })

        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours

        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(
                activity,
                object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        Log.e(TAG, "onShowAdComplete: " )
                    }
                })
        }

        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener ){

            if (isShowingAd){
                Log.e(TAG, "showAdIfAvailable: App open ad is already showing..." )
                return
            }

            if (!isAdAvailable()) {
                Log.e(TAG, "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

                appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdClicked() {
                        super.onAdClicked()
                        Log.e(TAG, "onAdClicked: " )
                    }

                    override fun onAdDismissedFullScreenContent() {
                        // Called when full screen content is dismissed.
                        // Set the reference to null so isAdAvailable() returns false.
                        Log.e(TAG, "Ad dismissed fullscreen content.")
                        appOpenAd = null
                        isShowingAd = false
                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Called when fullscreen content failed to show.
                        // Set the reference to null so isAdAvailable() returns false.
                        Log.e(TAG, adError.message)
                        appOpenAd = null
                        isShowingAd = false
                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.e(TAG, "onAdImpression: " )
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        Log.e(TAG, "Ad showed fullscreen content.")
                    }
                }

            isShowingAd = true
            appOpenAd!!.show(activity)

        }

    }

    companion object {
        private const val TAG = "MyApplication_TAG"
        @SuppressLint("StaticFieldLeak")
        var app: MyApplication? = null
            private set
    }
}
