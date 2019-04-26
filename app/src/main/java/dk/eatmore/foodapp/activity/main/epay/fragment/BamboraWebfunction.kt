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
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.BamborawebfunctionBinding
import dk.eatmore.foodapp.databinding.TransactionStatusBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.epay.ResultItem
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.CartListFunction
import dk.eatmore.foodapp.utils.Constants
import eu.epay.library.EpayWebView
import eu.epay.library.PaymentResultListener
import kotlinx.android.synthetic.main.bamborawebfunction.*
import kotlinx.android.synthetic.main.fragment_home_container.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import kotlinx.android.synthetic.main.transaction_status.*
import java.util.HashMap

class BamboraWebfunction : BaseFragment(), PaymentResultListener {


    private lateinit var binding: BamborawebfunctionBinding
    private val data = HashMap<String, String>()
    private var isprocess_on : Boolean = true  // some time payment accept method calls multiple times so we are managing using this variable.

    private var appliedgift_list: ArrayList<Paymentmethod.AppliedGiftModel> = ArrayList()
    private var addedDiscount_amount : Double =0.0
    private var addedDiscount_type : String=""
    private var addedDiscount_id : String=""
    private var addedProductlist: ArrayList<ResultItem> = arrayListOf()


    companion object {
        val TAG = "BamboraWebfunction"


        fun newInstance(addedProductlist: ArrayList<ResultItem>, addedDiscount_amount : Double, addedDiscount_type : String,addedDiscount_id : String, appliedgift_list: ArrayList<Paymentmethod.AppliedGiftModel>): BamboraWebfunction {

            val fragment = BamboraWebfunction()
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

            addedProductlist=arguments!!.getSerializable("addedProductlist") as ArrayList<ResultItem>
            addedDiscount_amount=arguments!!.getSerializable("addedDiscount_amount") as Double
            addedDiscount_type=arguments!!.getSerializable("addedDiscount_type") as String
            appliedgift_list=arguments!!.getSerializable("appliedgift_list") as ArrayList<Paymentmethod.AppliedGiftModel>
            addedDiscount_id=arguments!!.getSerializable("addedDiscount_id") as String

            progress_bar.visibility=View.VISIBLE
            val paymentView = EpayWebView(this@BamboraWebfunction, webiview, false)
            paymentView.LoadPaymentWindow(getData())
          //  checkout_delivery()
        }
    }
/*
    private fun checkout_delivery() {

        callAPI(CartListFunction.getcartpaymentAttributes(context!!)!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonobject = body as JsonObject
                if(jsonobject.get(Constants.STATUS).asBoolean){
                    EpayFragment.paymentattributes.order_no=jsonobject.get(Constants.ORDER_NO).asInt
                    if(jsonobject.has(Constants.EPAY_MERCHANT)) EpayFragment.paymentattributes.epay_merchant=jsonobject.get(Constants.EPAY_MERCHANT).asString
                    EpayFragment.paymentattributes.final_amount=jsonobject.get(Constants.ORDER_TOTAL).asDouble
                    loge(TAG,"final amount is "+EpayFragment.paymentattributes.final_amount)
                    if(EpayFragment.paymentattributes.final_amount <= 0.0){
                        progresswheel(progresswheel,false)
                        (parentFragment as EpayFragment).addFragment(R.id.epay_container,TransactionStatus.newInstance(),TransactionStatus.TAG,true)

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
*/



    fun setToolbarforThis() {
        showTabBar(false)
        txt_toolbar.text = getString(R.string.payment)
        img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
    }

    fun onBackpress()  {

        if(progress_bar.visibility == View.VISIBLE) return
        (parentFragment as EpayFragment).popFragment()
        val fragment= (parentFragment as EpayFragment).childFragmentManager.findFragmentByTag(Paymentmethod.TAG)
        (fragment as Paymentmethod).onlineTransactionFailed()
        showTabBar(true)
    }

    interface AdapterListener {
        fun itemClicked(parentView : Boolean , position : Int)
    }

    override fun PaymentWindowLoaded() {
        Log.e("epay--","PaymentWindowLoaded")
        progress_bar.visibility=View.GONE
    }

    override fun PaymentAccepted(map: MutableMap<String, String>?) {
        Log.e("epay--","PaymentAccepted")
        if(isprocess_on == true){
            progress_bar.visibility=View.GONE
            EpayFragment.paymentattributes.cardno=map!!.get(Constants.CARDNO).toString()
            EpayFragment.paymentattributes.txnid=map.get(Constants.TXNID).toString()
            EpayFragment.paymentattributes.paymenttype=map.get(Constants.PAYMENTTYPE).toString()
            EpayFragment.paymentattributes.txnfee=map.get(Constants.TXNFEE).toString()
            (parentFragment as EpayFragment).addFragment(R.id.epay_container,TransactionStatus.newInstance(addedProductlist,addedDiscount_amount,addedDiscount_type,addedDiscount_id,appliedgift_list),TransactionStatus.TAG,true)
            isprocess_on=false
        }

    }

    override fun PaymentWindowCancelled() {
        progress_bar.visibility=View.GONE
        (parentFragment as EpayFragment).popFragment()
        val fragment= (parentFragment as EpayFragment).childFragmentManager.findFragmentByTag(Paymentmethod.TAG)
        (fragment as Paymentmethod).onlineTransactionFailed()
    }

    override fun Debug(p0: String?) {
        Log.e("epay--","Debug")
    }

    override fun ErrorOccurred(p0: Int, p1: String?, p2: String?) {
        Log.e("epay--","ErrorOccurred")
        progress_bar.visibility=View.GONE

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
        data.put("amount",((EpayFragment.paymentattributes.final_amount *100).toInt()).toString() )

        //Random r = new Random();
        //int tempOrderno = r.nextInt(80 - 65) + 61235;

        //http://tech.epay.dk/en/specification#261
        data.put("orderid", EpayFragment.paymentattributes.order_no.toString())

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