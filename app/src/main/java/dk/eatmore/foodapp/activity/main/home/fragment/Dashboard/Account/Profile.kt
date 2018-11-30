package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.toolbar.*

class Profile : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private var profileEdit_fragment: ProfileEdit? = null
    private var healthreport: HealthReport? = null
    private var termscondition: TermsCondition? = null
    private var editaddress: EditAddress? = null
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
            txt_toolbar.text=getString(R.string.help)
            img_toolbar_back.visibility=View.GONE
            ui_model = createViewModel()
            ui_model.init()

/*
            health_report.setOnClickListener {
                healthreport = HealthReport.newInstance()
                addFragment(R.id.profile_container, healthreport!!, HealthReport.TAG, true)
            }
            profile_info_txt.setOnClickListener {
                profileEdit_fragment = ProfileEdit.newInstance()
                addFragment(R.id.profile_container, profileEdit_fragment!!, ProfileEdit.TAG, true)
            }
            Opening_hours.setOnClickListener {
                val openinghours = OpeningHours.newInstance()
                addFragment(R.id.profile_container, openinghours, OpeningHours.TAG, true)
            }
            gift_card_txt.setOnClickListener {
                coupan_fragment = Coupan.newInstance()
                addFragment(R.id.profile_container, coupan_fragment!!, Coupan.TAG, true)
            }
            terms_of_services.setOnClickListener {
                termscondition = TermsCondition.newInstance()
                addFragment(R.id.profile_container, termscondition!!, TermsCondition.TAG, true)
            }*/

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
     fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                getUIModel().observe(this@Profile, Observer<UI_Profile> {
                    refreshUI()
                })
            }

    private fun refreshUI() {
        val myclickhandler = MyClickHandler(this)
        val xml_profile = ui_model.getUIModel().value
        binding.xmlProfile = xml_profile
        binding.handlers = myclickhandler

    }

    fun logOut() {

        DialogUtils.openDialog(context!!, getString(R.string.are_you_sure_would), "",
                getString(R.string.logout), getString(R.string.cancel), ContextCompat.getColor(context!!, R.color.theme_color), object : DialogUtils.OnDialogClickListener {
            override fun onPositiveButtonClick(position: Int) {
                showProgressDialog()
                callAPI(ApiCall.clearcart(
                        auth_key = Constants.AUTH_VALUE,
                        customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!,
                        eatmore_app = true
                ), object : BaseFragment.OnApiCallInteraction {

                    override fun <T> onSuccess(body: T?) {
                        val jsonObject = body as JsonObject
                        (parentFragment as AccountFragment).signOut()
                        PreferenceUtil.clearAll()
                        val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
                        (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
                        PreferenceUtil.save()
                        // clear all but add id again to collect non user item into cart.
                        PreferenceUtil.putValue(PreferenceUtil.DEVICE_TOKEN, Settings.Secure.getString(context!!.getContentResolver(), Settings.Secure.ANDROID_ID))
                        PreferenceUtil.save()
                        (activity as HomeActivity).onBackPressed()
                        if(OrderFragment.ui_model?.reloadfragment !=null) OrderFragment.ui_model!!.reloadfragment.value=true
                        if(HomeFragment.ui_model?.reloadfragment !=null) HomeFragment.ui_model!!.reloadfragment.value=true  // reload last order from homefragment.
                        showProgressDialog()
                    }

                    override fun onFail(error: Int) {
                        when (error) {
                            404 -> {
                                showSnackBar(profile_container, getString(R.string.error_404))
                            }
                            100 -> {
                                showSnackBar(profile_container, getString(R.string.internet_not_available))
                            }
                        }
                        showProgressDialog()
                    }
                })
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
            } else if (healthreport != null && healthreport!!.isVisible) {
                childFragmentManager.popBackStack()
                return true
            }
            else if (termscondition != null && termscondition!!.isVisible) {
                childFragmentManager.popBackStack()
                return true
            }
            else if (editaddress != null && editaddress!!.isVisible) {
                childFragmentManager.popBackStack()
                return true
            }
            else {
                if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
                    return true
                } else {
                    return false  // this return would work for logout.
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
        fun healthreport(view: View){
            profile.healthreport = HealthReport.newInstance()
            profile.addFragment(R.id.profile_container, profile.healthreport!!, HealthReport.TAG, false)
        }
        fun profileInfo(view: View){
            profile.profileEdit_fragment = ProfileEdit.newInstance()
            profile.addFragment(R.id.profile_container, profile.profileEdit_fragment!!, ProfileEdit.TAG, false)
        }
        fun giftcart(view: View){
            profile.coupan_fragment = Coupan.newInstance()
            profile.addFragment(R.id.profile_container, profile.coupan_fragment!!, Coupan.TAG, false)
        }
        fun termsofservices(view: View){
            profile.termscondition = TermsCondition.newInstance()
            profile.addFragment(R.id.profile_container, profile.termscondition!!, TermsCondition.TAG, false)
        }
      /*  fun openinghours(view: View){
            profile.openinghours = OpeningHours.newInstance()
            profile.addFragment(R.id.profile_container, profile.openinghours!!, OpeningHours.TAG, true)
        }*/
        fun editaddress(view: View){
            profile.editaddress = EditAddress.newInstance()
            profile.addFragment(R.id.profile_container, profile.editaddress!!, EditAddress.TAG, false)
        }

    }


     class UIModel : ViewModel() {


        var uiData = MutableLiveData<UI_Profile>()

        fun init() {
            val ui_profile = UI_Profile(PreferenceUtil.getString(PreferenceUtil.FIRST_NAME, "")!!, PreferenceUtil.getString(PreferenceUtil.PHONE, "")!!, PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")!!)
            uiData.value = ui_profile
        }

        fun getUIModel(): LiveData<UI_Profile> {
            return uiData
        }
    }

    data class UI_Profile(var userName: String, var phone: String, var email: String)


}

