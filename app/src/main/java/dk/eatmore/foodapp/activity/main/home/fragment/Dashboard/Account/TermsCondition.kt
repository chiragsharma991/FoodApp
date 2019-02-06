package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bambora.nativepayment.handlers.BNPaymentHandler
import com.bambora.nativepayment.models.creditcard.CreditCard
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_profile_edit.*
import kotlinx.android.synthetic.main.fragment_signup.*
import java.util.ArrayList
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.annotation.TargetApi
import android.widget.Toast
import android.webkit.WebViewClient
import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.*
import dk.eatmore.foodapp.rest.ApiClient
import kotlinx.android.synthetic.main.termscondition.*
import kotlinx.android.synthetic.main.toolbar.*


class TermsCondition : BaseFragment() {


    private lateinit var binding: TermsconditionBinding


    companion object {
        // id =0 (termsofservices) || id =1 (cokkie_policy)
        var fragment_id= 0
        val TAG = "TermsCondition"
        fun newInstance(fragment_id : Int): TermsCondition {
            this.fragment_id = fragment_id
            return TermsCondition()
        }

    }


    override fun getLayout(): Int {
        return R.layout.termscondition
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            if(fragment_id==0){
                //termsofservices
                txt_toolbar.text=getString(R.string.terms_of_services)
                setwebview()
            }else{
                //cokkie_policy
                txt_toolbar.text=getString(R.string.cokkie_policy)
                setwebview()
            }

        }else{
            logd(TAG,"saveInstance NOT NULL")
            // (activity as HomeActivity).popWithTag(HealthReport.TAG)
        }
    }

    fun setwebview(){

        webview.getSettings().setJavaScriptEnabled(true) // enable javascript
        webview.loadUrl(if(fragment_id ==0) ApiClient.TERMS_CONDITION else ApiClient.COOKIES_POLICY)
        //http://eatmore.dk/web-view/t-o-s
        webview.setWebViewClient(WebViewController())
    }


    inner class WebViewController : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            loge(TAG,"shouldOverrideUrlLoading---")
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progress_bar.visibility=View.GONE
        }


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progress_bar.visibility=View.VISIBLE
        }


    }



    fun setToolbarforThis(){

    }

    fun onBackpress(){

        if (webview != null && webview.canGoBack()) {
            webview.goBack();
        } else {
        }

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




}
