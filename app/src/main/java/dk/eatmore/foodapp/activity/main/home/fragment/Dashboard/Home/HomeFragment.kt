package dk.eatmore.foodapp.fragment.Dashboard.Home


import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Address
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.*
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.facebook.*
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentHomeFragmentBinding
import dk.eatmore.foodapp.model.home.UI_HomeFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.epay.fragment.TransactionStatus
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.RateOrder
import dk.eatmore.foodapp.databinding.SwipeCartItemBinding
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList


class HomeFragment : CommanAPI() {


    private lateinit var binding: FragmentHomeFragmentBinding
    lateinit var clickEvent: MyClickHandler
    private var mAdapter: OrderListAdapter? = null
    var fragment: DetailsFragment? = null
    var swipeAdapter: SwipeAdapter? = null
    private lateinit var mAuth: FirebaseAuth
    val callbackManager = CallbackManager.Factory.create()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAnalytics: FirebaseAnalytics


    companion object {
        var count: Int = 0
        var ui_model: UIModel? = null
        val TAG = "HomeFragment"
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }


    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return inflater.inflate(getLayout(), container, false)
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }

    override fun getLayout(): Int {
        return R.layout.fragment_home_fragment
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {

        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")
            Glide.with(context!!).load(ContextCompat.getDrawable(context!!,R.mipmap.banner)).into(imageview);
            restaurant_logo.visibility=View.VISIBLE
            img_toolbar_back.visibility=View.GONE
            firebaseAnalytics = FirebaseAnalytics.getInstance(context!!);
            checkFirebaseAnalytics()
            clickEvent = MyClickHandler(this)
            binding.handlers = clickEvent
            ui_model = createViewModel()
            fetchLastOrder()
            find_rest_edt.imeOptions = EditorInfo.IME_ACTION_DONE
            find_rest_edt.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (find_rest_edt.text.trim().toString().length > 0) {
                        val restaurantlist = RestaurantList.newInstance(find_rest_edt.text.trim().toString())
                        addFragment(R.id.home_fragment_container, restaurantlist, RestaurantList.TAG, true)
                    }
                    return true
                }
            })
            // disable app bar scrolling.
            if (app_bar.getLayoutParams() != null) {
                val layoutParams = app_bar.getLayoutParams() as CoordinatorLayout.LayoutParams
                val appBarLayoutBehaviour = AppBarLayout.Behavior()
                appBarLayoutBehaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                    override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                        return false
                    }
                })
                layoutParams.behavior = appBarLayoutBehaviour
            }
            getcurrent_location()

        } else {
            logd(TAG, "saveInstance NOT NULL")
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        loge(TransactionStatus.TAG, "permission result---")
        when (requestCode) {
            1 -> {
                loge(TAG,"grant permission--"+grantResults.toString())

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loge(TAG,"PERMISSION_GRANTED (0)--")
                    getcurrent_location()

                } else {
                    loge(TAG,"PERMISSION_GRANTED false (0)--")
                    Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()

                }

                if (grantResults.size > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    loge(TAG,"PERMISSION_GRANTED (1)--")

                } else {
                    loge(TAG,"PERMISSION_GRANTED false (1)--")
                    Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }

                return
            }
        }
    }


