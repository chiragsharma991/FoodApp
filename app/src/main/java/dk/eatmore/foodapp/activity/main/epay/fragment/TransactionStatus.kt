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
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import kotlinx.android.synthetic.main.transaction_status.*
import retrofit2.Call


class TransactionStatus : BaseFragment() {

    var transition: Transition? = null
    private var timeslot: ArrayList<String>? = null
    private var selectedtimeslot_position: Int = 0
    lateinit var currentView: String  // check this view for block the user interaction on payment page if data is loding.
    private lateinit var binding: TransactionStatusBinding
    // we have two different API so we are passing call in "checkout API" it may be from pickup/delivery :
    private lateinit var checkout_api: Call<JsonObject>


    companion object {
        val TAG = "TransactionStatus"
      //  var moveonsearch = false


        fun newInstance(): TransactionStatus {
            return TransactionStatus()
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
            binding.statusIs = false
            binding.transactionhandler = myclickhandler
            progress_bar.visibility = View.VISIBLE
            setToolbarforThis()
            currentView = Constants.PROGRESSDIALOG
            Handler().postDelayed({
                if (Paymentmethod.isPaymentonline) {
                    // if amount is lees then 0 -> direct status page
                    // if amount is greater -> call transaction api
                    statusfrom_online()
                } else {
                    //  checkout api already called so direct move on -> status page.
                    statusfrom_cash()
                }
                showproductInfo()
            }, 800)

        }
    }

    private fun statusfrom_cash() {

        currentView = Constants.PAYMENTSTATUS
        binding.statusIs = true
        setdelivery_info()
        order_progress_text.text = "Ordre accepteret til"
        progress_bar.visibility = View.GONE
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
        addspantext()

    }


/*
    private fun checkout_pickup() {

        callAPI(CartListFunction.getcartpaymentAttributes(context!!)!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                currentView = Constants.PAYMENTSTATUS
                binding.statusIs = true

                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
                    EpayFragment.paymentattributes.order_no = jsonobject.get(Constants.ORDER_NO).asInt
                    lottie_transaction_status.visibility = View.VISIBLE
                    lottie_transaction_status.scale = 0.4f
                    status_view.visibility = View.INVISIBLE
                    lottie_transaction_status.playAnimation()
                    totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
                    request_status.text = String.format(getString(R.string.request_successful), getString(R.string.successful))
                    requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
                    order_number.text = String.format(getString(R.string.order_number), EpayFragment.paymentattributes.order_no)
                    val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                    intent.putExtra(Constants.CARTCNT, 0)
                    intent.putExtra(Constants.CARTAMT, "00.00")
                    LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

                } else {
                    lottie_transaction_status.visibility = View.GONE
                    lottie_transaction_status.scale = 0.4f
                    status_view.visibility = View.VISIBLE
                    status_icon.setImageResource(R.drawable.animated_vector_cross)
                    (status_icon.getDrawable() as Animatable).start()
                    totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
                    request_status.text = String.format(getString(R.string.request_successful), getString(R.string.failed))
                    requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
                    order_number.text = getString(R.string.na)
                }
                val v: Vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                else v.vibrate(200);
                addspantext()

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
                currentView = Constants.PAYMENTSTATUS
                binding.statusIs = true
                lottie_transaction_status.visibility = View.GONE
                lottie_transaction_status.scale = 0.4f
                status_view.visibility = View.VISIBLE
                status_icon.setImageResource(R.drawable.animated_vector_cross)
                (status_icon.getDrawable() as Animatable).start()
                totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
                request_status.text = String.format(getString(R.string.request_successful), getString(R.string.failed))
                requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
                order_number.text = getString(R.string.na)

            }
        })


    }
*/

    private fun statusfrom_online() {

        if (EpayFragment.paymentattributes.final_amount <= 0.0) {
            // if final amout is less then 0 so epay is not procees and direct get success.
            currentView = Constants.PAYMENTSTATUS
            binding.statusIs = true
            setdelivery_info()
            order_progress_text.text = "Ordre accepteret til"
            progress_bar.visibility = View.GONE

/*            lottie_transaction_status.visibility = View.VISIBLE
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
            return
        }


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
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)
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
                progress_bar.visibility = View.GONE
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
                    setdelivery_info()
                    order_progress_text.text = "Ordre accepteret til"
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

                } else {
                    setdelivery_info()
                    order_progress_text.text = "ordren mislykkedes"
                    /*    lottie_transaction_status.visibility = View.GONE
                        lottie_transaction_status.scale = 0.4f
                        status_view.visibility = View.VISIBLE
                        status_icon.setImageResource(R.drawable.animated_vector_cross)
                        (status_icon.getDrawable() as Animatable).start()
                        totalamount.text = String.format(getString(R.string.total_amount), BindDataUtils.convertCurrencyToDanish(EpayFragment.paymentattributes.final_amount.toString()))
                        request_status.text = String.format(getString(R.string.request_successful), getString(R.string.failed))
                        requested_user.text = PreferenceUtil.getString(PreferenceUtil.E_MAIL, "")
                        order_number.text = getString(R.string.na)*/
                }
                val v: Vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                else v.vibrate(200);
                addspantext()

            }

