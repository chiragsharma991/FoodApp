package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

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
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk.getApplicationContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import com.zendesk.belvedere.Belvedere
import com.zendesk.belvedere.BelvedereCallback
import com.zendesk.belvedere.BelvedereResult
import com.zendesk.logger.Logger
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.RowAttachedImgBinding
import dk.eatmore.foodapp.utils.BindDataUtils
import kotlinx.android.synthetic.main.kundlesupport.*
import org.json.JSONObject
import zendesk.support.*
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern


class KundleSupport : BaseFragment(), TextWatcher {


    private lateinit var binding: KundlesupportBinding
    private var uploadProvider: UploadProvider? = null
    private var requestProvider: RequestProvider? = null
    private var belvedere: Belvedere? = null
    private val DEFAULT_MIMETYPE = "application/octet-stream"
    private val attachmentlist = ArrayList<ImageParmsModel>()
    private val attachedImage = ArrayList<String>()
    var mAdapter: UniversalAdapter<ImageParmsModel, RowAttachedImgBinding>? = null
    private val inputValidStates = HashMap<EditText, Boolean>()


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
            name_edt.requestFocus()
            name_edt.addTextChangedListener(this)
            email_edt.addTextChangedListener(this)
            desc_edt.addTextChangedListener(this)
            inputValidStates[name_edt] = false
            inputValidStates[email_edt] = false
            inputValidStates[desc_edt] = false
            txt_toolbar_right_img.apply { visibility = View.VISIBLE; setImageResource(R.drawable.send) }
            txt_toolbar_right_img.setOnClickListener { submitdata() }
            txt_toolbar.text = getString(R.string.kundle_support)
            img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
            Logger.setLoggable(true);
            // Zendesk.INSTANCE.init(context!!, "https://xyz5070.zendesk.com", "5607f30269e67f046f086eae038d6c1abf60d0e6490a03ae", "mobile_sdk_client_e5c9b367c7d7adf62d77")
            Zendesk.INSTANCE.init(context!!, "https://innovium.zendesk.com", "20f6cabdd964361cc1235ee99b39573e7c3988fd0c2656e1", "mobile_sdk_client_be3ebfa969d5b2523efb")
           // Zendesk.INSTANCE.init(context!!, "https://innovium.zendesk.com", "d51eff307c24ec168cc8138d49467d1236384379905b6669", "mobile_sdk_client_fd158723e31a1991d210")
            val identity = AnonymousIdentity()
            Zendesk.INSTANCE.setIdentity(identity)


            Support.INSTANCE.init(Zendesk.INSTANCE)
            initializeZendeskProviders()
            initializeBelvedereFilePicker()
            val fragmentManager = childFragmentManager
            attach_fab.setOnClickListener { belvedere!!.showDialog(fragmentManager) }
            refreshview()
            //submit_btn.setOnClickListener { submitdata() }

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

