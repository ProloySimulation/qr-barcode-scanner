package com.proloy.qrscanner

import android.app.Application
import com.anythink.core.api.ATSDK
import com.facebook.ads.AudienceNetworkAds

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Audience Network Ads
        AudienceNetworkAds.initialize(this)

        // Initialize ATSDK (replace APP_ID and APP_KEY with your actual values)
        ATSDK.init(this, getString(R.string.app_id), getString(R.string.app_key))
    }
}