            override fun onFail(error: Int) {
                progress_bar.visibility = View.GONE
                when (error) {
                    404 -> {
                        showSnackBar(transaction_constraint, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(transaction_constraint, getString(R.string.internet_not_available))

                    }
                }
                currentView = Constants.PAYMENTSTATUS
                binding.statusIs = true
                setdelivery_info()
                order_progress_text.text = "ordren mislykkedes"
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

    fun setdelivery_info() {

        pickupdelivery_address.visibility = View.GONE
        expected_time.text = EpayFragment.paymentattributes.expected_time
        order_progress_text.text = "Order under behandling"
        order_date.text = getcurrentdate()
        order_number.text = EpayFragment.paymentattributes.order_no.toString()
        order_accepted_date.visibility = View.GONE
        onlin_offline_txt.text = if (Paymentmethod.isPaymentonline)  getString(R.string.online) else getString(R.string.kontant_betaling)
        restaurant_fulladdress.text = String.format(getString(R.string.restaurant_full_address), EpayFragment.paymentattributes.restaurant_name, EpayFragment.paymentattributes.restaurant_address)


        if (EpayFragment.isPickup) {
            // pickup
            rest_pickupdelivery_txt.text = getString(R.string.for_hent_selv)
            pickupdelivery_txt.text = String.format(getString(R.string.delivery_wid_expectedtime),getString(R.string.for_hent_selv),EpayFragment.paymentattributes.expected_time)
            rest_pickupdelivery_icon.setImageResource(if(EpayFragment.isPickup ) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_icon.setImageResource(if(EpayFragment.isPickup ) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_address.visibility = View.GONE
            pickupdelivery_address.text=EpayFragment.paymentattributes.payment_address
            ImageLoader.loadImagefromurl(context!!,EpayFragment.paymentattributes.restaurant_appicon,restaurant_img)
            restaurant_name.text = EpayFragment.paymentattributes.restaurant_name
            restaurant_contact.text = EpayFragment.paymentattributes.restaurant_phone
            restaurant_address.text = EpayFragment.paymentattributes.restaurant_address

        } else {
            // delivery
            rest_pickupdelivery_txt.text = getString(R.string.til_levering)
            pickupdelivery_txt.text = String.format(getString(R.string.delivery_wid_expectedtime),getString(R.string.til_levering),EpayFragment.paymentattributes.expected_time)
            rest_pickupdelivery_icon.setImageResource(if(EpayFragment.isPickup ) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_icon.setImageResource(if(EpayFragment.isPickup ) R.mipmap.hent_selv_dark else R.mipmap.motorcycle_dark)
            pickupdelivery_address.visibility = View.VISIBLE
            pickupdelivery_address.text=EpayFragment.paymentattributes.payment_address
            ImageLoader.loadImagefromurl(context!!,EpayFragment.paymentattributes.restaurant_appicon,restaurant_img)
            restaurant_name.text = EpayFragment.paymentattributes.restaurant_name
            restaurant_contact.text = EpayFragment.paymentattributes.restaurant_phone
            restaurant_address.text = EpayFragment.paymentattributes.restaurant_address
        }

    }


    private fun generateBillDetails(giftcardis: String) {

        var final_amount: Double = 0.0   // calculating final amount with included all tax.

        if (EpayFragment.isPickup) {
            // pick up:
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility = View.GONE
            additional_charge_layout.visibility = if (EpayFragment.paymentattributes.additional_charges_cash.toDouble() <= 0.0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility = View.VISIBLE
            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.order_total)
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            additional_charge_txt.text = if (Paymentmethod.isPaymentonline) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_online) else BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_cash)
            discountcoupan_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))
            discountgift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))

            if (giftcardis == Constants.GIFTCARD) {
                discountgift_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + if (Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
                                - EpayFragment.paymentattributes.discount_amount)

            } else if (giftcardis == Constants.COUPON) {
                discountcoupan_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountgift_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble() - EpayFragment.paymentattributes.discount_amount)
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + if (Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())

            } else {
                discountgift_layout.visibility = View.GONE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + if (Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())

            }

