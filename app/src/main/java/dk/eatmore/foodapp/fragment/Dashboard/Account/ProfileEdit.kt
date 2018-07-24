package dk.eatmore.foodapp.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentProfileEditBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_profile_edit.*
import kotlinx.android.synthetic.main.fragment_signup.*

class ProfileEdit : BaseFragment() {

    private lateinit var binding: FragmentProfileEditBinding



    companion object {

        val TAG = "ProfileEdit"
        fun newInstance(): ProfileEdit {
            return ProfileEdit()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_profile_edit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            profile_save_txt.setOnClickListener{

            //    parentFragment!!.childFragmentManager.popBackStack()

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
