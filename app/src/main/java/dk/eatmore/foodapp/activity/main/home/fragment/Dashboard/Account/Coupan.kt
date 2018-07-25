package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentCoupanBinding
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentProfileEditBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_signup.*

class Coupan : BaseFragment() {

    private lateinit var binding: FragmentCoupanBinding



    companion object {

        val TAG = "Coupan"
        fun newInstance(): Coupan {
            return Coupan()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_coupan
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
