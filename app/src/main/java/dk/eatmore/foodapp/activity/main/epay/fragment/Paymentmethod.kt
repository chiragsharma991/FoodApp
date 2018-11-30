package dk.eatmore.foodapp.activity.main.epay.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.*
import android.support.constraint.ConstraintSet
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.cart.fragment.Extratoppings
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Menu
import dk.eatmore.foodapp.adapter.PaymentmethodAdapter
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.databinding.DeliverytimeslotBinding
import dk.eatmore.foodapp.databinding.PaymentmethodBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.deliverytimeslot.*
import kotlinx.android.synthetic.main.paymentmethod.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import kotlinx.android.synthetic.main.transaction_status.*
import retrofit2.Call


class Paymentmethod : BaseFragment(), TextWatcher {

    var transition: Transition? = null
    private lateinit var binding: PaymentmethodBinding
    private var timeslot: ArrayList<String>?=null
    private var selectedtimeslot_position : Int = 0
    lateinit var currentView: String


    // we have two different API so we are passing call in "checkout API" it may be from pickup/delivery :
    private lateinit var checkout_api: Call<JsonObject>


    companion object {
        var isPaymentonline : Boolean =true
        val TAG = "Paymentmethod"



        fun newInstance(): Paymentmethod {
            return Paymentmethod()
        }
    }

