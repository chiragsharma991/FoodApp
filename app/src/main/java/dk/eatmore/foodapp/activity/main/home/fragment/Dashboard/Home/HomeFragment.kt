package dk.eatmore.foodapp.fragment.Dashboard.Home


import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.transition.ChangeBounds
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.Task;
import com.facebook.*
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentHomeFragmentBinding
import dk.eatmore.foodapp.model.home.UI_HomeFragment
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeFragmentBinding
    lateinit var clickEvent : MyClickHandler
    private  var mAdapter: OrderListAdapter?=null
    var fragment: DetailsFragment?=null
    private lateinit var mAuth: FirebaseAuth
    val callbackManager = CallbackManager.Factory.create()
    private lateinit var mGoogleSignInClient: GoogleSignInClient




    companion object {

        val TAG= "HomeFragment"
        fun newInstance() : HomeFragment {
            return HomeFragment()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       // return inflater.inflate(getLayout(), container, false)
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }

    override fun getLayout(): Int {
        return R.layout.fragment_home_fragment
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {

        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            clickEvent =MyClickHandler(this)
            binding.handlers=clickEvent

            recycler_view.apply {
                mAdapter = OrderListAdapter(this@HomeFragment,object: OrderListAdapter.AdapterListener {
                    override fun itemClicked(position: Int) {
                        loge(TAG,"on click....")
                        fragment = DetailsFragment.newInstance()
                        var enter :Slide?=null
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            enter = Slide()
                            enter.setDuration(300)
                            enter.slideEdge = Gravity.BOTTOM
                            val changeBoundsTransition :ChangeBounds = ChangeBounds()
                            changeBoundsTransition.duration = 300
                            //fragment!!.sharedElementEnterTransition=changeBoundsTransition
                            fragment!!.sharedElementEnterTransition=changeBoundsTransition
                            fragment!!.enterTransition=enter
                        }
                        addFragment(R.id.home_fragment_container,fragment!!,DetailsFragment.TAG,false)
                    }
                })
                layoutManager = LinearLayoutManager(getActivityBase())
                adapter = mAdapter
            }

        }else{
            logd(TAG,"saveInstance NOT NULL")

        }



    }


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG,"on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG,"on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG,"on pause...")


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      //  callbackManager.onActivityResult(requestCode, resultCode, data);
        loge(TAG,"---Activity Result---")
        super.onActivityResult(requestCode, resultCode, data)

          // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            val task :Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                 val account : GoogleSignInAccount = task.getResult(ApiException::class.java)
                 firebaseAuthWithGoogle(account)

            } catch ( e :ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
         //       updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

             Log.d(TAG, "firebaseAuthWithGoogle:" + account.displayName);
        // [START_EXCLUDE silent]
       // showProgressDialog();
        // [END_EXCLUDE]

        val  credential : AuthCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity!! ,object : OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e(TAG, "signInWithCredential:success");
                    val user :FirebaseUser = mAuth.getCurrentUser()!!;
                    mAuth.signOut();
                    mGoogleSignInClient.signOut().addOnCompleteListener(activity!!,
                            object:OnCompleteListener<Void> {
                                override fun onComplete(p0: Task<Void>) {

                                    loge(TAG,"out---")
                                }

                            })

                    // updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "signInWithCredential:failure", task.getException());
                 //   Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                  //  updateUI(null);
                }
                //                        hideProgressDialog();

            }

        })

        }





           /*     .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });*/



    // [START auth_with_google]



    inner class  MyClickHandler(internal var homefragment: HomeFragment) {


        fun onFindClicked(view: View) {
            val restaurantlist=RestaurantList.newInstance()
            addFragment(R.id.home_fragment_container,restaurantlist,RestaurantList.TAG,true)

        }

    }


    inner private class UIModel : ViewModel(){

        var uiData = MutableLiveData<UI_HomeFragment>()

        fun init() {
            val ui_homefragment= UI_HomeFragment("HomeFragment",false)
            uiData.value = ui_homefragment
        }
        fun set(body: Any?) {
            /* expensive operation, e.g. network request */
            //uiData.value = (body as LastOrder)
        }

        fun getUIModel(): LiveData<UI_HomeFragment> {
            return uiData
        }


    }





}

