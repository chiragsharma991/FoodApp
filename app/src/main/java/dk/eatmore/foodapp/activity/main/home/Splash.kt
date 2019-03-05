package dk.eatmore.foodapp.activity.main.home


import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.utils.BaseActivity
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.facebook.AccessToken
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.DialogUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.preference.PreferenceGroup
import android.provider.Settings
import android.support.constraint.ConstraintSet
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.animation.*
import android.widget.CheckBox
import android.widget.CompoundButton
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.TextDelegate
import com.airbnb.lottie.model.KeyPath
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.activity.main.home.intro.Intro_slider
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.splash_activity.*


class Splash : BaseActivity() {

    private lateinit var msplash: Splash
    private var currentVersionName: String? = null
    private var latestVersionName: String? = null


    companion object {
        val TAG = "Splash"
        var can_i_use_lastLogin=true
        fun newInstance(): Splash {
            return Splash()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)
        initView(savedInstanceState)
        //   log(TAG, "savedInstanceState..."+savedInstanceState)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge(TAG, "---Activity Result Activity---")
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun initView(savedInstanceState: Bundle?) {
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        hideKeyboard()
        fullScreen()
        PreferenceUtil.save()
        getCurrentVersion()
        screenPlay()

        //eatmore_logo_img.startAnimation(alphaAnimation)


        //val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        //img_logo.startAnimation(animation)


    }

    private fun screenPlay() {
        Handler().postDelayed({

          //  showComponents()

            Handler().postDelayed({
                val anim = ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setDuration(800);
                base_view.visibility=View.VISIBLE
                base_view.startAnimation(anim)
                eatmore_logo_img.visibility = View.VISIBLE
                shimmerLayout.startShimmerAnimation()
                shimmerLayout.repeatCount=0
                shimmerLayout.startAnimation(AnimationUtils.loadAnimation(this@Splash, R.anim.text_bottomto_top))
                Handler().postDelayed({
                       // GetLatestVersion().execute()
                    anyupdates()
                },4000)

            },100)

        },800)


    }

    private fun savedevice_token() {
        lottie_loader.visibility=View.VISIBLE
        callAPI(ApiCall.devicetoken (
                token = PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,"")!!,
                user_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!,
                device_type = Constants.DEVICE_TYPE_VALUE,
                eatmore_app = true,
                auth_key = Constants.AUTH_VALUE
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
//                    PreferenceUtil.putValue(PreferenceUtil.DEVICE_TOKEN,Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
//                    PreferenceUtil.save()
                    moveToLogin()
                }else{
                    lottie_loader.visibility=View.GONE
                    DialogUtils.openDialogDefault(context = this@Splash,btnNegative = "",btnPositive = getString(R.string.try_again), color = ContextCompat.getColor(this@Splash,R.color.black),msg =getString(R.string.error_407),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                        override fun onPositiveButtonClick(position: Int) {
                            savedevice_token()
                        }
                        override fun onNegativeButtonClick() {}
                    })
                }
            }

