package dk.eatmore.foodapp.activity.main.epay.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.support.constraint.ConstraintSet
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.cart.fragment.Extratoppings
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.HomeActivity
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
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.deliverytimeslot.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.paymentmethod.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import retrofit2.Call


class Paymentmethod : BaseFragment(), TextWatcher {

    var transition: Transition? = null
    private lateinit var binding: PaymentmethodBinding
    private var timeslot: ArrayList<String>?=null
    private var selectedtimeslot_position : Int = 0
    lateinit var currentView: String
    private var call_checkout  : Call<JsonObject>? =null
    private var call_applycode  : Call<JsonObject>? =null
    private var call_cancelorder  : Call<JsonObject>? =null



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
            binding.executePendingBindings()
            //setanim_toolbartitle(appbar,(activity as EpayActivity).txt_toolbar,getString(R.string.payment))
            applyonlinegift_edt.addTextChangedListener(this)
            applycashgift_edt.addTextChangedListener(this)
            paymentmethod_visible_are()
            setToolbarforThis()
            showproductInfo()
            addspantext()

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

        if(EpayFragment.isPickup){
            // pick up:

            subtotal_layout.visibility=View.VISIBLE
            restuptominimum_layout.visibility=if(EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility=View.GONE
            additional_charge_layout.visibility=if(EpayFragment.paymentattributes.additional_charges_cash.toDouble() <= 0.0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility=View.VISIBLE
            subtotal_txt.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.order_total)
            restuptominimum_txt.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            additional_charge_txt.text=if(isPaymentonline) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_online) else BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_cash)
            discountcoupan_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))
            discountgift_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))

            if(giftcardis ==Constants.GIFTCARD ){
                discountgift_layout.visibility=if(EpayFragment.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountcoupan_layout.visibility=View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + if(isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
                                - EpayFragment.paymentattributes.discount_amount)

            }else if(giftcardis ==Constants.COUPON){
                discountcoupan_layout.visibility=if(EpayFragment.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountgift_layout.visibility=View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble() - EpayFragment.paymentattributes.discount_amount)
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + if(isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())

            }else{
                discountgift_layout.visibility=View.GONE
                discountcoupan_layout.visibility=View.GONE
                final_amount = (
                        EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + if(isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())

            }

            total_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f",final_amount))
            online_btn.text = if(final_amount <= 0.0) getString(R.string.confirm) else getString(R.string.pay)

        }

        //--------------------------------------//---------------------------------------------//


        else{
            // delivery :
            subtotal_layout.visibility=View.VISIBLE
            restuptominimum_layout.visibility=if(EpayFragment.paymentattributes.upto_min_shipping.toDouble() <= 0.0) View.GONE else View.VISIBLE
            shipping_layout.visibility=if(EpayFragment.paymentattributes.shipping_charge.toDouble() <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility=if(EpayFragment.paymentattributes.additional_charges_online.toDouble() <= 0.0) View.GONE else View.VISIBLE
            total_layout.visibility=View.VISIBLE
            subtotal_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.order_total)
            restuptominimum_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.upto_min_shipping)
            shipping_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.shipping_charge)
            additional_charge_txt.text=if(isPaymentonline) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_online) else BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.additional_charges_cash)
            discountcoupan_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))
            discountgift_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.paymentattributes.discount_amount.toString()))


            if(giftcardis ==Constants.GIFTCARD ){
                discountgift_layout.visibility=if(EpayFragment.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountcoupan_layout.visibility=View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + if(isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
                                - EpayFragment.paymentattributes.discount_amount)

            }else if(giftcardis ==Constants.COUPON){
                discountcoupan_layout.visibility=if(EpayFragment.paymentattributes.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountgift_layout.visibility=View.GONE
                final_amount = (
                        (EpayFragment.paymentattributes.order_total.toDouble() - EpayFragment.paymentattributes.discount_amount)
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + if(isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
            }else{
                discountgift_layout.visibility=View.GONE
                discountcoupan_layout.visibility=View.GONE
                final_amount= (
                        EpayFragment.paymentattributes.order_total.toDouble()
                                + EpayFragment.paymentattributes.upto_min_shipping.toDouble()
                                + EpayFragment.paymentattributes.shipping_charge.toDouble()
                                + if(isPaymentonline) EpayFragment.paymentattributes.additional_charges_online.toDouble() else EpayFragment.paymentattributes.additional_charges_cash.toDouble())
            }

            total_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(String.format("%.2f",final_amount))
            online_btn.text = if(final_amount <= 0.0) getString(R.string.confirm) else getString(R.string.pay)
        }

        // Add restaurant address and time--
        address_txt.text=EpayFragment.paymentattributes.payment_address
        deliverytime_txt.text=EpayFragment.paymentattributes.payment_time
        paymenttype_img.setImageResource(if(EpayFragment.isPickup) R.mipmap.bag else R.mipmap.motorcycle)
        Glide.with(context!!)
                .load(EpayFragment.paymentattributes.online_logo)
                .into(online_payment_icon)
        Glide.with(context!!)
                .load(EpayFragment.paymentattributes.cash_logo)
                .apply(RequestOptions().error(BindDataUtils.getRandomDrawbleColor()))
                .into(cash_payment_icon)



    }


    fun addspantext() {
        val span = SpannableString(getString(R.string.brug_for_hjælp))
        span.setSpan(clickableSpan, (getString(R.string.brug_for_hjælp).trim().length - 8),
                getString(R.string.brug_for_hjælp).trim().length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        eatmore_contact.text = span
        eatmore_contact.movementMethod = LinkMovementMethod.getInstance()
    }

    val clickableSpan = object : ClickableSpan() {
        var dialog: AlertDialog? = null
        override fun onClick(textView: View) {
            Log.e(TAG, "onClick:--- ")
            dialog = AlertDialog.Builder(activity).setMessage("Do you want to call 73702515").setCancelable(true).setPositiveButton("yes") { dialogInterface, i ->
                if (is_callphn_PermissionGranted()) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" +"73702515"))
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
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" +"73702515"))
                    startActivity(intent)
                    //    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    private fun showproductInfo(){

        if(EpayFragment.ui_model!!.viewcard_list.value ==null){
            // this condition will null if all item has been deleted : so just clear view and inflate empty view on screen.
            add_parentitem_view.removeAllViewsInLayout()
            add_parentitem_view.invalidate()
            generateBillDetails(Constants.OTHER)
            return
        }

        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until EpayFragment.ui_model!!.viewcard_list.value!!.result!!.size){
            var inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view= inflater.inflate(R.layout.dynamic_raw_item,null)
            view.remove_item.tag=i
            view.remove_item.visibility=View.GONE
            view.item_name.text=EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].product_name
            view.item_price.text=if(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price !=null) BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].p_price!!) else "null"
            view.add_subitem_view.removeAllViewsInLayout()

            // fill first ingredients size if not null
            for (j in 0 until (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients?.size ?: 0)){
                inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val ingredientview= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                ingredientview.subitem_name.text=String.format(getString(R.string.minues),EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients!!.get(j).ingredient_name)
                ingredientview.subitem_name.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                ingredientview.subitem_price.visibility= View.INVISIBLE
                ingredientview.dummy_image.visibility= View.GONE
                view.add_subitem_view.addView(ingredientview)
            }

            // if attribute is present then fetch extratoppings only from attribute list
            if(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].is_attributes !=null && EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].is_attributes.equals("1")){
                if(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes !=null){
                    for (k in 0 until EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.size){
                        for (l in 0 until (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!![k].order_product_extra_topping_group?.size ?: 0)){
                            inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                            extratoppings.subitem_name.text=String.format(getString(R.string.plus),EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                            extratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
                            extratoppings.dummy_image.visibility= View.GONE
                            view.add_subitem_view.addView(extratoppings)
                        }
                    }
                }
            }
            else
            {
                // if extratopping group only present then add only extratoppings in the list.
                for (k in 0 until (EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group?.size ?:0)){
                    inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val onlyextratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                    onlyextratoppings.subitem_name.text=String.format(getString(R.string.plus),EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).ingredient_name)
                    // view.subitem_price.visibility=View.VISIBLE
                    onlyextratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(EpayFragment.ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                    onlyextratoppings.dummy_image.visibility= View.GONE
                    view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
        }
        generateBillDetails(Constants.OTHER)

    }


    private fun paymentmethod_visible_are(){

        if(isPaymentonline){
            online_method.visibility=View.VISIBLE
            cash_method.visibility=View.GONE
            giftonline_view.visibility=View.GONE
            giftcash_view.visibility=View.GONE
            EpayFragment.paymentattributes.discount_amount=0.0
            EpayFragment.paymentattributes.discount_id=0
            EpayFragment.paymentattributes.discount_type=""
            generateBillDetails(Constants.OTHER)
            progress_applyonlinegift.visibility=View.GONE
            progress_applycashgift.visibility=View.GONE
            error_of_onlinegiftcard.visibility=View.GONE
            error_of_cashgiftcard.visibility=View.GONE
            applycash_txt.visibility=View.VISIBLE
            applyonline_txt.visibility=View.VISIBLE
            online_rht_img.setImageResource(R.drawable.up_arrow)
            cash_rht_img.setImageResource(R.drawable.down_arrow)

        }else{
            online_method.visibility=View.GONE
            cash_method.visibility=View.VISIBLE
            giftonline_view.visibility=View.GONE
            giftcash_view.visibility=View.GONE
            EpayFragment.paymentattributes.discount_amount=0.0
            EpayFragment.paymentattributes.discount_id=0
            EpayFragment.paymentattributes.discount_type=""
            generateBillDetails(Constants.OTHER)
            progress_applyonlinegift.visibility=View.GONE
            progress_applycashgift.visibility=View.GONE
            error_of_onlinegiftcard.visibility=View.GONE
            error_of_cashgiftcard.visibility=View.GONE
            applycash_txt.visibility=View.VISIBLE
            applyonline_txt.visibility=View.VISIBLE
            online_rht_img.setImageResource(R.drawable.down_arrow)
            cash_rht_img.setImageResource(R.drawable.up_arrow)


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
        checkout_delivery()

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
        checkout_pickup()
    }


    private fun checkout_pickup() {
        showProgressDialog()
        call_checkout=CartListFunction.getcartpaymentAttributes(context!!)!!
        callAPI(call_checkout!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
                    if((jsonobject.has(Constants.IS_RESTAURANT_CLOSED) && jsonobject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean == true) &&
                            (jsonobject.has(Constants.PRE_ORDER) && jsonobject.get(Constants.PRE_ORDER).asBoolean == false) ){
                        val msg= if(jsonobject.has(Constants.MSG))jsonobject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                        any_preorder_closedRestaurant(jsonobject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean ,jsonobject.get(Constants.PRE_ORDER).asBoolean ,msg )
                    }else{
                        EpayFragment.paymentattributes.order_no = jsonobject.get(Constants.ORDER_NO).asInt
                        (parentFragment as EpayFragment).addFragment(R.id.epay_container,TransactionStatus.newInstance(),TransactionStatus.TAG,true)
                    }

                } else {
                    val msg= if(jsonobject.has(Constants.MSG))jsonobject.get(Constants.MSG).asString else getString(R.string.error_404)
                    DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),
                            color = ContextCompat.getColor(context!!, R.color.theme_color),msg = msg,
                            title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                        override fun onPositiveButtonClick(position: Int) {
                        }
                        override fun onNegativeButtonClick() {
                        }
                    })
                    //showSnackBar(pamentmethod_container, getString(R.string.error_404))
                }
            }

            override fun onFail(error: Int) {

                if(call_checkout!!.isCanceled){
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


    private fun checkout_delivery() {
        showProgressDialog()

        call_checkout=CartListFunction.getcartpaymentAttributes(context!!)!!
        callAPI(call_checkout!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonobject = body as JsonObject
                if(jsonobject.get(Constants.STATUS).asBoolean){


                    if((jsonobject.has(Constants.IS_RESTAURANT_CLOSED) && jsonobject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean == true) &&
                            (jsonobject.has(Constants.PRE_ORDER) && jsonobject.get(Constants.PRE_ORDER).asBoolean == false) ){
                        val msg= if(jsonobject.has(Constants.MSG))jsonobject.get(Constants.MSG).asString else "Sorry Restaurant has been closed."
                        any_preorder_closedRestaurant(jsonobject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean ,jsonobject.get(Constants.PRE_ORDER).asBoolean ,msg )
                    }else{
                        EpayFragment.paymentattributes.order_no=jsonobject.get(Constants.ORDER_NO).asInt
                        if(jsonobject.has(Constants.EPAY_MERCHANT)) EpayFragment.paymentattributes.epay_merchant=jsonobject.get(Constants.EPAY_MERCHANT).asString
                        EpayFragment.paymentattributes.final_amount=jsonobject.get(Constants.ORDER_TOTAL).asDouble
                        if(EpayFragment.paymentattributes.final_amount <= 0.0){
                            (parentFragment as EpayFragment).addFragment(R.id.epay_container,TransactionStatus.newInstance(),TransactionStatus.TAG,true)
                        }else{
                            (parentFragment as EpayFragment).addFragment(R.id.epay_container,BamboraWebfunction.newInstance(),BamboraWebfunction.TAG,true)
                        }
                    }
                }else
                {
                    // if card is empty then :
                    val msg= if(jsonobject.has(Constants.MSG))jsonobject.get(Constants.MSG).asString else getString(R.string.error_404)
                    DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!,R.color.black),msg = msg,title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                        override fun onPositiveButtonClick(position: Int) {
                            /**TODO  2 CASE : if user is coming from home/order and destroy pop all fragment*/
                        }
                        override fun onNegativeButtonClick() {
                        }
                    })

                }
            }

            override fun onFail(error: Int) {

                if(call_checkout!!.isCanceled){
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
    private fun checkpaymentAttributes () : Boolean? {
        var result = false
        val postParam = JsonObject()
        try {
            postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
            postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
            postParam.addProperty(Constants.FIRST_TIME, EpayFragment.paymentattributes.first_time)
            postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,"") )
            // postParam.addProperty(Constants.POSTAL_CODE, EpayFragment.paymentattributes.postal_code)
            postParam.addProperty(Constants.DISCOUNT_TYPE, EpayFragment.paymentattributes.discount_type)
            postParam.addProperty(Constants.DISCOUNT_AMOUNT, EpayFragment.paymentattributes.discount_amount)
            postParam.addProperty(Constants.DISCOUNT_ID,EpayFragment.paymentattributes.discount_id)
            postParam.addProperty(Constants.SHIPPING, if (EpayFragment.isPickup) context!!.getString(R.string.pickup) else context!!.getString(R.string.delivery))
            postParam.addProperty(Constants.TELEPHONE_NO, EpayFragment.paymentattributes.telephone_no)
            postParam.addProperty(Constants.ORDER_TOTAL, EpayFragment.paymentattributes.order_total)
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
            postParam.addProperty(Constants.ACCEPT_TC, "1")
            postParam.addProperty(Constants.PAYMETHOD, if(Paymentmethod.isPaymentonline) "1" else "2" )
            postParam.addProperty(Constants.EXPECTED_TIME, EpayFragment.paymentattributes.expected_time)
            postParam.addProperty(Constants.COMMENTS, EpayFragment.paymentattributes.comments)
            postParam.addProperty(Constants.DEVICE_TYPE,Constants.DEVICE_TYPE_VALUE)
            postParam.addProperty(Constants.FIRST_NAME, EpayFragment.paymentattributes.first_name)
            postParam.addProperty(Constants.ADDITIONAL_CHARGE, if(Paymentmethod.isPaymentonline) EpayFragment.paymentattributes.additional_charges_online else EpayFragment.paymentattributes.additional_charges_cash)
            postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
            postParam.addProperty(Constants.LANGUAGE, Constants.DA)
            val jsonarray=JsonArray()
            for (i in 0.until(EpayFragment.selected_op_id.size) ){
                val jsonobject= JsonObject()
                jsonobject.addProperty(Constants.OP_ID, EpayFragment.selected_op_id.get(i))
                jsonarray.add(jsonobject)
            }
            postParam.add(Constants.CARTPRODUCTS,jsonarray )

            if(EpayFragment.isPickup){
                //pickup--
                checkout_api=ApiCall.checkout_pickup(postParam)
                result=true

            }else{
                // delivery--
                postParam.addProperty(Constants.ADDRESS, EpayFragment.paymentattributes.address)
                postParam.addProperty(Constants.POSTAL_CODE, EpayFragment.paymentattributes.postal_code)
                postParam.addProperty(Constants.DISTANCE, EpayFragment.paymentattributes.distance)
                postParam.addProperty(Constants.MINIMUM_ORDER_PRICE, EpayFragment.paymentattributes.minimum_order_price)
                postParam.addProperty(Constants.SHIPPING_COSTS, EpayFragment.paymentattributes.shipping_charge)
                postParam.addProperty(Constants.UPTO_MIN_SHIPPING, EpayFragment.paymentattributes.upto_min_shipping)
                postParam.addProperty(Constants.SHIPPING_REMARK, "")
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
        call_applycode= ApiCall.applycode(
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN,"")!!,
                order_total = EpayFragment.paymentattributes.order_total,
                customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!,
                additional_charge = if(isPaymentonline) EpayFragment.paymentattributes.additional_charges_online else EpayFragment.paymentattributes.additional_charges_cash,
                code = if(isPaymentonline)applyonlinegift_edt.text.trim().toString() else applycashgift_edt.text.trim().toString(),
                r_key = PreferenceUtil.getString(PreferenceUtil.R_KEY,"")!!,
                shipping =if(EpayFragment.isPickup) getString(R.string.pickup_caps) else getString(R.string.delivery_caps),
                shipping_costs = EpayFragment.paymentattributes.shipping_charge,
                upto_min_shipping = EpayFragment.paymentattributes.upto_min_shipping
        )
        callAPI(call_applycode!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if(jsonobject.get(Constants.STATUS).asBoolean){
                    loge(TAG,"status is true")
                    EpayFragment.paymentattributes.discount_type=jsonobject.get(Constants.DISCOUNT_TYPE).asString
                    EpayFragment.paymentattributes.discount_amount=jsonobject.get(Constants.DISCOUNT_AMOUNT).asDouble
                    EpayFragment.paymentattributes.discount_id=jsonobject.get(Constants.DISCOUNT_ID).asInt
                    if(EpayFragment.paymentattributes.discount_type == Constants.GIFTCARD)
                        generateBillDetails(Constants.GIFTCARD)
                    else if(EpayFragment.paymentattributes.discount_type == Constants.COUPON)
                        generateBillDetails(Constants.COUPON)
                    error_of_cashgiftcard.setTextColor(ContextCompat.getColor(context!!,R.color.green))
                    error_of_onlinegiftcard.setTextColor(ContextCompat.getColor(context!!,R.color.green))


                }else{
                    loge(TAG,"status is false")
                    EpayFragment.paymentattributes.discount_amount=0.0
                    EpayFragment.paymentattributes.discount_id=0
                    EpayFragment.paymentattributes.discount_type=""
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

                if(call_applycode!!.isCanceled){
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
                progress_applyonlinegift.visibility=View.GONE
                progress_applycashgift.visibility=View.GONE
                applycash_txt.visibility=View.VISIBLE
                applyonline_txt.visibility=View.VISIBLE
            }
        })
    }

  /*  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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


    fun onlineTransactionFailed(){
        // in case user do cancel from transction screen.
        loge(TAG,"onlineTransactionFailed")
        call_cancelorder=ApiCall.cancelordertransaction(
                r_key =PreferenceUtil.getString(PreferenceUtil.R_KEY, "")!!,
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN, "")!!,
                order_no = EpayFragment.paymentattributes.order_no
        )
        callAPI(call_cancelorder!!, object : BaseFragment.OnApiCallInteraction {

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

                if(call_cancelorder!!.isCanceled){
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


    override fun onDestroyView() {

        logd(TAG, "onDestroyView...")

        call_checkout?.let {
            clearProgressDialog()
            it.cancel()
        }
        call_applycode?.let {
            progress_applyonlinegift.visibility=View.GONE
            progress_applycashgift.visibility=View.GONE
            applycash_txt.visibility=View.VISIBLE
            applyonline_txt.visibility=View.VISIBLE
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