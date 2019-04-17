package dk.eatmore.foodapp.activity.main.epay.fragment

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
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
import dk.eatmore.foodapp.adapter.CashonlineAdapter
import dk.eatmore.foodapp.databinding.PaymentmethodBinding
import dk.eatmore.foodapp.databinding.RowPaymethodBinding
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.epay.ApplyCodeModel
import dk.eatmore.foodapp.model.epay.ResultItem
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.include_calculationprice.*
import kotlinx.android.synthetic.main.paymentmethod.*
import kotlinx.android.synthetic.main.raw_giftdiscount.view.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import retrofit2.Call
import java.io.Serializable


class Paymentmethod : CommanAPI() {


    var transition: Transition? = null
    private lateinit var binding: PaymentmethodBinding
    private var timeslot: ArrayList<String>? = null
    private var selectedtimeslot_position: Int = 0
    lateinit var currentView: String
    private var call_checkout: Call<JsonObject>? = null
    private var call_applycode: Call<ApplyCodeModel>? = null
    private var call_cancelorder: Call<JsonObject>? = null
    private var is_continuefrom_online: Boolean = false
    private lateinit var mAdapter: CashonlineAdapter
    private lateinit var paymentinfo_list: ArrayList<PaymentInfoModel>
    val myclickhandler = MyClickHandler(this)
    private val timeoutHandler = Handler()
    private var finalizer: Runnable? = null


   private var canichangeSegment : Boolean = true
   var defaultpaymentmethodType : String =""
   var final_amount: Double = 0.0  // subtotal + additional (without eatmore balance)
   private var subtotal = 0.0 // only calculated subtotal
   var totaltopay = 0.0 // subtotal + additional (with eatmore balance)
   var eatmoreAppliedBalance = 0.0
   var restaurantAppliedBalance = 0.0
   var cpn_discount_type =""  // coupan_discount type if applied
   var cpn_discount_id ="" // coupan_discount id if applied
   var cpn_discount_amount=0.0 // coupan_discount amount if applied

   // you can get every discont/gift from here
   var appliedgift_list: ArrayList<AppliedGiftModel> = ArrayList()
   var addedDiscount_amount : Double =0.0
   var addedDiscount_type : String=""
   var addedDiscount_id : String=""
   var addedProductlist: ArrayList<ResultItem> = arrayListOf()

    // we have two different API so we are passing call in "checkout API" it may be from pickup/delivery :
    private lateinit var checkout_api: Call<JsonObject>


    companion object {
        val TAG = "Paymentmethod"
        var whatisthePaymethod : WhatIsThePaymethod? = null


        fun newInstance(): Paymentmethod {
            return Paymentmethod()
        }
    }

    enum class WhatIsThePaymethod {
        GIFT, ONLINE, CASH
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
            setToolbarforThis()
            binding.isProgress = true

