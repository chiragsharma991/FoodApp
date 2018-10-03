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
          //  binding.handlers = clickEvent
            email_edt.requestFocus()
            continue_btn.setEnabled(false)

            email_edt.addTextChangedListener(this)


        } else {
            logd(TAG, "saveInstance NOT NULL")

        }

    }



    override fun afterTextChanged(s: Editable?) {

        if (email_edt.text.hashCode() == s!!.hashCode()) {

        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        loge(TAG,"beforeTextChanged")

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        loge(TAG,"onTextChanged")

    }



    private fun FBsignupFunction() {
        loge(TAG, "signup...")
        showProgressDialog()
        callAPI(ApiCall.Signup(
                createRowdata(
                        auth_key = Constants.AUTH_VALUE,
                        eatmore_app = true,
                        email = email_edt.text.toString(),
                        first_name = arguments!!.getString("first_name",""),
                        fb_id = arguments!!.getString("fbid","")
                )
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                if (json.get("status").asBoolean) {
                    showSnackBar(clayout, json.get("msg").asString)
                    Handler().postDelayed({
                        (activity as HomeActivity).onBackPressed()
                        //exception
                        (parentFragment as AccountFragment).moveOnProfileInfo(
                                userName = arguments!!.getString("first_name","")+" "+arguments!!.getString("last_name",""),
                                email = email_edt.text.toString(),
                                phone = arguments!!.getString("phone",""),
                                login_from = Constants.FACEBOOK,
                                language = "en"
                        )
                    },800)

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
