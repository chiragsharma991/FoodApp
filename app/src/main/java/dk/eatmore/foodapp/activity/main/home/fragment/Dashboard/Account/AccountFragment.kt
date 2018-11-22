package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.fragment.Dashboard.Account.Signup
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_account_container.*
import android.provider.ContactsContract.Intents.Insert.EMAIL
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginResult
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
import kotlin.math.log
import android.widget.Toast
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import retrofit2.Call
import java.util.regex.Pattern


class AccountFragment : BaseFragment() {

    private lateinit var binding: FragmentAccountContainerBinding
    private lateinit var clickEvent: MyClickHandler
    lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    var facebookemail = "";
    var first_name = "";
    var last_name = "";
    var fbid = "";
    var phone = "";
    var profileuri = ""


    companion object {

        val TAG = "AccountFragment"
        val callbackManager = CallbackManager.Factory.create()
        fun newInstance(): AccountFragment {
            return AccountFragment()
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_account_container
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            mAuth = FirebaseAuth.getInstance();
            clickEvent = MyClickHandler(this)
            binding.handlers = clickEvent
            logd(TAG, "saveInstance NULL")
            txt_toolbar.text = getString(R.string.my_profile)
            img_toolbar_back.visibility=View.GONE
           /* acc_password_edt.imeOptions = EditorInfo.IME_ACTION_DONE
            acc_password_edt.setOnEditorActionListener(object  : TextView.OnEditorActionListener{
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        moveon_login()
                        return true
                    }else{
                        return false
                    }
                }

            })*/


            acc_forgot_txt.setOnClickListener {
                val fragment = Signup.newInstance()
                Signup.ID = 2
                addFragment(R.id.home_account_container, fragment, Signup.TAG, true)
            }


            acc_signup_txt.setOnClickListener {
                //    toolbar.setNavigationIcon(ContextCompat.getDrawable(context!!,R.drawable.back))
                val fragment = Signup.newInstance()
                Signup.ID = 1
                addFragment(R.id.home_account_container, fragment, Signup.TAG, true)
            }
            acc_login_btn.setOnClickListener {
                moveon_login(username = acc_email_edt.text.toString(), password_hash = acc_password_edt.text.toString())
            }
            // show Profle screen every time if user is already login.
            if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
                val fragment = Profile.newInstance()
                addFragment(R.id.home_account_container, fragment, Profile.TAG, false)
            }


        } else {
            logd(TAG, "saveInstance NOT NULL")
        }
    }

     private fun moveon_login(username : String , password_hash : String){

        if (isValidate()) {
            acc_email_edt.clearFocus()
            acc_password_edt.clearFocus()
            val jsonobject = JsonObject()
            jsonobject.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
            jsonobject.addProperty(Constants.EATMORE_APP, true)
            jsonobject.addProperty(Constants.USERNAME, username)
            jsonobject.addProperty(Constants.PASSWORD_HASH, password_hash)
            jsonobject.addProperty(Constants.DEVICE_TYPE, Constants.DEVICE_TYPE_VALUE)
            jsonobject.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))
            val call = ApiCall.login(jsonobject)
            loginAttempt(call)

        }
    }

    fun loginfrom_signup(username : String , password_hash : String){
        acc_email_edt.setText(username)
        acc_password_edt.setText(password_hash)
        moveon_login(username,password_hash)

    }

    private fun <T> loginAttempt(call: Call<T>) {
        //Login API
        showProgressDialog()
        loge(AccountFragment.TAG, "loginAttempt...")
        callAPI(call, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                if (json.get("status").asBoolean) {


                    if (json.has("fb_new_user")) {
                        // case : if user login from facebook

                        if (json.get("fb_new_user").asString == "0") {
                            // exsisting user (0):
                            loge(TAG, "exsisting user:")
                            moveOnProfileInfo(
                                    userName = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.USERNAME).asString,
                                    email = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.EMAIL).asString,
                                    telephone_no = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.TELEPHONE_NO).asString,
                                    first_name = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.FIRST_NAME).asString,
                                    customer_id = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.ID).asString,
                                    login_from = Constants.FACEBOOK,
                                    language = "en"
                            )
                            showProgressDialog()
                            showSnackBar(clayout, json.get("msg").asString)

                        } else {
                            //new user (1)
                            signupwidFacebook()
                            loge(TAG, "new user:")
                        }

                    } else {
                        // case : if user direct login from login button
                        loge(TAG, "direct login:")
                        moveOnProfileInfo(
                                userName = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.USERNAME).asString,
                                email = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.EMAIL).asString,
                                telephone_no = if(json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.TELEPHONE_NO).isJsonNull) "" else json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.TELEPHONE_NO).asString ,
                                first_name = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.FIRST_NAME).asString,
                                customer_id = json.getAsJsonObject(Constants.USER_DETAILS).get(Constants.ID).asString,
                                login_from = Constants.DIRECT,
                                language = "en"
                        )
                        showProgressDialog()
                        showSnackBar(clayout, json.get("msg").asString)
                    }


                } else {
                    showSnackBar(clayout, json.get("msg").asString)
                    showProgressDialog()

                }

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


    private fun signupwidFacebook() {
        // case if user is new
        loge(AccountFragment.TAG, "signup...")
        var jsonobject = JsonObject()
        jsonobject.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        jsonobject.addProperty(Constants.EATMORE_APP, true)
        jsonobject.addProperty(Constants.EMAIL, facebookemail)
        jsonobject.addProperty(Constants.FIRST_NAME, first_name)
        jsonobject.addProperty(Constants.FB_ID, fbid)


        callAPI(ApiCall.signup(jsonobject), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                if (json.get("status").asBoolean) {
                    jsonobject = JsonObject()
                    jsonobject.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                    jsonobject.addProperty(Constants.EATMORE_APP, true)
                    jsonobject.addProperty(Constants.FB_ID, fbid)
                    jsonobject.addProperty(Constants.DEVICE_TYPE, Constants.DEVICE_TYPE_VALUE)
                    jsonobject.addProperty(Constants.FB_EMAIL, facebookemail)
                    jsonobject.addProperty(Constants.IS_FACEBOOK, "1")
                    jsonobject.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))
                    showProgressDialog()
                    val call = ApiCall.fBlogin(jsonobject)
                    loginAttempt(call)

                } else {
                    showSnackBar(clayout, json.get("msg").asString)
                    showProgressDialog()

                }
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


    fun moveOnProfileInfo(
            userName: String,
            email: String,
            telephone_no : String,
            first_name: String,
            customer_id: String,
            login_from: String,
            language: String
    ) {
        loge(TAG, "move on dashboard.")

        PreferenceUtil.putValue(PreferenceUtil.USER_NAME, userName)
        PreferenceUtil.putValue(PreferenceUtil.E_MAIL, email)
        PreferenceUtil.putValue(PreferenceUtil.TELEPHONE_NO, telephone_no)
        PreferenceUtil.putValue(PreferenceUtil.LANGUAGE, language)
        PreferenceUtil.putValue(PreferenceUtil.LOGIN_FROM, login_from)
        PreferenceUtil.putValue(PreferenceUtil.FIRST_NAME, first_name)  // default wakeLock should be ON
        PreferenceUtil.putValue(PreferenceUtil.CUSTOMER_ID, customer_id)
        PreferenceUtil.putValue(PreferenceUtil.KSTATUS, true)  // show close restaurant
        PreferenceUtil.save()
        //showProgressDialog()
        val fragment = Profile.newInstance()
        addFragment(R.id.home_account_container, fragment, Profile.TAG, false)
        // no matter to pass true/false its just for triggering event to reload fragment.
        if(OrderFragment.ui_model?.reloadfragment !=null) OrderFragment.ui_model!!.reloadfragment.value=true
        // When user is comming from cart to login then:
        if (EpayActivity.moveonEpay){
            loge(TAG,"moveonEpay"+EpayActivity.moveonEpay)
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(0)
            EpayActivity.moveonEpay=false
        }


    }


    fun isValidate(): Boolean {

        return when {
            TextUtils.isEmpty(acc_email_edt.text.trim().toString()) -> {
                showSnackBar(acc_email_edt, getString(R.string.login_val_email))
                false
            }
            !validMail(acc_email_edt.text.trim().toString()) -> {
                showSnackBar(acc_email_edt, getString(R.string.invalid_email))
                false
            }
            TextUtils.isEmpty(acc_password_edt.text.trim().toString()) -> {
                showSnackBar(acc_password_edt, getString(R.string.login_val_pass))
                false
            }
            else -> true
        }
    }

    fun validMail(email: String): Boolean {

        val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()

    }


    fun facebookSign() {
        loge(TAG, "call to Facebook")
        login_buttonUser.performClick()
        login_buttonUser.setFragment(this@AccountFragment)
        login_buttonUser.setReadPermissions("email", "public_profile");
        login_buttonUser.registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        // App code
                        loge(TAG, "---success---")
                    //    handleFacebookAccessToken(loginResult.getAccessToken());

                        val request = GraphRequest.newMeRequest(
                                loginResult.accessToken
                        ) { `object`, response ->
                            // Application code
                            try {
                                loge(TAG, "Response:" + response.toString())
                                val fbJsonObject = response.jsonObject

                                if (fbJsonObject.has("email"))
                                    facebookemail = fbJsonObject.getString("email")
                                else
                                    facebookemail = ""

                                if (fbJsonObject.has("first_name"))
                                    first_name = fbJsonObject.getString("first_name")
                                else
                                    first_name = ""

                                if (fbJsonObject.has("last_name"))
                                    first_name = fbJsonObject.getString("first_name") + " " + fbJsonObject.getString("last_name")
                                else
                                    last_name = ""

                                if (fbJsonObject.has("id"))
                                    fbid = fbJsonObject.getString("id")
                                else
                                    fbid = ""

                                if (fbJsonObject.has("phone"))
                                    phone = fbJsonObject.getString("phone")
                                else
                                    phone = ""

                                if (com.facebook.Profile.getCurrentProfile() != null) {
                                    profileuri = "" + com.facebook.Profile.getCurrentProfile().getProfilePictureUri(200, 200)
                                    Log.e("Response", "userfile $profileuri")
                                } else {
                                    profileuri = ""
                                }
                                Log.e(TAG, "FsignInWithCredential:success" + facebookemail + " " + fbid + " " + profileuri + " " + first_name + " " + last_name)

                                if (facebookemail == "") {
                                    // case: any miss email from fb sdk :
                                    val fragment = FacebookSignup.newInstance()
                                    val bundle = Bundle()
                                    bundle.putString("first_name", first_name)
                                    bundle.putString("fbid", fbid)
                                    bundle.putString("last_name", last_name)
                                    bundle.putString("phone", phone)
                                    fragment.arguments = bundle
                                    Handler().postDelayed({
                                        addFragment(R.id.home_account_container, fragment, FacebookSignup.TAG, true)
                                    }, 800)
                                } else {
                                    // call to login api
                                    val jsonobject = JsonObject()
                                    jsonobject.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
                                    jsonobject.addProperty(Constants.EATMORE_APP, true)
                                    jsonobject.addProperty(Constants.FB_ID, fbid)
                                    jsonobject.addProperty(Constants.DEVICE_TYPE, Constants.DEVICE_TYPE_VALUE)
                                    jsonobject.addProperty(Constants.FB_EMAIL, facebookemail)
                                    jsonobject.addProperty(Constants.IS_FACEBOOK, "1")
                                    jsonobject.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))
                                    val call = ApiCall.fBlogin(jsonobject)
                                    loginAttempt(call)

                                }

                            } catch (e: Exception) {
                                Log.e(TAG, "exception:-- $e")
                                e.printStackTrace()
                            }
                        }

                        val parameters = Bundle()
                        parameters.putString("fields", "id,email,first_name,last_name")
                        request.parameters = parameters
                        request.executeAsync()


                    }

                    override fun onCancel() {
                        // App code
                        loge(TAG, "---cancel---")

                    }

                    override fun onError(exception: FacebookException) {
                        // App code
                        loge(TAG, "---exception---")

                    }
                })


    }

    fun recallfbLogin(email: String) {
        facebookemail = email
        val jsonobject = JsonObject()
        jsonobject.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        jsonobject.addProperty(Constants.EATMORE_APP, true)
        jsonobject.addProperty(Constants.FB_ID, fbid)
        jsonobject.addProperty(Constants.DEVICE_TYPE, Constants.DEVICE_TYPE_VALUE)
        jsonobject.addProperty(Constants.FB_EMAIL, facebookemail)
        jsonobject.addProperty(Constants.IS_FACEBOOK, "1")
        jsonobject.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))
        val call = ApiCall.fBlogin(jsonobject)
        loginAttempt(call)


    }


    fun googleSign() {
        showProgressDialog()
        mAuth = FirebaseAuth.getInstance();
        loge(TAG, "call to google")
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso);

        val signInIntent: Intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge(TAG, "---Activity Result account fragment---" + requestCode)
        super.onActivityResult(requestCode, resultCode, data)
        /*  if (requestCode == 9001) {
              val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);

              try {
                  val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                  handleGoogleAccessToken(account)

              } catch (e: ApiException) {
                  Log.w(HomeFragment.TAG, "Google sign in failed", e);
              }
          } else if (requestCode == 64206) {
              callbackManager.onActivityResult(requestCode, resultCode, data);
          }*/
    }

    private fun handleGoogleAccessToken(googleSignInAccount: GoogleSignInAccount) {
        // get details from googleSignInAccount
        val credential: AuthCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity!!, object : OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                if (task.isSuccessful()) {
                    val displayName = mAuth.getCurrentUser()!!.displayName
                    val email = mAuth.getCurrentUser()!!.email
                    val photo = mAuth.getCurrentUser()!!.photoUrl
                    val phone = mAuth.getCurrentUser()!!.phoneNumber
                    Log.e(TAG, "GsignInWithCredential:success" + email + " " + phone + " " + photo + " " + displayName)
                    moveOnProfileInfo(
                            userName = displayName.toString(),
                            first_name = phone.toString(),
                            customer_id = "",
                            telephone_no = "",
                            email = email.toString(),
                            login_from = Constants.GOOGLE,
                            language = "en"
                    )
                } else {
                    Log.e(HomeFragment.TAG, "GsignInWithCredential:failure", task.getException());
                }

            }

        })

    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {

        Log.e(TAG, "handleFacebookAccessToken:$accessToken")

        val credential = FacebookAuthProvider.getCredential(accessToken!!.getToken())
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!, object : OnCompleteListener<AuthResult> {
                    override
                    fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            val displayName = mAuth.getCurrentUser()!!.displayName
                            val email = mAuth.getCurrentUser()!!.email
                            val photo = mAuth.getCurrentUser()!!.photoUrl
                            val phone = mAuth.getCurrentUser()!!.getIdToken(true)
                            Log.e(TAG, "Fsign Accestoken:success" + email + " " + phone + " " + photo + " " + displayName)

                            /*moveOnProfileInfo(
                                    userName = displayName.toString(),
                                    phone = phone.toString(),
                                    email = email.toString(),
                                    login_from = Constants.FACEBOOK,
                                    language = "en"
                            )*/
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "FsignInWithCredential:failure", task.getException())
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }


                    }
                })

    }

    fun signOut() {
        loge(TAG, "sign out...")

        if (PreferenceUtil.getString(PreferenceUtil.LOGIN_FROM, "").equals(Constants.FACEBOOK)) {
            LoginManager.getInstance().logOut()
            mAuth.signOut()
        }
//        else if(PreferenceUtil.getString(PreferenceUtil.LOGIN_FROM, "").equals(Constants.GOOGLE)){
//            mGoogleSignInClient.signOut().addOnCompleteListener(activity!!, {})
//            mAuth.signOut()
//        }

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


    class MyClickHandler(val accountfragment: AccountFragment) {


        fun facebookSign(view: View) {
            accountfragment.facebookSign()
        }

        fun googleSign(view: View) {
            // accountfragment.googleSign()
        }

    }


}



