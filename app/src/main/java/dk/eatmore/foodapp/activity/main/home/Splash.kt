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
import android.os.Build
import android.support.constraint.ConstraintSet
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.view.View
import android.view.animation.*
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.TextDelegate
import com.airbnb.lottie.model.KeyPath
import com.facebook.shimmer.ShimmerFrameLayout
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
        fullScreen()
        getCurrentVersion()
        screenPlay()

        //eatmore_logo_img.startAnimation(alphaAnimation)


        //val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        //img_logo.startAnimation(animation)


    }

    private fun screenPlay() {
        Handler().postDelayed({

            showComponents()

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
                         moveToLogin()
                },6000)

            },100)

        },800)


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
                getString(R.string.go_on_playstore), getString(R.string.dismiss), ContextCompat.getColor(this, R.color.black), object : DialogUtils.OnDialogClickListener {
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

        if (PreferenceUtil.getString(PreferenceUtil.USER_NAME, "") == "") {
             startActivity(Intent(this, HomeActivity::class.java))
             finish()
        } else {
             startActivity(Intent(this, HomeActivity::class.java))
            finish()

        }
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