    override fun afterTextChanged(s: Editable?) {

        if (name_edt.text.hashCode() == s!!.hashCode()) {
            name_edt.error = null
            if (name_edt.text.trim().toString().length > 0) {
                inputValidStates[name_edt] = true
            } else
                inputValidStates[name_edt] = false

        } else if (email_edt.text.hashCode() == s.hashCode()) {
            email_edt.error = null
            if (validMail(email_edt.text.toString()))
                inputValidStates[email_edt] = true
            else
                inputValidStates[email_edt] = false
        } else if (desc_edt.text.hashCode() == s.hashCode()) {
            desc_edt.error = null
            if (desc_edt.text.trim().toString().length > 0)
                inputValidStates[desc_edt] = true
            else
                inputValidStates[desc_edt] = false
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }



    fun validationFields(): Boolean {
        var isvalidate: Boolean = true

        if (!inputValidStates[name_edt]!!) {
            name_edt.error = getString(R.string.enter_your_valid_name)
            isvalidate = false
        }
        if (!inputValidStates[email_edt]!!) {
            email_edt.error = getString(R.string.enter_valid_email_address)
            isvalidate = false
        }
        if (!inputValidStates[desc_edt]!!) {
            desc_edt.error = getString(R.string.indtast_en_værdi)
            isvalidate = false
        }

        return isvalidate
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
        for (i in 0.until(attachmentlist.size)) {
            attachedImage.add(attachmentlist[i].imgtoken)
        }

        Zendesk.INSTANCE.setIdentity(
                AnonymousIdentity.Builder()
                        .withNameIdentifier(email_edt.text.toString())
                        .build())
        val request = CreateRequest()
        request.setDescription(desc_edt.getText().toString())
        request.setSubject(name_edt.getText().toString())
        request.setAttachments(attachedImage)
        return request
    }

    private fun deleteimage(model: ImageParmsModel) {
        attachmentlist.removeAt(attachmentlist.indexOf(model))
        mAdapter!!.notifyDataSetChanged()
    }

    private fun submitdata() {

        if (validationFields()) {
            showProgressDialog()
            val request = buildCreateRequest()

            requestProvider!!.createRequest(request, object : ZendeskCallback<Request>() {
                override fun onSuccess(request: Request) {
                    showProgressDialog()
                    // Clear form
                    name_edt.setText("")
                    email_edt.setText("")
                    desc_edt.setText("")
                    attachmentlist.clear()
                    attachedImage.clear()
                    name_edt.requestFocus()
                    mAdapter!!.notifyDataSetChanged()

                }

                override fun onError(errorResponse: ErrorResponse) {
                    showProgressDialog()
                    Snackbar.make(kundlesupport_container, "Request creation failed: " + errorResponse.reason, Snackbar.LENGTH_LONG).show()

                }
            })
        }
    }

    fun refreshview() {
        recycler_view.apply {
            mAdapter = UniversalAdapter(context!!, attachmentlist, R.layout.row_attached_img, object : RecyclerCallback<RowAttachedImgBinding, ImageParmsModel> {
                override fun bindData(binder: RowAttachedImgBinding, model: ImageParmsModel) {
                    Glide.with(context!!)
                            .load(model.imglocalPath)
                            .apply(RequestOptions().error(BindDataUtils.getRandomDrawbleColor()))
                            .into(binder.imageview)
                    binder.deleteImg.setOnClickListener { deleteimage(model) }
                    binder.executePendingBindings()
                    //   binder.handler=this@OrderFragment
                }
            })
            layoutManager = LinearLayoutManager(getActivityBase(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        loge(TAG, "onActivityResult--")

        belvedere!!.getFilesFromActivityOnResult(requestCode, resultCode, data, object : BelvedereCallback<List<BelvedereResult>>() {
            override fun success(belvedereResults: List<BelvedereResult>?) {
                loge(TAG, "getFilesFromActivityOnResult--")

                if (belvedereResults != null && belvedereResults.size > 0) {
                    //  showProgressDialog()
                } else {
                    return
                }

                var i = 0
                val limit = belvedereResults.size
                while (i < limit) {
                    showProgressDialog()
                    val file = belvedereResults[i]
                    loge(TAG, "file-" + file.uri)
                    uploadProvider!!.uploadAttachment(
                            file.file.name,
                            file.file,
                            getMimeType(context!!, file.uri)!!,
                            object : ZendeskCallback<UploadResponse>() {
                                override fun onSuccess(uploadResponse: UploadResponse?) {
                                    loge(TAG,"upload response-"+uploadResponse)
                                    if (uploadResponse != null && uploadResponse.attachment != null) {
                                        val imageparmsmodel = ImageParmsModel(imglocalPath = file.uri, imgtoken = uploadResponse.token!!)
                                        attachmentlist.add(imageparmsmodel)
                                        mAdapter!!.notifyDataSetChanged()
                                        Log.e(TAG, String.format("onSuccess: Image successfully uploaded: %s",
                                                uploadResponse.attachment!!.contentUrl))

                                        showProgressDialog()

                                    }
                                    // Make sure to keep track of how many requests are in progress

                                }

                                override fun onError(errorResponse: ErrorResponse) {
                                    loge(TAG, "onError--  onActivityResult--"+errorResponse.responseBody)
                                    val json = Gson().fromJson(errorResponse.responseBody,JsonObject::class.java)
                                    val error_msg=(json as JsonObject).get("description").asString
                                    Toast.makeText(context!!,error_msg,Toast.LENGTH_SHORT).show()
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

    data class ImageParmsModel(var imgtoken: String = "", var imglocalPath: Uri?)


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")

        for (attachment in attachmentlist) {
            uploadProvider!!.deleteAttachment(attachment.imgtoken, null)
        }

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
