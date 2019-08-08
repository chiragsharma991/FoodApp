package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bambora.nativepayment.handlers.BNPaymentHandler
import com.bambora.nativepayment.models.creditcard.CreditCard
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentProfileEditBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
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
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.HealthreportBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.healthreport.*
import kotlinx.android.synthetic.main.toolbar.*


class HealthReport : BaseFragment() {


    private lateinit var binding: HealthreportBinding



    companion object {

        val TAG = "HealthReport"
        fun newInstance(health_report_link : String): HealthReport {
            val fragment =HealthReport()
            val bundle = Bundle()
            bundle.putString(Constants.HEALTH_REPORT_LINK,health_report_link)
            fragment.arguments = bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.healthreport
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            img_toolbar_back.setImageResource(R.drawable.back)
            txt_toolbar.text=getString(R.string.health_report)
            img_toolbar_back.setOnClickListener {
                onBackpress()
            }
            setwebview()
        }else{
            logd(TAG,"saveInstance NOT NULL")
           // (activity as HomeActivity).popWithTag(HealthReport.TAG)
        }
    }

    fun setwebview(){

        webview.getSettings().setJavaScriptEnabled(true) // enable javascript
        loge(TAG,"url is-"+arguments!!.getString(Constants.HEALTH_REPORT_LINK) )
        webview.loadUrl(arguments!!.getString(Constants.HEALTH_REPORT_LINK) as String)
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
        (activity as HomeActivity).onBackPressed()
        /*    if (webview != null && webview.canGoBack()) {
                webview.goBack();
            } else {
                val homefragment : HomeFragment = ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment()
                homefragment.popFragment()
            }*/
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
