package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.FragmentFbSignupBinding
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_fb_signup.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.HashMap
import java.util.regex.Pattern

class FacebookSignup : BaseFragment(), TextWatcher {


    private lateinit var binding: FragmentFbSignupBinding
    private lateinit var clickEvent: MyClickHandler
    private val inputValidStates = HashMap<EditText, Boolean>()


    companion object {

        val TAG = "FacebookSignup"
        var ID: Int = 1
        fun newInstance(): FacebookSignup {
            return FacebookSignup()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_fb_signup
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {

            clickEvent = MyClickHandler(this)
            img_toolbar_back.setOnClickListener { (activity as HomeActivity).onBackPressed() }
            binding.handlers = clickEvent
            email_edt.requestFocus()
            email_edt.addTextChangedListener(this)


        } else {
            logd(TAG, "saveInstance NOT NULL")

        }

    }



    override fun afterTextChanged(s: Editable?) {

        if (email_edt.text.hashCode() == s!!.hashCode()) {
            email_inputlayout.isErrorEnabled=false

        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        loge(TAG,"beforeTextChanged")

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        loge(TAG,"onTextChanged")

    }




    private fun FBsignupFunction() {

        if(validMail(email_edt.text.trim().toString())){
            continue_btn.isEnabled=false
            Handler().postDelayed({
                (parentFragment as AccountFragment).recallfbLogin(email_edt.text.trim().toString())
                (activity as HomeActivity).onBackPressed()
            },800)

        }else{
            email_inputlayout.error=getString(R.string.indtast_venligst_en_gylding_e_mail)
        }

    }


    fun createRowdata( auth_key: String , eatmore_app:Boolean , first_name:String , email:String , fb_id:String ) : JsonObject {
        val jsonobject= JsonObject()
        jsonobject.addProperty(Constants.AUTH_KEY,auth_key)
        jsonobject.addProperty(Constants.EATMORE_APP,eatmore_app)
        jsonobject.addProperty(Constants.FIRST_NAME,first_name)
        jsonobject.addProperty(Constants.EMAIL,email)
        jsonobject.addProperty(Constants.FB_ID,fb_id)

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


    class MyClickHandler(val facebooksignup: FacebookSignup) {


        fun signupFunction(view: View) {
            facebooksignup.FBsignupFunction()
        }


    }


}