/*
    override fun locationtracking_success(address: Address) {
        loge(TAG,"locationtracking_success-----")

    }

    override fun locationtracking_failed() {
        loge(TAG,"locationtracking_failed-----")
    }

*/

    fun checkFirebaseAnalytics() {
        val bundle = Bundle()
        bundle.putString("ButtonId", "1234");
        //   bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "test");
        //   bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        firebaseAnalytics.logEvent("checkButtonTest", bundle);
        //firebaseAnalytics.setAnalyticsCollectionEnabled(true);


    }

    fun getcurrent_location(){

        if(is_location_PermissionGranted()){
            loge(TAG,"is_location_PermissionGranted---")
            search_icon.visibility=View.GONE
            progress_bar.visibility=View.VISIBLE
            val   locationUpdate = GetLastLocation(activity!!, object : GetLastLocation.OnLocationInteraction {
                override fun onLocationUpdate(lat: Double, lng: Double) {
                    loge(TAG, "onLocationUpdate: $lat, $lng")
                    find_rest_edt.setText(getpostalfrom_latlang(latitude = lat,longitude = lng).trim())
                    search_icon.visibility=View.VISIBLE
                    progress_bar.visibility=View.GONE
                }

                override fun onReqPermission() {
                    loge(TAG, "onReqPermission: ")
                }
            })
        }
    }


    class UIModel : ViewModel() {
        var reloadfragment = MutableLiveData<Boolean>()

    }

    fun createViewModel(): UIModel =

            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                reloadfragment.removeObservers(this@HomeFragment)
                reloadfragment.observe(this@HomeFragment, Observer<Boolean> {
                    // reload fragment from here.
                    fetchLastOrder()
                })
            }


    fun swipeView(model: OrderFragment.Myorder_Model) {
        /**TODO: swipe view visible condition:
         * 1. from splash screen. 2. login and logout.
         */
        swipe_recycler.layoutManager = LinearLayoutManager(activity)
        swipeAdapter = SwipeAdapter(model, clickEvent)
        swipe_recycler.adapter = swipeAdapter
        // Swipe interface...
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(swipe_recycler)

    }

    internal var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            if (swipeAdapter != null) {
                count = 0
                swipeAdapter!!.notifyDataSetChanged()
            }
            //Remove swiped item from list and notify the RecyclerView
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Get RecyclerView item from the ViewHolder
                val itemView = viewHolder.itemView

                val p = Paint()
                p.color = ContextCompat.getColor(context!!, R.color.theme_color)
                loge(TAG, "dx is " + dX)
                if (dX > 0) {
                    /* Set your color for positive displacement */

                    // Draw Rect with varying right side, equal to displacement dX
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), 0f,
                            itemView.bottom.toFloat(), p)
                } else {
                    /* Set your color for negative displacement */

                    // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                    c.drawRect(itemView.right.toFloat() + 0f, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), p)
                }

                super.onChildDraw(null, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    inner class SwipeAdapter(val model: OrderFragment.Myorder_Model, val clickEvent: MyClickHandler) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding: SwipeCartItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.swipe_cart_item, parent, false)
            return MyViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MyViewHolder) {
                holder.binding.orderresult = model.last_order_details
                holder.binding.myclickhandler = clickEvent
                holder.binding.util = BindDataUtils

            }

        }

        override fun getItemCount(): Int {
            return count
        }


        private inner class MyViewHolder(val binding: SwipeCartItemBinding) : RecyclerView.ViewHolder(binding.root) {

        }
    }



    fun fetchLastOrder() {

        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)

        callAPI(ApiCall.lastorder(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val model = body as OrderFragment.Myorder_Model
                if (model.status) {
                    loge(TAG, model.last_order_details.toString())
                    count = 1
                    val animation = TranslateAnimation(0f, 0f, 200f, 0f)
                    animation.duration = 1000
                    animation.fillAfter = true
                    swipe_recycler.startAnimation(animation)
                    swipeView(model)

                } else {
                    count = 0
                    swipeAdapter?.notifyDataSetChanged()
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        //  showSnackBar(home_fragment_container, getString(R.string.error_404))
                    }
                    100 -> {

                        //  showSnackBar(home_fragment_container, getString(R.string.internet_not_available))
                    }
                }
            }
        })
    }

    override fun comman_apisuccess(status: String) {
        loge(TAG, "success..."+"-")
        moveon_reOrder("")
    }

    override fun comman_apifailed(error: String) {
        loge(TAG, "failed...")
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

    inner class MyClickHandler(internal var homefragment: HomeFragment) {

        fun onFindClicked(view: View) {

            if (find_rest_edt.text.trim().toString().length > 0 && homefragment.progress_bar.visibility==View.GONE ) {
                val restaurantlist = RestaurantList.newInstance(find_rest_edt.text.trim().toString())
                addFragment(R.id.home_fragment_container, restaurantlist, RestaurantList.TAG, true)
            }
        }
        fun onFindLocation(view: View) {
           if(homefragment.progress_bar.visibility==View.GONE)
           homefragment.getcurrent_location()

        }

        fun reOrder(view: View, model: OrderFragment.Orderresult) {
            loge(TAG, "reorder---")
            fetchReorder_info(model, home_fragment_container)
        }

        fun onDetails(view: View, model: OrderFragment.Orderresult) {
            val fragment = OrderedRestaurant.newInstance(model)
            addFragment(R.id.home_fragment_container, fragment, OrderedRestaurant.TAG, true)
        }

        fun onRate(view: View, model: OrderFragment.Orderresult) {

            val fragment = RateOrder.newInstance(order_no = model.order_no, orderresult = model)
            var enter: Slide? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                enter = Slide()
                enter.setDuration(Constants.BOTTOM_TO_TOP_ANIM.toLong())
                enter.slideEdge = Gravity.BOTTOM
                val changeBoundsTransition: ChangeBounds = ChangeBounds()
                changeBoundsTransition.duration = Constants.BOTTOM_TO_TOP_ANIM.toLong()
                //fragment!!.sharedElementEnterTransition=changeBoundsTransition
                fragment.sharedElementEnterTransition = changeBoundsTransition
                fragment.sharedElementReturnTransition = changeBoundsTransition
                fragment.enterTransition = enter
            }
            addFragment(R.id.home_fragment_container, fragment, RateOrder.TAG, false)


        }
        /*   fun onClose(view: View, model: OrderFragment.Orderresult) {
               count = 0
               swipeAdapter!!.notifyDataSetChanged()
           }*/


    }


}

