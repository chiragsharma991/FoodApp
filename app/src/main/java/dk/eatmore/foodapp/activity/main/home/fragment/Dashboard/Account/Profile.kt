package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_signup.*

class Profile : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private var profileEdit_fragment: ProfileEdit? = null
    private var coupan_fragment: Coupan? = null
    private lateinit var ui_model: UIModel


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
            ui_model = createViewModel()
            ui_model.init()


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


/*    ui_model = ViewModelProviders.of(this).get(UIModel::class.java)
    ui_model!!.getUIModel().observe(this, Observer<UI_OrderFragment>{
        loge(TAG,"observer success---")
        binding.uiOrder=ui_model!!.getUIModel().value
    })
    ui_model!!.init()*/

    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                getUIModel().observe(this@Profile, Observer<UI_Profile> {
                    refreshUI()
                })
            }

    private fun refreshUI() {
        loge(TAG, "refreshUI...")
        val myclickhandler = MyClickHandler(this)
        val xml_profile = ui_model.getUIModel().value
        binding.xmlProfile = xml_profile
        binding.handlers=myclickhandler

    }

     fun logOut() {

        DialogUtils.openDialog(context!!,"Are you sure you would logout?","",
                "Logout","cancel", ContextCompat.getColor(context!!,R.color.theme_color), object : DialogUtils.OnDialogClickListener {
            override fun onPositiveButtonClick(position: Int) {
                (parentFragment as AccountFragment).signOut()
                PreferenceUtil.clearAll()
                PreferenceUtil.save()
                (activity as HomeActivity).onBackPressed()
            }
            override fun onNegativeButtonClick() {
            }
        })
    }


    fun backpress(): Boolean {
        if (profileEdit_fragment != null && profileEdit_fragment!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (coupan_fragment != null && coupan_fragment!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else {
            if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
                return true
            } else {
                return false
            }
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



    class MyClickHandler(val profile: Profile) {

        fun signout(view: View) {
            profile.logOut()
        }

    }

}

private class UIModel : ViewModel() {


    var uiData = MutableLiveData<UI_Profile>()

    fun init() {
        val ui_profile = UI_Profile(PreferenceUtil.getString(PreferenceUtil.USER_NAME, "")!!, PreferenceUtil.getString(PreferenceUtil.PHONE, "")!!, PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")!!)
        uiData.value = ui_profile
    }

    fun getUIModel(): LiveData<UI_Profile> {
        return uiData
    }
}

data class UI_Profile(var userName: String, var phone: String, var email: String)
