package dk.eatmore.foodapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import dk.eatmore.foodapp.BuildConfig
import dk.eatmore.foodapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


abstract class BaseActivity : AppCompatActivity()
{
    // protected abstract fun getLayout(): Int

    // protected abstract fun init(savedInstancedState: Bundle?)

/*    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())
        init(savedInstanceState)


    }*/

    fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    fun changeStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun loge(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg)
    }

    fun logd(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg)
    }

    fun fullScreen() {
        if (Build.VERSION.SDK_INT >= 21) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }


    fun rightToLeftAnimation(context:Context): Animation {
        val animation = AnimationUtils.loadAnimation(context, R.anim.enter_from_right)
        animation.duration = 250
        return animation
    }

    fun isPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) === PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    fun <T>  callAPI(call: Call<T>, onAliCallInteraction: BaseFragment.OnApiCallInteraction) {
        if (isInternetAvailable()) {
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    try {
                        if (response.isSuccessful) {
                            onAliCallInteraction.onSuccess(response.body())
                        } else {
                           // var mErrorBody: String = response.errorBody()!!.string()
                        }
                    } catch (e: Exception) {
                        loge("error of catch ",e.toString())
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    loge("on failure","-----"+t.message)
                    onAliCallInteraction.onFail(404)
                }
            })
        } else {
            onAliCallInteraction.onFail(100)
            //  showSnackBar(view, getString(R.string.internet_not_available))
        }
    }

    fun showSnackBar(view: View, string: String) {
        Snackbar.make(view, string, Snackbar.LENGTH_SHORT).show()
    }

    fun hideKeyboard() {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    fun showToast(string: String){
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }

    fun popFragment(): Boolean {
        var isPop = false
        try {
            if (supportFragmentManager.backStackEntryCount > 0) {
                hideKeyboard()
                isPop = true
                supportFragmentManager.popBackStack()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isPop
    }

    fun popAllFragment() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
    }
    fun popWithTag(tag : String) {
        supportFragmentManager.popBackStack(tag,0)



    }
    fun pop() {
        supportFragmentManager.popBackStack()
    }

    /**
     * Check fragment added or not
     */
    fun isAddedFragment(tag: String): Boolean {
        var frag = supportFragmentManager.findFragmentByTag(tag)
        return frag != null && frag.isVisible && frag.isVisible
    }

    /**
     * @param tag which fragment you need to visible
     */
    fun showFragment(tag: String) {
        hideKeyboard()
        supportFragmentManager.popBackStack(tag, 0)
    }

    /**
     * @param container is the id of adding fragment
     * @param fragment -> your fragment
     * @param tag Name of tag any
     */
    fun addFragment(container: Int, fragment: Fragment, tag: String, isAnimation :Boolean) {
        hideKeyboard()
        addFragment(container, fragment, tag, isAnimation, true)
    }

    /**
     * @param container is the id of adding fragment
     * @param fragment -> your fragment
     * @param tag Name of tag any
     * @param isAnimation for support animation
     */
    fun addFragment(container: Int, fragment: Fragment, tag: String, isAnimation: Boolean, isAddToBackStack: Boolean) {
        hideKeyboard()

        var mFragTransaction = supportFragmentManager.beginTransaction()
        if (isAnimation)
            mFragTransaction.setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_from_left)
        mFragTransaction.add(container, fragment, tag)
        if (isAddToBackStack)
            mFragTransaction.addToBackStack(tag)
        mFragTransaction.commit()
    }

    // Transition

    fun translateAnim(from_x :Float ,to_x : Float, from_y : Float , to_y : Float , duration : Long, fill :Boolean) : TranslateAnimation {
        val animation = TranslateAnimation(from_x, to_x, from_y, to_y)
        animation.duration = duration
        animation.fillAfter = fill
        return  animation
    }


}