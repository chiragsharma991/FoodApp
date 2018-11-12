package dk.eatmore.foodapp.activity.main.epay.fragment

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.databinding.BamborawebfunctionBinding
import dk.eatmore.foodapp.databinding.TransactionStatusBinding
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.CartListFunction
import dk.eatmore.foodapp.utils.Constants
import eu.epay.library.EpayWebView
import eu.epay.library.PaymentResultListener
import kotlinx.android.synthetic.main.bamborawebfunction.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import kotlinx.android.synthetic.main.transaction_status.*
import java.util.HashMap

 class BamboraWebfunction : BaseFragment(), PaymentResultListener {


     private lateinit var binding: BamborawebfunctionBinding
     private val data = HashMap<String, String>()

     companion object {
         val TAG = "BamboraWebfunction"



         fun newInstance(): BamboraWebfunction {
             return BamboraWebfunction()
         }
     }

     override fun getLayout(): Int {
         return R.layout.bamborawebfunction
     }


     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
         return binding.root

     }


     override fun initView(view: View?, savedInstanceState: Bundle?) {
         if (savedInstanceState == null) {
             logd(TAG, "saveInstance NULL")
             setToolbarforThis()
             progresswheel(progresswheel,true)
             checkout_delivery()

         }
     }
     private fun checkout_delivery() {

         callAPI(CartListFunction.getcartpaymentAttributes(context!!)!!, object : BaseFragment.OnApiCallInteraction {

             override fun <T> onSuccess(body: T?) {
                 val jsonobject = body as JsonObject
                 if(jsonobject.get(Constants.STATUS).asBoolean){
                     EpayActivity.paymentattributes.order_no=jsonobject.get(Constants.ORDER_NO).asInt
                     if(jsonobject.has(Constants.EPAY_MERCHANT)) EpayActivity.paymentattributes.epay_merchant=jsonobject.get(Constants.EPAY_MERCHANT).asString
                     EpayActivity.paymentattributes.final_amount=jsonobject.get(Constants.ORDER_TOTAL).asDouble
                     loge(TAG,"final amount is "+EpayActivity.paymentattributes.final_amount)
                     if(EpayActivity.paymentattributes.final_amount <= 0.0){
                         progresswheel(progresswheel,false)
                         (activity as EpayActivity).addFragment(R.id.epay_container,TransactionStatus.newInstance(),TransactionStatus.TAG,true)
                     }else{
                         val paymentView = EpayWebView(this@BamboraWebfunction, webiview, false)
                         paymentView.LoadPaymentWindow(getData())                     }

                 }else
                 {
                     showSnackBar(transaction_constraint, getString(R.string.error_404))
                     progresswheel(progresswheel,false)

                 }
             }

             override fun onFail(error: Int) {
                 when (error) {
                     404 -> {
                         showSnackBar(bambora_container, getString(R.string.error_404))
                     }
                     100 -> {
                         showSnackBar(bambora_container, getString(R.string.internet_not_available))

                     }
                 }
                 progresswheel(progresswheel,false)

             }
         })
     }



     fun setToolbarforThis() {
         (activity as EpayActivity).txt_toolbar.text = ""
      //   (activity as EpayActivity).img_toolbar_back.setOnClickListener { onBackpress() }
     }

     fun onBackpress() {
         (activity as EpayActivity).popFragment()
         val fragment= (activity as EpayActivity).supportFragmentManager.findFragmentByTag(Paymentmethod.TAG)
         (fragment as Paymentmethod).onlineTransactionFailed()

     }

    interface AdapterListener {
        fun itemClicked(parentView : Boolean , position : Int)
    }


    override fun PaymentWindowLoaded() {
        Log.e("epay--","PaymentWindowLoaded")
        progresswheel(progresswheel,false)
    }

    override fun PaymentAccepted(map: MutableMap<String, String>?) {
        Log.e("epay--","PaymentAccepted")
        progresswheel(progresswheel,false)
        EpayActivity.paymentattributes.cardno=map!!.get(Constants.CARDNO).toString()
        EpayActivity.paymentattributes.txnid=map.get(Constants.TXNID).toString()
        EpayActivity.paymentattributes.paymenttype=map.get(Constants.PAYMENTTYPE).toString()
        EpayActivity.paymentattributes.txnfee=map.get(Constants.TXNFEE).toString()
        (activity as EpayActivity).addFragment(R.id.epay_container,TransactionStatus.newInstance(),TransactionStatus.TAG,true)

    }

    override fun PaymentWindowCancelled() {
        progresswheel(progresswheel,false)
        (activity as EpayActivity).popFragment()
        val fragment= (activity as EpayActivity).supportFragmentManager.findFragmentByTag(Paymentmethod.TAG)
        (fragment as Paymentmethod).onlineTransactionFailed()
    }

    override fun Debug(p0: String?) {
        Log.e("epay--","Debug")

    }

    override fun ErrorOccurred(p0: Int, p1: String?, p2: String?) {
        Log.e("epay--","ErrorOccurred")
        progresswheel(progresswheel,false)

    }

    override fun PaymentWindowLoading() {
        Log.e("epay--","PaymentWindowLoading")

    }

    override fun PaymentLoadingAcceptPage() {
        Log.e("epay--","PaymentLoadingAcceptPage")
    }

    fun getData(): Map<String, String> {

        data.put("merchantnumber", Constants.EPAY_MERCHANT_TEST)

        //http://tech.epay.dk/en/specification#259
        data.put("currency", "DKK")

        //http://tech.epay.dk/en/specification#260
        data.put("amount",((EpayActivity.paymentattributes.final_amount *100).toInt()).toString() )

        //Random r = new Random();
        //int tempOrderno = r.nextInt(80 - 65) + 61235;

        //http://tech.epay.dk/en/specification#261
        data.put("orderid", EpayActivity.paymentattributes.order_no.toString())

        //http://tech.epay.dk/en/specification#262
        //data.put("windowid", "1");

        //http://tech.epay.dk/en/specification#263
        data.put("paymentcollection", "0")

        //http://tech.epay.dk/en/specification#264
        data.put("lockpaymentcollection", "0")

        //http://tech.epay.dk/en/specification#265
        //data.put("paymenttype", "1,2,3");

        //http://tech.epay.dk/en/specification#266
        data.put("language", "0")

        //http://tech.epay.dk/en/specification#267
        data.put("encoding", "UTF-8")

        //http://tech.epay.dk/en/specification#269
        //data.put("mobilecssurl", "");

        //http://tech.epay.dk/en/specification#270
        // Authorize
        data.put("instantcapture", "0")

        // Direct Capture
        //data.put("instantcapture", "1");

        //http://tech.epay.dk/en/specification#272
        //data.put("splitpayment", "0");

        //http://tech.epay.dk/en/specification#275
        //data.put("callbackurl", "");

        //http://tech.epay.dk/en/specification#276
        data.put("instantcallback", "1")

        //http://tech.epay.dk/en/specification#278
        //data.put("ordertext", "");

        //http://tech.epay.dk/en/specification#279
        //data.put("group", "group");

        //http://tech.epay.dk/en/specification#280
        //data.put("description", "");

        //http://tech.epay.dk/en/specification#281
        //data.put("hash", "");

        //http://tech.epay.dk/en/specification#282
        //data.put("subscription", "0");

        //http://tech.epay.dk/en/specification#283
        //data.put("subscriptionname", "0");

        //http://tech.epay.dk/en/specification#284
        //data.put("mailreceipt", "");

        //http://tech.epay.dk/en/specification#286
        //data.put("googletracker", "0");

        //http://tech.epay.dk/en/specification#287
        //data.put("backgroundcolor", "");

        //http://tech.epay.dk/en/specification#288
        //data.put("opacity", "");

        //http://tech.epay.dk/en/specification#289
        //data.put("declinetext", "");

        data.put("ownreceipt", "1")

        Log.e("payment raw data", "payment$data")

        return data
    }

}