    override fun getLayout(): Int {
        return R.layout.paymentmethod
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            isPaymentonline=true
            val myclickhandler=MyClickHandler(this)
            binding.handlers=myclickhandler
            //setanim_toolbartitle(appbar,(activity as EpayActivity).txt_toolbar,getString(R.string.payment))
            applyonlinegift_edt.addTextChangedListener(this)
            applycashgift_edt.addTextChangedListener(this)
            paymentmethod_visible_are()
            setToolbarforThis()
            generateBillDetails(Constants.OTHER)
        }
    }

    override fun afterTextChanged(s: Editable?) {

        error_of_onlinegiftcard.visibility=View.GONE
        error_of_cashgiftcard.visibility=View.GONE
        generateBillDetails(Constants.OTHER)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private fun generateBillDetails(giftcardis : String){

        var final_amount : Double = 0.0   // calculating final amount with included all tax.

        if(EpayActivity.isPickup){
            // pick up:

            subtotal_layout.visibility=View.VISIBLE
            restuptominimum_layout.visibility=if(EpayActivity.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility=View.GONE
            additional_charge_layout.visibility=if(EpayActivity.paymentattributes.additional_charges_cash.toDouble() <= 0.0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility=View.VISIBLE
            subtotal_txt.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.order_total)
            restuptominimum_txt.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.upto_min_shipping)
            additional_charge_txt.text=if(isPaymentonline) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.additional_charges_online) else BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.additional_charges_cash)
            discountcoupan_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.discount_amount.toString()))
            discountgift_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.discount_amount.toString()))

            if(giftcardis ==Constants.GIFTCARD ){
                discountgift_layout.visibility=if(EpayActivity.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountcoupan_layout.visibility=View.GONE
                        final_amount = (
                                 (EpayActivity.paymentattributes.order_total.toDouble()
                                + EpayActivity.paymentattributes.upto_min_shipping.toDouble()
                                + if(isPaymentonline) EpayActivity.paymentattributes.additional_charges_online.toDouble() else EpayActivity.paymentattributes.additional_charges_cash.toDouble())
                        - EpayActivity.paymentattributes.discount_amount)

            }else if(giftcardis ==Constants.COUPON){
                discountcoupan_layout.visibility=if(EpayActivity.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountgift_layout.visibility=View.GONE
                final_amount = (
                                 (EpayActivity.paymentattributes.order_total.toDouble() - EpayActivity.paymentattributes.discount_amount)
                                + EpayActivity.paymentattributes.upto_min_shipping.toDouble()
                                + if(isPaymentonline) EpayActivity.paymentattributes.additional_charges_online.toDouble() else EpayActivity.paymentattributes.additional_charges_cash.toDouble())

            }else{
                discountgift_layout.visibility=View.GONE
                discountcoupan_layout.visibility=View.GONE
                final_amount = (
                                  EpayActivity.paymentattributes.order_total.toDouble()
                                + EpayActivity.paymentattributes.upto_min_shipping.toDouble()
                                + if(isPaymentonline) EpayActivity.paymentattributes.additional_charges_online.toDouble() else EpayActivity.paymentattributes.additional_charges_cash.toDouble())

            }

            total_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f",final_amount))
            online_btn.text = if(final_amount <= 0.0) getString(R.string.confirm) else getString(R.string.pay)

        }

        //--------------------------------------//---------------------------------------------//


        else{
            // delivery :
            subtotal_layout.visibility=View.VISIBLE
            restuptominimum_layout.visibility=if(EpayActivity.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE
            shipping_layout.visibility=if(EpayActivity.paymentattributes.shipping_charge.toDouble() <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility=if(EpayActivity.paymentattributes.additional_charges_online.toDouble() <= 0.0) View.GONE else View.VISIBLE
            total_layout.visibility=View.VISIBLE
            subtotal_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.order_total)
            restuptominimum_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.upto_min_shipping)
            shipping_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.shipping_charge)
            additional_charge_txt.text=if(isPaymentonline) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.additional_charges_online) else BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.additional_charges_cash)
            discountcoupan_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.discount_amount.toString()))
            discountgift_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayActivity.paymentattributes.discount_amount.toString()))


            if(giftcardis ==Constants.GIFTCARD ){
                discountgift_layout.visibility=if(EpayActivity.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountcoupan_layout.visibility=View.GONE
                final_amount = (
                                 (EpayActivity.paymentattributes.order_total.toDouble()
                                + EpayActivity.paymentattributes.upto_min_shipping.toDouble()
                                + EpayActivity.paymentattributes.shipping_charge.toDouble()
                                + if(isPaymentonline) EpayActivity.paymentattributes.additional_charges_online.toDouble() else EpayActivity.paymentattributes.additional_charges_cash.toDouble())
                                - EpayActivity.paymentattributes.discount_amount)

            }else if(giftcardis ==Constants.COUPON){
                discountcoupan_layout.visibility=if(EpayActivity.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountgift_layout.visibility=View.GONE
                final_amount = (
                                 (EpayActivity.paymentattributes.order_total.toDouble() - EpayActivity.paymentattributes.discount_amount)
                                + EpayActivity.paymentattributes.upto_min_shipping.toDouble()
                                + EpayActivity.paymentattributes.shipping_charge.toDouble()
                                + if(isPaymentonline) EpayActivity.paymentattributes.additional_charges_online.toDouble() else EpayActivity.paymentattributes.additional_charges_cash.toDouble())
            }else{
                discountgift_layout.visibility=View.GONE
                discountcoupan_layout.visibility=View.GONE
                final_amount= (
                                  EpayActivity.paymentattributes.order_total.toDouble()
                                + EpayActivity.paymentattributes.upto_min_shipping.toDouble()
                                + EpayActivity.paymentattributes.shipping_charge.toDouble()
                                + if(isPaymentonline) EpayActivity.paymentattributes.additional_charges_online.toDouble() else EpayActivity.paymentattributes.additional_charges_cash.toDouble())
            }

            total_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f",final_amount))
            online_btn.text = if(final_amount <= 0.0) getString(R.string.confirm) else getString(R.string.pay)
        }
    }



    private fun paymentmethod_visible_are(){

        if(isPaymentonline){
            online_method.visibility=View.VISIBLE
            cash_method.visibility=View.GONE
            giftonline_view.visibility=View.GONE
            giftcash_view.visibility=View.GONE
            EpayActivity.paymentattributes.discount_amount=0.0
            EpayActivity.paymentattributes.discount_id=0
            EpayActivity.paymentattributes.discount_type=""
            generateBillDetails(Constants.OTHER)
            progress_applyonlinegift.visibility=View.GONE
            progress_applycashgift.visibility=View.GONE
            error_of_onlinegiftcard.visibility=View.GONE
            error_of_cashgiftcard.visibility=View.GONE
            applycash_txt.visibility=View.VISIBLE
            applyonline_txt.visibility=View.VISIBLE

        }else{
            online_method.visibility=View.GONE
            cash_method.visibility=View.VISIBLE
            giftonline_view.visibility=View.GONE
            giftcash_view.visibility=View.GONE
            EpayActivity.paymentattributes.discount_amount=0.0
            EpayActivity.paymentattributes.discount_id=0
            EpayActivity.paymentattributes.discount_type=""
            generateBillDetails(Constants.OTHER)
            progress_applyonlinegift.visibility=View.GONE
            progress_applycashgift.visibility=View.GONE
            error_of_onlinegiftcard.visibility=View.GONE
            error_of_cashgiftcard.visibility=View.GONE
            applycash_txt.visibility=View.VISIBLE
            applyonline_txt.visibility=View.VISIBLE

        }
    }


    private fun showapplycoupanOnline(){
        if(giftonline_view.visibility == View.VISIBLE)
            giftonline_view.visibility=View.GONE
        else
            giftonline_view.visibility=View.VISIBLE
    }

    private fun showapplycoupanCash(){

        if(giftcash_view.visibility == View.VISIBLE)
            giftcash_view.visibility=View.GONE
        else
            giftcash_view.visibility=View.VISIBLE
    }


    private  fun press_applycoupanOnline(){
        if(applyonlinegift_edt.text.trim().toString().length > 0)
            applygiftcoupan()
    }

    private  fun press_applycoupanCash(){
        if(applycashgift_edt.text.trim().toString().length > 0)
            applygiftcoupan()
    }

    private  fun continuefromOnline(){

        if(!isInternetAvailable()){
            showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))
            return
        }
        if(checkpaymentAttributes() == false){
            showSnackBar(pamentmethod_container, getString(R.string.error_404))
            return
        }
        (activity as EpayActivity).addFragment(R.id.epay_container,BamboraWebfunction.newInstance(),BamboraWebfunction.TAG,true)

    }

    private  fun continuefromCash(){

        if(!isInternetAvailable()){
            showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))
            return
        }
        if(checkpaymentAttributes() == false){
           showSnackBar(pamentmethod_container, getString(R.string.error_404))
           return
        }
        (activity as EpayActivity).addFragment(R.id.epay_container,TransactionStatus.newInstance(),TransactionStatus.TAG,true)

    }


    // check all details are available or not.
    private fun checkpaymentAttributes () : Boolean? {
        var result = false
        val postParam = JsonObject()
        try {
            postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
            postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
            postParam.addProperty(Constants.FIRST_TIME, EpayActivity.paymentattributes.first_time)
            postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,"") )
            // postParam.addProperty(Constants.POSTAL_CODE, EpayActivity.paymentattributes.postal_code)
            postParam.addProperty(Constants.DISCOUNT_TYPE, EpayActivity.paymentattributes.discount_type)
            postParam.addProperty(Constants.DISCOUNT_AMOUNT, EpayActivity.paymentattributes.discount_amount)
            postParam.addProperty(Constants.DISCOUNT_ID,EpayActivity.paymentattributes.discount_id)
            postParam.addProperty(Constants.SHIPPING, if (EpayActivity.isPickup) context!!.getString(R.string.pickup) else context!!.getString(R.string.delivery))
            postParam.addProperty(Constants.TELEPHONE_NO, EpayActivity.paymentattributes.telephone_no)
            postParam.addProperty(Constants.ORDER_TOTAL, EpayActivity.paymentattributes.order_total)
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
            postParam.addProperty(Constants.ACCEPT_TC, "1")
            postParam.addProperty(Constants.PAYMETHOD, if(Paymentmethod.isPaymentonline) "1" else "2" )
            postParam.addProperty(Constants.EXPECTED_TIME, EpayActivity.paymentattributes.expected_time)
            postParam.addProperty(Constants.COMMENTS, EpayActivity.paymentattributes.comments)
            postParam.addProperty(Constants.DEVICE_TYPE,Constants.DEVICE_TYPE_VALUE)
            postParam.addProperty(Constants.FIRST_NAME, EpayActivity.paymentattributes.first_name)
            postParam.addProperty(Constants.ADDITIONAL_CHARGE, if(Paymentmethod.isPaymentonline) EpayActivity.paymentattributes.additional_charges_online else EpayActivity.paymentattributes.additional_charges_cash)
            val jsonarray=JsonArray()
            for (i in 0.until(EpayActivity.selected_op_id.size) ){
                val jsonobject= JsonObject()
                jsonobject.addProperty(Constants.OP_ID, EpayActivity.selected_op_id.get(i))
                jsonarray.add(jsonobject)
            }
            postParam.add(Constants.CARTPRODUCTS,jsonarray )

            if(EpayActivity.isPickup){
                //pickup--
                checkout_api=ApiCall.checkout_pickup(postParam)
                result=true

            }else{
                // delivery--
                postParam.addProperty(Constants.ADDRESS, EpayActivity.paymentattributes.address)
                postParam.addProperty(Constants.POSTAL_CODE, EpayActivity.paymentattributes.postal_code)
                postParam.addProperty(Constants.DISTANCE, EpayActivity.paymentattributes.distance)
                postParam.addProperty(Constants.MINIMUM_ORDER_PRICE, EpayActivity.paymentattributes.minimum_order_price)
                postParam.addProperty(Constants.SHIPPING_COSTS, EpayActivity.paymentattributes.shipping_charge)
                postParam.addProperty(Constants.UPTO_MIN_SHIPPING, EpayActivity.paymentattributes.upto_min_shipping)
                postParam.addProperty(Constants.SHIPPING_REMARK, "")
                postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
                checkout_api= ApiCall.checkout_delivery(postParam)
                result=true
            }



        }catch (error : Exception){
            return result
        }

        return result

    }


    private fun applygiftcoupan() {
        progress_applyonlinegift.visibility=View.VISIBLE
        progress_applycashgift.visibility=View.VISIBLE
        applycash_txt.visibility=View.GONE
        applyonline_txt.visibility=View.GONE

        callAPI(ApiCall.applycode(
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN,"")!!,
                order_total = EpayActivity.paymentattributes.order_total,
                customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!,
                additional_charge = if(isPaymentonline) EpayActivity.paymentattributes.additional_charges_online else EpayActivity.paymentattributes.additional_charges_cash,
                code = if(isPaymentonline)applyonlinegift_edt.text.trim().toString() else applycashgift_edt.text.trim().toString(),
                r_key = PreferenceUtil.getString(PreferenceUtil.R_KEY,"")!!,
                shipping =if(EpayActivity.isPickup) getString(R.string.pickup_caps) else getString(R.string.delivery_caps),
                shipping_costs = EpayActivity.paymentattributes.shipping_charge,
                upto_min_shipping = EpayActivity.paymentattributes.upto_min_shipping



        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if(jsonobject.get(Constants.STATUS).asBoolean){
                    loge(TAG,"status is true")
                    EpayActivity.paymentattributes.discount_type=jsonobject.get(Constants.DISCOUNT_TYPE).asString
                    EpayActivity.paymentattributes.discount_amount=jsonobject.get(Constants.DISCOUNT_AMOUNT).asDouble
                    EpayActivity.paymentattributes.discount_id=jsonobject.get(Constants.DISCOUNT_ID).asInt
                    if(EpayActivity.paymentattributes.discount_type == Constants.GIFTCARD)
                    generateBillDetails(Constants.GIFTCARD)
                    else if(EpayActivity.paymentattributes.discount_type == Constants.COUPON)
                    generateBillDetails(Constants.COUPON)
                    error_of_cashgiftcard.setTextColor(ContextCompat.getColor(context!!,R.color.green))
                    error_of_onlinegiftcard.setTextColor(ContextCompat.getColor(context!!,R.color.green))


                }else{
                    loge(TAG,"status is false")
                    EpayActivity.paymentattributes.discount_amount=0.0
                    EpayActivity.paymentattributes.discount_id=0
                    EpayActivity.paymentattributes.discount_type=""
                    generateBillDetails(Constants.OTHER)
                    error_of_cashgiftcard.setTextColor(ContextCompat.getColor(context!!,R.color.theme_color))
                    error_of_onlinegiftcard.setTextColor(ContextCompat.getColor(context!!,R.color.theme_color))
                }
                // this is progress bar and error text to settext and visible
                progress_applyonlinegift.visibility=View.GONE
                progress_applycashgift.visibility=View.GONE
                applycash_txt.visibility=View.VISIBLE
                applyonline_txt.visibility=View.VISIBLE
                error_of_onlinegiftcard.visibility=View.VISIBLE
                error_of_cashgiftcard.visibility=View.VISIBLE
                error_of_onlinegiftcard.text=jsonobject.get(Constants.MSG).asString
                error_of_cashgiftcard.text=jsonobject.get(Constants.MSG).asString
            }

            override fun onFail(error: Int) {
                progress_applyonlinegift.visibility=View.GONE
                progress_applycashgift.visibility=View.GONE
                applycash_txt.visibility=View.VISIBLE
                applyonline_txt.visibility=View.VISIBLE
                when (error) {
                    404 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))

                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        loge(TAG,"permission result---")
        when (requestCode) {
            1 -> {

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" +"88826543"))
                    startActivity(intent)
                    //    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }





    // set common toolbar from this and set pre fragment toolbar from this.

    fun setToolbarforThis() {
        (activity as EpayActivity).txt_toolbar.text = getString(R.string.payment)
     //   (activity as EpayActivity).img_toolbar_back.setOnClickListener { onBackpress() }
    }

    fun onBackpress() {
        (activity as EpayActivity).popFragment()
    }

    fun onlineTransactionFailed(){
        // in case user do cancel from transction screen.
        loge(TAG,"onlineTransactionFailed")

        callAPI(ApiCall.cancelordertransaction(
                r_key =PreferenceUtil.getString(PreferenceUtil.R_KEY, "")!!,
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN, "")!!,
                order_no = EpayActivity.paymentattributes.order_no

        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
                    loge(TransactionStatus.TAG," ordertransaction is success ")
                    DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!,R.color.black),msg = getString(R.string.transaction_has_been_declined),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                        override fun onPositiveButtonClick(position: Int) {
                            loge(TAG,"ok button---")
                        }
                        override fun onNegativeButtonClick() {
                        }
                    })
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))

                    }
                }
            }
        })




    }

    class  MyClickHandler(val paymentmethod: Paymentmethod) {


        fun onClick(view: View, value :String) {
            when(value){
                "11" ->{
                    // online payment type selection
                    if(!isPaymentonline && paymentmethod.progress_applycashgift.visibility == View.GONE ){
                        isPaymentonline=true
                        paymentmethod.paymentmethod_visible_are()
                    }
                }

                "12" ->{
                    // online gift apply text
                    paymentmethod.showapplycoupanOnline()
                }
                "13" ->{
                    // press apply gift button
                    paymentmethod.press_applycoupanOnline()
                }
                "14" ->{
                    // keep continue button from online
                    paymentmethod.continuefromOnline()
                }




                "21" ->{
                    // cash payment type selection
                    if(isPaymentonline && paymentmethod.progress_applycashgift.visibility == View.GONE){
                        isPaymentonline=false
                        paymentmethod.paymentmethod_visible_are()
                    }

                }

                "22" ->{
                    // cash gift apply text
                    paymentmethod.showapplycoupanCash()
                }
                "23" ->{
                    // press apply gift button from cash
                    paymentmethod.press_applycoupanCash()
                }
                "24" ->{
                    // keep continue button from cash
                    paymentmethod.continuefromCash()
                }

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        logd(Menu.TAG, "on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(Menu.TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(Menu.TAG, "on pause...")

    }

}