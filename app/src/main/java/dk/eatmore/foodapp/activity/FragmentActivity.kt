package dk.eatmore.foodapp.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseActivity
import dk.eatmore.foodapp.databinding.FragmentActivityBinding
import dk.eatmore.foodapp.fragment.FindRestaurant

class FragmentActivity :BaseActivity(){

    private lateinit var binding: FragmentActivityBinding
    private val findRestaurant= FindRestaurant.newInstance()

    companion object {
        val TAG="FragmentActivity"
        fun newInstance() : FragmentActivity{
            return FragmentActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.fragment_activity)
        initView(savedInstanceState)
     //   log(TAG, "savedInstanceState..."+savedInstanceState)

    }

    private fun initView(savedInstanceState: Bundle?) {
        fullScreen()
        if(savedInstanceState==null)
        supportFragmentManager.beginTransaction().replace(R.id.container_view, findRestaurant, FindRestaurant.TAG).addToBackStack(FindRestaurant.TAG).commit()


    }

    override fun onDestroy() {
        super.onDestroy()
        log(TAG,"on destroy...")
    }

    override fun onPause() {
        super.onPause()
        log(TAG,"on pause...")

    }


}