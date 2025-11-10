package com.example.glidedemo

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.firebase.FirebaseApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import java.util.concurrent.CopyOnWriteArrayList

class App : Application() {

    companion object {
        private var _instance: App? = null
        fun getInstance() = _instance!!
        fun getContext() = _instance!!.applicationContext!!
    }


    val activities = CopyOnWriteArrayList<Activity>()

    override fun onCreate() {
        super.onCreate()
        _instance = this
        FirebaseApp.initializeApp(this)
        val config =
            AppMetricaConfig.newConfigBuilder("b96ce3d4-bbf9-4acf-ab22-6a5c57da0a68").build()
        // Initializing the AppMetrica SDK.
        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this)
        listen()
    }

    fun finishAllActivity() {
        activities.forEach {
            it.finish()
        }
        activities.clear()
    }


    private fun listen() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                for (act in activities) {
                    if (act.javaClass.simpleName == activity.javaClass.simpleName) {
                        activity.finish()
                        activities.remove(activity)
                        break
                    }
                }
                if (activity.localClassName != "app.lawnchair.LawnchairLauncher") {
                    activities.add(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                activities.remove(activity)

            }
        })
    }


}