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
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
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
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.cart.fragment.Extratoppings
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.adapter.PaymentmethodAdapter
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.databinding.DeliverytimeslotBinding
import dk.eatmore.foodapp.databinding.PaymentmethodBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.ProductInfo.Menu
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.paymentmethod.*
import kotlinx.android.synthetic.main.toolbar_plusone.*


class Paymentmethod : BaseFragment() {

    var transition: Transition? = null
    private lateinit var binding: PaymentmethodBinding
    private var timeslot: ArrayList<String>?=null
    private var selectedtimeslot_position : Int = 0
    private lateinit var mAdapter : PaymentmethodAdapter
    lateinit var currentView: String






    companion object {
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
            currentView=Constants.PAYMENTMETHOD
            setToolbarforThis()
            proceed_view.setOnClickListener{(activity as EpayActivity).finishActivity()}
            transaction_statusview.visibility=View.GONE
            processDialog.visibility=View.VISIBLE
          //  fetch_PickupTime()
            recycler_view.apply {
                val list= arrayOfNulls<Int>(2)
                mAdapter = PaymentmethodAdapter(context!!,list, object : PaymentmethodAdapter.AdapterListener {
                    override fun itemClicked(parentView: Boolean, position: Int) {
                        currentView=Constants.PROGRESSDIALOG
                        (activity as EpayActivity).img_toolbar_back.setImageResource(R.drawable.close)
                        (activity as EpayActivity).img_toolbar_back.setOnClickListener{
                            if(currentView !=Constants.PROGRESSDIALOG)
                            (activity as EpayActivity).finishActivity()
                        }
                        showComponents()
                    }
                })
                layoutManager = LinearLayoutManager(context)
                adapter = mAdapter
            }






        }else{
            (activity as EpayActivity).popWithTag(Paymentmethod.TAG)
        }
    }


    private fun fetch_PickupTime() {

  /*      callAPI(ApiCall.getPickuptime(
                r_token = Constants.R_TOKEN,
                r_key = Constants.R_KEY,
                shipping = "Pickup",
                language = "en"
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if (jsonobject.get("status").asBoolean) {
                    timeslot = ArrayList()
                    for (i in 0 until jsonobject.getAsJsonArray("times").size()) {
                        timeslot!!.add(jsonobject.getAsJsonArray("times")[i].asJsonObject.get("dt").asString)
                    }
                    delivery_time_slot.text=timeslot!![0]
                    binding.isLoading=false

                }else{
                    showSnackBar(address_container, getString(R.string.error_404))
                    binding.isLoading=false

                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(address_container, getString(R.string.error_404))
                        binding.isLoading=false
                    }
                    100 -> {
                        showSnackBar(address_container, getString(R.string.internet_not_available))
                        binding.isLoading=false

                    }
                }
                //showProgressDialog()


            }
        })*/


    }



    private fun showComponents(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.e("run","success---")
            val constraintSet = ConstraintSet()
            constraintSet.clone(activity, R.layout.transaction_status)

            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(1.0f)
             transition.duration = 800
                transition.setInterpolator( FastOutSlowInInterpolator());


            TransitionManager.beginDelayedTransition(constraint,transition)
            constraintSet.applyTo(constraint) //here constraint is the name of view to which we are applying the constraintSet
            Handler().postDelayed({
                transaction_statusview.visibility=View.VISIBLE
                processDialog.visibility=View.GONE

                lottie_transaction_status.visibility=View.VISIBLE
                lottie_transaction_status.scale=0.4f
                status_view.visibility=View.INVISIBLE

                status_icon.setImageResource(R.drawable.animated_vector_cross)
                (status_icon.getDrawable() as Animatable).start()

                lottie_transaction_status.playAnimation()
                val v : Vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                else v.vibrate(200);
                addspantext()
                currentView=Constants.PAYMENTSTATUS


            },4000)


        }


    }


    fun addspantext(){
        val span = SpannableString(getString(R.string.if_you_have_any_questions)+ " " + "88826543")
        span.setSpan(clickableSpan, getString(R.string.if_you_have_any_questions).trim({ it <= ' ' }).length+1,
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
        (activity as EpayActivity).txt_toolbar.text = ""
        (activity as EpayActivity).img_toolbar_back.setOnClickListener { onBackpress() }
    }

    fun onBackpress() {
        val frag = (activity as EpayActivity).supportFragmentManager.findFragmentByTag(DeliveryTimeslot.TAG)
        (frag as DeliveryTimeslot).setToolbarforThis()
        (activity as EpayActivity).popFragment()

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