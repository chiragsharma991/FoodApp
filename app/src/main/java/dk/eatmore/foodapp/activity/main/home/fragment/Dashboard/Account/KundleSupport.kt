package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.app.ProgressDialog
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.KundlesupportBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.toolbar.*
import zendesk.core.Zendesk
import zendesk.core.AnonymousIdentity
import zendesk.support.request.RequestActivity
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk.getApplicationContext
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import com.zendesk.belvedere.Belvedere
import com.zendesk.belvedere.BelvedereCallback
import com.zendesk.belvedere.BelvedereResult
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.ImageLoader
import kotlinx.android.synthetic.main.kundlesupport.*
import zendesk.support.*
import java.net.URL
import java.util.ArrayList


class KundleSupport : BaseFragment() {


    private lateinit var binding: KundlesupportBinding
    private var uploadProvider: UploadProvider? = null
    private var requestProvider: RequestProvider? = null
    private var belvedere: Belvedere? = null
    private val DEFAULT_MIMETYPE = "application/octet-stream"
    private val attachmentsUploaded = ArrayList<String>()


    companion object {
        val TAG = "KundleSupport"
        private val SUBDOMAIN_URL = "https://xyz5070.zendesk.com"
        private val APPLICATION_ID = "5607f30269e67f046f086eae038d6c1abf60d0e6490a03ae"
        private val OAUTH_CLIENT_ID = "mobile_sdk_client_e5c9b367c7d7adf62d77"

        fun newInstance(): KundleSupport {
            return KundleSupport()
        }

    }


    override fun getLayout(): Int {
        return R.layout.kundlesupport
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root


    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            txt_toolbar.text = getString(R.string.kundle_support)
            txt_toolbar_right.setOnClickListener { (activity as HomeActivity).onBackPressed() }
            Zendesk.INSTANCE.init(context!!, "https://xyz5070.zendesk.com", "5607f30269e67f046f086eae038d6c1abf60d0e6490a03ae", "mobile_sdk_client_e5c9b367c7d7adf62d77")
            val identity = AnonymousIdentity()
            Zendesk.INSTANCE.setIdentity(identity)
            Support.INSTANCE.init(Zendesk.INSTANCE)
            initializeZendeskProviders()
            initializeBelvedereFilePicker()
            val fragmentManager = childFragmentManager
          //  attach_fab.setOnClickListener { belvedere!!.showDialog(fragmentManager) }
            submit_btn.setOnClickListener { submitdata() }



            // RequestActivity.builder().show(context!!)
/*
            RequestActivity.builder()
                    .withRequestSubject("Testing Support SDK 2.0")
                    .withTags("2.0", "testing")
                    .show(context!!);*/
/*

            val requestActivityIntent = RequestActivity.builder()
                    .withRequestSubject("Testing Support SDK 2.0")
                    .withTags("2.0", "testing")
                    .intent(context)
*/


        } else {
            logd(TAG, "saveInstance NOT NULL")
            // (activity as HomeActivity).popWithTag(HealthReport.TAG)
        }
    }

    private fun initializeZendeskProviders() {
        uploadProvider = Support.INSTANCE.provider()!!.uploadProvider()
        requestProvider = Support.INSTANCE.provider()!!.requestProvider()
    }

    private fun initializeBelvedereFilePicker() {
        belvedere = Belvedere.from(context!!)
                .withContentType("image/*")
                .withAllowMultiple(true)
                .build()
    }


    private fun buildCreateRequest(): CreateRequest {
        val request = CreateRequest()
        request.setDescription(desc_edt.getText().toString())
        request.setSubject(email_edt.getText().toString())
        request.setAttachments(attachmentsUploaded)
        return request
    }

    private fun submitdata() {
        showProgressDialog()
        val request = buildCreateRequest()

        requestProvider!!.createRequest(request, object : ZendeskCallback<Request>() {
            override fun onSuccess(request: Request) {
                showProgressDialog()
                // Clear form
                email_edt.setText("")
                desc_edt.setText("")
                attachmentsUploaded.clear()
                // Clear list of uploaded attachments
            }

            override fun onError(errorResponse: ErrorResponse) {
                showProgressDialog()
                Snackbar.make(kundlesupport_container, "Request creation failed: " + errorResponse.reason, Snackbar.LENGTH_LONG).show()

            }
        })

    }

    fun refreshview(){
/*        recycler_view.apply {
            mAdapter = UniversalAdapter(context!!, list, R.layout.row_attached_img, object : RecyclerCallback<RowOrderedPizzaBinding, Orderresult> {
                override fun bindData(binder: RowOrderedPizzaBinding, model: Orderresult) {
                    binder.orderresult = model
                    binder.util = BindDataUtils
                    binder.myclickhandler = myclickhandler
                    binder.executePendingBindings()
                    //   binder.handler=this@OrderFragment
                }
            })
            layoutManager = LinearLayoutManager(getActivityBase())
            adapter = mAdapter
        }*/


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        loge(TAG, "onActivityResult--")

        belvedere!!.getFilesFromActivityOnResult(requestCode, resultCode, data, object : BelvedereCallback<List<BelvedereResult>>() {
            override fun success(belvedereResults: List<BelvedereResult>?) {
                loge(TAG, "getFilesFromActivityOnResult--")

                if (belvedereResults != null && belvedereResults.size > 0) {
                    //  progressDialog("Uploading your attachments...").show()
                    showProgressDialog()
                } else {
                    return
                }

                var i = 0
                val limit = belvedereResults.size
                while (i < limit) {

                    val file = belvedereResults[i]
                    loge(TAG, "file-" + file.uri)
                 /*   Glide.with(context!!)
                            .load(file.uri)
                            .apply(RequestOptions().error(BindDataUtils.getRandomDrawbleColor()))
                            .into(imageview)*/

                    uploadProvider!!.uploadAttachment(
                            file.file.name,
                            file.file,
                            getMimeType(getApplicationContext(), file.uri)!!,
                            object : ZendeskCallback<UploadResponse>() {
                                override fun onSuccess(uploadResponse: UploadResponse?) {
                                    if (uploadResponse != null && uploadResponse.attachment != null) {
                                        attachmentsUploaded.add(uploadResponse.token!!)
                                        Log.e(TAG, String.format("onSuccess: Image successfully uploaded: %s",
                                                uploadResponse.attachment!!.contentUrl))





                                        showProgressDialog()

                                    }
                                    // Make sure to keep track of how many requests are in progress

                                }

                                override fun onError(errorResponse: ErrorResponse) {
                                    loge(TAG, "onError--  onActivityResult--")

                                    showProgressDialog()

                                    // Make sure to keep track of how many requests are in progress

                                }
                            })

                    i++
                }
            }
        })
    }


    private fun getMimeType(context: Context, file: Uri?): String? {
        val cr = context.getContentResolver()
        return if (file != null) cr.getType(file) else DEFAULT_MIMETYPE
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
