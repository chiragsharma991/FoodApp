package dk.eatmore.foodapp.utils

import android.app.Application
import android.support.multidex.MultiDexApplication
import dk.eatmore.foodapp.storage.PreferenceUtil

class EatMoreApplication : MultiDexApplication() , LifeCycleDelegate{


    override fun onCreate() {
        super.onCreate()
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