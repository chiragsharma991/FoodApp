package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_signup.*

class Profile : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private var profileEdit_fragment: ProfileEdit? = null
    private var coupan_fragment: Coupan? = null


    companion object {

        val TAG = "Profile"
        fun newInstance(): Profile {
            return Profile()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_profile
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")

            profile_info_txt.setOnClickListener {
                profileEdit_fragment = ProfileEdit.newInstance()
                addFragment(R.id.profile_container, profileEdit_fragment!!, ProfileEdit.TAG, true)
            }
            gift_card_txt.setOnClickListener {
                coupan_fragment = Coupan.newInstance()
                addFragment(R.id.profile_container, coupan_fragment!!, Coupan.TAG, true)
            }

        } else {
            logd(TAG, "saveInstance NOT NULL")

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

    fun backpress(): Boolean {
        if(profileEdit_fragment !=null && profileEdit_fragment!!.isVisible){
            childFragmentManager.popBackStack()
            return true
        }else if(coupan_fragment !=null && coupan_fragment!!.isVisible){
            childFragmentManager.popBackStack()
            return true
        } else {
            return false
        }
    }
}
