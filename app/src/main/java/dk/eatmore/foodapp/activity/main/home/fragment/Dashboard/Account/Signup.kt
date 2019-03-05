package dk.eatmore.foodapp.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.toolbar.*
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
            img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
            clickEvent = MyClickHandler(this)
            binding.handlers = clickEvent
            // acc_signup_btn.setEnabled(false)
            //  acc_signup_btn.alpha = 0.5F

            first_name.addTextChangedListener(this)
            sign_up_email_edt.addTextChangedListener(this)
            sign_up_password_edt.addTextChangedListener(this)
            sign_up_cnf_password_edt.addTextChangedListener(this)
            forgot_email_edt.addTextChangedListener(this)

            first_name.setOnFocusChangeListener(this)
            sign_up_email_edt.setOnFocusChangeListener(this)
            sign_up_password_edt.setOnFocusChangeListener(this)
            sign_up_cnf_password_edt.setOnFocusChangeListener(this)

            inputValidStates[first_name] = false
            inputValidStates[sign_up_email_edt] = false
            inputValidStates[sign_up_password_edt] = false
            inputValidStates[sign_up_cnf_password_edt] = false

            forgot_email_edt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (forgot_email_edt.text.trim().length > 0) {
                        if (validMail(forgot_email_edt.text.toString())) {
                            forgotFunction()
                        } else {
                            forgot_email_inputlayout.error = getString(R.string.enter_valid_email_address)
                        }
                    }
                    return false
                }
            })

            logd(TAG, "saveInstance NULL")
            when (ID) {
                1 -> {
                    first_name.requestFocus()
                    txt_toolbar.text = getString(R.string.opret_konto)
                    signup_view.visibility = View.VISIBLE
                    forget_password_view.visibility = View.GONE
                }
                2 -> {
                    forgot_email_edt.requestFocus()
                    txt_toolbar.text = getString(R.string.forgot_password)
                    signup_view.visibility = View.GONE
                    forget_password_view.visibility = View.VISIBLE
                    acc_forget_btn.setOnClickListener({
                        if (forgot_email_edt.text.trim().length > 0) {
                            loge("TAG-", "" + validMail(forgot_email_edt.text.toString()))
                            if (validMail(forgot_email_edt.text.toString())) {
                                forgotFunction()
                            } else {
                                forgot_email_inputlayout.error = getString(R.string.indtast_venligst_dine_mail)

                            }
                        }
                    })
                }
            }


        } else {
            logd(TAG, "saveInstance NOT NULL")

        }

    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {

    }

    override fun afterTextChanged(s: Editable?) {

        loge(TAG, "afterTextChanged---")
        if (first_name.text.hashCode() == s!!.hashCode()) {
            sign_up_firstname_inputlayout.isErrorEnabled = false
        } else if (sign_up_email_edt.text.hashCode() == s.hashCode()) {
            sign_up_email_inputlayout.isErrorEnabled = false
            // validationFields()
        } else if (sign_up_password_edt.text.hashCode() == s.hashCode()) {
            sign_up_password_inputlayout.isErrorEnabled = false
            // validationFields()
        } else if (sign_up_cnf_password_edt.text.hashCode() == s.hashCode()) {
            sign_up_cnf_password_inputlayout.isErrorEnabled = false
        } else if (forgot_email_edt.text.hashCode() == s.hashCode()) {
            forgot_email_inputlayout.isErrorEnabled = false
        }
    }

    fun signup_validation(): Boolean {
        var result = true

        if (first_name.text.trim().toString().length > 0) {
            inputValidStates[first_name] = true
        } else {
            inputValidStates[first_name] = false
        }
        if (validMail(sign_up_email_edt.text.toString())) {
            inputValidStates[sign_up_email_edt] = true
        } else {
            inputValidStates[sign_up_email_edt] = false
        }
        // validationFields()
        if (sign_up_password_edt.text.trim().toString().length >= 6) {
            inputValidStates[sign_up_password_edt] = true
        } else {
            inputValidStates[sign_up_password_edt] = false
        }
        // validationFields()
        if (sign_up_cnf_password_edt.text.trim().length <= 0) {
            inputValidStates[sign_up_cnf_password_edt] = false
        } else if (sign_up_cnf_password_edt.text.trim().toString().equals(sign_up_password_edt.text.trim().toString())) {
            inputValidStates[sign_up_cnf_password_edt] = true
        } else {
            inputValidStates[sign_up_cnf_password_edt] = false
        }


        if (!inputValidStates[first_name]!!) {
            sign_up_firstname_inputlayout.isErrorEnabled = true
            sign_up_firstname_inputlayout.error = getString(R.string.indtast_vengligst_dit_navn)
            result = false
        }
        if (!inputValidStates[sign_up_email_edt]!!) {
            sign_up_email_inputlayout.error = getString(R.string.indtast_venligst_en_gylding_e_mail)
            result = false
        }
        if (!inputValidStates[sign_up_password_edt]!!) {
            if (sign_up_password_edt.text.trim().length <= 0) {
                sign_up_password_inputlayout.error = getString(R.string.enter_your_unique_password)
            } else {
                sign_up_password_inputlayout.error = getString(R.string.password_must_consist)
            }
            result = false
        }
        if (!inputValidStates[sign_up_cnf_password_edt]!!) {
            if (sign_up_cnf_password_edt.text.trim().length <= 0) {
                sign_up_cnf_password_inputlayout.error = getString(R.string.enter_your_confirm_password)
            } else {
                sign_up_cnf_password_inputlayout.error = getString(R.string.your_password_and_confirm_password_do_not_match)
            }
            result = false
        }

        return result
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        loge(TAG, "beforeTextChanged")

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        loge(TAG, "onTextChanged")

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
        subscribe_chk.alpha = if (enabled) 1.0F else 0.5F
        subscribe_txt.alpha = if (enabled) 1.0F else 0.5F
    }


    private fun signupFunction() {
        if (!signup_validation()) return
        loge(TAG, "signup...")
        showProgressDialog()
        callAPI(ApiCall.signup(
                createRowdata(
                        auth_key = Constants.AUTH_VALUE,
                        eatmore_app = true,
                        email = sign_up_email_edt.text.toString(),
                        first_name = first_name.text.toString(),
                        password_hash = sign_up_password_edt.text.toString(),
                        subscribe = if (subscribe_chk.isChecked) "1" else "0"
                )
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                if (json.get("status").asBoolean) {
                    showSnackBar(clayout, json.get("msg").asString)
                    Handler().postDelayed({
                        (parentFragment as AccountFragment).loginfrom_signup(username = sign_up_email_edt.text.toString(), password_hash = sign_up_password_edt.text.toString())
                        (activity as HomeActivity).onBackPressed()
                    }, 800)

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

    private fun forgotFunction() {
        showProgressDialog()
        callAPI(ApiCall.forgot_password(auth_key = Constants.AUTH_VALUE, email = forgot_email_edt.text.trim().toString(), device_type = Constants.DEVICE_TYPE_VALUE, eatmore_app = true, app = Constants.RESTAURANT_FOOD_ANDROID), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                if (json.get("status").asBoolean) {
                    Toast.makeText(context!!, json.get(Constants.MSG).asString, Toast.LENGTH_SHORT).show()
                    //showSnackBar(clayout, json.get(Constants.MSG).asString)
                    (activity as HomeActivity).onBackPressed()
                } else {
                    showSnackBar(clayout, json.get(Constants.MSG).asString)
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



    fun createRowdata(auth_key: String, eatmore_app: Boolean, first_name: String, email: String, password_hash: String, subscribe: String): JsonObject {
        val jsonobject = JsonObject()
        jsonobject.addProperty(Constants.AUTH_KEY, auth_key)
        jsonobject.addProperty(Constants.EATMORE_APP, eatmore_app)
        jsonobject.addProperty(Constants.FIRST_NAME, first_name)
        jsonobject.addProperty(Constants.EMAIL, email)
        jsonobject.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
        jsonobject.addProperty(Constants.PASSWORD_HASH, password_hash)
        jsonobject.addProperty(Constants.SUBSCRIBE, subscribe)
        jsonobject.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        return jsonobject

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

    fun backpress(): Boolean {
        //  (parentFragment as AccountFragment).img_toolbar_back.visibility=View.GONE
        // (parentFragment as AccountFragment).txt_toolbar.text=getString(R.string.my_profile)
        return true
    }


    class MyClickHandler(val signupActivity: Signup) {


        fun signupFunction(view: View) {
            signupActivity.signupFunction()
        }


    }


}
