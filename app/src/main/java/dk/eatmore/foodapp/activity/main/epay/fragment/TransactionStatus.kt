package dk.eatmore.foodapp.activity.main.epay.fragment


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.*
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Menu
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.databinding.TransactionStatusBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.epay.ResultItem
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.raw_giftdiscount.view.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import kotlinx.android.synthetic.main.transaction_status.*
import retrofit2.Call


class TransactionStatus : CommanAPI() {


    var transition: Transition? = null
    private var timeslot: ArrayList<String>? = null
    private var selectedtimeslot_position: Int = 0
    lateinit var currentView: String  // check this view for block the user interaction on payment page if data is loding.
    private lateinit var binding: TransactionStatusBinding
    // we have two different API so we are passing call in "checkout API" it may be from pickup/delivery :
    private lateinit var checkout_api: Call<JsonObject>
    private var call_check_order: Call<JsonObject>? = null
    private var call_favorite: Call<JsonObject>? = null
    private val timeoutHandler = Handler()
    private var finalizer: Runnable? = null
    private var addedDiscount_id : String=""
    private var addedProductlist: ArrayList<ResultItem> = arrayListOf()
    private var appliedgift_list: ArrayList<Paymentmethod.AppliedGiftModel> = ArrayList()
    private var addedDiscount_amount = 0.0
    private var addedDiscount_type = ""



    companion object {
        val TAG = "TransactionStatus"
        //  var moveonsearch = false


        fun newInstance(addedProductlist: ArrayList<ResultItem>,addedDiscount_amount : Double,addedDiscount_type : String , addedDiscount_id : String , appliedgift_list: ArrayList<Paymentmethod.AppliedGiftModel>): TransactionStatus {

            val fragment = TransactionStatus()
            val bundle =Bundle()
            bundle.putSerializable("addedProductlist",addedProductlist)
            bundle.putSerializable("addedDiscount_amount", addedDiscount_amount)
            bundle.putSerializable("addedDiscount_type", addedDiscount_type)
            bundle.putSerializable("addedDiscount_id", addedDiscount_id)
            bundle.putSerializable("appliedgift_list", appliedgift_list)
            fragment.arguments=bundle
            return fragment
        }
    }

