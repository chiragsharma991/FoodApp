package dk.eatmore.foodapp.fragment.Dashboard

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.toolbar.*

class AccountFragment : BaseFragment() {

    private lateinit var binding: FragmentAccountContainerBinding



    companion object {

        val TAG = "AccountFragment"
        fun newInstance(): AccountFragment {
            return AccountFragment()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_account_container
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
        txt_toolbar.text=getString(R.string.my_profile)


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



