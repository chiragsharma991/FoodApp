package dk.eatmore.foodapp.activity.main.home

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.utils.BaseActivity
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.facebook.AccessToken
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.OpeningHours
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.menu_restaurant.*
import org.json.JSONException
import java.util.*


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
        //   log(TAG, "savedInstanceState..."+savedInstanceState)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge(TAG, "---Activity Result Activity---")
        AccountFragment.callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun initView(savedInstanceState: Bundle?) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        mHomeContainerFragment = HomeContainerFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.home_container, mHomeContainerFragment, HomeContainerFragment.TAG).commit()

    }

    fun getHomeContainerFragment(): Fragment {

        return mHomeContainerFragment

    }

    override fun onResume() {
        super.onResume()
        if(Splash.can_i_use_lastLogin  && PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
            loge(TAG,"Last login Api calling... ")
            callAPI(ApiCall.lastLogin(
                    r_token = Constants.R_TOKEN,
                    r_key = Constants.R_KEY,
                    device_type = "Android",
                    customer_id = "1713"
            ), object : BaseFragment.OnApiCallInteraction {

                override fun <T> onSuccess(body: T?) {
                    loge(TAG,"Last login success ")
                    Splash.can_i_use_lastLogin=false

                }
                override fun onFail(error: Int) {
                    loge(TAG,"Last login error "+error)
                }
            })
        }
    }

    override fun onBackPressed() {
        loge(TAG, "back pressed...")
        var pop = mHomeContainerFragment.getContainerFragment().popFragment()

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