    override fun getLayout(): Int {
        return R.layout.transaction_status
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            val myclickhandler = MyClickHandler(this)
            addedProductlist=arguments!!.getSerializable("addedProductlist") as ArrayList<ResultItem>
            addedDiscount_amount=arguments!!.getSerializable("addedDiscount_amount") as Double
            addedDiscount_type=arguments!!.getSerializable("addedDiscount_type") as String
            appliedgift_list=arguments!!.getSerializable("appliedgift_list") as ArrayList<Paymentmethod.AppliedGiftModel>
            addedDiscount_id=arguments!!.getSerializable("addedDiscount_id") as String


            loge(TAG,"check list--"+addedProductlist.size+"--"+addedDiscount_amount+"--"+addedDiscount_type)
            binding.statusIs = false
            binding.transactionhandler = myclickhandler
            setToolbarforThis()
            currentView = Constants.PROGRESSDIALOG


            Handler().postDelayed({
                if (Paymentmethod.whatisthePaymethod == Paymentmethod.WhatIsThePaymethod.ONLINE) {
                    // if amount is lees then 0 -> direct status page
                    // if amount is greater -> call transaction api
                    statusfrom_online()
                } else {
                    //  checkout api already called so direct move on -> status page.
                    statusfrom_cash()
                }
                showproductInfo(addedProductlist,addedDiscount_amount,addedDiscount_type)

            }, 1200)

        }
    }

    private fun statusfrom_cash() {

        currentView = Constants.PAYMENTSTATUS
        binding.statusIs = true
        setdelivery_info()
        /*  lottie_transaction_status.visibility = View.VISIBLE
          lottie_transaction_status.scale = 0.4f
          status_view.visibility = View.INVISIBLE
          lottie_transaction_status.playAnimation()

          totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
          request_status.text = String.format(getString(R.string.request_successful), getString(R.string.successful))
          requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
          order_number.text = String.format(getString(R.string.order_number), EpayFragment.paymentattributes.order_no)*/

        val intent = Intent(Constants.CARTCOUNT_BROADCAST)
        intent.putExtra(Constants.CARTCNT, 0)
        intent.putExtra(Constants.CARTAMT, "00.00")
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

        val v: Vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        else v.vibrate(200);
        check_order()
    }

    private fun statusfrom_online() {

/*
        if (EpayFragment.paymentattributes.final_amount <= 0.0) {
            // if final amout is less then 0 so epay is not procees and direct get success.
            currentView = Constants.PAYMENTSTATUS
            binding.statusIs = true
            setdelivery_info()

*/
/*            lottie_transaction_status.visibility = View.VISIBLE
            lottie_transaction_status.scale = 0.4f
            status_view.visibility = View.INVISIBLE
            lottie_transaction_status.playAnimation()
            totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
            request_status.text = String.format(getString(R.string.request_successful), getString(R.string.successful))
            requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
            order_number.text = String.format(getString(R.string.order_number), EpayFragment.paymentattributes.order_no)*//*

            val intent = Intent(Constants.CARTCOUNT_BROADCAST)
            intent.putExtra(Constants.CARTCNT, 0)
            intent.putExtra(Constants.CARTAMT, "00.00")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            check_order()
            return
        }
*/


        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.ORDER_NO, EpayFragment.paymentattributes.order_no)
        postParam.addProperty(Constants.CARDNO, EpayFragment.paymentattributes.cardno)
        postParam.addProperty(Constants.TXNID, EpayFragment.paymentattributes.txnid)
        postParam.addProperty(Constants.TXNFEE, EpayFragment.paymentattributes.txnfee)
        postParam.addProperty(Constants.PAYMENTTYPE, EpayFragment.paymentattributes.paymenttype)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        val jsonarray = JsonArray()

        for (i in 0.until(EpayFragment.selected_op_id.size)) {
            val jsonobject = JsonObject()
            jsonobject.addProperty(Constants.OP_ID, EpayFragment.selected_op_id.get(i))
            jsonarray.add(jsonobject)
        }
        postParam.add(Constants.CARTPRODUCTS, jsonarray)

        callAPI(ApiCall.ordertransaction(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                currentView = Constants.PAYMENTSTATUS
                binding.statusIs = true
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
                    setdelivery_info()
                    /*     lottie_transaction_status.visibility = View.VISIBLE
                         lottie_transaction_status.scale = 0.4f
                         status_view.visibility = View.INVISIBLE
                         lottie_transaction_status.playAnimation()
                         totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
                         request_status.text = String.format(getString(R.string.request_successful), getString(R.string.successful))
                         requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
                         order_number.text = String.format(getString(R.string.order_number), EpayFragment.paymentattributes.order_no)*/
                    val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                    intent.putExtra(Constants.CARTCNT, 0)
                    intent.putExtra(Constants.CARTAMT, "00.00")
                    LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
                    check_order()
                } else {

                    // setdelivery_info()
                    //*TODO : api failed case

                }
                val v: Vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                else v.vibrate(200);
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(transaction_constraint, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(transaction_constraint, getString(R.string.internet_not_available))

                    }
                }
                //*TODO Api fail condition

                currentView = Constants.PAYMENTSTATUS
                binding.statusIs = true
                /*   lottie_transaction_status.visibility = View.GONE
                   lottie_transaction_status.scale = 0.4f
                   status_view.visibility = View.VISIBLE
                   status_icon.setImageResource(R.drawable.animated_vector_cross)
                   (status_icon.getDrawable() as Animatable).start()
                   totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
                   request_status.text = String.format(getString(R.string.request_successful), getString(R.string.failed))
                   requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
                   order_number.text = getString(R.string.na)*/
            }
        })


    }

    private fun check_order() {

        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.ORDER_NO, EpayFragment.paymentattributes.order_no)
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        call_check_order = ApiCall.check_order(jsonObject = postParam)

        callAPI(call_check_order!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                // check_order api responce
                var accept_reject_time=""
                var pickup_delivery_time=""
                var reject_reason=""
                val order_status = jsonObject.get(Constants.ORDER_STATUS).asString
                val payment_status = jsonObject.get(Constants.PAYMENT_STATUS).asString
                if(jsonObject.has(Constants.ACCEPT_REJECT_TIME)){
                    accept_reject_time = if (jsonObject.get(Constants.ACCEPT_REJECT_TIME).isJsonNull) "" else jsonObject.get(Constants.ACCEPT_REJECT_TIME).asString
                }
                if(jsonObject.has(Constants.PICKUP_DELIVERY_TIME)){
                    pickup_delivery_time = if (jsonObject.get(Constants.PICKUP_DELIVERY_TIME).isJsonNull) "" else jsonObject.get(Constants.PICKUP_DELIVERY_TIME).asString
                }
                if(jsonObject.has(Constants.REJECT_REASON)){
                    reject_reason = if(jsonObject.get(Constants.REJECT_REASON).isJsonNull) "" else jsonObject.get(Constants.REJECT_REASON).asString
                }

                if (call_check_order != null) {

                    if (order_status.toLowerCase() == Constants.PENDING_RESTAURANT || order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT) {
                        confirm_time_txt.visibility = View.VISIBLE
                    } else {
                        confirm_time_txt.visibility = View.GONE
                    }

                    order_progress_text.setTextColor(ContextCompat.getColor(context!!,R.color.black_txt_regular))

                    if (order_status.toLowerCase() == Constants.PENDING_RESTAURANT || order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT) {
                        // Processing view
                        order_progress_text.text = "Ordre under behandling"
                        transaction_progress_bar.visibility = View.VISIBLE

                    } else {
                        // status view
                        transaction_progress_bar.visibility = View.GONE
                        if (payment_status.toLowerCase() == Constants.REFUNDED) {
                            order_progress_text.text = "Ordre er refunderet"
                            order_accepted_time.text = "Bare rolig, har du benyttet et betalingskort, så er betalingen allerede refunderet."

                        } else {
                            if (order_status.toLowerCase() == Constants.REJECTED) {
                                order_progress_text.text = "Ordre annulleret af restauranten, årsag: ${reject_reason}"
                                order_progress_text.setTextColor(ContextCompat.getColor(context!!,R.color.theme_color))
                                order_accepted_time.text = "Bare rolig, har du benyttet et betalingskort, så er betalingen allerede annulleret. Du kan nu lave en ny bestilling."

                            } else if (order_status.toLowerCase() == Constants.ACCEPTED) {
                                order_progress_text.text = "Ordre accepteret til"
                                if (pickup_delivery_time.length > 0) {
                                    order_accepted_time.visibility = View.VISIBLE
                                    //order_accepted_time.text = pickup_delivery_time
                                    order_accepted_time.text = String.format(getString(R.string.order_accept_date),BindDataUtils.parsewithoutTimeToddMMyyyy(pickup_delivery_time),BindDataUtils.parseTimeToHHmm(pickup_delivery_time))
                                }else{
                                    order_accepted_time.visibility = View.GONE
                                }
                            }
                        }
                    }
                }

                // can i call again?

                if ((order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT) || (order_status.toLowerCase() == Constants.PENDING_RESTAURANT) || (order_status.toLowerCase() == Constants.ACCEPTED && payment_status.toLowerCase() != Constants.REFUNDED)) {
                    // call

                    // we are continuous running because if manager wrong press--- he can change status.
                    finalizer = object : Runnable {
                        override fun run() {
                            loge(TAG, "Handler-----")
                            check_order()

                        }
                    }
                    timeoutHandler.postDelayed(finalizer, 5 * 1000)
                } else {
                    // stop calling api
                    // payment has been refunded---

                }

            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        // showSnackBar(containerview, getString(R.string.error_404))
                     //   loge(TAG,getString(R.string.error_404))

                    }
                    100 -> {
                        // showSnackBar(containerview, getString(R.string.internet_not_available))
                      //  loge(TAG,getString(R.string.internet_not_available))
                    }
                }
            }
        })

    }


    fun setdelivery_info() {

        transaction_progress_bar.visibility = View.VISIBLE
        transaction_progress_bar.loadUrl("file:///android_asset/sandclock.svg")
        confirm_time_txt.visibility = View.VISIBLE
        order_accepted_time.visibility = View.GONE
        order_progress_text.text = "Ordre under behandling"
        thank_you_txt.text = String.format(getString(R.string.tak_for_din), PreferenceUtil.getString(PreferenceUtil.E_MAIL, ""))
        pickupdelivery_address.visibility = View.GONE
        expected_time.text = String.format(getString(R.string.kl_hh_mm), BindDataUtils.parseTimeToHHmm(EpayFragment.paymentattributes.expected_time))
        order_date.text = getcurrentdate()
        order_number.text = String.format(getString(R.string.order_no),EpayFragment.paymentattributes.order_no.toString())
        onlin_offline_txt.text = if (Paymentmethod.whatisthePaymethod == Paymentmethod.WhatIsThePaymethod.ONLINE) getString(R.string.online) else getString(R.string.kontant_betaling)
        restaurant_fulladdress.text = String.format(getString(R.string.restaurant_full_address), EpayFragment.paymentattributes.restaurant_name, EpayFragment.paymentattributes.restaurant_address)
        addspantext()


        if (DetailsFragment.isPickup) {
            // pickup
            rest_pickupdelivery_txt.text = getString(R.string.for_hent_selv)
            pickupdelivery_txt.text = String.format(getString(R.string.delivery_wid_expectedtime), getString(R.string.for_hent_selv), BindDataUtils.parseTimeToHHmm(EpayFragment.paymentattributes.expected_time))
            rest_pickupdelivery_icon.setImageResource(if (DetailsFragment.isPickup) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_icon.setImageResource(if (DetailsFragment.isPickup) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_address.visibility = View.GONE
            customer_name.visibility = View.GONE
            ImageLoader.loadImageRoundCornerFromUrl(context = context!!,cornerSize = 32,fromFile = EpayFragment.paymentattributes.restaurant_appicon,imageView = restaurant_img)
            restaurant_name.text = EpayFragment.paymentattributes.restaurant_name
            restaurant_contact.text = EpayFragment.paymentattributes.restaurant_phone
            restaurant_address.text = EpayFragment.paymentattributes.restaurant_address

        } else {
            // delivery
            rest_pickupdelivery_txt.text = getString(R.string.til_levering)
            pickupdelivery_txt.text = String.format(getString(R.string.delivery_wid_expectedtime), getString(R.string.til_levering),BindDataUtils.parseTimeToHHmm(EpayFragment.paymentattributes.expected_time))
            rest_pickupdelivery_icon.setImageResource(if (DetailsFragment.isPickup) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_icon.setImageResource(if (DetailsFragment.isPickup) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_address.visibility = View.VISIBLE
            pickupdelivery_address.text = EpayFragment.paymentattributes.payment_address
            customer_name.text = EpayFragment.paymentattributes.first_name
            customer_name.visibility = View.VISIBLE
            ImageLoader.loadImageRoundCornerFromUrl(context = context!!,cornerSize = 32,fromFile = EpayFragment.paymentattributes.restaurant_appicon,imageView = restaurant_img)
            restaurant_name.text = EpayFragment.paymentattributes.restaurant_name
            restaurant_contact.text = EpayFragment.paymentattributes.restaurant_phone
            restaurant_address.text = EpayFragment.paymentattributes.restaurant_address
        }

    }


/*
    private fun generateBillDetails(giftcardis: String) {

        var final_amount: Double = 0.0   // calculating final amount with included all tax.

        if (DetailsFragment.isPickup) {
            // pick up:
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility = View.GONE
            additional_charge_layout.visibility = if (EpayFragment.paymentattributes.additional_charges_cash.toDouble() <= 0.0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility = View.VISIBLE
            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.subtotal)
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            additional_charge_txt.text =BindDataUtils.convertCurrencyToDanishWithoutLabel(getAdditionalCharge(Paymentmethod.whatisthePaymethod!!))
            discountcoupan_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))
            discountgift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))

            if (giftcardis == Constants.GIFTCARD) {
                discountgift_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.subtotal.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble()
                                - EpayFragment.paymentattributes.discount_amount))

            } else if (giftcardis == Constants.COUPON) {
                discountcoupan_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountgift_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.subtotal.toDouble() - EpayFragment.paymentattributes.discount_amount)
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble())

            } else {
                discountgift_layout.visibility = View.GONE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        EpayFragment.paymentattributes.subtotal.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble())

            }

            total_txt.text = String.format(getString(R.string.dkk_price),BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f", final_amount)))

        }

        //--------------------------------------//---------------------------------------------//


        else {
            // delivery :
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE
            shipping_layout.visibility = if (EpayFragment.paymentattributes.shipping_charge.toDouble() <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility = if (EpayFragment.paymentattributes.additional_charges_online.toDouble() <= 0.0) View.GONE else View.VISIBLE
            total_layout.visibility = View.VISIBLE
            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.subtotal)
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            shipping_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.shipping_charge)
            additional_charge_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(getAdditionalCharge(Paymentmethod.whatisthePaymethod!!))
            discountcoupan_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))
            discountgift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))


            if (giftcardis == Constants.GIFTCARD) {
                discountgift_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.subtotal.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble())
                                - EpayFragment.paymentattributes.discount_amount)

            } else if (giftcardis == Constants.COUPON) {
                discountcoupan_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountgift_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.subtotal.toDouble() - EpayFragment.paymentattributes.discount_amount)
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble())
            } else {
                discountgift_layout.visibility = View.GONE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        EpayFragment.paymentattributes.subtotal.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble())
            }

            total_txt.text = String.format(getString(R.string.dkk_price),BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f", final_amount)))

        }


    }
*/


/*
    private fun showproductInfo() {

        favorite_btn.text=if(EpayFragment.paymentattributes.is_fav)getString(R.string.markere_som_favorit) else getString(R.string.fjern_favorit)

        if (EpayFragment.ui_model!!.viewcard_list.value == null) {
            // this condition will null if all item has been deleted : so just clear view and inflate empty view on screen.
            add_parentitem_view.removeAllViewsInLayout()
            add_parentitem_view.invalidate()
            generateBillDetails(if (EpayFragment.paymentattributes.discount_type == Constants.GIFTCARD) Constants.GIFTCARD else if (EpayFragment.paymentattributes.discount_type == Constants.COUPON) Constants.COUPON else Constants.OTHER)
            return
        }

        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until EpayFragment.ui_model!!.viewcard_list.value!!.result!!.size) {
            var inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dynamic_raw_item, null)
            view.remove_item.tag = i
            view.remove_item.visibility = View.GONE
            view.item_name.text = String.format(getString(R.string.qty_n_price),EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].quantity,EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].product_name)
            view.item_price.text = if (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price != null) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price!!) else "null"
            view.add_subitem_view.removeAllViewsInLayout()

            // fill first ingredients size if not null
            for (j in 0 until (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients?.size
                    ?: 0)) {
                inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val ingredientview = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                ingredientview.subitem_name.text = String.format(getString(R.string.minues), EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients!!.get(j).ingredient_name)
                ingredientview.subitem_name.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                ingredientview.subitem_price.visibility = View.INVISIBLE
                ingredientview.dummy_image.visibility = View.GONE
                view.add_subitem_view.addView(ingredientview)
            }

            // if attribute is present then fetch extratoppings only from attribute list
            if (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].is_attributes != null && EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].is_attributes.equals("1")) {
                if (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes != null) {
                    for (k in 0 until EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.size) {

                        // attribute_value_name = AB
                        inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val attribute_value_name= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                        attribute_value_name.subitem_name.text=EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).attribute_value_name
                        attribute_value_name.subitem_price.visibility=View.INVISIBLE
                        attribute_value_name.dummy_image.visibility= View.GONE
                        view.add_subitem_view.addView(attribute_value_name)


                        for (l in 0 until (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!![k].order_product_extra_topping_group?.size
                                ?: 0)) {
                            inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                            extratoppings.subitem_name.text = String.format(getString(R.string.plus), EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                            extratoppings.subitem_price.visibility=View.INVISIBLE
                          //  extratoppings.subitem_price.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
                            extratoppings.dummy_image.visibility = View.GONE
                            view.add_subitem_view.addView(extratoppings)
                        }
                    }
                }
            } else {
                // if extratopping group only present then add only extratoppings in the list.
                for (k in 0 until (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group?.size
                        ?: 0)) {
                    inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val onlyextratoppings = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                    onlyextratoppings.subitem_name.text = String.format(getString(R.string.plus), EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).ingredient_name)
                    // view.subitem_price.visibility=View.VISIBLE
                    onlyextratoppings.subitem_price.visibility=View.INVISIBLE
                    //onlyextratoppings.subitem_price.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                    onlyextratoppings.dummy_image.visibility = View.GONE
                    view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
        }
        generateBillDetails(if (EpayFragment.paymentattributes.discount_type == Constants.GIFTCARD) Constants.GIFTCARD else if (EpayFragment.paymentattributes.discount_type == Constants.COUPON) Constants.COUPON else Constants.OTHER)

    }
*/


    fun showproductInfo (list: ArrayList<ResultItem>, discount_amount: Double, discount_type: String) {

        loge(TAG,"showproductInfo--"+discount_amount+" type -"+discount_type)

        var subtotal = 0.0
        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until list.size) {
            var inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dynamic_raw_item, null)
            view.remove_item.tag = i
            view.remove_item.visibility = View.GONE
            view.item_name.text = String.format(getString(R.string.qty_n_price), list[i].quantity, list[i].product_name)
            view.item_price.text = if (list[i].p_price != null) BindDataUtils.convertCurrencyToDanishWithoutLabel(list[i].p_price!!) else "null"
            view.add_subitem_view.removeAllViewsInLayout()

            subtotal += BindDataUtils.reformatIntodecimal(list[i].p_price!!)

            // fill first ingredients size if not null
            for (j in 0 until (list[i].removed_ingredients?.size ?: 0)) {
                inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val ingredientview = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                ingredientview.subitem_name.text = String.format(getString(R.string.minues), list[i].removed_ingredients!!.get(j).ingredient_name)
                ingredientview.subitem_name.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                ingredientview.subitem_price.visibility = View.INVISIBLE
                ingredientview.dummy_image.visibility = View.GONE
                view.add_subitem_view.addView(ingredientview)
            }

            // if attribute is present then fetch extratoppings only from attribute list
            if (list[i].is_attributes != null && list[i].is_attributes.equals("1")) {
                if (list[i].ordered_product_attributes != null) {
                    for (k in 0 until list[i].ordered_product_attributes!!.size) {

                        // attribute_value_name = AB
                        inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val attribute_value_name = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                        attribute_value_name.subitem_name.text = list[i].ordered_product_attributes!!.get(k).attribute_value_name
                        attribute_value_name.subitem_price.visibility = View.INVISIBLE
                        attribute_value_name.dummy_image.visibility = View.GONE
                        view.add_subitem_view.addView(attribute_value_name)



                        for (l in 0 until (list[i].ordered_product_attributes!![k].order_product_extra_topping_group?.size
                                ?: 0)) {
                            inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                            extratoppings.subitem_name.text = String.format(getString(R.string.plus), list[i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                            extratoppings.subitem_price.visibility = View.INVISIBLE
                            //extratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
                            extratoppings.dummy_image.visibility = View.GONE
                            view.add_subitem_view.addView(extratoppings)
                        }
                    }
                }
            } else {
                // if extratopping group only present then add only extratoppings in the list.
                for (k in 0 until (list[i].order_product_extra_topping_group?.size ?: 0)) {
                    inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val onlyextratoppings = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                    onlyextratoppings.subitem_name.text = String.format(getString(R.string.plus), list[i].order_product_extra_topping_group!!.get(k).ingredient_name)
                    // view.subitem_price.visibility=View.VISIBLE
                    // onlyextratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                    onlyextratoppings.subitem_price.visibility = View.GONE
                    onlyextratoppings.dummy_image.visibility = View.GONE
                    view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
        }

        generateBillDetails(subtotal,discount_amount,discount_type)

    }



     fun generateBillDetails(subtotal : Double,discount_amount: Double, discount_type: String) {

        var final_amount = subtotal
         loge(TAG,"discount type - "+discount_type)


         loge(TAG,"generateBillDetails--"+final_amount)

        if (DetailsFragment.isPickup) {
            // pick up:
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility = View.GONE
            additional_charge_layout.visibility = if (getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble() <= 0.0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility = View.VISIBLE

            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(subtotal.toString())
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            additional_charge_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(getAdditionalCharge(Paymentmethod.whatisthePaymethod!!))

            if (discount_type == Constants.ORDER_DISCOUNT) {
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount), discount_amount)
                    final_amount = final_amount - discount_amount
                } else {
                    discountcoupan_layout.visibility = View.GONE
                }

            } else if (discount_type == Constants.EATMORE_COUPON  || discount_type == Constants.RESTAURANT_COUPON) {
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount), discount_amount)
                    final_amount = final_amount - discount_amount
                } else {
                    discountcoupan_layout.visibility = View.GONE
                }

            } else {
                // none of the discount
                discountcoupan_layout.visibility = View.GONE

            }

            final_amount =
                    (   final_amount
                            + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                            + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble())

            if(appliedgift_list.size > 0){

                discountgift_layout.visibility=View.VISIBLE
                discountgift_layout.removeAllViews()
                discountgift_layout.invalidate()

                for(appliedgiftmodel in appliedgift_list){

                    val inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflater.inflate(R.layout.raw_giftdiscount, null)
                    view.discountgift_txt.text = if(appliedgiftmodel.gift_type == Constants.EATMORE ) "Eatmore Giftcard"  else if(appliedgiftmodel.gift_type == Constants.RESTAURANT ) "Restaurant Giftcard" else appliedgiftmodel.gift_type
                    view.discountgift_value.text = String.format(getString(R.string.discount), appliedgiftmodel.applied_gift_value)
                    discountgift_layout.addView(view)
                }


            }else{
                discountgift_layout.visibility=View.GONE
            }


            total_txt.text = String.format(getString(R.string.dkk_price),BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f", EpayFragment.paymentattributes.final_amount)))


        }

        //--------------------------------------//---------------------------------------------//


        else {

            // delivery :
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE
            shipping_layout.visibility = if (EpayFragment.paymentattributes.shipping_charge.toDouble() <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility = if (getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble() <= 0.0) View.GONE else View.VISIBLE
            total_layout.visibility = View.VISIBLE

            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(subtotal.toString())
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            shipping_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.shipping_charge)
            additional_charge_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(getAdditionalCharge(Paymentmethod.whatisthePaymethod!!))



            if (discount_type == Constants.ORDER_DISCOUNT) {
                loge(TAG,"ORDER_DISCOUNT-"+discount_amount)
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount), discount_amount)
                    final_amount = final_amount - discount_amount
                } else {
                    discountcoupan_layout.visibility = View.GONE
                }

            } else if (discount_type == Constants.EATMORE_COUPON  || discount_type == Constants.RESTAURANT_COUPON) {
                loge(TAG,"COUPON_DISCOUNT-"+discount_amount)
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount), discount_amount)
                    final_amount = final_amount - discount_amount
                } else {
                    discountcoupan_layout.visibility = View.GONE
                }

            } else {
                // none of the discount
                loge(TAG,"none discount -"+discount_amount)
                discountcoupan_layout.visibility = View.GONE

            }

            final_amount =
                    (         final_amount
                            + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                            + EpayFragment.paymentattributes.shipping_charge.toDouble()
                            + getAdditionalCharge(Paymentmethod.whatisthePaymethod!!).toDouble())


            if(appliedgift_list.size > 0){

                discountgift_layout.visibility=View.VISIBLE
                discountgift_layout.removeAllViews()
                discountgift_layout.invalidate()

                for(appliedgiftmodel in appliedgift_list){

                    val inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflater.inflate(R.layout.raw_giftdiscount, null)
                    view.discountgift_txt.text = if(appliedgiftmodel.gift_type == Constants.EATMORE ) "Eatmore Giftcard"  else if(appliedgiftmodel.gift_type == Constants.RESTAURANT ) "Restaurant Giftcard" else appliedgiftmodel.gift_type
                    view.discountgift_value.text = String.format(getString(R.string.discount), appliedgiftmodel.applied_gift_value)
                    discountgift_layout.addView(view)
                }


            }else{
                discountgift_layout.visibility=View.GONE
            }

            total_txt.text = String.format(getString(R.string.dkk_price),BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f", EpayFragment.paymentattributes.final_amount)))

        }


    }



    fun addspantext() {
        val span = SpannableString(String.format(getString(R.string.har_du_brug), EpayFragment.paymentattributes.restaurant_phone).trim())
        span.setSpan(clickableSpan, (String.format(getString(R.string.har_du_brug), EpayFragment.paymentattributes.restaurant_phone).trim().length - (EpayFragment.paymentattributes.restaurant_phone.trim().length + 1)),
                String.format(getString(R.string.har_du_brug), EpayFragment.paymentattributes.restaurant_phone).trim().length -1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        contact_txt.text = span
        contact_txt.movementMethod = LinkMovementMethod.getInstance()
    }

    val clickableSpan = object : ClickableSpan() {
        var dialog: AlertDialog? = null
        override fun onClick(textView: View) {
            Log.e(TAG, "onClick:--- ")
            dialog = AlertDialog.Builder(activity).setMessage("Do you want to call ${EpayFragment.paymentattributes.restaurant_phone.trim()}").setCancelable(true).setPositiveButton("yes") { dialogInterface, i ->
                if (is_callphn_PermissionGranted()) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + EpayFragment.paymentattributes.restaurant_phone.trim()))
                    startActivity(intent)
                }
            }.setNegativeButton("no") { dialogInterface, i -> dialog!!.dismiss() }.show()
        }

        override fun updateDrawState(ds: TextPaint) {
            //
            super.updateDrawState(ds)
            ds.isUnderlineText = true
            //                ds.setColor(getResources().getColor(R.color.orange));
            try {
                val colour = ContextCompat.getColor(context!!, R.color.dark_blue)
                ds.color = colour
            } catch (e: Exception) {
                Log.e(TAG, "updateDrawState: error " + e.message)
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        loge(TAG, "permission result---")
        when (requestCode) {
            0 -> {

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" +EpayFragment.paymentattributes.restaurant_phone.trim()))
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
        // if you finish all payment process then:
        showTabBar(false)
        img_toolbar_back.setImageResource(R.drawable.close)
        img_toolbar_back.setOnClickListener {
            when (currentView) {
                Constants.PROGRESSDIALOG -> {
                }
                Constants.PAYMENTSTATUS -> {
                    onBackpress()
                }
            }
        }
    }

    fun favourite(){

        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))      // if restaurant is closed then
        postParam.addProperty(Constants.RESTAURANT_ID,EpayFragment.paymentattributes.restaurant_id)
        if(EpayFragment.paymentattributes.is_fav){
            // unfavourite--
            DialogUtils.openDialog(context = context!!,btnNegative = getString(R.string.no) , btnPositive = getString(R.string.yes),color = ContextCompat.getColor(context!!, R.color.theme_color),msg = getString(R.string.vil_du_fjerne),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                override fun onPositiveButtonClick(position: Int) {
                    call_favorite = ApiCall.remove_favorite_restaurant(jsonObject = postParam)
                    remove_favorite_restaurant(call_favorite!!,null)
                }
                override fun onNegativeButtonClick() {
                }
            })

        }else{
            // favourite---
            call_favorite = ApiCall.add_favorite_restaurant(jsonObject = postParam)
            setfavorite(call_favorite!!,null)
        }
    }

    override fun comman_apisuccess(jsonObject: JsonObject, api_tag: String) {
        when(api_tag ){
            Constants.COM_ADD_FAVORITE_RESTAURANT->{
                if(EpayFragment.paymentattributes.is_fav){
                    EpayFragment.paymentattributes.is_fav=false
                    favorite_btn.text=if(EpayFragment.paymentattributes.is_fav)getString(R.string.markere_som_favorit) else getString(R.string.fjern_favorit)
                }else{
                    EpayFragment.paymentattributes.is_fav=true
                    favorite_btn.text=if(EpayFragment.paymentattributes.is_fav)getString(R.string.markere_som_favorit) else getString(R.string.fjern_favorit)
                }
            }

        }
    }

    override fun comman_apifailed(error: String, api_tag: String) {
        when(api_tag ){

            Constants.COM_ADD_FAVORITE_RESTAURANT->{
                favorite_btn.text=if(EpayFragment.paymentattributes.is_fav)getString(R.string.markere_som_favorit) else getString(R.string.fjern_favorit)
            }
        }
    }



    fun onBackpress() {

        val fragment = (parentFragment as EpayFragment).parentFragment
        if (fragment is HomeFragment) {
            val homeFragment = fragment
            for (i in 0 until (homeFragment.childFragmentManager.backStackEntryCount - if (HomeFragment.is_from_reorder) 1 else 2)) {
                homeFragment.childFragmentManager.popBackStack()
            }
        } else {
            val orderfragment = fragment as OrderFragment
            for (i in 0 until orderfragment.childFragmentManager.backStackEntryCount - 1) {
                orderfragment.childFragmentManager.popBackStack()
            }
        }
        if (OrderFragment.ui_model?.reloadfragment != null) OrderFragment.ui_model!!.reloadfragment.value = true
        if (HomeFragment.ui_model?.reloadfragment != null) HomeFragment.ui_model!!.reloadfragment.value = true  // reload last order from homefragment.
        showTabBar(true)

    }

    fun moveonlastorder() {

        val fragment = (parentFragment as EpayFragment).parentFragment
        if (fragment is HomeFragment) {
            HomeFragment.is_from_reorder=false
            val homeFragment = fragment
            for (i in 0 until (homeFragment.childFragmentManager.backStackEntryCount)) {
                homeFragment.childFragmentManager.popBackStack()
            }
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(1,800)

        } else {
            val orderfragment = fragment as OrderFragment
            for (i in 0 until orderfragment.childFragmentManager.backStackEntryCount) {
                orderfragment.childFragmentManager.popBackStack()
            }
        }
        if (OrderFragment.ui_model?.reloadfragment != null) OrderFragment.ui_model!!.reloadfragment.value = true
        if (HomeFragment.ui_model?.reloadfragment != null) HomeFragment.ui_model!!.reloadfragment.value = true  // reload last order from homefragment.
        showTabBar(true)

    }

    fun getAdditionalCharge(whatisthePaymethod : Paymentmethod.WhatIsThePaymethod) : String{

        when (whatisthePaymethod) {
            Paymentmethod.WhatIsThePaymethod.GIFT ->   { return EpayFragment.paymentattributes.additional_charges_giftcard.trim()}
            Paymentmethod.WhatIsThePaymethod.ONLINE -> { return EpayFragment.paymentattributes.additional_charges_online.trim()}
            Paymentmethod.WhatIsThePaymethod.CASH ->   { return EpayFragment.paymentattributes.additional_charges_cash.trim() }
            else ->                                    { return "0" }
        }

    }


    class MyClickHandler(val transactionstatus: TransactionStatus) {


        fun onClick(view: View, value: String) {
            when (value) {
                "0" -> {
                    transactionstatus.onBackpress()
                }
                "1" -> {
                    transactionstatus.moveonlastorder()
                }
                "2" -> {
                    transactionstatus.favourite()
                }
            }

        }

    }

    override fun onDestroyView() {

        super.onDestroyView()

        loge(TransactionStatus.TAG, "onDestroyView...")

        timeoutHandler.removeCallbacks(finalizer)

        if (call_check_order != null) {
            call_check_order!!.cancel()
        }

        if (call_favorite != null) {
            call_favorite!!.cancel()
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