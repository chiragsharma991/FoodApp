package dk.eatmore.foodapp.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_signup.*
import java.util.HashMap
import java.util.regex.Pattern

class Signup : BaseFragment(), TextWatcher, View.OnFocusChangeListener {


    private lateinit var binding: FragmentSignupBinding
    private lateinit var clickEvent: MyClickHandler
    private val inputValidStates = HashMap<EditText, Boolean>()


    companion object {

        val TAG = "Signup"
        var ID: Int = 1
        fun newInstance(): Signup {
            return Signup()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_signup
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            clickEvent = MyClickHandler(this)
            binding.handlers = clickEvent
            sign_up_email_edt.requestFocus()
            acc_signup_btn.setEnabled(false)
            acc_signup_btn.text = getString(R.string.enter_valid_email_address)

            sign_up_email_edt.addTextChangedListener(this)
            sign_up_password_edt.addTextChangedListener(this)
            sign_up_cnf_password_edt.addTextChangedListener(this)

            sign_up_email_edt.setOnFocusChangeListener(this)
            sign_up_password_edt.setOnFocusChangeListener(this)
            sign_up_cnf_password_edt.setOnFocusChangeListener(this)

            inputValidStates[sign_up_email_edt] = false
            inputValidStates[sign_up_password_edt] = false
            inputValidStates[sign_up_cnf_password_edt] = false

            logd(TAG, "saveInstance NULL")
            when (ID) {
                1 -> {
                    signup_view.visibility = View.VISIBLE
                    forget_password_view.visibility = View.GONE
                }
                2 -> {
                    signup_view.visibility = View.GONE
                    forget_password_view.visibility = View.VISIBLE
                }
            }


        } else {
            logd(TAG, "saveInstance NOT NULL")

        }

    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {

        loge(TAG, " focus is- " + hasFocus)
        if(!!hasFocus){
            validationFields()
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (sign_up_email_edt.text.hashCode() == s!!.hashCode()) {

            if (validMail(sign_up_email_edt.text.toString())) {
                inputValidStates[sign_up_email_edt] = true
            } else {
                inputValidStates[sign_up_email_edt] = false
            }
           // validationFields()
        } else if (sign_up_password_edt.text.hashCode() == s.hashCode()) {
           if(sign_up_password_edt.text.trim().toString().length >= 7){
               inputValidStates[sign_up_password_edt] = true

           }else{
               inputValidStates[sign_up_password_edt] = false

           }
           // validationFields()
        }
        else if (sign_up_cnf_password_edt.text.hashCode() == s.hashCode()) {
           if(sign_up_cnf_password_edt.text.trim().toString().equals(sign_up_password_edt.text.trim().toString())){
               inputValidStates[sign_up_cnf_password_edt] = true
               validationFields()
           }else{
               inputValidStates[sign_up_cnf_password_edt] = false
               validationFields()
           }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        loge(TAG,"beforeTextChanged")

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        loge(TAG,"onTextChanged")

    }


    fun validationFields() {
        updateButtonState()

        if (!inputValidStates[sign_up_email_edt]!!) {
            acc_signup_btn.text = getString(R.string.enter_valid_email_address)
            sign_up_email_inputlayout.error= "check the value "
            return
        }else{
            sign_up_email_inputlayout.isErrorEnabled=false
        }
        if (!inputValidStates[sign_up_password_edt]!!) {
            acc_signup_btn.text = getString(R.string.enter_unique_password)
            sign_up_password_inputlayout.error=" "
            return
        }else{
            sign_up_password_inputlayout.isErrorEnabled=false

        }

        if (!inputValidStates[sign_up_cnf_password_edt]!!) {
            loge(TAG,"invalid state false...")
            acc_signup_btn.text = getString(R.string.enter_the_confirm_password)
            sign_up_cnf_password_inputlayout.error=" "
            return
        }else{
            acc_signup_btn.text = getString(R.string.signup)
            sign_up_cnf_password_inputlayout.isErrorEnabled=false
        }
    }

    private fun updateButtonState() {
        var enabled = true
        for (key in inputValidStates.keys) {
            if (!inputValidStates[key]!!) {
                enabled = false
                break
            }
        }
        acc_signup_btn.setEnabled(enabled)
        acc_signup_btn.alpha = if (enabled) 1.0F else 0.5F
    }


    private fun signupFunction() {
        loge(TAG, "signup...")
        showProgressDialog()
        callAPI(ApiCall.Signup(
                username = "Hardcode test",
                password_hash = sign_up_password_edt.text.toString(),
                r_key = Constants.R_KEY,
                r_token = Constants.R_TOKEN,
                email = sign_up_email_edt.text.toString(),
                first_name = "Hardcode fst name",
                house_no = "1255",
                postal_code = "800",
                telephone_no = "9764728465"
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                if (json.get("status").asBoolean) {
                    showSnackBar(clayout, json.get("msg").asString)
                } else {
                    showSnackBar(clayout, json.get("msg").asString)
                }
                showProgressDialog()

            }

            override fun onFail(error: Int) {

                when (error) {
                    404 -> {
                        showSnackBar(clayout, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })

    }

    /* private fun validation(){
         sign_up_name_edt


     }*/


/*    private  fun validation(): Boolean {

        return when {
            TextUtils.isEmpty(sign_up_user_edt.text.trim().toString()) -> {
                sign_up_user_inputlayout.error="Please enter the username"
                false
            }
            TextUtils.isEmpty(sign_up_email_edt.text.trim().toString()) -> {
                sign_up_email_inputlayout.error="Please enter the email address"
                false
            }
            TextUtils.isEmpty(sign_up_phn_edt.text.trim().toString()) -> {
                sign_up_phn_inputlayout.error="Please enter the phone number"
                false
            }
            TextUtils.isEmpty(sign_up_password_edt.text.trim().toString()) -> {
                sign_up_password_inputlayout.error="Please enter the password"
                false
            }
            TextUtils.isEmpty(sign_up_cnf_password_edt.text.trim().toString()) -> {
                sign_up_cnf_password_inputlayout.error="Please enter the conferm password"
                false
            }

            else -> {

                if(!validMail(sign_up_email_edt.text.trim().toString())){
                    sign_up_email_inputlayout.error=getString(R.string.login_val_email)
                }else if(sign_up_phn_edt.text.trim().toString().length >= 7){
                    sign_up_phn_inputlayout.error="Phone number is not valid"
                }


                true

            }
        }
    }*/

    fun validMail(email: String): Boolean {

        val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()

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


    class MyClickHandler(val signupActivity: Signup) {


        fun signupFunction(view: View) {
            signupActivity.signupFunction()
        }


    }


}
