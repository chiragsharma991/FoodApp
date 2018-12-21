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
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
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
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Menu
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.adapter.PaymentmethodAdapter
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.databinding.DeliverytimeslotBinding
import dk.eatmore.foodapp.databinding.PaymentmethodBinding
import dk.eatmore.foodapp.databinding.TransactionStatusBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.CartListFunction
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.deliverytimeslot.*
import kotlinx.android.synthetic.main.paymentmethod.*
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
        var moveonsearch =false


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
            val myclickhandler=MyClickHandler(this)
            binding.statusIs = false
            binding.transactionhandler=myclickhandler
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
            },800)

        }
    }

    private fun statusfrom_cash(){

            currentView = Constants.PAYMENTSTATUS
            binding.statusIs = true
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

                val jsonobject = body as JsonObject
                if (jsonobject.get(Constants.STATUS).asBoolean) {
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


    fun addspantext() {
        val span = SpannableString(getString(R.string.if_you_have_any_questions) + " " + "88826543")
        span.setSpan(clickableSpan, getString(R.string.if_you_have_any_questions).trim({ it <= ' ' }).length + 1,
                getString(R.string.if_you_have_any_questions).trim({ it <= ' ' }).length + 8 + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        phone_txt.text = span
        phone_txt.movementMethod = LinkMovementMethod.getInstance()

    }

    val clickableSpan = object : ClickableSpan() {
        var dialog: AlertDialog? = null
        override fun onClick(textView: View) {
            Log.e(TAG, "onClick:--- ")
            dialog = AlertDialog.Builder(activity).setMessage("Do you want to call ${"88826543"}").setCancelable(true).setPositiveButton("yes") { dialogInterface, i ->
                if (isPermissionGranted()) {
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
            1 -> {

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
            when(currentView){
                Constants.PROGRESSDIALOG ->{ }
                Constants.PAYMENTSTATUS -> {
                    onBackpress()
                }
            }
        }
    }

    fun onBackpress() {
        //(activity as EpayActivity).popFragment()
        moveonsearch=true
        val fragment = (parentFragment as EpayFragment).parentFragment
        if(fragment is HomeFragment){
            fragment.popAllFragment()
        }else{
            // order fragment : Reorder
            (fragment as OrderFragment).popAllFragment()
        }
        if(OrderFragment.ui_model?.reloadfragment !=null) OrderFragment.ui_model!!.reloadfragment.value=true
        if(HomeFragment.ui_model?.reloadfragment !=null) HomeFragment.ui_model!!.reloadfragment.value=true  // reload last order from homefragment.
        showTabBar(true)


    }

    class MyClickHandler(val transactionstatus: TransactionStatus) {


        fun onClick(view: View, value: String) {
            when (value) {
                "0" -> {
                    transactionstatus.onBackpress()
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