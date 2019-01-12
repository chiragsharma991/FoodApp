package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.ChangePasswordBinding
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseActivity
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.change_password.*
import kotlinx.android.synthetic.main.toolbar.*

class ChangePassword : BaseActivity(), TextWatcher {


    private lateinit var binding: ChangePasswordBinding
    private val inputValidStates = java.util.HashMap<AppCompatEditText, Boolean>()


    companion object {
        val TAG = "ChangePassword"
        fun newInstance(): ChangePassword {
            return ChangePassword()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.change_password)
        initView(savedInstanceState)

    }

    private fun initView(savedInstanceState: Bundle?) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        txt_toolbar.text = getString(R.string.change_password)
        img_toolbar_back.setImageResource(R.drawable.close)
        img_toolbar_back.setOnClickListener { finish() }
        old_password_edt.addTextChangedListener(this)
        new_password_edt.addTextChangedListener(this)
        confirm_password_edt.addTextChangedListener(this)
        inputValidStates[old_password_edt] = false
        inputValidStates[new_password_edt] = false
        inputValidStates[confirm_password_edt] = false
        binding.updateBtn.setOnClickListener{
            if (validationFields()){
            hideKeyboard()
            updatePassword()
        }}
        binding.confirmPasswordEdt.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.confirmPasswordEdt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (validationFields()){
                    hideKeyboard()
                    updatePassword()
                }
                return true
            }
        })
    }

    override fun afterTextChanged(s: Editable?) {

        if (old_password_edt.text.hashCode() == s!!.hashCode()) {
            old_password_edt.error = null
            if (old_password_edt.text.trim().toString().length >= 8)
                inputValidStates[old_password_edt] = true
            else
                inputValidStates[old_password_edt] = false
        } else if (new_password_edt.text.hashCode() == s.hashCode()) {
            new_password_edt.error = null
            if (new_password_edt.text.trim().toString().length >= 8)
                inputValidStates[new_password_edt] = true
            else
                inputValidStates[new_password_edt] = false
        } else if (confirm_password_edt.text.hashCode() == s.hashCode()) {
            confirm_password_edt.error = null
            if (confirm_password_edt.text.trim().toString().length <= 0)
                inputValidStates[confirm_password_edt] = false
            else if (confirm_password_edt.text.trim().toString() != new_password_edt.text.trim().toString())
                inputValidStates[confirm_password_edt] = false
            else
                inputValidStates[confirm_password_edt] = true
        }
    }

    fun validationFields(): Boolean {

        var isvalidate: Boolean = true

        if (!inputValidStates[old_password_edt]!!) {
            old_password_edt.error = getString(R.string.enter_your_old_password)
            isvalidate = false
        }

        if (!inputValidStates[new_password_edt]!!) {
            if (new_password_edt.text.trim().toString().length <= 0)
                new_password_edt.error = getString(R.string.enter_your_new_password)
            else
                new_password_edt.error = getString(R.string.password_must_consist)
            isvalidate = false

        }
        if (!inputValidStates[confirm_password_edt]!!) {
            if (confirm_password_edt.text.trim().toString().length <= 0)
                confirm_password_edt.error = getString(R.string.enter_your_confirm_password)
            else
                confirm_password_edt.error = getString(R.string.your_password_and_confirm_password_do_not_match)
            isvalidate = false

        }

        return isvalidate
    }


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }


    private fun updatePassword() {

        showProgressDialog()
        callAPI(ApiCall.change_password(
                old_password = old_password_edt.text.trim().toString(),
                newpassword = new_password_edt.text.trim().toString(),
                language = "en",
                id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, "")!!,
                eatmore_app = true,
                device_type = Constants.DEVICE_TYPE_VALUE,
                auth_key = Constants.AUTH_VALUE

        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                showProgressDialog()
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    DialogUtils.openDialogDefault(context = this@ChangePassword,btnNegative = "",btnPositive = getString(R.string.ok),
                            color = ContextCompat.getColor(this@ChangePassword, R.color.black),msg = jsonObject.get(Constants.MSG).asString,
                            title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                        override fun onPositiveButtonClick(position: Int) {
                            finish()
                        }
                        override fun onNegativeButtonClick() {
                        }
                    })

                } else {
                    DialogUtils.openDialogDefault(context = this@ChangePassword,btnNegative = "",btnPositive = getString(R.string.ok),
                            color = ContextCompat.getColor(this@ChangePassword, R.color.black),msg = jsonObject.get(Constants.MSG).asString,
                            title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                        override fun onPositiveButtonClick(position: Int) {
                        }
                        override fun onNegativeButtonClick() {
                        }
                    })
                }
            }

            override fun onFail(error: Int) {
                showProgressDialog()
                when (error) {
                    404 -> {
                        showSnackBar(userprofile_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(userprofile_container, getString(R.string.internet_not_available))
                    }
                }

            }
        })
    }

}




