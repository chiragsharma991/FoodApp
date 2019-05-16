package dk.eatmore.foodapp.fragment.Dashboard.Home

import android.app.Dialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.text.Editable
import android.text.TextWatcher
import android.transition.Slide
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.epay.fragment.DeliveryTimeslot
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.EditAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.SelectAddress
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAddressBinding
import dk.eatmore.foodapp.databinding.RowAddressBinding
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.home.Postalcity
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.fragment_address.*
import kotlinx.android.synthetic.main.infodialog.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call


class Address : CommanAPI(), TextWatcher {

    private lateinit var binding: FragmentAddressBinding
    private var mAdapter: UniversalAdapter<User, RowAddressBinding>? = null
    private lateinit var homeFragment: HomeFragment
    private val inputValidStates = java.util.HashMap<EditText, Boolean>()
    private lateinit var restaurant: Restaurant
    private var postalcity: java.util.LinkedHashMap<String, String>? = null
    private var call_userinfo: Call<JsonObject>? = null
    private var call_deliveryDetails: Call<JsonObject>? = null


    companion object {

        val TAG = "Address"
        var ui_model: UIModel? = null
        fun newInstance(restaurant: Restaurant): Address {
            val fragment = Address()
            val bundle = Bundle()
            bundle.putSerializable(Constants.RESTAURANT, restaurant)
            fragment.arguments = bundle
            return fragment
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
            progress_bar.visibility = View.VISIBLE
            restaurant = arguments!!.getSerializable(Constants.RESTAURANT) as Restaurant
            binding.isPickup = DetailsFragment.isPickup
            binding.executePendingBindings()
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




            if (RestaurantList.ui_model != null) {
                // Add postal code if restaurant list is open anotherwise null
                val list_ = PreferenceUtil.getString(PreferenceUtil.POSTALCITY,"")
                val type = object:TypeToken<ArrayList<Postalcity>>() {}.getType()
                val list : ArrayList<Postalcity>? = Gson().fromJson(list_,type)

                postalcity = java.util.LinkedHashMap<String, String>()
                for (i in 0 until if(list == null ) 0 else list.size) {
                    postalcity!!.put(RestaurantList.ui_model!!.restaurantList.value!!.postal_city[i].postal_code, RestaurantList.ui_model!!.restaurantList.value!!.postal_city[i].city_name)
                }
            }


            /*     postnumber_edt.imeOptions = EditorInfo.IME_ACTION_DONE
                 postnumber_edt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                     override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                         loge(TAG, "post number edit...")
                         moveon_next()
                         return true
                     }

                 })*/
            proceed_view_nxt.setOnClickListener {
                moveon_next()
            }
            change_txt.setOnClickListener {

                if (progress_bar.visibility == View.GONE) {
                    val fragment = SelectAddress.newInstance()
                    var enter: Slide? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        enter = Slide()
                        enter.setDuration(300)
                        enter.slideEdge = Gravity.BOTTOM
                        fragment.enterTransition = enter
                    }
                    (parentFragment as EpayFragment).addFragment(R.id.epay_container, fragment, SelectAddress.TAG, false)
                }
            }
            ui_model = createViewModel()
            checkinfo_restaurant_closed()

        } else {
            logd(TAG, "saveInstance NOT NULL")
        }

    }

    override fun comman_apisuccess(jsonObject: JsonObject, api_tag: String) {
        when(api_tag ){
            Constants.COM_INFO_RESTAURANT_CLOSED->{
                // add tab variables
                val msg= if(jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else ""
                if(jsonObject.has(Constants.IS_DELIVERY_PRESENT) && jsonObject.has(Constants.IS_PICKUP_PRESENT)){
                    DetailsFragment.delivery_present=jsonObject.get(Constants.IS_DELIVERY_PRESENT).asBoolean
                    DetailsFragment.pickup_present=jsonObject.get(Constants.IS_PICKUP_PRESENT).asBoolean
                }
                if((DetailsFragment.isPickup && !DetailsFragment.pickup_present) || (!DetailsFragment.isPickup && !DetailsFragment.delivery_present)){
                    // [pickup(true) && pickuppresent(false) || delivery(true) && deliverypresent (false)]

                    val message=getdeliverymsg_error(jsonObject)
                    DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.black),msg = message ,title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                        override fun onPositiveButtonClick(position: Int) {
                            (parentFragment as EpayFragment).popAllFragment()
                            (parentFragment as EpayFragment).reloadScreen()
                        }
                        override fun onNegativeButtonClick() {
                        }
                    })
                }else{
                    // check if restaurant is closed or not
                    // making restaurant closed equation to satisfy comman function.
                    when(getrestaurantstatus(is_restaurant_closed =jsonObject.get(Constants.IS_RESTAURANT_CLOSED)?.asBoolean, pre_order =jsonObject.get(Constants.PRE_ORDER)?.asBoolean )){

                        RestaurantState.CLOSED ->{
                            any_preorder_closedRestaurant(is_restaurant_closed = true ,pre_order = false,msg =msg ) // set hard code to close restaurant.
                        }
                        else ->{
                            fetchuserInfo()
                        }
                    }
                }

            }

        }

    }

    override fun comman_apifailed(error: String, api_tag: String) {
        when(api_tag ){
            Constants.COM_INFO_RESTAURANT_CLOSED->{
                if(error == getString(R.string.error_404)){
                    showSnackBarIndefinite(address_container, getString(R.string.error_404))
                }else if(error == getString(R.string.internet_not_available)){
                    showSnackBarIndefinite(address_container, getString(R.string.internet_not_available))
                }
            }
        }
    }


    fun moveon_next() {

        if (validationFields()) {

            /**TODO  API CALL LOGIC FROM ADDRESS SCREEN
             * DELIVERY: we are calling "deliveryDetails" api if delivery is select because : user can edit their address and info so we have to submit that information.
             * PICKUP: we are not calling api in this section because user just entered name and phone and it can be proceed.
             * DELIVERY TIME SLOT:
             * - if i use delivery api then i store time slot and pass another screen so i never call api in "delivery time slot" screen if i am coming from "delivery"
             * - if i call api in "delivery time slot" only on one condition to get time, if i am coming from "Pickup"
             */

            if (DetailsFragment.isPickup) {
                //pickup address (on payment)
                EpayFragment.paymentattributes.payment_address = "${restaurant.address} ${restaurant.postal_code}"
                EpayFragment.paymentattributes.first_name = name_edt.text.toString()
                EpayFragment.paymentattributes.telephone_no = telephone_number_edt.text.toString()
                EpayFragment.paymentattributes.upto_min_shipping = "0"
                val fragment = DeliveryTimeslot.newInstance(null)
                (parentFragment as EpayFragment).addFragment(R.id.epay_container, fragment, DeliveryTimeslot.TAG, true)
            } else {
                if (proceed_view_nxt.isEnabled == false) return
                proceed_view_nxt.isEnabled = false
                val userinfomodel = ui_model!!.user_infoList.value!!.user_info
                EpayFragment.paymentattributes.payment_address = "${street_edt.text.trim()},${house_edt.text.trim()},${floor_edt.text.trim()} ${city_edt.text.trim()} ${postnumber_edt.text.trim()}"
                submitdelivery()
            }

        }
    }

    fun onFragmentResult(model: EditAddress.Messages) {
        // backpress from next fragment.
        loge(TAG, "on fragment result---" + model.address_title)
        street_edt.setText(model.street)
        house_edt.setText(model.house_no)
        floor_edt.setText(model.floor_door)
        postnumber_edt.setText(model.postal_code)

    }


    override fun afterTextChanged(s: Editable?) {
        loge(TAG, "after text changed...")

        if (name_edt.text.hashCode() == s!!.hashCode()) {
            name_edt.error = null
            if (name_edt.text.trim().toString().length > 0)
                inputValidStates[name_edt] = true
            else
                inputValidStates[name_edt] = false

        } else if (postnumber_edt.text.hashCode() == s!!.hashCode()) {
            postnumber_edt.error = null

            if (postnumber_edt.text.trim().toString().length > 0) {
                if (postalcity == null) {
                    inputValidStates[postnumber_edt] = false
                } else {
                    inputValidStates[postnumber_edt] = true
                    city_edt.setText(postalcity!!.get(postnumber_edt.text.toString()))
                }
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
        if (DetailsFragment.isPickup) {

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

                if (telephone_number_edt.text.trim().length <= 0) {
                    telephone_number_edt.error = getString(R.string.enter_your_number)
                } else {
                    telephone_number_edt.error = getString(R.string.enter_the_valid_number)
                }
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
                user_infoList.removeObservers(this@Address)
                user_infoList.observe(this@Address, Observer<UserInfoModel> {
                    refreshview()
                })
            }

    private fun refreshview() {

        binding.userInfo = ui_model!!.user_infoList.value!!.user_info
        binding.executePendingBindings()
        val json = Gson().toJson(ui_model!!.user_infoList.value!!.postal_city)
        PreferenceUtil.putValue(PreferenceUtil.POSTALCITY,json).also { PreferenceUtil.save() }
        name_edt.setText(ui_model!!.user_infoList.value!!.user_info.name)
        telephone_number_edt.setText(ui_model!!.user_infoList.value!!.user_info.telephone_no)
        street_edt.setText(ui_model!!.user_infoList.value!!.user_info.street)
        house_edt.setText(ui_model!!.user_infoList.value!!.user_info.house_no)
        floor_edt.setText(ui_model!!.user_infoList.value!!.user_info.floor_door)
        postnumber_edt.setText(ui_model!!.user_infoList.value!!.user_info.postal_code)


    }


    private fun fetchuserInfo() {
        // progresswheel(progresswheel,true)
        progress_bar.visibility = View.VISIBLE
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        } else {
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }
        call_userinfo = ApiCall.userInfo(jsonObject = postParam)
        callAPI(call_userinfo!!, object : BaseFragment.OnApiCallInteraction {

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


                    if(jsonObject.getAsJsonObject(Constants.USER_INFO).has("giftcard_details") && !jsonObject.getAsJsonObject(Constants.USER_INFO).get("giftcard_details").isJsonNull) {

                        if(jsonObject.getAsJsonObject(Constants.USER_INFO).getAsJsonObject("giftcard_details").has("eatmore") && 0 < jsonObject.getAsJsonObject(Constants.USER_INFO).getAsJsonObject("giftcard_details").get("eatmore").asDouble){
                            EpayFragment.paymentattributes.giftcard_details[Constants.EATMORE]=jsonObject.getAsJsonObject(Constants.USER_INFO).getAsJsonObject("giftcard_details").get("eatmore").asString
                        }
                        if(jsonObject.getAsJsonObject(Constants.USER_INFO).getAsJsonObject("giftcard_details").has("restaurant") && 0 < jsonObject.getAsJsonObject(Constants.USER_INFO).getAsJsonObject("giftcard_details").get("restaurant").asDouble){
                            EpayFragment.paymentattributes.giftcard_details[Constants.RESTAURANT]=jsonObject.getAsJsonObject(Constants.USER_INFO).getAsJsonObject("giftcard_details").get("restaurant").asString
                        }
                    }

                    ui_model!!.user_infoList.value = userinfo_model
                    loge(TAG, "data is---" + userinfo_model.user_info.telephone_no + " " + userinfo_model.user_info.name)
                    //  progresswheel(progresswheel,false)
                    // ui_model!!.user_infoList.value!!.user_info.name
                    progress_bar.visibility = View.GONE

                } else {
                    progress_bar.visibility = View.GONE
                  //  empty_view.visibility = View.VISIBLE
                }


            }

            override fun onFail(error: Int) {

                if (call_userinfo!!.isCanceled) {
                    return
                }

                when (error) {
                    404 -> {
                        showSnackBar(address_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(address_container, getString(R.string.internet_not_available))
                    }
                }
                //progresswheel(progresswheel,false)
                progress_bar.visibility = View.GONE

            }
        })
    }


    private fun submitdelivery() {
        //progresswheel(progresswheel,true)
        progress_bar.visibility = View.VISIBLE
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_TOTAL, EpayFragment.paymentattributes.subtotal)
        postParam.addProperty(Constants.SHIPPING, if (DetailsFragment.isPickup) context!!.getString(R.string.pickup_) else context!!.getString(R.string.delivery_))
        postParam.addProperty(Constants.STREET, street_edt.text.toString())
        postParam.addProperty(Constants.HOUSE_NO, house_edt.text.toString())
        postParam.addProperty(Constants.FLOOR_DOOR, floor_edt.text.toString())
        postParam.addProperty(Constants.POSTAL_CODE, postnumber_edt.text.toString())
        postParam.addProperty(Constants.CITY, city_edt.text.toString())
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)

        call_deliveryDetails = ApiCall.deliveryDetails(jsonObject = postParam)
        callAPI(call_deliveryDetails!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    proceed_view_nxt.isEnabled = true
                    //progresswheel(progresswheel,false)
                    progress_bar.visibility = View.GONE

                    //        EpayFragment.paymentattributes.shipping_charge=if(jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString =="") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString
                    val show_msg: Boolean = if (jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHOW_MSG].isJsonNull) false else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHOW_MSG].asBoolean
                    val is_delivery_allowed: Boolean = if (jsonObject.getAsJsonObject(Constants.RESULT)[Constants.IS_DELIVERY_ALLOWED].isJsonNull) true else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.IS_DELIVERY_ALLOWED].asBoolean

                    if (show_msg) {
                        val msg = jsonObject.get(Constants.MSG).asString
                        DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(context!!, R.color.black), msg = msg, title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                            override fun onPositiveButtonClick(position: Int) {
                                if (!is_delivery_allowed) {
                                    (activity as HomeActivity).onBackPressed()
                                } else {
                                    //move next
                                    calculated_arguments(jsonObject)
                                }
                            }

                            override fun onNegativeButtonClick() {
                            }
                        })
                    } else {
                        //move next
                        calculated_arguments(jsonObject)
                    }

                } else {
                    //progresswheel(progresswheel,false)
                    progress_bar.visibility = View.GONE
                    showSnackBar(address_container, getString(R.string.error_404))
                    proceed_view_nxt.isEnabled = true
                }
            }

            override fun onFail(error: Int) {

                if (call_deliveryDetails!!.isCanceled) {
                    return
                }

                when (error) {
                    404 -> {
                        showSnackBar(address_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(address_container, getString(R.string.internet_not_available))
                    }
                }

                proceed_view_nxt.isEnabled = true
                //progresswheel(progresswheel,false)
                progress_bar.visibility = View.GONE
            }
        })
    }


    private fun calculated_arguments(jsonObject: JsonObject) {

        EpayFragment.paymentattributes.first_name = name_edt.text.toString()
        EpayFragment.paymentattributes.telephone_no = telephone_number_edt.text.toString()
        //                        finalCartJson.put("address", street + " " + houseNo + " " + floorDoorString + ", " + postal_code + " " + city);
        EpayFragment.paymentattributes.address = String.format(getString(R.string.fulladdress), street_edt.text.trim().toString(), house_edt.text.trim().toString(), floor_edt.text.trim().toString(), postnumber_edt.text.trim().toString(), city_edt.text.trim().toString())
        EpayFragment.paymentattributes.postal_code = postnumber_edt.text.toString()
        //   EpayActivity.paymentattributes.discount_type=jsonObject.getAsJsonObject(Constants.RESULT)[Constants.DISCOUNT_TYPE].asString
        // EpayActivity.paymentattributes.discount_amount=jsonObject.getAsJsonObject(Constants.RESULT)[Constants.DISCOUNT_AMOUNT].asString
        EpayFragment.paymentattributes.shipping_charge = if (!jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.SHIPPING_CHARGE) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].isJsonNull || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString
        EpayFragment.paymentattributes.upto_min_shipping = if (!jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.UPTO_MIN_SHIPPING) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.UPTO_MIN_SHIPPING].isJsonNull || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.UPTO_MIN_SHIPPING].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.UPTO_MIN_SHIPPING].asString
        EpayFragment.paymentattributes.minimum_order_price = if (!jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.MIN_ORDER_SHIPPING) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.MIN_ORDER_SHIPPING].isJsonNull || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.MIN_ORDER_SHIPPING].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.MIN_ORDER_SHIPPING].asString
        //  EpayActivity.paymentattributes.additional_charges_cash=jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_CASH].asString
        EpayFragment.paymentattributes.additional_charges_online = if (!(jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.ADDITIONAL_CHARGES_ONLINE)) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString
        EpayFragment.paymentattributes.additional_charges_cash = if (!(jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.ADDITIONAL_CHARGES_CASH)) || jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString
        EpayFragment.paymentattributes.additional_charges_giftcard = if (!(jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.ADDITIONAL_CHARGES_GIFTCARD)) || jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_GIFTCARD).asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_GIFTCARD).asString

        EpayFragment.paymentattributes.distance = if (!jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.USER_DISTANCE) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.USER_DISTANCE].isJsonNull || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.USER_DISTANCE].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.USER_DISTANCE].asString
        EpayFragment.paymentattributes.first_time = if (!jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.FIRST_TIME) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.FIRST_TIME].isJsonNull || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.FIRST_TIME].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.FIRST_TIME].asString


        val time_list = LinkedHashMap<String, String>()
        for (i in 0.until(jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).size())) {
            time_list.put((jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.ACTUAL].asString,
                    (jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.DISPLAY].asString)
        }
        val fragment = DeliveryTimeslot.newInstance(time_list)
        (parentFragment as EpayFragment).addFragment(R.id.epay_container, fragment, DeliveryTimeslot.TAG, true)

    }

    private fun getpostal_city(jsonObject: JsonObject): ArrayList<Postalcity> {

        val list = ArrayList<Postalcity>()
        val entrySet = jsonObject.getAsJsonObject(Constants.POSTAL_CITY).entrySet()
        for (entry in entrySet) {
            list.add(Postalcity(postal_code = entry.key ,city_name = jsonObject.getAsJsonObject(Constants.POSTAL_CITY).get(entry.key).asString))
        }
        return list
    }


    fun setToolbarforThis() {

        txt_toolbar.text = getString(R.string.dine_oplysninger)
        txt_toolbar_right_img.apply { visibility = if (DetailsFragment.isPickup) View.GONE else View.VISIBLE; setImageResource(R.drawable.info_outline) }
        img_toolbar_back.setImageResource(R.drawable.back)
        img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
        txt_toolbar_right_img.setOnClickListener {
            CartListFunction.showDialog(context = context!!, restaurant = restaurant)
        }
    }

    /*  fun onBackpress() {
          (activity as EpayActivity).txt_toolbar.text = getString(R.string.basket)
          (activity as EpayActivity).popFragment()

      }*/


    override fun onDestroyView() {

        logd(TAG, "onDestroyView...")
        ui_model?.let {
            ViewModelProviders.of(this).get(UIModel::class.java).user_infoList.removeObservers(this@Address)
        }

        call_deliveryDetails?.let {
            proceed_view_nxt.isEnabled = true
            //progresswheel(progresswheel,false)
            progress_bar.visibility = View.GONE
            it.cancel()
        }

        call_userinfo?.let {
            progress_bar.visibility = View.GONE
            //progresswheel(progresswheel,false)
            it.cancel()
        }

        super.onDestroyView()
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

    data class UserInfoModel(
            val status: Boolean = false,
            val user_info: User_Info,
            val postal_city: ArrayList<Postalcity> = arrayListOf()


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


