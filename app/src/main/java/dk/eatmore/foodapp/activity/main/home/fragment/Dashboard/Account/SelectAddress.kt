package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.RowSelectAddressBinding
import dk.eatmore.foodapp.databinding.SelectAddressBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.AddressForm
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.select_address.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call

class SelectAddress : BaseFragment() {

    private lateinit var binding: SelectAddressBinding
    private var call_shippingaddress_list : Call<EditAddress.EditaddressListModel>? = null
    private val myclickhandler = MyClickHandler(this)



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
            txt_toolbar.text = getString(R.string.address)
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            txt_toolbar_right.text= getString(R.string.add)
            txt_toolbar_right.setOnClickListener{
                if(progress_bar.visibility == View.GONE)
                editAddress(null)
            }
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
                    if(progress_bar.visibility == View.GONE){
                        val fragment=(parentFragment as EpayFragment).childFragmentManager.findFragmentByTag(Address.TAG)
                        (fragment as Address).onFragmentResult(model)
                        (activity as HomeActivity).onBackPressed()
                    }
                }
                binder.myClickHandler=myclickhandler
                binder.executePendingBindings()
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


    private fun editAddress(model: EditAddress.Messages?) {
        loge(TAG,"edit function")
        val fragment : AddressForm
        if(model == null){
            // Add address
            fragment = AddressForm.newInstance()
        }else{
            // Edit address
            fragment = AddressForm.newInstance(
                    address = model
            )
        }
        var enter : Slide?=null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(Constants.BOTTOM_TO_TOP_ANIM.toLong())
            enter.slideEdge = Gravity.BOTTOM
            val changeBoundsTransition : ChangeBounds = ChangeBounds()
            changeBoundsTransition.duration = Constants.BOTTOM_TO_TOP_ANIM.toLong()
            //fragment!!.sharedElementEnterTransition=changeBoundsTransition
            fragment.sharedElementEnterTransition=changeBoundsTransition
            fragment.sharedElementReturnTransition=changeBoundsTransition
            fragment.enterTransition=enter
        }
        (parentFragment as EpayFragment).addFragment(R.id.epay_container, fragment, AddressForm.TAG, false)


    }

    private fun deleteuserInfo(id : String) {
        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.ID, id)
        postParam.addProperty(Constants.IS_LOGIN, "1")
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)
        callAPI(ApiCall.delete_shippingaddress(jsonObject = postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    Toast.makeText(context,jsonObject.get(Constants.MSG).asString, Toast.LENGTH_SHORT).show()
                    fetchuserInfo()
                }
            }

            override fun onFail(error: Int) {
                showProgressDialog()
                when (error) {
                    404 -> {
                        showSnackBar(editaddress_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(editaddress_container, getString(R.string.internet_not_available))
                    }
                }
            }
        })
    }



    fun fetchuserInfo() {
        progress_bar.visibility=View.VISIBLE
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.IS_LOGIN, "1")
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)
        call_shippingaddress_list=ApiCall.shippingaddress_list(jsonObject = postParam)
        callAPI(call_shippingaddress_list!!, object : BaseFragment.OnApiCallInteraction {

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

    private fun deleteAddress(model: EditAddress.Messages){
        loge(TAG,"delete function")
        DialogUtils.openDialog(context!!, getString(R.string.are_you_sure_to_delete), "",
                getString(R.string.yes), getString(R.string.no), ContextCompat.getColor(context!!, R.color.theme_color), object : DialogUtils.OnDialogClickListener {
            override fun onPositiveButtonClick(p: Int) {
                deleteuserInfo(model.id)
            }

            override fun onNegativeButtonClick() {
            }
        })
    }


    override fun onDestroyView() {

        super.onDestroyView()

        logd(TAG, "onDestroyView...")

        ui_model?.let {
            ViewModelProviders.of(this).get(UIModel::class.java).addressList.removeObservers(this@SelectAddress)
        }

        if (call_shippingaddress_list != null) {
            call_shippingaddress_list!!.cancel()
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

    class MyClickHandler(val selectaddress: SelectAddress) {

        fun deleteAddress(view: View , model: EditAddress.Messages) {
            if(selectaddress.progress_bar.visibility == View.GONE)
            selectaddress.deleteAddress(model)

        }
        fun editAddress(view: View, model: EditAddress.Messages) {
            if(selectaddress.progress_bar.visibility == View.GONE)
            selectaddress.editAddress(model)

        }
    }




}
