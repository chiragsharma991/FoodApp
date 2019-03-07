package dk.eatmore.foodapp.utils

import android.app.Application
import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import dk.eatmore.foodapp.storage.PreferenceUtil
import io.fabric.sdk.android.Fabric

class EatMoreApplication : MultiDexApplication() , LifeCycleDelegate{


    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())
        PreferenceUtil.init(this)
        val lifeCycleHandler = AppLifecycleHandler(this)
        registerLifecycleHandler(lifeCycleHandler)

    }

    override fun onAppBackgrounded() {


    }

    override fun onAppForegrounded() {



    }

    private fun registerLifecycleHandler(lifeCycleHandler: AppLifecycleHandler) {
        registerActivityLifecycleCallbacks(lifeCycleHandler)
        registerComponentCallbacks(lifeCycleHandler)
    }



}