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
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment


class HomeActivity : BaseActivity(){

    private lateinit var mHomeContainerFragment: HomeContainerFragment


    companion object {
        val TAG="HomeActivity"
        fun newInstance() : HomeActivity {
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
        loge(TAG,"---Activity Result Activity---")

        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun initView(savedInstanceState: Bundle?) {
       // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        fullScreen()
      //  changeStatusBarColor(R.color.white)
        if(savedInstanceState==null){
            // if you not take in this condition than if you change orientation then fragment added again and again.
        }
        else{
        }









        mHomeContainerFragment = HomeContainerFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.home_container, mHomeContainerFragment,HomeContainerFragment.TAG).commit()

    }

    fun getFragmentFromAdapter() :Fragment{

        return  mHomeContainerFragment

    }

    override fun onBackPressed() {
        var pop = mHomeContainerFragment.getContainerFragment().popFragment()

    }





    override fun onDestroy() {
        super.onDestroy()
        logd(TAG,"on destroy...")
    }

    override fun onPause() {
        super.onPause()
        logd(TAG,"on pause...")

    }



}