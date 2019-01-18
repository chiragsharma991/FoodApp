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
import com.google.gson.JsonObject
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
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_address.*
import kotlinx.android.synthetic.main.infodialog.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call


class Address : BaseFragment(), TextWatcher {

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
            progress_bar.visibility = View.GONE
            restaurant = arguments!!.getSerializable(Constants.RESTAURANT) as Restaurant
            binding.isPickup = EpayFragment.isPickup
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
                loge(AddressForm.TAG, "postal size is-" + RestaurantList.ui_model!!.restaurantList.value!!.postal_city.size.toString())
                postalcity = java.util.LinkedHashMap<String, String>()
                for (i in 0 until RestaurantList.ui_model!!.restaurantList.value!!.postal_city.size) {
                    postalcity!!.put(RestaurantList.ui_model!!.restaurantList.value!!.postal_city[i].postal_code, RestaurantList.ui_model!!.restaurantList.value!!.postal_city[i].city_name)
                }
            }

            postnumber_edt.imeOptions = EditorInfo.IME_ACTION_DONE
            postnumber_edt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    loge(TAG, "post number edit...")
                    moveon_next()
                    return true
                }

            })
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
            if (ui_model!!.user_infoList.value == null) {
                fetchuserInfo()
            } else {
                refreshview()
            }

        } else {
            logd(TAG, "saveInstance NOT NULL")
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

            if (EpayFragment.isPickup) {
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
                EpayFragment.paymentattributes.payment_address = "${userinfomodel.street},${userinfomodel.house_no},${userinfomodel.floor_door} ${userinfomodel.city} ${userinfomodel.postal_code}"
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
        if (EpayFragment.isPickup) {

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
                user_infoList.removeObservers(this@Address)
                user_infoList.observe(this@Address, Observer<UserInfoModel> {
                    refreshview()
                })
            }

    private fun refreshview() {

        binding.userInfo = ui_model!!.user_infoList.value!!.user_info
        binding.executePendingBindings()
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
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)
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

                    ui_model!!.user_infoList.value = userinfo_model
                    loge(TAG, "data is---" + userinfo_model.user_info.telephone_no + " " + userinfo_model.user_info.name)
                    //  progresswheel(progresswheel,false)
                    // ui_model!!.user_infoList.value!!.user_info.name
                    progress_bar.visibility = View.GONE

                } else {
                    progress_bar.visibility = View.GONE
                    empty_view.visibility = View.VISIBLE
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
        postParam.addProperty(Constants.ORDER_TOTAL, EpayFragment.paymentattributes.order_total)
        postParam.addProperty(Constants.SHIPPING, if (EpayFragment.isPickup) getString(R.string.pickup) else getString(R.string.delivery))
        postParam.addProperty(Constants.STREET, street_edt.text.toString())
        postParam.addProperty(Constants.HOUSE_NO, house_edt.text.toString())
        postParam.addProperty(Constants.FLOOR_DOOR, floor_edt.text.toString())
        postParam.addProperty(Constants.POSTAL_CODE, postnumber_edt.text.toString())
        postParam.addProperty(Constants.CITY, city_edt.text.toString())
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)

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
        EpayFragment.paymentattributes.shipping_charge = if (jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.SHIPPING_CHARGE].asString
        EpayFragment.paymentattributes.upto_min_shipping = if (jsonObject.getAsJsonObject(Constants.RESULT)[Constants.UPTO_MIN_SHIPPING].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.UPTO_MIN_SHIPPING].asString
        EpayFragment.paymentattributes.minimum_order_price = if (jsonObject.getAsJsonObject(Constants.RESULT)[Constants.MIN_ORDER_SHIPPING].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.MIN_ORDER_SHIPPING].asString
        //  EpayActivity.paymentattributes.additional_charges_cash=jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_CASH].asString
        EpayFragment.paymentattributes.additional_charges_online = if (!(jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.ADDITIONAL_CHARGES_ONLINE)) || jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.ADDITIONAL_CHARGES_ONLINE].asString
        EpayFragment.paymentattributes.additional_charges_cash = if (!(jsonObject.getAsJsonObject(Constants.RESULT).has(Constants.ADDITIONAL_CHARGES_CASH)) || jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT).get(Constants.ADDITIONAL_CHARGES_CASH).asString

        EpayFragment.paymentattributes.distance = if (jsonObject.getAsJsonObject(Constants.RESULT)[Constants.USER_DISTANCE].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.USER_DISTANCE].asString
        EpayFragment.paymentattributes.first_time = if (jsonObject.getAsJsonObject(Constants.RESULT)[Constants.FIRST_TIME].asString == "") "0" else jsonObject.getAsJsonObject(Constants.RESULT)[Constants.FIRST_TIME].asString


        val time_list = LinkedHashMap<String, String>()
        for (i in 0.until(jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).size())) {
            time_list.put((jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.ACTUAL].asString,
                    (jsonObject.getAsJsonObject(Constants.RESULT).getAsJsonArray(Constants.TIME_LIST).get(i) as JsonObject)[Constants.DISPLAY].asString)
        }
        val fragment = DeliveryTimeslot.newInstance(time_list)
        (parentFragment as EpayFragment).addFragment(R.id.epay_container, fragment, DeliveryTimeslot.TAG, true)

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

        txt_toolbar.text = getString(R.string.address)
        txt_toolbar_right_img.apply { visibility = if (EpayFragment.isPickup) View.GONE else View.VISIBLE; setImageResource(R.drawable.info_outline) }
        img_toolbar_back.setImageResource(R.drawable.back)
        img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
        txt_toolbar_right_img.setOnClickListener {
            showDialog(context = context!!, restaurant = restaurant)
        }
    }

    /*  fun onBackpress() {
          (activity as EpayActivity).txt_toolbar.text = getString(R.string.basket)
          (activity as EpayActivity).popFragment()

      }*/

    fun showDialog(restaurant: Restaurant, context: Context) {
        val dialog = Dialog(context, R.style.AppCompatAlertDialogStyle_Transparent)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.infodialog)

        val shippinginfo_container = dialog.shippinginfo_container as LinearLayout

        try {

            if (!(restaurant.shipping_charges.size > 0)) {
                // show empty
                return
            }
            shippinginfo_container.removeAllViewsInLayout()
            if (restaurant.shipping_type == "by_distance") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Fra (km)", "Til (km)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    else if (i == 2)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    //  textView1.typeface= Typeface.DEFAULT_BOLD
                    //  textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                shippinginfo_container.addView(parent)
                val view = View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin = 8
                view.alpha = 0.3f
                view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                view.layoutParams = vparms
                shippinginfo_container.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        val parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].from_pd)
                        else if (j == 1)
                            textView1.text = if (restaurant.shipping_charges[i].to_pd == null) Constants.OPEFTER else BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].to_pd!!)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.START
                        else if (j == 2)
                            textView1.gravity = Gravity.START
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.Subtitle_TextViewSmall)
                        // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    shippinginfo_container.addView(parent)
                    val view = View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin = 8
                    view.alpha = 0.3f
                    view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                    view.layoutParams = vparms
                    shippinginfo_container.addView(view)


                }


            } else if (restaurant.shipping_type == "by_postal") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Postnr.", "Min. (kr.)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    else if (i == 2)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    //  textView1.typeface= Typeface.DEFAULT_BOLD
                    //  textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                shippinginfo_container.addView(parent)
                val view = View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin = 8
                view.alpha = 0.3f
                view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                view.layoutParams = vparms
                shippinginfo_container.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    var parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = restaurant.shipping_charges[i].postal_code
                        else if (j == 1)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].minimum_order_price)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.START
                        else if (j == 2)
                            textView1.gravity = Gravity.START
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.Subtitle_TextViewSmall)
                        // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    shippinginfo_container.addView(parent)
                    val view = View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin = 8
                    view.alpha = 0.3f
                    view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                    view.layoutParams = vparms
                    shippinginfo_container.addView(view)


                }


            } else if (restaurant.shipping_type == "by_order_price") {

                var parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL

                val headerlist = arrayListOf("Fra (Pris)", "Til (Pris.)", "Pris (kr.)")
                // Add header
                for (i in 0..2) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 8
                    else if (i == 2)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms


                    textView1.text = headerlist[i]
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    else if (i == 2)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    //  textView1.typeface= Typeface.DEFAULT_BOLD
                    //  textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                    parent.addView(textView1)

                }

                shippinginfo_container.addView(parent)
                val view = View(context)
                val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                vparms.topMargin = 8
                view.alpha = 0.3f
                view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                view.layoutParams = vparms
                shippinginfo_container.addView(view)


                // Add values
                for (i in 0 until restaurant.shipping_charges.size) {
                    parent = LinearLayout(context)
                    var parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.topMargin = 8
                    parent.layoutParams = parms
                    parent.orientation = LinearLayout.HORIZONTAL

                    // add row
                    for (j in 0..2) {
                        // add column
                        // Add textview 1
                        val textView1 = AppCompatTextView(context)
                        parms = LinearLayout.LayoutParams(0,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        parms.weight = 1f
                        if (j == 0)
                            parms.rightMargin = 8
                        else if (j == 1)
                            parms.rightMargin = 8
                        else if (j == 2)
                            parms.rightMargin = 0
                        textView1.layoutParams = parms

                        if (j == 0)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].from_pd)
                        else if (j == 1)
                            textView1.text = if (restaurant.shipping_charges[i].to_pd == null) Constants.OPEFTER else BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].to_pd!!)
                        else if (j == 2)
                            textView1.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges[i].price)

                        if (j == 0)
                            textView1.gravity = Gravity.START
                        else if (j == 1)
                            textView1.gravity = Gravity.START
                        else if (j == 2)
                            textView1.gravity = Gravity.START
                        textView1.setSingleLine(true)
                        textView1.setTextAppearance(context, R.style.Subtitle_TextViewSmall)
                        // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB

                        parent.addView(textView1)

                    }
                    shippinginfo_container.addView(parent)
                    val view = View(context)
                    val vparms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    vparms.topMargin = 8
                    view.alpha = 0.3f
                    view.background = ContextCompat.getDrawable(context!!, R.color.divider_color)
                    view.layoutParams = vparms
                    shippinginfo_container.addView(view)


                }


            } else if (restaurant.shipping_type == "flat_rate") {

                val parent = LinearLayout(context)
                val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                parms.topMargin = 8
                parent.layoutParams = parms
                parent.orientation = LinearLayout.HORIZONTAL
                val headerlist = arrayListOf("Pris (Kr.)", BindDataUtils.convertCurrencyToDanishWithoutLabel(restaurant.shipping_charges.get(0).price))
                // Add header
                for (i in 0..1) {

                    // Add textview 1
                    val textView1 = AppCompatTextView(context)
                    val parms = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                    parms.weight = 1f
                    if (i == 0)
                        parms.rightMargin = 8
                    else if (i == 1)
                        parms.rightMargin = 0
                    textView1.layoutParams = parms
                    textView1.text = headerlist[i]
                    textView1.typeface = Typeface.DEFAULT_BOLD
                    if (i == 0)
                        textView1.gravity = Gravity.START
                    else if (i == 1)
                        textView1.gravity = Gravity.START
                    textView1.setSingleLine(true)
                    textView1.setTextAppearance(context, R.style.SubtitleMidium_TextViewSmall)
                    // textView1.typeface= Typeface.DEFAULT_BOLD
                    // textView1.setTextColor(ContextCompat.getColor(context!!, R.color.black_light)) // hex color 0xAARRGGBB
                    parent.addView(textView1)
                }

                shippinginfo_container.addView(parent)


            }


        } catch (e: Exception) {
            Log.e("exception", e.message.toString())
        }

        dialog.show()

    }


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


