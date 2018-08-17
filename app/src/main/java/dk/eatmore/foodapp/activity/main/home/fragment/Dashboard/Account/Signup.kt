package dk.eatmore.foodapp.fragment.Dashboard.Account

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.databinding.FragmentSignupBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_signup.*
import java.util.regex.Pattern

class Signup : BaseFragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var clickEvent: MyClickHandler




    companion object {

        val TAG = "Signup"
        var ID :Int=1
        fun newInstance(): Signup {
            return Signup()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_signup
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            clickEvent = MyClickHandler(this)
            logd(TAG,"saveInstance NULL")
            when (ID){
                1 ->{
                    signup_view.visibility=View.VISIBLE
                    forget_password_view.visibility=View.GONE
                }
                2->{
                    signup_view.visibility=View.GONE
                    forget_password_view.visibility=View.VISIBLE
                }
            }


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }




    }

    private fun signupFunction(){


    }

   /* private fun validation(){
        sign_up_name_edt


    }*/


    private  fun validation(): Boolean {

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
    }

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
