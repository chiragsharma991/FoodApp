package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.fragment.Dashboard.Account.Signup
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_account_container.*
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginResult
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import kotlinx.android.synthetic.main.toolbar.*
import android.widget.Toast
import android.text.TextUtils
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
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import retrofit2.Call
import java.util.regex.Pattern


class AccountFragment : BaseFragment() {

    private lateinit var binding: FragmentAccountContainerBinding
    private lateinit var clickEvent: MyClickHandler
    val callbackManager = CallbackManager.Factory.create()
    lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    var facebookemail=""; var first_name="";  var last_name=""; var fbid=""; var phone=""; var profileuri=""

    companion object {

        val TAG = "AccountFragment"
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
            clickEvent = MyClickHandler(this)
            binding.handlers = clickEvent
            logd(TAG, "saveInstance NULL")
            txt_toolbar.text = getString(R.string.my_profile)
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

                if (isValidate()){
                   val call= ApiCall.login(
                            username = acc_email_edt.text.toString(),
                            password_hash = acc_password_edt.text.toString(),
                            r_key = Constants.R_KEY,
                            r_token = Constants.R_TOKEN,
                            device_type = "Android",
                            is_facebook = "0",
                            is_google = "0",
                            language = "en"
                    )
                    showProgressDialog()
                    loginAttempt(call)

                }


            }


        } else {
            logd(TAG, "saveInstance NOT NULL")
        }
    }

    private fun <T> loginAttempt(call: Call<T>) {
        showProgressDialog()
        loge(AccountFragment.TAG, "loginAttempt...")
        callAPI(call, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                if (json.get("status").asBoolean) {

                    if(json.get("fb_new_user").asString == "0") {
                        // exsisting user:
                        moveOnProfileInfo(
                                userName = json.getAsJsonObject("user_details").get("first_name").asString + " " + json.getAsJsonObject("user_details").get("last_name").asString,
                                email = json.getAsJsonObject("user_details").get("email").asString,
                                phone = json.getAsJsonObject("user_details").get("telephone_no").asString,
                                login_from = Constants.DIRECT,
                                language = "en"
                        )
                        loge(TAG,"old user...")
                        showSnackBar(clayout, json.get("msg").asString)
                        showProgressDialog()


                    }else{
                        //new user
                        loge(TAG,"new user...")

                      //  signupwidFacebook()
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

        /**
         *  you are new user from facbook so please sign up and u can move on dashboard: But-
         *  if you have not email / name  then move on
         * another screen and fill details and call signup.
         * */
        //    var facebookemail=""; var first_name="";  var last_name=""; var fbid=""; var phone=""; var profileuri=""
        if(facebookemail=="" && first_name==""){

            showProgressDialog()
            val fragment = FacebookSignup.newInstance()
            val bundle=Bundle()
            bundle.putString("first_name",first_name)
            bundle.putString("fbid",fbid)
            bundle.putString("last_name",last_name)
            bundle.putString("phone",phone)
            fragment.arguments=bundle
            addFragment(R.id.home_account_container, fragment, Signup.TAG, false)

        }else{
            loge(AccountFragment.TAG, "signup...")
            callAPI(ApiCall.Signup(
                    createRowdata(
                            auth_key = Constants.AUTH_VALUE,
                            eatmore_app = true,
                            email = facebookemail,
                            first_name = first_name,
                            fb_id = fbid
                    )
            ), object : BaseFragment.OnApiCallInteraction {

                override fun <T> onSuccess(body: T?) {
                    val json = body as JsonObject  // please be mind you are using jsonobject(Gson)
                    if (json.get("status").asBoolean) {
                        showProgressDialog()
                        showSnackBar(clayout, json.get("msg").asString)
                        moveOnProfileInfo(
                                userName = json.getAsJsonObject("user_details").get("first_name").asString + " " + json.getAsJsonObject("user_details").get("last_name").asString,
                                email = json.getAsJsonObject("user_details").get("email").asString,
                                phone = json.getAsJsonObject("user_details").get("telephone_no").asString,
                                login_from = Constants.FACEBOOK,
                                language = "en"
                        )

                    } else {
                        showProgressDialog()
                        showSnackBar(clayout, json.get("msg").asString)
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

    }


    fun moveOnProfileInfo(
            userName: String,
            email: String,
            phone: String,
            login_from: String,
            language: String
    ) {

        PreferenceUtil.putValue(PreferenceUtil.USER_NAME, userName)
        PreferenceUtil.putValue(PreferenceUtil.E_MAIL, email)
        PreferenceUtil.putValue(PreferenceUtil.LANGUAGE, language)
        PreferenceUtil.putValue(PreferenceUtil.LOGIN_FROM, login_from)
        PreferenceUtil.putValue(PreferenceUtil.PHONE, phone)  // default wakeLock should be ON
        PreferenceUtil.putValue(PreferenceUtil.KSTATUS, true)  // show close restaurant
        PreferenceUtil.save()
        showProgressDialog()
        val fragment = Profile.newInstance()
        addFragment(R.id.home_account_container, fragment, Profile.TAG, true)

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

       // showProgressDialog()
        loge(TAG, "call to Facebook")
        mAuth = FirebaseAuth.getInstance();
        login_buttonUser.performClick()
        login_buttonUser.setFragment(this@AccountFragment)
        login_buttonUser.setReadPermissions("email", "public_profile");
        login_buttonUser.registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        // App code
                        loge(TAG, "---success---")
                        handleFacebookAccessToken(loginResult.getAccessToken());

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


                                Log.e(TAG, "FsignInWithCredential:success" + facebookemail + " " + phone + " " + profileuri + " " + first_name+" "+last_name)

                                val call= ApiCall.FBlogin(
                                      first_name=first_name,
                                        fb_id = fbid,
                                        email = facebookemail,
                                        eatmore_app = true,
                                        auth_key = Constants.AUTH_VALUE
                                )
                                loginAttempt(call)

                            } catch (e: Exception) {
                                Log.e("e", "e $e")
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
        loge(TAG, "---Activity Result---" + requestCode)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                handleGoogleAccessToken(account)

            } catch (e: ApiException) {
                Log.w(HomeFragment.TAG, "Google sign in failed", e);
            }
        } else if (requestCode == 64206) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
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
                            phone = phone.toString(),
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
                            /*      val displayName = mAuth.getCurrentUser()!!.displayName
                                  val email = mAuth.getCurrentUser()!!.email
                                  val photo = mAuth.getCurrentUser()!!.photoUrl
                                  val phone = mAuth.getCurrentUser()!!.phoneNumber
                                  Log.e(TAG, "FsignInWithCredential:success" + email + " " + phone + " " + photo + " " + displayName)

                                  moveOnProfileInfo(
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
        } else if (PreferenceUtil.getString(PreferenceUtil.LOGIN_FROM, "").equals(Constants.GOOGLE)) {
            mGoogleSignInClient.signOut().addOnCompleteListener(activity!!, {})
            mAuth.signOut()
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


    class MyClickHandler(val accountfragment: AccountFragment) {


        fun facebookSign(view: View) {
            accountfragment.facebookSign()
        }

        fun googleSign(view: View) {
            accountfragment.googleSign()
        }

    }


}



