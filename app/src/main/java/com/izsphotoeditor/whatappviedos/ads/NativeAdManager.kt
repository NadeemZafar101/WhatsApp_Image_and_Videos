package com.izsphotoeditor.whatappviedos.ads
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd

class NativeAdManager(val context: Activity) {
    private var dashboardNativeAd: NativeAd?= null
    val TAG = "nativeAdTraceAndTesting"

    companion object {
        private var instance: NativeAdManager? = null
        fun getInstance(activity: Context): NativeAdManager {
            if (instance == null) {
                instance = NativeAdManager(activity as Activity)
            }
            return instance!!
        }

    }
    fun showNativeDashboardAd(templateView: TemplateView?){
        try{
            if(dashboardNativeAd != null ){
                val styles = NativeTemplateStyle.Builder()
                    .build()
                templateView!!.setStyles(styles)
                templateView.setNativeAd(dashboardNativeAd)
                templateView.visibility = View.VISIBLE
            }else{
                templateView!!.visibility = View.INVISIBLE

            }
        }catch (e : java.lang.Exception){
            e.printStackTrace()
        }
    }
    fun loadNativeDashboardAd(nativeAdCallback: NativeAdCallback){
        if (dashboardNativeAd == null){
            val adLoader: AdLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd {
                    dashboardNativeAd = it
                }
                .withAdListener(object : AdListener() {
                    fun onAdFailedToLoad(errorCode: Int) {

                    }
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        nativeAdCallback.adLoaded()
                        Log.e(TAG, "onAdLoaded  ad is loaded")
                    }
                })
                .build()

            val adRequest = AdRequest.Builder().build()
            adLoader.loadAd(adRequest)
        }
    }
}