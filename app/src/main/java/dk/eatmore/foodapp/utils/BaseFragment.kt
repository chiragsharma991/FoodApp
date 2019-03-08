package dk.eatmore.foodapp.utils

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
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
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ParseException
import android.os.Build
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.ProgressBar
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dk.eatmore.foodapp.BuildConfig
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.RestaurantClosed
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.Profile
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.Dashboard.Account.Signup
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.CategoryList
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.rest.ApiClient
import dk.eatmore.foodapp.rest.ApiInterface
import dk.eatmore.foodapp.storage.PreferenceUtil
import kotlinx.android.synthetic.main.category_list.*
import kotlinx.android.synthetic.main.fragment_home_container.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import org.json.JSONObject
import java.util.regex.Pattern


abstract class BaseFragment : Fragment() {

    abstract fun getLayout(): Int
    abstract fun initView(view: View?, savedInstanceState: Bundle?)
    lateinit var displayMetrics: DisplayMetrics
    private  var dialog: ProgressDialog? = null
    private var backpress_timeout : Boolean = false
    private val timeoutHandler = Handler()
    private var finalizer: Runnable? = null





    override fun onAttach(context: Context?) {
        super.onAttach(context)
        displayMetrics = DisplayMetrics()
        getActivityBase().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun showTabBar(show : Boolean){
        if(show) ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).bottom_menu.visibility =View.VISIBLE
        else ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).bottom_menu.visibility =View.GONE
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
    }

    fun addFragment(container: Int, fragment: Fragment, tag: String, isAnimated: Boolean) {
        // fragment tag is for get fragment from container but backstack tag is for remove fragment from backstack queue.
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

    fun is_callphn_PermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            //phone permission
            if (context!!.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                //ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CALL_PHONE), 1)
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 0)
                return false
            }

            } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }
    fun is_location_PermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            // location permission
            if (context!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    context!!.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true

            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
                return false
            }

        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }

    }

    fun getpostalfrom_latlang(latitude : Double ,longitude :Double ) : String{

        val gcd = Geocoder(context!!)
        val addresses  :List<Address> = gcd.getFromLocation(latitude,longitude,1)
        for (address : Address in  addresses) {
            if(address.getLocality()!=null && address.getPostalCode()!=null){
               // Log.e("",address.getLocality())
                Log.e("getpostalfrom_latlang",address.getPostalCode())
                return address.getPostalCode()
            }
        }
        return ""
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
                // loge("TAG","scroll---"+scrollRange+"+"+verticalOffset)
                 if ((scrollRange + verticalOffset == 0) && isShow) {
                     val alpha = AlphaAnimation(1.0f,0.0f)
                     alpha.duration=500
                     val bundle=arguments
                     appcompattextview.text=title
                     // appcompattextview.startAnimation(alpha)
                    // appcompattextview.visibility=View.VISIBLE
                     loge("TAG","range == 0"+title)
                     isShow = false

                 } else if (!isShow) {
                     if(scrollRange + verticalOffset > scrollRange-50){
                         appcompattextview.text=""
                         //   appcompattextview.visibility=View.GONE
                         loge("TAG","range not 0")
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
                        is DetailsFragment ->{
                            fragment.onBackpress()
                        }
                        is RestaurantList ->{
                            fragment.onBackpress()
                        }
                        is Profile -> {
                            if (!fragment.backpress()) childFragmentManager.popBackStack()
                        }
                        is OrderedRestaurant -> {
                             fragment.backpress()
                        }
                        is EpayFragment ->{
                            if(!fragment.backpress()) childFragmentManager.popBackStack()
                        }
                        else -> childFragmentManager.popBackStack()


                    }
                 //   DrawableCompat.setTint(ContextCompat.getDrawable(activity!!, R.drawable.close)!!, ContextCompat.getColor(activity!!, R.color.white));

                }
            }else{
                loge("Backpress", "--Move on finish--")
                val fragment=((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getContainerFragment()
                when(fragment){

                    is OrderFragment ->{
                        ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(0,0)
                    }
                    is AccountFragment ->{
                        fragment.onBackpress()
                    }
                    is HomeFragment ->{
                        if(backpress_timeout){
                            timeoutHandler.removeCallbacks(finalizer)
                            (activity as HomeActivity).finish()
                        }else{
                            backpress_timeout=true
                            Toast.makeText(context,"Press again to exit",Toast.LENGTH_SHORT).show()
                            finalizer = object : Runnable {
                                override fun run() {
                                    backpress_timeout=false
                                }
                            }
                            timeoutHandler.postDelayed(finalizer, 2 * 1000)
                        }



                    }

                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isPop
    }

    fun popAllFragment() {

              for (i in 0 until childFragmentManager.backStackEntryCount) {
                  childFragmentManager.popBackStack()
              }
    }

    fun popWithTag(tag: String) {
        childFragmentManager.popBackStack(tag, 0)
    }

    fun pop() {
        childFragmentManager.popBackStack()
    }





    inline fun <reified T> popfrom_to( targetfragment: T){

//*TODO : Please be make sure if you have this base + A fragment + B fragment > and you call this from B, you get= activity null because you are destroying intance B itself.

        loop@ for(i in 0 until(childFragmentManager.backStackEntryCount) ){
            childFragmentManager.executePendingTransactions()
            val fragment = childFragmentManager.findFragmentByTag(childFragmentManager.getBackStackEntryAt(childFragmentManager.backStackEntryCount-1 ).name)
            if (fragment != null && fragment.isVisible) {

              //  targetfragment is DetailsFragment
                when (fragment) {
                    is  DetailsFragment-> {
                        break@loop
                    } else->{
                    childFragmentManager.popBackStack()
                }
                }
            }
        }
    }


    fun any_preorder_closedRestaurant(is_restaurant_closed : Boolean?, pre_order : Boolean?,msg : String?) : Boolean{

        if((is_restaurant_closed !=null && is_restaurant_closed == true) &&
                (pre_order !=null && pre_order == false) ){
            // Test if restaurant is closed.
            val status= msg?:getString(R.string.sorry_restaurant_has_been_closed)
            DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.black),msg = status,title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                override fun onPositiveButtonClick(position: Int) {

                    /*TODO: this condition is happen when you are comming from reorder/Home before epayfragment*/
                    if(parentFragment is HomeFragment){
                        DetailsFragment.canIrefreshpre_Function=true
                        val homeFragment =(parentFragment as HomeFragment)
                        homeFragment.popfrom_to(DetailsFragment)
                        val detailsFragment=homeFragment.childFragmentManager.findFragmentByTag(DetailsFragment.TAG)
                        if(detailsFragment !=null)
                        (detailsFragment as DetailsFragment).fetch_category_menu()

                    }else if(parentFragment is OrderFragment){
                        showTabBar(true)
                        val orderFragment =(parentFragment as OrderFragment)
                        orderFragment.popAllFragment()
                    }

                    /*TODO: this condition is happen when you are comming from reorder 2 tab after epayfragment*/
                    else if(parentFragment is EpayFragment){
                        DetailsFragment.canIrefreshpre_Function=true
                        val epayFragment= parentFragment as EpayFragment
                        if(epayFragment.parentFragment is HomeFragment){
                            val homeFragment = epayFragment.parentFragment as HomeFragment
                            homeFragment.popfrom_to(DetailsFragment)
                            val detailsFragment=homeFragment.childFragmentManager.findFragmentByTag(DetailsFragment.TAG)
                            if(detailsFragment !=null)
                            (detailsFragment as DetailsFragment).fetch_category_menu()
                        }else{
                            showTabBar(true)
                            val orderFragment =epayFragment.parentFragment as OrderFragment
                            orderFragment.popAllFragment()
                        }
                    }
                }
                override fun onNegativeButtonClick() {
                }
            })
            return true
        }else{
            return false
        }

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

    fun validMail(email: String): Boolean {

        //  val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val EMAIL_PATTERN = "^[ÆØÅæøåA-Za-z0-9._%+-]+@(?:[ÆØÅæøåA-Za-z0-9-]+\\.)+[A-Za-z]{2,6}\$"
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()

    }



    fun isGpsEnable() : Boolean {
        val  manager = context!!.getSystemService(Context.LOCATION_SERVICE ) as LocationManager
        val statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return statusOfGPS
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
    fun clearProgressDialog() {
        if(dialog !=null){
            dialog!!.dismiss()
            dialog=null
        }
    }

    fun getDefaultApiParms() : JsonObject{
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.DEVICE_TYPE, Constants.DEVICE_TYPE_VALUE)
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        return postParam
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
        hideKeyboard()
        if (isInternetAvailable()) {
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    try {
                        if (response.isSuccessful) {
                            loge("response.body----",response.body().toString())
                            val gson=Gson()
                            val json=gson.toJson(response.body()) // convert body to normal json
                            var convertedObject = gson.fromJson(json, JsonObject::class.java) // convert into Jsonobject
                            loge("response.convertedObject----",convertedObject.toString())

                            if(convertedObject.has(Constants.WHOLE_SYSTEM)){
                                if(convertedObject.get(Constants.WHOLE_SYSTEM).isJsonNull){
                                    onAliCallInteraction.onSuccess(response.body())
                                }else{
                                    if((convertedObject.get(Constants.WHOLE_SYSTEM).asBoolean== true) || (convertedObject.get(Constants.RESTAURANT_FOOD_ANDROID).asBoolean== true) ){
                                        onAliCallInteraction.onFail(0)
                                        val intent = Intent(activity, RestaurantClosed::class.java)
                                        val bundle = Bundle()
                                        bundle.putString(Constants.MESSAGE_TITLE,convertedObject.get(Constants.MESSAGE_TITLE).asString)
                                        bundle.putString(Constants.MESSAGE_DETAILS,convertedObject.get(Constants.MESSAGE_DETAILS).asString)
                                        intent.putExtras(bundle)
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                    }else{
                                        onAliCallInteraction.onSuccess(response.body())
                                    }
                                }
                            }else{
                                   onAliCallInteraction.onSuccess(response.body())

                            }


                        } else {
                            // var mErrorBody: String = response.errorBody()!!.string()
                            onAliCallInteraction.onFail(404)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    loge("onFailure--##---", "" + t.message)
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
        val s = SimpleDateFormat(dateFormat,Locale.ENGLISH)
        cal.add(Calendar.DAY_OF_YEAR, days)
        return s.format(Date(cal.timeInMillis))
    }

    fun gettimefrom_date(target_date : String, target_format : String): Long {
        //val sdf = SimpleDateFormat("dd/MM/yyyy")
        val sdf = SimpleDateFormat(target_format,Locale.ENGLISH)
        val strDate = sdf.parse(target_date)
        return strDate.time
    }
    fun getcurrentdate(): String {
        //val sdf = SimpleDateFormat("dd/MM/yyyy")
        val time =Calendar.getInstance().time
        val df =SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val currentdate : String =df.format(time)
        return currentdate
    }




    // Transition

    fun translateAnim(from_x: Float, to_x: Float, from_y: Float, to_y: Float, duration: Long, fill: Boolean): TranslateAnimation {
        val animation = TranslateAnimation(from_x, to_x, from_y, to_y)
        animation.duration = duration
        animation.fillAfter = fill
        return animation
    }

    fun progresswheel(progresswheel : ProgressWheel , isvisible : Boolean){
        when (isvisible){
            true ->{
                progresswheel.startSpinning()
                progresswheel.visibility=View.VISIBLE
            }
            false->{

                progresswheel.stopSpinning()
                progresswheel.visibility=View.GONE
            }

        }


    }


}