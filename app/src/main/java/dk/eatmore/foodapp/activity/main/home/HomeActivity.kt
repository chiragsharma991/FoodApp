package dk.eatmore.foodapp.activity.main.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.facebook.AccessToken
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.OpeningHours
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.ProductInfo.CategoryList
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import org.json.JSONException
import java.util.*
import android.os.Build
import dk.eatmore.foodapp.utils.*
import org.greenrobot.eventbus.Subscribe


class HomeActivity : BaseActivity() {

    private lateinit var mHomeContainerFragment: HomeContainerFragment


    companion object {
        val TAG = "HomeActivity"

        fun newInstance(): HomeActivity {
            return HomeActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        initView(savedInstanceState)

     //   val parsingEvents =ParsingEvents.ActivityFragmentMessage ("chirag ActivityFragmentMessage")
      //  GlobalBus.bus.post(parsingEvents)
        //   log(TAG, "savedInstanceState..."+savedInstanceState)

    }

 /*   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge(TAG, "---Activity Result home Activity---")
        AccountFragment.callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
*/

    private fun initView(savedInstanceState: Bundle?) {
      //  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
       // fullScreen()
        setLightStatusBar(this)
        mHomeContainerFragment = HomeContainerFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.home_container, mHomeContainerFragment, HomeContainerFragment.TAG).addToBackStack(HomeContainerFragment.TAG).commit()

    }

    fun getHomeContainerFragment(): Fragment {

        return mHomeContainerFragment

    }

    fun setLightStatusBar( activity: Activity) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            var flags = activity.getWindow().getDecorView().systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.getWindow().getDecorView().systemUiVisibility = flags
            activity.window.statusBarColor = Color.WHITE
        }
    }

    override fun onResume() {
        super.onResume()
        // Responce are comming as a empty data so you always get response from error method.
        if(Splash.can_i_use_lastLogin  && PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
            loge(TAG,"Last login Api calling... ")
            callAPI(ApiCall.lastLogin(
                    customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!,
                    device_type = Constants.DEVICE_TYPE_VALUE,
                    eatmore_app = true,
                    auth_key = Constants.AUTH_VALUE
            ), object : BaseFragment.OnApiCallInteraction {

                override fun <T> onSuccess(body: T?) {
                    loge(TAG,"Last login success -----")
                    Splash.can_i_use_lastLogin=false

                }
                override fun onFail(error: Int) {
                    loge(TAG,"Last login error---- "+error)
                    Splash.can_i_use_lastLogin=false

                }
            })
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // this is result from enable gps or disable gps
        if(data != null){
            loge(TAG,"onActivityResult---"+resultCode+"||"+requestCode+"||"+data)
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == Constants.REQUEST_CHECK_SETTINGS) {
                if (resultCode == Activity.RESULT_OK) {
                    (getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().getcurrent_location()

                }else{
                    (getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().gpsPermissiondenied()
                }
            }
        }
    }//1||64206||Intent { (has extras) }   &&  ---Activity Result account fragment---64206

    override fun onBackPressed() {
        loge(TAG, "back pressed...")
        mHomeContainerFragment.getContainerFragment().popFragment()

        /*     val fragment = supportFragmentManager.findFragmentById(R.id.home_container)
             when(fragment){
                 is HomeContainerFragment ->{
                     loge(TAG,"Home container fragment---")
                      mHomeContainerFragment.getContainerFragment().popFragment()
                 }

                 else ->{
                     loge(TAG,"Detail container fragment---")
                     supportFragmentManager.popBackStack()
                 }

             }*/
    }

    fun fragmentTab_is() : Int{

        // check which screen is open?
        var result : Int = 0
        when(mHomeContainerFragment.getContainerFragment()){
            is HomeFragment ->{
                result= 0
            }
            is OrderFragment ->{
                result= 1
            }
            is AccountFragment ->{
                result= 2
            }
        }
        return result
    }

  /*  @Subscribe
    fun  getMesage(parsingevents : ParsingEvents.FragmentActivityMessage){
        loge(TAG,"--get FragmentActivityMessage-"+parsingevents.message)
    }*/

    override fun onStart() {
        loge(TAG, "onStart...")
        super.onStart()
        //GlobalBus.bus.register(this)
    }

    override fun onStop() {
        loge(TAG, "onStop...")
        super.onStop()
        //GlobalBus.bus.unregister(this)
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