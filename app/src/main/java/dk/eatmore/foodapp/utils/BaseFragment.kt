package dk.eatmore.foodapp.utils

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.AppCompatTextView
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.ProgressBar
import dk.eatmore.foodapp.BuildConfig
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.Profile
import dk.eatmore.foodapp.fragment.ProductInfo.CategoryList
import dk.eatmore.foodapp.rest.ApiClient
import dk.eatmore.foodapp.rest.ApiInterface
import kotlinx.android.synthetic.main.category_list.*
import kotlinx.android.synthetic.main.toolbar_plusone.*


abstract class BaseFragment : Fragment() {

    abstract fun getLayout(): Int
    abstract fun initView(view: View?, savedInstanceState: Bundle?)
    lateinit var displayMetrics: DisplayMetrics
    private  var dialog: ProgressDialog? = null



    override fun onAttach(context: Context?) {
        super.onAttach(context)
        displayMetrics = DisplayMetrics()
        getActivityBase().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
    }

    fun addFragment(container: Int, fragment: Fragment, tag: String, isAnimated: Boolean) {
        hideKeyboard()
        val mfragmentTransaction = childFragmentManager.beginTransaction()
        if (isAnimated)
            mfragmentTransaction.setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_from_left)
        mfragmentTransaction.add(container, fragment, tag).addToBackStack(tag).commit()
    }

    fun rightToLeftAnimation(context:Context): Animation {
        val animation = AnimationUtils.loadAnimation(context, R.anim.enter_from_right)
        animation.duration = 250
        return animation
    }

    fun isPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context!!.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                //ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CALL_PHONE), 1)
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1)
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }


     fun setanim_toolbartitle(appbar :AppBarLayout , appcompattextview : AppCompatTextView ,title : String) {

         val appbar=appbar
         val appcompattextview= appcompattextview
         val title = title

         appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
             internal var isShow = true
             internal var scrollRange = -1

             override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                 if (scrollRange == -1) {
                     scrollRange = appBarLayout.totalScrollRange
                 }
                 if ((scrollRange + verticalOffset == 0) && isShow) {
                     val alpha = AlphaAnimation(1.0f,0.0f)
                     alpha.duration=500
                     val bundle=arguments
                     appcompattextview.text=title
                     // appcompattextview.startAnimation(alpha)
                     appcompattextview.visibility=View.VISIBLE
                     isShow = false

                 } else if (!isShow) {
                     if(scrollRange + verticalOffset > scrollRange-50){
                         appcompattextview.visibility=View.GONE
                         isShow = true
                     }
                 }
             }
         })
    }


    fun popFragment(): Boolean {
        var isPop = false
        try {
            if (childFragmentManager.backStackEntryCount > 0) {
                /**
                 * Check Filter Fragment Appear or not, Filter Type Fragment Also
                 */
                hideKeyboard()
                loge("backStackCount", childFragmentManager.backStackEntryCount.toString())
                val fragment = childFragmentManager.findFragmentByTag(childFragmentManager.getBackStackEntryAt(childFragmentManager.backStackEntryCount - 1).name)
                if (fragment != null && fragment.isVisible) {
                    isPop = true
                    when (fragment) {

                        is CategoryList -> {
                            fragment.backpress()
                            childFragmentManager.popBackStack()
                        }
                        is Profile -> {
                            if (!fragment.backpress()) childFragmentManager.popBackStack()
                        }
                        else -> childFragmentManager.popBackStack()


                    }
                 //   DrawableCompat.setTint(ContextCompat.getDrawable(activity!!, R.drawable.close)!!, ContextCompat.getColor(activity!!, R.color.white));

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isPop
    }

    fun popAllFragment() {


        /*      for (i in 0 until childFragmentManager.backStackEntryCount) {
                  childFragmentManager.popBackStack()
              }*/
    }

    fun popWithTag(tag: String) {
        childFragmentManager.popBackStack(tag, 0)
    }

    fun pop() {
        childFragmentManager.popBackStack()
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    fun seterror(view : View){
        if(!isInternetAvailable()){
            showSnackBar(view,getString(R.string.internet_not_available))
        }else{
            showSnackBar(view,getString(R.string.error_404))


        }
    }


    fun showSnackBar(view: View, string: String) {
        Snackbar.make(view, string, Snackbar.LENGTH_SHORT).show()
    }



    fun showProgressDialog() {
        if(dialog ==null){
            dialog = ProgressDialog(activity);
            dialog!!.setMessage(getString(R.string.please_wait))
            dialog!!.setCancelable(false)
            dialog!!.setCanceledOnTouchOutside(false)
            dialog!!.show()
        }else{
            dialog!!.dismiss()
            dialog=null
        }


    }


    fun getActivityBase(): Activity {
        return activity!!
    }

    fun loge(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.e(tag, msg)
    }

    fun logd(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg)
    }

    /**
     * API CAll START
     */

    fun getApiInterface(): ApiInterface {
        return ApiClient.getClient()!!.create(ApiInterface::class.java)
    }

    fun <T> callAPI(call: Call<T>, onAliCallInteraction: OnApiCallInteraction) {
        if (isInternetAvailable()) {
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    try {
                        if (response.isSuccessful) {
                            onAliCallInteraction.onSuccess(response.body())
                        } else {
                            // var mErrorBody: String = response.errorBody()!!.string()
                            onAliCallInteraction.onFail(404)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    loge("Base", "" + t.message)
                    onAliCallInteraction.onFail(100)
                }
            })
        } else {
            onAliCallInteraction.onFail(100)
            // showSnackBar(getString(R.string.internet_not_available))
        }
    }

    interface OnApiCallInteraction {
        //  100 > network not found  : 404 > server error.
        fun <T> onSuccess(body: T?)

        fun onFail(error: Int)
    }

    /**
     * API Call END
     */

    fun hideKeyboard() {
        if (view != null) {
            val imm = getActivityBase().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);


        }
    }

    fun showKeyboard() {
        if (view != null) {
            val imm = getActivityBase().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //  imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


        }
    }

    fun getHeight(): Int {
        return displayMetrics.heightPixels
    }

    fun getWidth(): Int {
        return displayMetrics.widthPixels
    }


    fun getCalculatedDate(dateFormat: String, days: Int): String {
        val cal = Calendar.getInstance()
        val s = SimpleDateFormat(dateFormat)
        cal.add(Calendar.DAY_OF_YEAR, days)
        return s.format(Date(cal.timeInMillis))
    }


    // Transition

    fun translateAnim(from_x: Float, to_x: Float, from_y: Float, to_y: Float, duration: Long, fill: Boolean): TranslateAnimation {
        val animation = TranslateAnimation(from_x, to_x, from_y, to_y)
        animation.duration = duration
        animation.fillAfter = fill
        return animation
    }


}