package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bambora.nativepayment.handlers.BNPaymentHandler
import com.bambora.nativepayment.models.creditcard.CreditCard
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.FragmentProfileBinding
import dk.eatmore.foodapp.databinding.FragmentProfileEditBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_profile_edit.*
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern

class ProfileEdit : BaseFragment(), TextWatcher {


    private lateinit var binding: FragmentProfileEditBinding
    private val inputValidStates = HashMap<EditText, Boolean>()



    companion object {

        val TAG = "ProfileEdit"
        fun newInstance(): ProfileEdit {
            return ProfileEdit()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_profile_edit
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            txt_toolbar.text=getString(R.string.profile_info)
            img_toolbar_back.setOnClickListener{(activity as HomeActivity).onBackPressed()}
            name_edt.requestFocus()
            name_edt.addTextChangedListener(this)
            email_edt.addTextChangedListener(this)
            inputValidStates[name_edt] = false
            inputValidStates[email_edt] = false
            name_edt.setText(PreferenceUtil.getString(PreferenceUtil.FIRST_NAME,""))
            email_edt.setText(PreferenceUtil.getString(PreferenceUtil.E_MAIL,""))
            telephone_edt.setText(PreferenceUtil.getString(PreferenceUtil.TELEPHONE_NO,""))
            telephone_edt.imeOptions= EditorInfo.IME_ACTION_DONE
            telephone_edt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (validationFields()) {
                        saveuserInfo()
                    }
                    return true
                }
            })
            update_view.setOnClickListener{
                if (validationFields()) {
                    saveuserInfo()
                }
            }


        }else{
            logd(TAG,"saveInstance NOT NULL")
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
        return isvalidate
    }

    fun validMail(email: String): Boolean {

        val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()

    }


    private fun saveuserInfo() {

        showProgressDialog()
        callAPI(ApiCall.update_record(
                auth_key = Constants.AUTH_VALUE,
                id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, "")!!,
                email = email_edt.text.trim().toString(),
                eatmore_app = true,
                first_name = name_edt.text.trim().toString(),
                telephone_no = telephone_edt.text.trim().toString()

        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    Toast.makeText(context,jsonObject.get(Constants.MSG).asString, Toast.LENGTH_SHORT).show()
                    (activity as HomeActivity).onBackPressed()
                }else{
                    showSnackBar(userprofile_container,jsonObject.get(Constants.MSG).asString)
                }
                showProgressDialog()
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(userprofile_container, getString(R.string.error_404))
                    }
                    100 -> {
                        showSnackBar(userprofile_container, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
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
