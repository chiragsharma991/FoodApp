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
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.fragment.Dashboard.Home.AddressForm
import dk.eatmore.foodapp.model.ModelUtility
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.editaddress.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import java.io.Serializable

class EditAddress : BaseFragment() {

    private lateinit var binding: EditaddressBinding
    private lateinit var clickEvent: MyClickHandler
    private var is_visible : Boolean = false
    private var call_shippingaddress_list : Call<EditaddressListModel>? = null
    private var call_delete_shippingaddress : Call<JsonObject>? = null
    private var mAdapter: UniversalAdapter<Messages, RowAddressBinding>?=null



    companion object {

        val TAG = "EditAddress"
        var ui_model: UIModel? = null
        fun newInstance(): EditAddress {
            return EditAddress()
        }

    }

    override fun getLayout(): Int {
        return R.layout.editaddress
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            error_txt.visibility=View.GONE
            txt_toolbar.text=getString(R.string.leveringsadresse)
            txt_toolbar_right.text= getString(R.string.add)
            txt_toolbar_right.visibility=View.VISIBLE
            txt_toolbar_right.setOnClickListener{ editAddress(null) }
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            if(RestaurantList.ui_model == null) is_visible = false else is_visible= true
            progress_bar.visibility=View.GONE
            ui_model = createViewModel()
            clickEvent = MyClickHandler(this)
            if (ui_model!!.editaddressList.value == null) {
                fetchuserInfo()
            } else {
                refreshview()
            }

        }else{
            logd(TAG,"saveInstance NOT NULL")
        }


    }

    class UIModel : ViewModel() {
        var editaddressList = MutableLiveData<EditaddressListModel>()
    }

    private fun createViewModel(): EditAddress.UIModel =

            ViewModelProviders.of(this).get(EditAddress.UIModel::class.java).apply {
                editaddressList.observe(this@EditAddress, Observer<EditaddressListModel> {
                    refreshview()
                })
            }




     fun fetchuserInfo() {
        progress_bar.visibility=View.VISIBLE
        error_txt.visibility=View.GONE
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.IS_LOGIN, "1")
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
         postParam.addProperty(Constants.LANGUAGE, Constants.DA)
         call_shippingaddress_list=ApiCall.shippingaddress_list(jsonObject = postParam)
         callAPI(call_shippingaddress_list!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val editaddresslist = body as EditaddressListModel
                if (editaddresslist.status) {
                    if(editaddresslist.messages.size > 0){
                        loge(TAG,"status--"+editaddresslist.messages.size.toString())
                        for (i in 0 until editaddresslist.messages.size){
                            if(editaddresslist.messages[i].address_title == null || editaddresslist.messages[i].address_title =="")
                                editaddresslist.messages[i].address_title="Address "+(i+1)
                        }
                        ui_model!!.editaddressList.value=editaddresslist
                    }else{
                        if(mAdapter != null){
                            ui_model!!.editaddressList.value!!.messages.clear()
                            mAdapter!!.notifyDataSetChanged()
                        }
                        error_txt.visibility=View.VISIBLE
                    }


                    progress_bar.visibility=View.GONE
                }
            }

            override fun onFail(error: Int) {

                if (call_shippingaddress_list!!.isCanceled ) {
                    return
                }
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


    private fun deleteuserInfo(id : String) {
        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.ID, id)
        postParam.addProperty(Constants.IS_LOGIN, "1")
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        call_delete_shippingaddress=ApiCall.delete_shippingaddress(jsonObject = postParam)
        callAPI(call_delete_shippingaddress!!, object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                Toast.makeText(context,jsonObject.get(Constants.MSG).asString,Toast.LENGTH_SHORT).show()
                    fetchuserInfo()
                }
                showProgressDialog()
            }

            override fun onFail(error: Int) {
                if (call_delete_shippingaddress!!.isCanceled ) {
                    return
                }
                when (error) {
                    404 -> {
                        showSnackBar(editaddress_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(editaddress_container, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
    }



    private fun refreshview(){
        loge(TAG,"refresh view...")

        mAdapter = UniversalAdapter(context!!, ui_model!!.editaddressList.value!!.messages, R.layout.row_address, object : RecyclerCallback<RowAddressBinding, Messages> {
            override fun bindData(binder: RowAddressBinding, model: Messages) {
                setRecyclerData(binder, model)
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view.adapter = mAdapter

    }


    private fun setRecyclerData(binder: RowAddressBinding, model: Messages) {
        binder.editaddressListModel=model
        binder.myClickHandler=clickEvent
        binder.isVisible=is_visible
    }

    private fun deleteAddress(model: Messages){
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
    private fun editAddress(model: Messages?) {
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
        (parentFragment as Profile).addFragment(R.id.profile_container, fragment, AddressForm.TAG, false)

    }



    override fun onDestroy() {

        logd(TAG, "on destroy...")

        call_shippingaddress_list?.let {
            it.cancel()
        }
        call_delete_shippingaddress?.let {
            it.cancel()
        }

        super.onDestroy()
    }


    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")
    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")
    }

    data class EditaddressListModel(
            val status: Boolean = false,
            val msg: String ="",
            val messages: ArrayList<Messages>

    ) : ModelUtility(), Serializable

    data class Messages(
            var id: String = "",
            var customer_id: String = "",
            var street: String = "",
            var house_no: String = "",
            var postal_code: String = "",
            var floor_door: String = "",
            var city: String = "",
            var address_title: String? = null

    ) : Serializable

    class MyClickHandler(val editaddress: EditAddress) {

        fun deleteAddress(view: View , model: Messages) {
            if(editaddress.progress_bar.visibility == View.GONE){
                editaddress.deleteAddress(model)
            }
        }
        fun editAddress(view: View, model: Messages) {
            if(editaddress.progress_bar.visibility == View.GONE){
                editaddress.editAddress(model)
            }
        }
    }

}
