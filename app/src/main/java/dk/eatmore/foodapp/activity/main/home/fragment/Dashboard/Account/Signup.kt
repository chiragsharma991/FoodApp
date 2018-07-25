package dk.eatmore.foodapp.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_signup.*

class Signup : BaseFragment() {

    private lateinit var binding: FragmentSignupBinding



    companion object {

        val TAG = "Signup"
        var ID :Int=1
        fun newInstance(): Signup {
            return Signup()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_signup
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            when (ID){
                1 ->{
                    signup_view.visibility=View.VISIBLE
                    forget_password_view.visibility=View.GONE
                }
                2->{
                    signup_view.visibility=View.GONE
                    forget_password_view.visibility=View.VISIBLE
                }
            }


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
