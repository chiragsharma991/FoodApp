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
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*


class AccountFragment : BaseFragment() {

    private lateinit var binding: FragmentAccountContainerBinding
    private lateinit var clickEvent: MyClickHandler
    val callbackManager = CallbackManager.Factory.create()
    lateinit var mAuth :FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient





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
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            clickEvent=MyClickHandler(this)
            binding.handlers=clickEvent
            logd(TAG,"saveInstance NULL")
            txt_toolbar.text=getString(R.string.my_profile)
            acc_forgot_txt.setOnClickListener {
                val fragment = Signup.newInstance()
                Signup.ID =2
                addFragment(R.id.home_account_container,fragment, Signup.TAG,true)
            }


            acc_signup_txt.setOnClickListener{
                //    toolbar.setNavigationIcon(ContextCompat.getDrawable(context!!,R.drawable.back))
                val fragment = Signup.newInstance()
                Signup.ID =1
                addFragment(R.id.home_account_container,fragment, Signup.TAG,true)
            }
            acc_login_btn.setOnClickListener{
                //    toolbar.setNavigationIcon(ContextCompat.getDrawable(context!!,R.drawable.back))
                val fragment = Profile.newInstance()
                addFragment(R.id.home_account_container,fragment, Profile.TAG,true)
            }



        }else{
            logd(TAG,"saveInstance NOT NULL")

        }



    }


    fun facebookSign(){
        loge(TAG,"call to Facebook")
        mAuth = FirebaseAuth.getInstance();
        login_buttonUser.performClick()
        login_buttonUser.setFragment(this@AccountFragment)
        login_buttonUser.setReadPermissions("email", "public_profile");
        login_buttonUser.registerCallback(callbackManager,
                   object : FacebookCallback<LoginResult> {
                       override fun onSuccess(loginResult: LoginResult) {
                           // App code
                           loge(TAG,"---success---")
                           handleFacebookAccessToken(loginResult.getAccessToken());

                    /*       val request = GraphRequest.newMeRequest(
                                   loginResult.accessToken
                           ) { `object`, response ->
                               // Application code
                               try {
                                   loge(TAG,"Response:"+response.toString())
                                   val fbJsonObject = response.jsonObject

                               } catch (e: Exception) {
                                   Log.e("e", "e $e")
                                   e.printStackTrace()
                               }
                           }
                           val parameters = Bundle()
                           parameters.putString("fields", "id,email,first_name,last_name")
                           request.parameters = parameters
                           request.executeAsync()*/

                       }

                       override fun onCancel() {
                           // App code
                           loge(TAG,"---cancel---")

                       }

                       override fun onError(exception: FacebookException) {
                           // App code
                           loge(TAG,"---exception---")

                       }
                   })



    }

    fun googleSign(){

        mAuth = FirebaseAuth.getInstance();
        loge(TAG,"call to google")
        val gso : GoogleSignInOptions =  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso);

        val signInIntent : Intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge(TAG,"---Activity Result---"+requestCode)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            val task :Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                val account : GoogleSignInAccount = task.getResult(ApiException::class.java)
                handleGoogleAccessToken(account)

            } catch ( e : ApiException) {
                Log.w(HomeFragment.TAG, "Google sign in failed", e);
            }
        }else if(requestCode == 64206){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private fun handleGoogleAccessToken(googleSignInAccount: GoogleSignInAccount) {
        // get details from googleSignInAccount
        val  credential : AuthCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity!! ,object : OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                if (task.isSuccessful()) {
                    val email = mAuth.getCurrentUser()!!.email
                    val photo = mAuth.getCurrentUser()!!.photoUrl
                    val phone = mAuth.getCurrentUser()!!.phoneNumber
                    Log.e(TAG, "GsignInWithCredential:success"+email+" "+phone+" "+photo)
                    signOut()
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
                            val email = mAuth.getCurrentUser()!!.email
                            val photo = mAuth.getCurrentUser()!!.photoUrl
                            val phone = mAuth.getCurrentUser()!!.phoneNumber
                            Log.e(TAG, "FsignInWithCredential:success"+email+" "+phone+" "+photo)


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "FsignInWithCredential:failure", task.getException())
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }


                    }
                })

    }


    private fun signOut() {
        // Firebase sign out
        mAuth.signOut()

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(activity!!, {})

        // Facebook logout
        // LoginManager.getInstance().logOut()
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


    class  MyClickHandler(val accountfragment : AccountFragment) {


        fun facebookSign(view: View) {
            accountfragment.facebookSign()
        }
        fun googleSign(view: View) {
            accountfragment.googleSign()
        }

    }


}