            total_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f", final_amount))

        }

        //--------------------------------------//---------------------------------------------//


        else {
            // delivery :
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE
            shipping_layout.visibility = if (EpayFragment.paymentattributes.shipping_charge.toDouble() <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility = if (EpayFragment.paymentattributes.additional_charges_online.toDouble() <= 0.0) View.GONE else View.VISIBLE
            total_layout.visibility = View.VISIBLE
            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.order_total)
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            shipping_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.shipping_charge)
            additional_charge_txt.text = if (Paymentmethod.isPaymentonline) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_online) else BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_cash)
            discountcoupan_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))
            discountgift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))


            if (giftcardis == Constants.GIFTCARD) {
                discountgift_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + if (Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
                                - EpayFragment.paymentattributes.discount_amount)

            } else if (giftcardis == Constants.COUPON) {
                discountcoupan_layout.visibility = if (EpayFragment.paymentattributes.discount_amount <= 0) View.GONE else View.VISIBLE
                discountgift_layout.visibility = View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble() - EpayFragment.paymentattributes.discount_amount)
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + if (Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
            } else {
                discountgift_layout.visibility = View.GONE
                discountcoupan_layout.visibility = View.GONE
                final_amount = (
                        EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + if (Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
            }

            total_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f", final_amount))
        }


    }


    private fun showproductInfo() {

        if (EpayFragment.ui_model!!.viewcard_list.value == null) {
            // this condition will null if all item has been deleted : so just clear view and inflate empty view on screen.
            add_parentitem_view.removeAllViewsInLayout()
            add_parentitem_view.invalidate()
            generateBillDetails(if(EpayFragment.paymentattributes.discount_type == Constants.GIFTCARD) Constants.GIFTCARD else if (EpayFragment.paymentattributes.discount_type == Constants.COUPON) Constants.COUPON else Constants.OTHER )
            return
        }

        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until EpayFragment.ui_model!!.viewcard_list.value!!.result!!.size) {
            var inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dynamic_raw_item, null)
            view.remove_item.tag = i
            view.remove_item.visibility = View.GONE
            view.item_name.text = EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].product_name
            view.item_price.text = if (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price != null) BindDataUtils.convertCurrencyToDanish(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price!!) else "null"
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
                        for (l in 0 until (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!![k].order_product_extra_topping_group?.size
                                ?: 0)) {
                            inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                            extratoppings.subitem_name.text = String.format(getString(R.string.plus), EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                            extratoppings.subitem_price.text = BindDataUtils.convertCurrencyToDanish(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
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
                    onlyextratoppings.subitem_price.text = BindDataUtils.convertCurrencyToDanish(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                    onlyextratoppings.dummy_image.visibility = View.GONE
                    view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
        }
        generateBillDetails(if(EpayFragment.paymentattributes.discount_type == Constants.GIFTCARD) Constants.GIFTCARD else if (EpayFragment.paymentattributes.discount_type == Constants.COUPON) Constants.COUPON else Constants.OTHER )

    }


    fun addspantext() {
        val span = SpannableString(getString(R.string.if_you_have_any_questions) + " " + "88826543")
        span.setSpan(clickableSpan, getString(R.string.if_you_have_any_questions).trim({ it <= ' ' }).length + 1,
                getString(R.string.if_you_have_any_questions).trim({ it <= ' ' }).length + 8 + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        //   phone_txt.text = span
        //    phone_txt.movementMethod = LinkMovementMethod.getInstance()

    }

    val clickableSpan = object : ClickableSpan() {
        var dialog: AlertDialog? = null
        override fun onClick(textView: View) {
            Log.e(TAG, "onClick:--- ")
            dialog = AlertDialog.Builder(activity).setMessage("Do you want to call ${"88826543"}").setCancelable(true).setPositiveButton("yes") { dialogInterface, i ->
                if (is_callphn_PermissionGranted()) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "88826543"))
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
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "88826543"))
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

    fun onBackpress() {

        val fragment = (parentFragment as EpayFragment).parentFragment
        if (fragment is HomeFragment) {
            val homeFragment = fragment
             for(i in 0 until(homeFragment.childFragmentManager.backStackEntryCount-2) ){
                 homeFragment.childFragmentManager.popBackStack()
             }
        } else {
            val orderfragment=fragment as OrderFragment
            for (i in 0 until orderfragment.childFragmentManager.backStackEntryCount -1 ) {
                orderfragment.childFragmentManager.popBackStack()
            }
        }
        if (OrderFragment.ui_model?.reloadfragment != null) OrderFragment.ui_model!!.reloadfragment.value = true
        if (HomeFragment.ui_model?.reloadfragment != null) HomeFragment.ui_model!!.reloadfragment.value = true  // reload last order from homefragment.
        showTabBar(true)

    }

    fun moveonlastorder(){

        val fragment = (parentFragment as EpayFragment).parentFragment
        if (fragment is HomeFragment) {
            val homeFragment = fragment
            for(i in 0 until(homeFragment.childFragmentManager.backStackEntryCount) ){
                homeFragment.childFragmentManager.popBackStack()
            }
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(1)

        } else {
            val orderfragment=fragment as OrderFragment
            for (i in 0 until orderfragment.childFragmentManager.backStackEntryCount ) {
                orderfragment.childFragmentManager.popBackStack()
            }
        }
        if (OrderFragment.ui_model?.reloadfragment != null) OrderFragment.ui_model!!.reloadfragment.value = true
        if (HomeFragment.ui_model?.reloadfragment != null) HomeFragment.ui_model!!.reloadfragment.value = true  // reload last order from homefragment.
        showTabBar(true)
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