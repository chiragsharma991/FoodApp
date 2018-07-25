package dk.eatmore.foodapp.fragment.Dashboard.Order

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentOrderedRestaurantBinding
import dk.eatmore.foodapp.utils.BaseFragment

class OrderedRestaurant : BaseFragment() {

    private lateinit var binding: FragmentOrderedRestaurantBinding



    companion object {

        val TAG = "OrderedRestaurant"
        fun newInstance(): OrderedRestaurant {
            return OrderedRestaurant()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_ordered_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }




    }


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

}
