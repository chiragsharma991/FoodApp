package dk.eatmore.foodapp.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentActivityBinding
import dk.eatmore.foodapp.fragment.FindRestaurant
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.utils.BaseActivity

class HomeActivity : BaseActivity(){

    private lateinit var mHomeContainerFragment: HomeContainerFragment


    companion object {
        val TAG="HomeActivity"
        fun newInstance() : FragmentActivity{
            return FragmentActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        initView(savedInstanceState)
        //   log(TAG, "savedInstanceState..."+savedInstanceState)

    }


    private fun initView(savedInstanceState: Bundle?) {
        //fullScreen()
        if(savedInstanceState==null){
            mHomeContainerFragment = HomeContainerFragment.newInstance()
            supportFragmentManager.beginTransaction().replace(R.id.home_container, mHomeContainerFragment, "").commit()
        }


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