            override fun onFail(error: Int) {
                lottie_loader.visibility=View.GONE
                when (error) {
                    404 -> {
                        DialogUtils.openDialogDefault(context = this@Splash,btnNegative = "",btnPositive = getString(R.string.ok), color = ContextCompat.getColor(this@Splash,R.color.black),msg =getString(R.string.error_404),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                            override fun onPositiveButtonClick(position: Int) {finish()}
                            override fun onNegativeButtonClick() {}
                        })
                    }
                    100 -> {
                        DialogUtils.openDialogDefault(context = this@Splash,btnNegative = "",btnPositive = getString(R.string.ok), color = ContextCompat.getColor(this@Splash,R.color.black),msg =getString(R.string.internet_not_available),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                            override fun onPositiveButtonClick(position: Int) {finish()}
                            override fun onNegativeButtonClick() {}
                        })
                    }
                }
            }
        })
    }


    private fun anyupdates() {
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)
        callAPI(ApiCall.force_update(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    var androidversion=""
                    var latestversion=""
                    try {
                        val pm = packageManager
                        val pInfo = pm.getPackageInfo(packageName, 0)
                        androidversion=pInfo.versionName
                        latestversion=jsonObject.get(Constants.DATA).asJsonObject.get(Constants.EATMORE_VERSION_ANDROID).asString
                        if(latestversion == androidversion){
                            //ignore
                            savedevice_token()
                        }else{
                            // update
                            if(jsonObject.get(Constants.DATA).asJsonObject.get(Constants.FORCE_UPDATE_ANDROID).asString == "1"){
                                // force update
                                DialogUtils.openDialogDefault(context = this@Splash,btnNegative = "",btnPositive = getString(R.string.ok), color = ContextCompat.getColor(this@Splash,R.color.default_permission),msg = getString(R.string.this_version_is_no_longer), title = getString(R.string.update),onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                                    override fun onPositiveButtonClick(position: Int) {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URLOFAPPFROMPLAYSTORE)))
                                        finish()
                                    }
                                    override fun onNegativeButtonClick() {}
                                })
                            }else{
                                // non force update
                                if(PreferenceUtil.getBoolean(PreferenceUtil.IS_SKIP_VERSION,false)){
                                    // if version skip is true
                                    if(PreferenceUtil.getString(PreferenceUtil.SKIPED_VERSION_NAME,"") == latestversion){
                                        // skip alert and continue
                                        savedevice_token()
                                    }else{
                                       // show alert bcz version is changed
                                        val builder = AlertDialog.Builder(this@Splash, R.style.AppCompatAlertDialogDefaultStyle)
                                        builder.setMessage(getString(R.string.this_version_is_no_longer))
                                        builder.setTitle(getString(R.string.update))
                                        builder.setCancelable(false)
                                        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URLOFAPPFROMPLAYSTORE)))
                                            finish()
                                        }
                                        builder.setNegativeButton(getString(R.string.cancel)){_,_ ->
                                            savedevice_token()
                                        }
                                        builder.setNeutralButton(getString(R.string.skip_this_version)){_,_ ->
                                            //dont show again this version
                                            PreferenceUtil.putValue(PreferenceUtil.SKIPED_VERSION_NAME,latestversion)
                                            PreferenceUtil.putValue(PreferenceUtil.IS_SKIP_VERSION,true)
                                            PreferenceUtil.save()
                                            savedevice_token()
                                        }
                                        val alert = builder.create()
                                        alert.show()
                                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this@Splash,R.color.default_permission))
                                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this@Splash,R.color.default_permission))
                                        alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this@Splash,R.color.default_permission))
                                    }

                                } else {
                                    // if version skip is false
                                    val builder = AlertDialog.Builder(this@Splash, R.style.AppCompatAlertDialogDefaultStyle)
                                    builder.setMessage(getString(R.string.this_version_is_no_longer))
                                    builder.setTitle(getString(R.string.update))
                                    builder.setCancelable(false)
                                    builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URLOFAPPFROMPLAYSTORE)))
                                        finish()
                                    }
                                    builder.setNegativeButton(getString(R.string.cancel)){_,_ ->
                                        savedevice_token()
                                    }
                                    builder.setNeutralButton(getString(R.string.skip_this_version)){_,_ ->
                                        //dont show again this version
                                        PreferenceUtil.putValue(PreferenceUtil.SKIPED_VERSION_NAME,latestversion)
                                        PreferenceUtil.putValue(PreferenceUtil.IS_SKIP_VERSION,true)
                                        PreferenceUtil.save()
                                        savedevice_token()
                                    }
                                    val alert = builder.create()
                                    alert.show()
                                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this@Splash,R.color.default_permission))
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this@Splash,R.color.default_permission))
                                    alert.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this@Splash,R.color.default_permission))

                                }

                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        loge(TAG,e.message.toString())
                    }
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        DialogUtils.openDialogDefault(context = this@Splash,btnNegative = "",btnPositive = getString(R.string.ok), color = ContextCompat.getColor(this@Splash,R.color.black),msg =getString(R.string.error_404),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                            override fun onPositiveButtonClick(position: Int) {finish()}
                            override fun onNegativeButtonClick() {}
                        })
                    }
                    100 -> {
                        DialogUtils.openDialogDefault(context = this@Splash,btnNegative = "",btnPositive = getString(R.string.ok), color = ContextCompat.getColor(this@Splash,R.color.black),msg =getString(R.string.internet_not_available),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                            override fun onPositiveButtonClick(position: Int) {finish()}
                            override fun onNegativeButtonClick() {}
                        })
                    }
                }
            }
        })
    }

    private fun getCurrentVersion() {

        try {

            val pm = this.packageManager
            val pInfo = pm.getPackageInfo(this.packageName, 0)

            currentVersionName = pInfo.versionName
            Log.e(TAG, "currentVersionName : $currentVersionName")


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private inner class GetLatestVersion : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): String? {

            try {
                var playStoreAppVersion: String? = null
                var inReader: BufferedReader? = null
                var uc: URLConnection? = null
                val doc: DocumentsContract.Document? = null
                val currentVersion_PatternSeq = "<div[^>]*?>Current\\sVersion</div><span[^>]*?>(.*?)><div[^>]*?>(.*?)><span[^>]*?>(.*?)</span>"
                val appVersion_PatternSeq = "htlgb\">([^<]*)</s"
                val urlData = StringBuilder()
                //Play Store - URL
                val urlOfAppFromPlayStore = "https://play.google.com/store/apps/details?id=dk.eatmore.asbendospizza"

                val url = URL("https://play.google.com/store/apps/details?id=dk.eatmore.rns")//+ this@SplashActivity.getPackageName())
                uc = url.openConnection()
                if (uc == null) {
                    return null
                }
                uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                inReader = BufferedReader(InputStreamReader(uc.getInputStream()))
                if (null != inReader) {
                    var str = ""
                    while ((inReader.readLine()) != null) {
                        str = inReader.readLine()
                        urlData.append(str)
                    }
                }
                // Get the current version pattern sequence
                val versionString = getAppVersion(currentVersion_PatternSeq, urlData.toString())
                if (null == versionString) {
                    return null
                } else {
                    // get version from "htlgb">X.X.X</span>
                    playStoreAppVersion = getAppVersion(appVersion_PatternSeq, versionString)
                    latestVersionName = playStoreAppVersion
                    Log.e(TAG, "latestVersionCode : $latestVersionName")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "error from play store: " + e.message)
            }

            return latestVersionName

        }

        private fun getAppVersion(patternString: String, inputString: String): String? {
            try {
                //Create a pattern
                val pattern = Pattern.compile(patternString) ?: return null

                //Match the pattern string in provided string
                val matcher = pattern.matcher(inputString)
                if (null != matcher && matcher.find()) {
                    return matcher.group(1)
                }

            } catch (ex: PatternSyntaxException) {

                ex.printStackTrace()
            }

            return null
        }


        override fun onPostExecute(version: String) {
            super.onPostExecute(version)

            try {

                if (currentVersionName != null && latestVersionName != null) {

                    if (checkForUpdate(currentVersionName!!, latestVersionName!!))
                        versionUpdate(latestVersionName!!)
                    else {
                        moveToLogin()
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "on Exception... : ${e.message} ")

            }


        }


        fun checkForUpdate(existingVersion: String, newVersion: String): Boolean {
            if (TextUtils.isEmpty(existingVersion) || TextUtils.isEmpty(newVersion)) {
                return false
            }

            if (existingVersion.equals(newVersion, ignoreCase = true)) {
                return false
            }

            var newVersionIsGreater = false
            val existingVersionArray = existingVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val newVersionArray = newVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val maxIndex = Math.max(existingVersionArray.size, newVersionArray.size)
            for (i in 0 until maxIndex) {
                var newValue: Int
                var excValue: Int
                try {
                    excValue = Integer.parseInt(existingVersionArray[i])
                } catch (e: ArrayIndexOutOfBoundsException) {
                    excValue = 0
                }

                try {
                    newValue = Integer.parseInt(newVersionArray[i])
                } catch (e: ArrayIndexOutOfBoundsException) {
                    newValue = 0
                }
                Log.e(TAG, "checkForUpdate - old values is - $excValue and new values is $newValue")

                if (excValue < newValue) {
                    newVersionIsGreater = true
                    continue
                }
            }
            return newVersionIsGreater
        }


    }

    fun versionUpdate(versionName: String) {


        DialogUtils.openDialog(this, "${getString(R.string.app_name)} $versionName", getString(R.string.new_update),
                getString(R.string.go_on_playstore), getString(R.string.dismiss), ContextCompat.getColor(this, R.color.theme_color), object : DialogUtils.OnDialogClickListener {
            override fun onPositiveButtonClick(position: Int) {
                //  startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlOfAppFromPlayStore)))
                moveToLogin()
            }

            override fun onNegativeButtonClick() {
                moveToLogin()

            }
        })

    }

    private fun moveToLogin() {

        if(PreferenceUtil.getBoolean(PreferenceUtil.CLOSE_INTRO_SLIDE, false) == true){
            // move -> Home Activity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else{
            // move -> Intro slide
            startActivity(Intent(this, Intro_slider::class.java))
            finish()
        }

   /*     if (PreferenceUtil.getString(PreferenceUtil.USER_NAME, "") == "") {
             startActivity(Intent(this, HomeActivity::class.java))
             finish()
        } else {
             startActivity(Intent(this, HomeActivity::class.java))
            finish()

        }*/
    }


    private fun showComponents(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.e("run","success---")
            val constraintSet = ConstraintSet()
            constraintSet.clone(this, R.layout.splash_activity_overlay)

            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(2.0f)
            transition.duration = 1200
        //    transition.setInterpolator(new FastOutSlowInInterpolator());


            TransitionManager.beginDelayedTransition(constraint,transition)
            constraintSet.applyTo(constraint) //here constraint is the name of view to which we are applying the constraintSet
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }


}