            finalizer = object : Runnable {
                override fun run() {
                    loge(TAG, "Handler-----")
                    refreshview()
                    addspantext()
                    showproductInfo(EpayFragment.ui_model!!.viewcard_list.value?.result,EpayFragment.paymentattributes.discount_amount,EpayFragment.paymentattributes.discount_type,EpayFragment.paymentattributes.discount_id,false)
                }
            }
            timeoutHandler.postDelayed(finalizer, 600)
        }
    }


    private fun refreshview() {

        paymentinfo_list = ArrayList<PaymentInfoModel>()

        // set default payment method name
        if(EpayFragment.paymentattributes.online_logo != ""){
            // online is present don't go any where
            defaultpaymentmethodType = getString(R.string.online_payment).trim()
            whatisthePaymethod = WhatIsThePaymethod.ONLINE

        }else if(EpayFragment.paymentattributes.cash_logo != ""){
            // cash is present online is not present.
            defaultpaymentmethodType = getString(R.string.cash_payment).trim()
            whatisthePaymethod = WhatIsThePaymethod.CASH

        }else{
            // no one present (this condition will not present)
            defaultpaymentmethodType=""
        }

        // prepare list for payment method.
        EpayFragment.paymentattributes.giftcard_details[Constants.EATMORE]?.let {
            loge(TAG, "eatmore balance-" + it)
            paymentinfo_list.add(PaymentInfoModel(payment_type = Constants.EATMORE, error_expand = false, gift_expand = false, image_path = EpayFragment.paymentattributes.online_logo, view_expand = false, btn_txt = getString(R.string.confirm), gift_loader = false, edittextvalue = "", balance = it))
        }
        EpayFragment.paymentattributes.giftcard_details[Constants.RESTAURANT]?.let {
            loge(TAG, "restaurant balance-" + it)
            paymentinfo_list.add(PaymentInfoModel(payment_type = Constants.RESTAURANT, error_expand = false, gift_expand = false, image_path = EpayFragment.paymentattributes.online_logo, view_expand = false, btn_txt = getString(R.string.confirm), gift_loader = false, edittextvalue = "", balance = it))
        }
        if (EpayFragment.paymentattributes.online_logo != "") {
            paymentinfo_list.add(PaymentInfoModel(payment_type = getString(R.string.online_payment), error_expand = false, gift_expand = false, image_path = EpayFragment.paymentattributes.online_logo, view_expand = true, btn_txt = getString(R.string.pay), gift_loader = false, edittextvalue = ""))
        }

        if (EpayFragment.paymentattributes.cash_logo != "") {
            paymentinfo_list.add(PaymentInfoModel(payment_type = getString(R.string.cash_payment), error_expand = false, gift_expand = false, image_path = EpayFragment.paymentattributes.cash_logo, view_expand = false, btn_txt = getString(R.string.confirm), gift_loader = false, edittextvalue = ""))
        }

        //TODO : add loader to prevent from gift APIs


        mAdapter = CashonlineAdapter(context!!, paymentinfo_list, myclickhandler, this, canichangeSegment, appliedgift_list )
        recycler_paymethod.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_paymethod.adapter = mAdapter
        Handler().postDelayed({ binding.isProgress = false }, 500)
    }

    data class PaymentInfoModel(
            var payment_type: String,
            var btn_txt: String,
            var view_expand: Boolean,
            var walletBtn_expand: Boolean=false,
            val image_path: String,
            var gift_expand: Boolean,
            var error_expand: Boolean,
            var gift_loader: Boolean,
            var balance: String = "",
            var ischeck: Boolean = false,
            var edittextvalue: String
    )
    data class AppliedGiftModel(
            var gift_type: String,
            var actual_gift_value: String,
            var applied_gift_value: Double
    ) : Serializable


    private fun generateBillDetails(subtotal : Double,discount_amount: Double, discount_type: String,changeintoDefault : Boolean) {

        final_amount = subtotal
        EpayFragment.paymentattributes.upto_min_shipping = calculateuptominPrice(subtotal)

        loge(TAG,"generateBillDetails--"+final_amount)

        if (DetailsFragment.isPickup) {
            // pick up:
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility = View.GONE
            additional_charge_layout.visibility = if (getAdditionalCharge(whatisthePaymethod!!).toDouble() <= 0.0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility = View.VISIBLE

            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(subtotal.toString())
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            additional_charge_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(getAdditionalCharge(whatisthePaymethod!!))

            if (discount_type == Constants.ORDER_DISCOUNT) {
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(discount_amount.toString().trim()) )
                    final_amount = final_amount - discount_amount
                } else {
                    discountcoupan_layout.visibility = View.GONE
                }

            } else if (discount_type == Constants.EATMORE_COUPON  || discount_type == Constants.RESTAURANT_COUPON) {
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(discount_amount.toString().trim()))
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
                      + getAdditionalCharge(whatisthePaymethod!!).toDouble())


            // Check Eatmore balance:
            applyeatmorebalance(changeintoDefault)
        }

        //--------------------------------------//---------------------------------------------//


        else {

            // delivery :
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE
            shipping_layout.visibility = if (EpayFragment.paymentattributes.shipping_charge.toDouble() <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility = if (getAdditionalCharge(whatisthePaymethod!!).toDouble() <= 0.0) View.GONE else View.VISIBLE
            total_layout.visibility = View.VISIBLE

            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(subtotal.toString())
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            shipping_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.shipping_charge)
            additional_charge_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(getAdditionalCharge(whatisthePaymethod!!))



            if (discount_type == Constants.ORDER_DISCOUNT) {
                loge(TAG,"ORDER_DISCOUNT-"+discount_amount)
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(discount_amount.toString().trim()))
                    final_amount = final_amount - discount_amount
                } else {
                    discountcoupan_layout.visibility = View.GONE
                }

            } else if (discount_type == Constants.COUPON) {
                loge(TAG,"COUPON_DISCOUNT-"+discount_amount)
                if (discount_amount > 0.0) {
                    discountcoupan_layout.visibility = View.VISIBLE
                    discountcoupan_txt.text = String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(discount_amount.toString().trim()))
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
                            + getAdditionalCharge(whatisthePaymethod!!).toDouble())


            // Check Eatmore balance:

            applyeatmorebalance(changeintoDefault)

        }




    }


    private  fun calculateuptominPrice(subtotal: Double) : String {

        val min_orderprice = EpayFragment.paymentattributes.minimum_order_price.trim().toDouble()

        if(subtotal >= min_orderprice ){
            // no amount would charge for uptomin
            return "0"

        }else{
            // calculate uptomin amount on sutotal not on any discount
            val result = min_orderprice - subtotal
            return  result.toString()
        }

    }

     fun getAdditionalCharge(whatisthePaymethod : WhatIsThePaymethod) : String{

        when (whatisthePaymethod) {
            WhatIsThePaymethod.GIFT ->   { return EpayFragment.paymentattributes.additional_charges_giftcard.trim()}
            WhatIsThePaymethod.ONLINE -> { return EpayFragment.paymentattributes.additional_charges_online.trim()}
            WhatIsThePaymethod.CASH ->   { return EpayFragment.paymentattributes.additional_charges_cash.trim() }
            else ->                      { return "0" }
        }

    }


    fun addspantext() {
        val span = SpannableString(getString(R.string.brug_for_hjælp))
        span.setSpan(clickableSpan, (getString(R.string.brug_for_hjælp).trim().length - 8),
                getString(R.string.brug_for_hjælp).trim().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        eatmore_contact.text = span
        eatmore_contact.movementMethod = LinkMovementMethod.getInstance()
    }


    val clickableSpan = object : ClickableSpan() {
        var dialog: AlertDialog? = null
        override fun onClick(textView: View) {
            Log.e(TAG, "onClick:--- ")
            dialog = AlertDialog.Builder(activity).setMessage("Do you want to call 73702515").setCancelable(true).setPositiveButton("yes") { dialogInterface, i ->
                if (is_callphn_PermissionGranted()) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "73702515"))
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
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "73702515"))
                    startActivity(intent)
                    //    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


     fun showproductInfo (list: ArrayList<ResultItem>? , discount_amount: Double, discount_type: String , discount_id: String , changeintoDefault : Boolean) {

        loge(TAG,"showproductInfo--"+discount_amount+" type -"+discount_type)

        if (list == null) {
            // this condition will null if all item has been deleted : so just clear view and inflate empty view on screen.
            add_parentitem_view.removeAllViewsInLayout()
            add_parentitem_view.invalidate()
           // generateBillDetails(Constants.OTHER, 0.0)
            return
        }
         addedProductlist = list
         addedDiscount_amount = discount_amount
         addedDiscount_type=discount_type
         addedDiscount_id=discount_id
         subtotal = 0.0

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

        generateBillDetails(subtotal,discount_amount,discount_type,changeintoDefault)

    }


    private fun continuefromOnline() {

        if (!isInternetAvailable()) {
            showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))
            return
        }
        if (checkpaymentAttributes() == false) {
            showSnackBar(pamentmethod_container, getString(R.string.error_404))
            return
        }
        showProgressDialog()
        checkinfo_restaurant_closed()
    }

    private fun continuefromCash () {

        if (!isInternetAvailable()) {
            showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))
            return
        }
        if (checkpaymentAttributes() == false) {
            showSnackBar(pamentmethod_container, getString(R.string.error_404))
            return
        }
        showProgressDialog()
        checkinfo_restaurant_closed()

    }


    override fun comman_apisuccess(jsonObject: JsonObject, api_tag: String) {

        when (api_tag) {

            Constants.COM_INFO_RESTAURANT_CLOSED -> {
                // add tab var
                val msg = if (jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else ""
                if (jsonObject.has(Constants.IS_DELIVERY_PRESENT) && jsonObject.has(Constants.IS_PICKUP_PRESENT)) {
                    DetailsFragment.delivery_present = jsonObject.get(Constants.IS_DELIVERY_PRESENT).asBoolean
                    DetailsFragment.pickup_present = jsonObject.get(Constants.IS_PICKUP_PRESENT).asBoolean
                }

                // check if restaurant is closed or not
                // making restaurant closed equation to satisfy comman function.
                when (getrestaurantstatus(is_restaurant_closed = jsonObject.get(Constants.IS_RESTAURANT_CLOSED)?.asBoolean, pre_order = jsonObject.get(Constants.PRE_ORDER)?.asBoolean)) {
                    RestaurantState.CLOSED -> {
                        showProgressDialog()
                        any_preorder_closedRestaurant(is_restaurant_closed = true, pre_order = false, msg = msg) // set hard code to close restaurant.
                    }
                    else -> {
                        val message = getdeliverymsg_error(jsonObject)
                        if ((DetailsFragment.isPickup && !DetailsFragment.pickup_present) || (!DetailsFragment.isPickup && !DetailsFragment.delivery_present)) {
                            // if you are comming from pickup and end of the movement pickup is not present then:
                            // [pickup(true) && pickuppresent(false) || delivery(true) && deliverypresent (false)]
                            showProgressDialog()
                            DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(context!!, R.color.black), msg = message, title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                                override fun onPositiveButtonClick(position: Int) {
                                    (parentFragment as EpayFragment).popAllFragment()
                                    (parentFragment as EpayFragment).reloadScreen()
                                }

                                override fun onNegativeButtonClick() {
                                }
                            })
                        } else {
                            //normal flow
                            eatmoreAppliedBalance = 0.0
                            restaurantAppliedBalance = 0.0
                            for(appliedgiftmodel in appliedgift_list){
                                if(appliedgiftmodel.gift_type ==Constants.EATMORE){
                                    eatmoreAppliedBalance =appliedgiftmodel.applied_gift_value
                                }
                                else if(appliedgiftmodel.gift_type == Constants.RESTAURANT){
                                    restaurantAppliedBalance =appliedgiftmodel.applied_gift_value
                                }
                            }

                            if(totaltopay > 0){
                                if (whatisthePaymethod == WhatIsThePaymethod.ONLINE) checkoutfrom_online() else checkoutfrom_cash()
                            }
                            else{
                                checkoutfrom_cash()  // if balance is 0 then move on cash .
                            }
                        }
                    }
                }

            }

        }


    }

    override fun comman_apifailed(error: String, api_tag: String) {
        when (api_tag) {
            Constants.COM_INFO_RESTAURANT_CLOSED -> {
                if (error == getString(R.string.error_404)) {
                    showSnackBarIndefinite(pamentmethod_container, getString(R.string.error_404))
                } else if (error == getString(R.string.internet_not_available)) {
                    showSnackBarIndefinite(pamentmethod_container, getString(R.string.internet_not_available))
                }
            }
        }
    }

    private fun checkoutfrom_cash() {
        loge(TAG, "chechout cash---")

        call_checkout = CartListFunction.getcartpaymentAttributes(context!!,this)!!
        callAPI(call_checkout!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
                    when (getrestaurantstatus(is_restaurant_closed = jsonobject.get(Constants.IS_RESTAURANT_CLOSED)?.asBoolean, pre_order = jsonobject.get(Constants.PRE_ORDER)?.asBoolean)) {

                        RestaurantState.CLOSED -> {
                            val msg = if (jsonobject.has(Constants.MSG)) jsonobject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                            any_preorder_closedRestaurant(is_restaurant_closed = true, pre_order = false, msg = msg) // set hard code to close restaurant.
                        }
                        else -> {
                            EpayFragment.paymentattributes.order_no = jsonobject.get(Constants.ORDER_NO).asInt
                            (parentFragment as EpayFragment).addFragment(R.id.epay_container, TransactionStatus.newInstance(addedProductlist,addedDiscount_amount,addedDiscount_type,addedDiscount_id,appliedgift_list), TransactionStatus.TAG, true)
                            EpayFragment.paymentattributes.final_amount = jsonobject.get(Constants.ORDER_TOTAL).asDouble

                        }
                    }

                } else {
                    val msg = if (jsonobject.has(Constants.MSG)) jsonobject.get(Constants.MSG).asString else getString(R.string.error_404)
                    DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok),
                            color = ContextCompat.getColor(context!!, R.color.theme_color), msg = msg,
                            title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                        override fun onPositiveButtonClick(position: Int) {
                        }

                        override fun onNegativeButtonClick() {
                        }
                    })
                    //showSnackBar(pamentmethod_container, getString(R.string.error_404))
                }
            }

            override fun onFail(error: Int) {

                if (call_checkout!!.isCanceled) {
                    return
                }
                when (error) {
                    404 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))

                    }
                }
                showProgressDialog()

            }
        })


    }


    private fun checkoutfrom_online() {
        // showProgressDialog()
        loge(TAG, "chechout online---")
        call_checkout = CartListFunction.getcartpaymentAttributes(context!!,this)!!
        callAPI(call_checkout!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {


                    when (getrestaurantstatus(is_restaurant_closed = jsonobject.get(Constants.IS_RESTAURANT_CLOSED)?.asBoolean, pre_order = jsonobject.get(Constants.PRE_ORDER)?.asBoolean)) {

                        RestaurantState.CLOSED -> {
                            val msg = if (jsonobject.has(Constants.MSG)) jsonobject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                            any_preorder_closedRestaurant(is_restaurant_closed = true, pre_order = false, msg = msg) // set hard code to close restaurant.
                        }
                        else -> {
                            EpayFragment.paymentattributes.order_no = jsonobject.get(Constants.ORDER_NO).asInt
                            if (jsonobject.has(Constants.EPAY_MERCHANT)) EpayFragment.paymentattributes.epay_merchant = jsonobject.get(Constants.EPAY_MERCHANT).asString
                            EpayFragment.paymentattributes.final_amount = jsonobject.get(Constants.ORDER_TOTAL).asDouble

                      /*      if (EpayFragment.paymentattributes.final_amount <= 0.0) {
                                (parentFragment as EpayFragment).addFragment(R.id.epay_container, TransactionStatus.newInstance(), TransactionStatus.TAG, true)
                            } else {
                                (parentFragment as EpayFragment).addFragment(R.id.epay_container, BamboraWebfunction.newInstance(), BamboraWebfunction.TAG, true)
                            }*/

                            (parentFragment as EpayFragment).addFragment(R.id.epay_container, BamboraWebfunction.newInstance(addedProductlist,addedDiscount_amount,addedDiscount_type,addedDiscount_id,appliedgift_list), BamboraWebfunction.TAG, true)

                        }
                    }
                } else {
                    // if card is empty then :
                    val msg = if (jsonobject.has(Constants.MSG)) jsonobject.get(Constants.MSG).asString else getString(R.string.error_404)
                    any_preorder_closedRestaurant(is_restaurant_closed = true, pre_order = false, msg = msg) // set hard code to close restaurant.
                }
            }

            override fun onFail(error: Int) {

                if (call_checkout!!.isCanceled) {
                    return
                }
                when (error) {
                    404 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))

                    }
                }
                showProgressDialog()
            }
        })
    }


    // check all details are available or not.
    private fun checkpaymentAttributes(): Boolean? {
        var result = false
        val postParam = JsonObject()
        try {
            postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
            postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
            postParam.addProperty(Constants.FIRST_TIME, EpayFragment.paymentattributes.first_time)
            postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
            // postParam.addProperty(Constants.POSTAL_CODE, EpayFragment.paymentattributes.postal_code)
            postParam.addProperty(Constants.EATMORE_GIFTCARD, eatmoreAppliedBalance)
            postParam.addProperty(Constants.RESTAURANT_GIFTCARD, restaurantAppliedBalance)
            postParam.addProperty(Constants.DISCOUNT_TYPE, addedDiscount_type)
            postParam.addProperty(Constants.DISCOUNT_AMOUNT, addedDiscount_amount)
            postParam.addProperty(Constants.DISCOUNT_ID, addedDiscount_id)
            postParam.addProperty(Constants.SHIPPING, if (DetailsFragment.isPickup) context!!.getString(R.string.pickup_) else context!!.getString(R.string.delivery_))
            postParam.addProperty(Constants.TELEPHONE_NO, EpayFragment.paymentattributes.telephone_no)
            postParam.addProperty(Constants.ORDER_TOTAL, totaltopay.toString())
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
            postParam.addProperty(Constants.ACCEPT_TC, "1")
            postParam.addProperty(Constants.PAYMETHOD, if (whatisthePaymethod == WhatIsThePaymethod.ONLINE) "1" else "2")
            postParam.addProperty(Constants.EXPECTED_TIME, EpayFragment.paymentattributes.expected_time)
            postParam.addProperty(Constants.COMMENTS, EpayFragment.paymentattributes.comments)
            postParam.addProperty(Constants.DEVICE_TYPE, Constants.DEVICE_TYPE_VALUE)
            postParam.addProperty(Constants.FIRST_NAME, EpayFragment.paymentattributes.first_name)
            postParam.addProperty(Constants.ADDITIONAL_CHARGE,getAdditionalCharge(whatisthePaymethod!!))
            postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
            postParam.addProperty(Constants.LANGUAGE, Constants.DA)
            val jsonarray = JsonArray()
            for (i in 0.until(EpayFragment.selected_op_id.size)) {
                val jsonobject = JsonObject()
                jsonobject.addProperty(Constants.OP_ID, EpayFragment.selected_op_id.get(i))
                jsonarray.add(jsonobject)
            }
            postParam.add(Constants.CARTPRODUCTS, jsonarray)

            if (DetailsFragment.isPickup) {
                //pickup--
                checkout_api = ApiCall.checkout_pickup(postParam)
                result = true

            } else {
                // delivery--
                postParam.addProperty(Constants.ADDRESS, EpayFragment.paymentattributes.address)
                postParam.addProperty(Constants.POSTAL_CODE, EpayFragment.paymentattributes.postal_code)
                postParam.addProperty(Constants.DISTANCE, EpayFragment.paymentattributes.distance)
                postParam.addProperty(Constants.MINIMUM_ORDER_PRICE, EpayFragment.paymentattributes.minimum_order_price)
                postParam.addProperty(Constants.SHIPPING_COSTS, EpayFragment.paymentattributes.shipping_charge)
                postParam.addProperty(Constants.UPTO_MIN_SHIPPING, EpayFragment.paymentattributes.upto_min_shipping)
                postParam.addProperty(Constants.SHIPPING_REMARK, "")
                checkout_api = ApiCall.checkout_delivery(postParam)
                result = true
            }


        } catch (error: Exception) {
            return result
        }

        return result

    }


    fun applygiftcoupan(binder: RowPaymethodBinding, model: PaymentInfoModel) {

        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_TOTAL, subtotal)
        postParam.addProperty(Constants.ADDITIONAL_CHARGE, getAdditionalCharge(whatisthePaymethod!!))
        postParam.addProperty(Constants.CODE, model.edittextvalue.trim())
        postParam.addProperty(Constants.SHIPPING, if (DetailsFragment.isPickup) getString(R.string.pickup_caps) else getString(R.string.delivery_caps))
        postParam.addProperty(Constants.SHIPPING_COSTS, EpayFragment.paymentattributes.shipping_charge)
        postParam.addProperty(Constants.IS_LOGIN, "1")
        postParam.addProperty(Constants.UPTO_MIN_SHIPPING, EpayFragment.paymentattributes.upto_min_shipping)
        postParam.addProperty(Constants.CHECKOUT_PAYMENT, if (whatisthePaymethod == WhatIsThePaymethod.ONLINE) getString(R.string.online_payment) else getString(R.string.cash_payment))

        call_applycode = ApiCall.applycode(postParam)
        callAPI(call_applycode!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val applycodemodel = body as ApplyCodeModel
                if (applycodemodel.status) {

                    cpn_discount_amount=applycodemodel.discount_amount!!
                    cpn_discount_id=applycodemodel.discount_id!!.trim()
                    cpn_discount_type=applycodemodel.discount_type!!.trim()

                    showproductInfo(list =applycodemodel.result ,discount_amount = cpn_discount_amount ?: 0.0 ,discount_type =cpn_discount_type,discount_id =cpn_discount_id ,changeintoDefault = false)
                    binder.errorOfPromotioncode.setTextColor(ContextCompat.getColor(context!!, R.color.green))


                } else {

                    cpn_discount_amount=0.0
                    cpn_discount_id=""
                    cpn_discount_type=""

                    showproductInfo(list =applycodemodel.result ,discount_amount = EpayFragment.paymentattributes.discount_amount ,discount_type =EpayFragment.paymentattributes.discount_type,discount_id = EpayFragment.paymentattributes.discount_id,changeintoDefault = false )
                    binder.errorOfPromotioncode.setTextColor(ContextCompat.getColor(context!!, R.color.theme_color))
                }


                model.gift_loader = false
                model.error_expand = true
                binder.errorOfPromotioncode.text = applycodemodel.msg
                binder.errorOfPromotioncode.visibility = View.VISIBLE
                binder.executePendingBindings()
                mAdapter.notifyDataSetChanged()
            }

            override fun onFail(error: Int) {

                if (call_applycode!!.isCanceled) {
                    return
                }
                when (error) {
                    404 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(pamentmethod_container, getString(R.string.internet_not_available))

                    }
                }
                model.gift_loader = false
                model.error_expand = true
                binder.errorOfPromotioncode.visibility = View.VISIBLE
                binder.errorOfPromotioncode.setTextColor(ContextCompat.getColor(context!!, R.color.theme_color))
                binder.errorOfPromotioncode.text = getString(R.string.error_404)
                binder.executePendingBindings()
                mAdapter.notifyDataSetChanged()

            }
        })
    }

    /*  override fun promotion_view(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
      }*/


    // set common toolbar from this and set pre fragment toolbar from this.

    fun setToolbarforThis() {
        txt_toolbar.text = getString(R.string.payment)
        img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
    }


    fun onlineTransactionFailed() {
        // in case user do cancel from transction screen.
        loge(TAG, "onlineTransactionFailed")
        call_cancelorder = ApiCall.cancelordertransaction(
                r_key = PreferenceUtil.getString(PreferenceUtil.R_KEY, "")!!,
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN, "")!!,
                order_no = EpayFragment.paymentattributes.order_no
        )
        callAPI(call_cancelorder!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
                    loge(TransactionStatus.TAG, " ordertransaction is success ")
                    DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(context!!, R.color.black), msg = getString(R.string.transaction_has_been_declined), title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                        override fun onPositiveButtonClick(position: Int) {
                            loge(TAG, "ok button---")
                        }

                        override fun onNegativeButtonClick() {
                        }
                    })
                }
            }

            override fun onFail(error: Int) {

                if (call_cancelorder!!.isCanceled) {
                    return
                }

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

     /* changeintoDefault : when you want to set one payment method open by default on any click on check box then you can set this.
     * **/

     fun applyeatmorebalance(changeintoDefault : Boolean) {

        // as a gift cart from (eatmore/restaurant) type
        totaltopay = final_amount
        var totalEatmore_balance = 0.0
        for (paymentInfoModel in paymentinfo_list) {
            if (paymentInfoModel.ischeck) {
                totalEatmore_balance += paymentInfoModel.balance.toDouble()
            }
        }
         setgiftdiscountprice(final_amount)  // set only discount text and gift button expand function

         if (totalEatmore_balance > 0.0) {

            // applied gift eatmore balance

            if (totalEatmore_balance >= totaltopay) {
                // if eatmore balance is more then product selected
                totaltopay= 0.0

                for (paymentInfoModel in paymentinfo_list) {
                     paymentInfoModel.view_expand=false
                     paymentInfoModel.error_expand=false
                }

                mAdapter.canichangeSegment=false
                mAdapter.notifyDataSetChanged()

            } else {
                // eatmore balance is not enought then:
                totaltopay = totaltopay - totalEatmore_balance

                if(changeintoDefault){
                    for (paymentinfomodel in paymentinfo_list)
                    {
                        paymentinfomodel.error_expand=false
                        if(paymentinfomodel.payment_type.trim() == defaultpaymentmethodType.trim()) paymentinfomodel.view_expand=true  // set default open dynamically
                        else paymentinfomodel.view_expand=false
                    }
                    mAdapter.canichangeSegment=true

                }
                mAdapter.notifyDataSetChanged()
            }

        } else {
            // Not applied gift eatmore balance
            // When you uncheck from  balance

            if(changeintoDefault){

                for (paymentinfomodel in paymentinfo_list){
                    paymentinfomodel.error_expand=false
                    if(paymentinfomodel.payment_type.trim() == defaultpaymentmethodType.trim()) paymentinfomodel.view_expand=true  // set default open dynamically
                    else paymentinfomodel.view_expand=false
                }
                mAdapter.canichangeSegment=true
            }

            mAdapter.notifyDataSetChanged()

        }

        total_txt.text = String.format(getString(R.string.dkk_price), BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f", totaltopay)))

        // Add restaurant address and time--
        address_txt.text = EpayFragment.paymentattributes.payment_address
        deliverytime_txt.text = EpayFragment.paymentattributes.payment_time
        paymenttype_img.setImageResource(if (DetailsFragment.isPickup) R.mipmap.bag else R.mipmap.motorcycle)


    }


    private fun setgiftdiscountprice (final_amount : Double) {
        var expandbtnName =""
        var remaining_balance = final_amount
        //discountgift_layout.removeAllViewsInLayout()
        discountgift_layout.removeAllViews()
        discountgift_layout.invalidate()

        if(appliedgift_list.size > 0){
            // one or morethen one gift balance are applied
            loop@ for (appliedgiftmodel in appliedgift_list){

                if(appliedgiftmodel.actual_gift_value.trim().toDouble() >= remaining_balance){
                    // balance is enough no other required
                    appliedgiftmodel.applied_gift_value=remaining_balance

                    val inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflater.inflate(R.layout.raw_giftdiscount, null)
                    view.discountgift_txt.text = if(appliedgiftmodel.gift_type == Constants.EATMORE ) "Eatmore Giftcard"  else if(appliedgiftmodel.gift_type == Constants.RESTAURANT ) "Restaurant Giftcard" else appliedgiftmodel.gift_type
                    view.discountgift_value.text = String.format(getString(R.string.discount), appliedgiftmodel.applied_gift_value)
                    discountgift_layout.addView(view)

                    expandbtnName=appliedgiftmodel.gift_type

                    break@loop

                }else{
                    // balance is not enough
                    remaining_balance = remaining_balance - appliedgiftmodel.actual_gift_value.trim().toDouble()
                    appliedgiftmodel.applied_gift_value=appliedgiftmodel.actual_gift_value.trim().toDouble()

                    val inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflater.inflate(R.layout.raw_giftdiscount, null)
                    view.discountgift_txt.text = if(appliedgiftmodel.gift_type == Constants.EATMORE ) "Eatmore Giftcard"  else if(appliedgiftmodel.gift_type == Constants.RESTAURANT ) "Restaurant Giftcard" else appliedgiftmodel.gift_type
                    view.discountgift_value.text = String.format(getString(R.string.discount), appliedgiftmodel.applied_gift_value)
                    discountgift_layout.addView(view)

                    expandbtnName=""
                }

            }
        }



        // expand gift balance button


        for (paymentInfoModel in paymentinfo_list) {
            if (paymentInfoModel.payment_type.trim() == expandbtnName.trim()) {
                paymentInfoModel.walletBtn_expand=true
            }else{
                paymentInfoModel.walletBtn_expand=false
            }
        }

    }


    class MyClickHandler(val paymentmethod: Paymentmethod) {


        fun onClick(view: View, value: String, model: PaymentInfoModel) {
            Log.e("TAG", "click--" + model)

            when (value) {


                "02" -> {
                    model.gift_expand = if (model.gift_expand) false else true
                    model.error_expand = false
                    paymentmethod.mAdapter.notifyDataSetChanged()
                }
                "04" -> {
                    // proceed next online/cash.
                    if (model.payment_type == paymentmethod.getString(R.string.online_payment)) {
                        // online

                      //  paymentmethod.whatisthePaymethod=WhatIsThePaymethod.ONLINE
                        paymentmethod.continuefromOnline()

                    } else {
                        //cash
                        //paymentmethod.whatisthePaymethod=WhatIsThePaymethod.CASH
                        paymentmethod.continuefromCash()

                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        logd(Menu.TAG, "on destroy...")
    }


    override fun onDestroyView() {

        logd(TAG, "onDestroyView...")

        timeoutHandler.removeCallbacks(finalizer)


        call_checkout?.let {
            clearProgressDialog()
            it.cancel()
        }
        call_applycode?.let {
            it.cancel()
        }
        call_cancelorder?.let {
            it.cancel()
        }

        super.onDestroyView()
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