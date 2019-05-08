package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.net.ParseException
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.gift.GiftBalanceParentAdapter
import dk.eatmore.foodapp.databinding.FragmentContactmethodBinding
import dk.eatmore.foodapp.databinding.FragmentCoupanBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_account_container.*
import kotlinx.android.synthetic.main.fragment_contactmethod.*
import kotlinx.android.synthetic.main.fragment_coupan.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*



class ContactMethod : BaseFragment() {

    private lateinit var binding: FragmentContactmethodBinding
    private lateinit var list : ArrayList<Profile.GiftType>
    private val uihandler : UiHandler = UiHandler(this)
    private var call_api: Call<JsonObject>? = null



    companion object {

        val TAG = "ContactMethod"
        fun newInstance(): ContactMethod {
            val fragment = ContactMethod()
            val bundle = Bundle()
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_contactmethod
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            binding.uiHandler=uihandler
            binding.notificationSetModel= NotificationSetModel()
            txt_toolbar.text="kontaktmetode"
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            fetchNotificationSetting()


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }


    }



    private fun fetchNotificationSetting(){
        val postParam = getDefaultApiParms()
        postParam.addProperty(Constants.CUSTOMER_ID,PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.IS_LOGIN,"1")
        postParam.addProperty(Constants.IS_EDIT,"0")
        call_api=ApiCall.notificationSetting(postParam)

        callAPI(call_api!!, object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val response = body as JsonObject
                val notificationsetmodel = GsonBuilder().create().fromJson(response.toString(),NotificationSetModel::class.java)
                notificationsetmodel.isloder=false
                binding.notificationSetModel = notificationsetmodel

            }

            override fun onFail(error: Int) {

                if (call_api!!.isCanceled) {
                    return
                }

                when (error) {
                    404 -> {
                        showSnackBar(viewcontainer, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(viewcontainer, getString(R.string.internet_not_available))
                    }
                }
            }
        })

    }



    private fun postNotificationSetting(){

        showProgressDialog()
        val postParam = getDefaultApiParms()
        postParam.addProperty(Constants.CUSTOMER_ID,PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.IS_LOGIN,"1")
        postParam.addProperty(Constants.IS_EDIT,"1")
        postParam.addProperty("order_sms",if(ordersms_chk.isChecked) "1" else "0")
        postParam.addProperty("order_email",if(orderemail_chk.isChecked) "1" else "0")
        postParam.addProperty("order_notification",if(orderpush_chk.isChecked) "1" else "0")
        postParam.addProperty("other_sms",if(othersms_chk.isChecked) "1" else "0")
        postParam.addProperty("other_email",if(otheremail_chk.isChecked) "1" else "0")
        postParam.addProperty("other_notification",if(otherpush_chk.isChecked) "1" else "0")
        call_api=ApiCall.notificationSetting(postParam)

        callAPI(call_api!!, object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val response = body as JsonObject
                (activity as HomeActivity).onBackPressed()
                Toast.makeText(context,"Saved.",Toast.LENGTH_SHORT).show()

            }

            override fun onFail(error: Int) {
                showProgressDialog()
                when (error) {
                    404 -> {
                        showSnackBar(viewcontainer, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(viewcontainer, getString(R.string.internet_not_available))
                    }
                }
            }
        })

    }




    data class NotificationSetModel(
            val status: Boolean=false,
            val order_sms: Boolean=false,
            val order_email: Boolean=false,
            val order_notification: Boolean=false,
            val other_sms: Boolean=false,
            val other_email: Boolean=false,
            val other_notification: Boolean=false,
            var isloder : Boolean =true,
            val msg: String=""
    )




    class UiHandler (val contactmethod : ContactMethod) {


        fun submitdata (view: View) {

            contactmethod.postNotificationSetting()

        }

    }

    override fun onDestroyView() {
        loge(TAG, "onDestroyView...")
        super.onDestroyView()
        call_api?.cancel()

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



