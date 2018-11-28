package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.RowSelectAddressBinding
import dk.eatmore.foodapp.databinding.SelectAddressBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.select_address.*
import kotlinx.android.synthetic.main.toolbar.*

class SelectAddress : BaseFragment() {

    private lateinit var binding: SelectAddressBinding


    companion object {

        val TAG = "SelectAddress"
        var ui_model: UIModel? = null
        fun newInstance(): SelectAddress {
            return SelectAddress()
        }

    }

    override fun getLayout(): Int {
        return R.layout.select_address
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            (activity as EpayActivity).txt_toolbar_right_img.apply { visibility= if(EpayActivity.isPickup) View.GONE else View.GONE  ; setImageResource(R.drawable.info_outline) }
            error_txt.visibility=View.GONE
            progress_bar.visibility=View.GONE
            ui_model = createViewModel()
            //clickEvent = MyClickHandler(this)
            fetchuserInfo()

        }else{
            logd(TAG,"saveInstance NOT NULL")
        }

    }

    private fun refreshview(){
        loge(TAG,"refresh view...")
        val mAdapter = UniversalAdapter(context!!, ui_model!!.addressList.value!!.messages, R.layout.row_select_address, object : RecyclerCallback<RowSelectAddressBinding, EditAddress.Messages> {
            override fun bindData(binder: RowSelectAddressBinding, model: EditAddress.Messages) {
                binder.editaddressListModel=model
                binder.rowContain.setOnClickListener{
                    val fragment=(activity as EpayActivity).supportFragmentManager.findFragmentByTag(Address.TAG)
                    (fragment as Address).onFragmentResult(model)
                    (activity as EpayActivity).onBackPressed()
                }
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view.adapter = mAdapter
    }

    class UIModel : ViewModel() {
        var addressList = MutableLiveData<EditAddress.EditaddressListModel>()
    }

    private fun createViewModel(): SelectAddress.UIModel =
    ViewModelProviders.of(this).get(SelectAddress.UIModel::class.java).apply {
        addressList.removeObservers(this@SelectAddress)
        addressList.observe(this@SelectAddress, Observer<EditAddress.EditaddressListModel> {
                    refreshview()
                })
            }


    fun fetchuserInfo() {
        progress_bar.visibility=View.VISIBLE
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.IS_LOGIN, "1")
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))

        callAPI(ApiCall.shippingaddress_list(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val editaddresslist = body as EditAddress.EditaddressListModel
                if (editaddresslist.status) {
                    if(editaddresslist.messages.size > 0){
                        loge(TAG,"status--"+editaddresslist.messages.size.toString())
                        for (i in 0 until editaddresslist.messages.size){
                            if(editaddresslist.messages[i].address_title == null || editaddresslist.messages[i].address_title =="")
                                editaddresslist.messages[i].address_title="Address "+(i+1)
                        }
                        ui_model!!.addressList.value=editaddresslist
                    }else{
                        error_txt.visibility=View.VISIBLE
                    }
                    progress_bar.visibility=View.GONE
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(editaddress_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(editaddress_container, getString(R.string.internet_not_available))
                    }
                }
                progress_bar.visibility=View.GONE
            }
        })
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
