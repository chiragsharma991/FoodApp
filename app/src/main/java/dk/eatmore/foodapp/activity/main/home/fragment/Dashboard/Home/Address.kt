package dk.eatmore.foodapp.fragment.Dashboard.Home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.epay.fragment.DeliveryTimeslot
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAddressBinding
import dk.eatmore.foodapp.databinding.RowAddressBinding
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_address.*
import kotlinx.android.synthetic.main.toolbar.*


class Address : BaseFragment(), TextWatcher {

    private lateinit var binding: FragmentAddressBinding
    private var mAdapter: UniversalAdapter<User, RowAddressBinding>? = null
    private lateinit var homeFragment: HomeFragment
    private val inputValidStates = java.util.HashMap<EditText, Boolean>()


    companion object {

        val TAG = "Address"
        var ui_model: UIModel? = null
        fun newInstance(): Address {
            return Address()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_address
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            binding.isPickup = EpayActivity.isPickup
            setToolbarforThis()
            postnumber_edt.addTextChangedListener(this)
            name_edt.addTextChangedListener(this)
            telephone_number_edt.addTextChangedListener(this)
            street_edt.addTextChangedListener(this)
            house_edt.addTextChangedListener(this)
            city_edt.addTextChangedListener(this)
            inputValidStates[postnumber_edt] = false
            inputValidStates[name_edt] = false
            inputValidStates[telephone_number_edt] = false
            inputValidStates[street_edt] = false
            inputValidStates[house_edt] = false
            inputValidStates[city_edt] = false
            postnumber_edt.imeOptions=EditorInfo.IME_ACTION_DONE
            postnumber_edt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    moveon_next()
                    return true
                }

            })
            ui_model = createViewModel()
            if (ui_model!!.user_infoList.value == null) {
                fetchuserInfo()
            } else {
                refreshview()
            }
            proceed_view_nxt.setOnClickListener {
                moveon_next()
            }






        } else {
            logd(TAG, "saveInstance NOT NULL")
        }

    }

    fun moveon_next(){

        if (validationFields()) {

            /**TODO  API CALL LOGIC FROM ADDRESS SCREEN
             * DELIVERY: we are calling "deliveryDetails" api if delivery is select because : user can edit their address and info so we have to submit that information.
             * PICKUP: we are not calling api in this section because user just entered name and phone and it can be proceed.
             * DELIVERY TIME SLOT:
             * - if i use delivery api then i store time slot and pass another screen so i never call api in "delivery time slot" screen if i am coming from "delivery"
             * - if i call api in "delivery time slot" only on one condition to get time, if i am coming from "Pickup"
             */

            if(EpayActivity.isPickup){
                EpayActivity.paymentattributes.first_name=name_edt.text.toString()
                EpayActivity.paymentattributes.telephone_no=telephone_number_edt.text.toString()
                EpayActivity.paymentattributes.upto_min_shipping="0"
                val fragment = DeliveryTimeslot.newInstance(null)
                (activity as EpayActivity).addFragment(R.id.epay_container,fragment, DeliveryTimeslot.TAG,true)
            }
            else{
                if(proceed_view_nxt.isEnabled == false) return
                proceed_view_nxt.isEnabled =false
                submitdelivery()
            }
        }
    }




    override fun afterTextChanged(s: Editable?) {


        if (name_edt.text.hashCode() == s!!.hashCode()) {
            name_edt.error = null
            if (name_edt.text.trim().toString().length > 0)
                inputValidStates[name_edt] = true
            else
                inputValidStates[name_edt] = false

        } else if (postnumber_edt.text.hashCode() == s!!.hashCode()) {
            postnumber_edt.error = null
            if (postnumber_edt.text.trim().toString().length > 0) {
                inputValidStates[postnumber_edt] = true
                city_edt.setText(ui_model!!.user_infoList.value!!.postal_city.get(postnumber_edt.text.toString()))
            } else
                inputValidStates[postnumber_edt] = false

        } else if (telephone_number_edt.text.hashCode() == s!!.hashCode()) {
            telephone_number_edt.error = null
            if (telephone_number_edt.text.trim().toString().length >= 8)
                inputValidStates[telephone_number_edt] = true
            else
                inputValidStates[telephone_number_edt] = false

        } else if (street_edt.text.hashCode() == s!!.hashCode()) {
            street_edt.error = null
            if (street_edt.text.trim().toString().length > 0)
                inputValidStates[street_edt] = true
            else
                inputValidStates[street_edt] = false

        } else if (house_edt.text.hashCode() == s!!.hashCode()) {
            house_edt.error = null
            if (house_edt.text.trim().toString().length > 0)
                inputValidStates[house_edt] = true
            else
                inputValidStates[house_edt] = false

        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }


    fun validationFields(): Boolean {
        var isvalidate: Boolean = true
        // Test validtion between pickup/delivery
        if (EpayActivity.isPickup) {

            if (!inputValidStates[name_edt]!!) {
                name_edt.error = getString(R.string.enter_your_valid_name)
                isvalidate = false
            }

            if (!inputValidStates[telephone_number_edt]!!) {
                telephone_number_edt.error = getString(R.string.enter_the_valid_number)
                isvalidate = false
            }


        } else {

            if (!inputValidStates[name_edt]!!) {
                name_edt.error = getString(R.string.enter_your_valid_name)
                isvalidate = false
            }
            if (!inputValidStates[postnumber_edt]!!) {
                postnumber_edt.error = getString(R.string.enter_your_postal_number)
                isvalidate = false
            }
            if (!inputValidStates[telephone_number_edt]!!) {
                telephone_number_edt.error = getString(R.string.enter_the_valid_number)
                isvalidate = false
            }
            if (!inputValidStates[street_edt]!!) {
                street_edt.error = getString(R.string.enter_your_street_number)
                isvalidate = false
            }
            if (!inputValidStates[house_edt]!!) {
                house_edt.error = getString(R.string.enter_your_house_number)
                isvalidate = false
            }
            if (city_edt.text.trim().length <= 0) {
                postnumber_edt.error = getString(R.string.postal_number_is_not_valid)
                isvalidate = false
            }
        }


        return isvalidate
    }

    class UIModel : ViewModel() {

        var user_infoList = MutableLiveData<UserInfoModel>()


    }

    private fun createViewModel(): Address.UIModel =

            ViewModelProviders.of(this).get(Address.UIModel::class.java).apply {
                user_infoList.observe(this@Address, Observer<UserInfoModel> {
                    refreshview()
                })
            }

    private fun refreshview() {

        binding.userInfo = ui_model!!.user_infoList.value!!.user_info
        name_edt.setText(ui_model!!.user_infoList.value!!.user_info.name)
        telephone_number_edt.setText(ui_model!!.user_infoList.value!!.user_info.telephone_no)
        street_edt.setText(ui_model!!.user_infoList.value!!.user_info.street)
        house_edt.setText(ui_model!!.user_infoList.value!!.user_info.house_no)
        floor_edt.setText(ui_model!!.user_infoList.value!!.user_info.floor_door)
        postnumber_edt.setText(ui_model!!.user_infoList.value!!.user_info.postal_code)
    }


    private fun fetchuserInfo() {
        progresswheel(progresswheel,true)
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        } else {
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }

        callAPI(ApiCall.userInfo(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    val userinfo_model = UserInfoModel(
                            status = jsonObject.get(Constants.STATUS).asBoolean,
                            postal_city = getpostal_city(jsonObject),
                            user_info = User_Info(
                                    name = if (jsonObject.getAsJsonObject(Constants.USER_INFO).get("name").isJsonNull) "" else jsonObject.getAsJsonObject(Constants.USER_INFO).get("name").asString,
                                    city = if (jsonObject.getAsJsonObject(Constants.USER_INFO).get("city").isJsonNull) "" else jsonObject.getAsJsonObject(Constants.USER_INFO).get("city").asString,
                                    house_no = if (jsonObject.getAsJsonObject(Constants.USER_INFO).get("house_no").isJsonNull) "" else jsonObject.getAsJsonObject(Constants.USER_INFO).get("house_no").asString,
                                    postal_code = if (jsonObject.getAsJsonObject(Constants.USER_INFO).get("postal_code").isJsonNull) "" else jsonObject.getAsJsonObject(Constants.USER_INFO).get("postal_code").asString,
                                    street = if (jsonObject.getAsJsonObject(Constants.USER_INFO).get("street").isJsonNull) "" else jsonObject.getAsJsonObject(Constants.USER_INFO).get("street").asString,
                                    telephone_no = if (jsonObject.getAsJsonObject(Constants.USER_INFO).get("telephone_no").isJsonNull) "" else jsonObject.getAsJsonObject(Constants.USER_INFO).get("telephone_no").asString,
                                    floor_door = if (jsonObject.getAsJsonObject(Constants.USER_INFO).get("floor_door").isJsonNull) "" else jsonObject.getAsJsonObject(Constants.USER_INFO).get("floor_door").asString
                            )

                    )

                    ui_model!!.user_infoList.value = userinfo_model
                    loge(TAG, "data is---" + userinfo_model.user_info.telephone_no + " " + userinfo_model.user_info.name)
                    progresswheel(progresswheel,false)
                    // ui_model!!.user_infoList.value!!.user_info.name
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(address_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(address_container, getString(R.string.internet_not_available))
                    }
                }
                progresswheel(progresswheel,false)
            }
        })
    }


    private fun submitdelivery() {
        progresswheel(progresswheel,true)
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_TOTAL,EpayActivity.paymentattributes.order_total)
        postParam.addProperty(Constants.SHIPPING, if (EpayActivity.isPickup) getString(R.string.pickup) else getString(R.string.delivery))
        postParam.addProperty(Constants.STREET, street_edt.text.toString())
        postParam.addProperty(Constants.HOUSE_NO, house_edt.text.toString())
        postParam.addProperty(Constants.FLOOR_DOOR, floor_edt.text.toString())
        postParam.addProperty(Constants.POSTAL_CODE, postnumber_edt.text.toString())
        postParam.addProperty(Constants.CITY, city_edt.text.toString())


        callAPI(ApiCall.deliveryDetails(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {

                    EpayActivity.paymentattributes.first_name=name_edt.text.toString()
                    EpayActivity.paymentattributes.telephone_no=telephone_number_edt.text.toString()
                    //                        finalCartJson.put("address", street + " " + houseNo + " " + floorDoorString + ", " + postal_code + " " + city);
                    EpayActivity.paymentattributes.address=String.format(getString(R.string.fulladdress),street_edt.text.trim().toString(),house_edt.text.trim().toString(),floor_edt.text.trim().toString(),postnumber_edt.text.trim().toString(),city_edt.text.trim().toString())
                    EpayActivity.paymentattributes.postal_code=postnumber_edt.text.toString()
                 //   EpayActivity.paymentattributes.discount_type=jsonObject.getAsJsonObject(Constants.RESULT)[Constants.DISCOUNT_TYPE].asString
                   // EpayActivity.paymentattributes.discount_amount=jsonObject.getAsJsonObject(Constants.RESULT)[Constants.DISCOUNT_AMOUNT].asString
                    EpayActivity.paymentattributes.shipping_charge=if(jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString =="") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString
                    EpayActivity.paymentattributes.upto_min_shipping=if(jsonObject.getAsJsonObject(Constants.RESULT)[Constants.UPTO_MIN_SHIPPING].asString =="")"0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.UPTO_MIN_SHIPPING].asString
                    EpayActivity.paymentattributes.minimum_order_price=if(jsonObject.getAsJsonObject(Constants.RESULT)[Constants.MIN_ORDER_SHIPPING].asString== "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.MIN_ORDER_SHIPPING].asString
                  //  EpayActivity.paymentattributes.additional_charges_cash=jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_CASH].asString
                    EpayActivity.paymentattributes.additional_charges_online=if(!(jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.ADDITIONAL_CHARGES_CASH)) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString=="") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString
                    EpayActivity.paymentattributes.additional_charges_cash=if(!(jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.ADDITIONAL_CHARGES_CASH)) || jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString=="") "0" else jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString

                    EpayActivity.paymentattributes.distance=if(jsonObject.getAsJsonObject(Constants.RESULT)[Constants.USER_DISTANCE].asString =="") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.USER_DISTANCE].asString
                    EpayActivity.paymentattributes.first_time=if(jsonObject.getAsJsonObject(Constants.RESULT)[Constants.FIRST_TIME].asString== "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.FIRST_TIME].asString



                    val time_list =LinkedHashMap<String,String>()
                    for (i in 0.until(jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).size())){
                        time_list.put((jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.ACTUAL].asString,
                                (jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.DISPLAY].asString )
                    }
                     val fragment = DeliveryTimeslot.newInstance(time_list)
                     (activity as EpayActivity).addFragment(R.id.epay_container,fragment, DeliveryTimeslot.TAG,true)
                    proceed_view_nxt.isEnabled = true
                    progresswheel(progresswheel,false)

                } else {
                    progresswheel(progresswheel,false)
                    showSnackBar(address_container, getString(R.string.error_404))
                    proceed_view_nxt.isEnabled = true
                }
            }
            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(address_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(address_container, getString(R.string.internet_not_available))
                    }
                }
                proceed_view_nxt.isEnabled = true
                progresswheel(progresswheel,false)

            }
        })
    }

    private fun getpostal_city(jsonObject: JsonObject): Map<String, String> {
        val map = HashMap<String, String>()
        val entrySet = jsonObject.getAsJsonObject(Constants.POSTAL_CITY).entrySet()
        for (entry in entrySet) {
            map.put(entry.key, jsonObject.getAsJsonObject(Constants.POSTAL_CITY).get(entry.key).asString)
        }
        return map
    }


    fun setToolbarforThis() {

        (activity as EpayActivity).txt_toolbar.text = getString(R.string.address)
        (activity as EpayActivity).img_toolbar_back.setImageResource(R.drawable.back)
      /*  (activity as EpayActivity).img_toolbar_back.setOnClickListener {
            onBackpress()
        }*/
    }

  /*  fun onBackpress() {
        (activity as EpayActivity).txt_toolbar.text = getString(R.string.basket)
        (activity as EpayActivity).popFragment()

    }*/




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

    data class UserInfoModel(
            val status: Boolean = false,
            val user_info: User_Info,
            val postal_city: Map<String, String>

    )

    data class User_Info(
            var name: String = "",
            var telephone_no: String = "",
            var street: String = "",
            var house_no: String = "",
            var postal_code: String = "",
            var floor_door: String = "",
            var city: String = "") {
        /*  init {
              name = name ?: ""
              telephone_no = telephone_no ?: ""
              street = street ?: ""
              house_no = house_no ?: ""
              postal_code = postal_code ?: ""
              city = city ?: ""
          }*/
    }


}


/*
@BindingAdapter("android:layout_setText")
fun setText(view : View, value : String) {
    Log.e("test---","-----")
   // (view as AppCompatEditText).setText("test")

}
*/


