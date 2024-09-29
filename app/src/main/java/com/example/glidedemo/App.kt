package com.example.glidedemo

import android.app.Application
import com.google.firebase.FirebaseApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

class App : Application() {

    companion object {
        private var _instance: App? = null
        fun getContext() = _instance!!.applicationContext!!
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        FirebaseApp.initializeApp(this)
        val config =
            AppMetricaConfig.newConfigBuilder("b96ce3d4-bbf9-4acf-ab22-6a5c57da0a68").build()
        // Initializing the AppMetrica SDK.
        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this)
    }


}