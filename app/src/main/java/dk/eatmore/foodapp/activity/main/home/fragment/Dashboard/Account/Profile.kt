package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.gson.JsonObject
import dk.eatmore.foodapp.BuildConfig
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.Subscribe
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.Support
import zendesk.support.request.RequestActivity

class Profile : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private var profileEdit_fragment: ProfileEdit? = null
    private var healthreport: HealthReport? = null
    private var termscondition: TermsCondition? = null
    private var restpaymentmethods: RestPaymentMethods? = null
    private var editaddress: EditAddress? = null
    private var ratetheapp: RatetheAPP? = null
    private var kundlesupport: KundleSupport? = null
    private var kundlechatsupport: KundleChatSupport? = null
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
            txt_toolbar.text = getString(R.string.my_profile)
            img_toolbar_back.visibility = View.GONE
            setCurrentVersion()
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

    private fun setCurrentVersion() {

        try {
            val pm = context!!.packageManager
            val pInfo = pm.getPackageInfo(context!!.packageName, 0)
            app_version.text = String.format(getString(R.string.app_version), pInfo.versionCode.toString(), pInfo.versionName)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun logOut() {

        DialogUtils.openDialog(context!!, getString(R.string.are_you_sure_would), "",
                getString(R.string.logout), getString(R.string.cancel), ContextCompat.getColor(context!!, R.color.theme_color), object : DialogUtils.OnDialogClickListener {
            override fun onPositiveButtonClick(position: Int) {
                clearaccount()
            }

            override fun onNegativeButtonClick() {
            }
        })
    }

    fun clearaccount() {
        var mToken = ""
        val cpy_is_skip_version = PreferenceUtil.getBoolean(PreferenceUtil.IS_SKIP_VERSION, false)
        val cpy_skiped_version_name = PreferenceUtil.getString(PreferenceUtil.SKIPED_VERSION_NAME, "")

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(activity!!, object : OnSuccessListener<InstanceIdResult> {
            override fun onSuccess(instanceIdResult: InstanceIdResult) {
                mToken = instanceIdResult.getToken()
                Log.e(TAG, "Refreshed token:--- " + mToken)
            }
        })
        showProgressDialog()
        callAPI(ApiCall.clearcart(
                auth_key = Constants.AUTH_VALUE,
                customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, "")!!,
                eatmore_app = true
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                (parentFragment as AccountFragment).signOut()
                PreferenceUtil.clearAll()
                val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
                (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
                fragmentof.getOrderFragment().popAllFragment()
                PreferenceUtil.save()
                // clear all but add id again to collect non user item into cart.
                PreferenceUtil.putValue(PreferenceUtil.DEVICE_TOKEN, mToken)
                PreferenceUtil.putValue(PreferenceUtil.CLOSE_INTRO_SLIDE, true)
                PreferenceUtil.putValue(PreferenceUtil.IS_SKIP_VERSION, cpy_is_skip_version)
                PreferenceUtil.putValue(PreferenceUtil.SKIPED_VERSION_NAME, cpy_skiped_version_name!!)
                PreferenceUtil.save()
                (activity as HomeActivity).onBackPressed()
                if (OrderFragment.ui_model?.reloadfragment != null) OrderFragment.ui_model!!.reloadfragment.value = true
                if (HomeFragment.ui_model?.reloadfragment != null) HomeFragment.ui_model!!.reloadfragment.value = true  // reload last order from homefragment.
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


    @Subscribe
    fun onEvent(parsingevents: ParsingEvents.EventFromRestaurantList) {
        loge(TAG, "--EventFromRestaurantList" )
        for (i in 0 until childFragmentManager.backStackEntryCount) {
            childFragmentManager.popBackStack()
        }
        ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().childFragmentManager.popBackStack()
    }


    fun backpress(): Boolean {
        loge(TAG,"home direct--------------")

        if (profileEdit_fragment != null && profileEdit_fragment!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (coupan_fragment != null && coupan_fragment!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (healthreport != null && healthreport!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (termscondition != null && termscondition!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (restpaymentmethods != null && restpaymentmethods!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (editaddress != null && editaddress!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (coupan_fragment != null && coupan_fragment!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (ratetheapp != null && ratetheapp!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (kundlesupport != null && kundlesupport!!.isVisible) {
            childFragmentManager.popBackStack()
            return true
        } else if (kundlechatsupport != null && kundlechatsupport!!.isVisible) {
            kundlechatsupport!!.backpress()
            return true
        } else {
            if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
                ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(0,0) // if user is login and press only back then move->Home
                return true
            } else {
                return false  // this return would work for logout.
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logd(TAG, "onStart...")
        GlobalBus.bus.register(this)
    }

    override fun onStop() {
        super.onStop()
        logd(TAG, "onStop...")
        GlobalBus.bus.unregister(this)

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

        fun healthreport(view: View) {
            profile.healthreport = HealthReport.newInstance("http://www.findsmiley.dk")
            profile.addFragment(R.id.profile_container, profile.healthreport!!, HealthReport.TAG, false)
        }

        fun profileInfo(view: View) {
            profile.profileEdit_fragment = ProfileEdit.newInstance()
            profile.addFragment(R.id.profile_container, profile.profileEdit_fragment!!, ProfileEdit.TAG, false)
        }

        fun giftcart(view: View) {
            profile.coupan_fragment = Coupan.newInstance()
            profile.addFragment(R.id.profile_container, profile.coupan_fragment!!, Coupan.TAG, false)
        }

        fun termsofservices(view: View) {
            profile.termscondition = TermsCondition.newInstance(0)
            profile.addFragment(R.id.profile_container, profile.termscondition!!, TermsCondition.TAG, false)
        }

        fun rest_pay_methods(view: View) {
            profile.restpaymentmethods = RestPaymentMethods.newInstance()
            profile.addFragment(R.id.profile_container, profile.restpaymentmethods!!, RestPaymentMethods.TAG, false)

        }

        fun cokkie_policy(view: View) {
            profile.termscondition = TermsCondition.newInstance(1)
            profile.addFragment(R.id.profile_container, profile.termscondition!!, TermsCondition.TAG, false)
        }

        /*  fun openinghours(view: View){
              profile.openinghours = OpeningHours.newInstance()
              profile.addFragment(R.id.profile_container, profile.openinghours!!, OpeningHours.TAG, true)
          }*/
        fun editaddress(view: View) {
            profile.editaddress = EditAddress.newInstance()
            profile.addFragment(R.id.profile_container, profile.editaddress!!, EditAddress.TAG, false)
        }

        fun ratetheapp(view: View) {
            profile.ratetheapp = RatetheAPP.newInstance()
            profile.addFragment(R.id.profile_container, profile.ratetheapp!!, RatetheAPP.TAG, false)
        }

        fun kundle_support(view: View) {
            // chat support
            profile.kundlechatsupport = KundleChatSupport.newInstance()
            profile.addFragment(R.id.profile_container, profile.kundlechatsupport!!, KundleChatSupport.TAG, false)
        }

        fun kuntakt_os(view: View) {
            // msg support
            profile.kundlesupport = KundleSupport.newInstance()
            profile.addFragment(R.id.profile_container, profile.kundlesupport!!, KundleSupport.TAG, false)
        }

        fun go_onfavorite(view: View) {
            profile.showProgressDialog()
            val fragmentof = (profile.activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            val homefragment = ((profile.activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment()
            homefragment.popAllFragment()
            homefragment.go_onfavorite()
            Handler().postDelayed({
                profile.showProgressDialog()
                ((profile.activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(0,0)
            }, 2000)
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

