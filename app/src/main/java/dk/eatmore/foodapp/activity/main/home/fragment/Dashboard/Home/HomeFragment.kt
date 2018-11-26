package dk.eatmore.foodapp.fragment.Dashboard.Home


import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.DataBindingUtil
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.facebook.*
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentHomeFragmentBinding
import dk.eatmore.foodapp.model.home.UI_HomeFragment
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.*
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.databinding.SwipeCartItemBinding
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeFragmentBinding
    lateinit var clickEvent: MyClickHandler
    private var mAdapter: OrderListAdapter? = null
    var fragment: DetailsFragment? = null
    private var swipeAdapter: SwipeAdapter? = null
    private lateinit var mAuth: FirebaseAuth
    val callbackManager = CallbackManager.Factory.create()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var count: Int = 0


    companion object {

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
            clickEvent = MyClickHandler(this)
            binding.handlers = clickEvent
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


/*
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
*/
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


        } else {
            logd(TAG, "saveInstance NOT NULL")

        }

    }

    private fun swipeView(model: OrderFragment.Myorder_Model) {

        swipe_recycler.layoutManager = LinearLayoutManager(activity)
        swipeAdapter = SwipeAdapter(model,clickEvent)
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

    private inner class SwipeAdapter(val model: OrderFragment.Myorder_Model,val clickEvent: MyClickHandler) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding :SwipeCartItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.swipe_cart_item,parent,false)
            return MyViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MyViewHolder) {

                holder.binding.orderresult = model.last_order_details
                holder.binding.myclickhandler=clickEvent
                holder.binding.util=BindDataUtils

            }



          /*  if (holder is SwipeHolder) {
                val vh: SwipeHolder = holder
                vh.ordered_item_name.text = jsonObject.getAsJsonObject("last_order_details").get("restaurant_name").asString
                vh.item_price.text = BindDataUtils.convertCurrencyToDanish(jsonObject.getAsJsonObject("last_order_details").get("total_to_pay").asString)
                vh.item_date.text = BindDataUtils.parseDateToddMMyyyy(jsonObject.getAsJsonObject("last_order_details").get("order_date").asString)
                vh.item_status.text = if (jsonObject.getAsJsonObject("last_order_details").get("enable_rating").asBoolean && jsonObject.getAsJsonObject("last_order_details").get("order_status").asString.toLowerCase().equals("accepted")) getString(R.string.rate_it_exclaim) else getString(R.string.ordre_under_behandling)
                vh.item_status.visibility = if (jsonObject.getAsJsonObject("last_order_details").get("enable_rating").asBoolean) View.VISIBLE else View.GONE
                Glide.with(context!!).load(jsonObject.getAsJsonObject("last_order_details").get("app_icon").asString).into(vh.imageview);

            }*/
        }

        override fun getItemCount(): Int {
            return count
        }

/*
        inner class SwipeHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {



            init {
                  close_btn?.setOnClickListener { view ->
                      count = 0
                      swipeAdapter!!.notifyDataSetChanged()
                  }

                row_contoller.setOnClickListener{
                    val model = OrderFragment.Orderresult()
                    model.order_date
                    val fragment = OrderedRestaurant.newInstance(find_rest_edt.text.trim().toString())
                    addFragment(R.id.home_fragment_container, fragment, OrderedRestaurant.TAG, true)


//                    val fragment = OrderedRestaurant.newInstance(restaurantname = model.restaurant_name,appicon = model.app_icon,orderdate = model.order_date,ordernumber = model.order_no,enable_rating = model.enable_rating, orderresult = model)
//                    addFragment(R.id.home_order_container,fragment, OrderedRestaurant.TAG,true)
                }
                reorder_btn.setOnClickListener{

                }


            }
        }
*/

        private inner class MyViewHolder(val binding: SwipeCartItemBinding) : RecyclerView.ViewHolder(binding.root)  {

        }
    }


    fun fetchLastOrder() {

        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))

        callAPI(ApiCall.lastorder(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val model = body as OrderFragment.Myorder_Model
                if (model.status) {
                    loge(TAG, model.last_order_details.toString())
                    count = 1
                    swipeView(model)

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

            if (find_rest_edt.text.trim().toString().length > 0) {
                val restaurantlist = RestaurantList.newInstance(find_rest_edt.text.trim().toString())
                addFragment(R.id.home_fragment_container, restaurantlist, RestaurantList.TAG, true)
            }


        }

        fun reOrder(view: View , model: OrderFragment.Orderresult) {
            loge(TAG,"reorder---")
        }
        fun onDetails(view: View, model: OrderFragment.Orderresult) {

        }
        fun onRate(view: View, model: OrderFragment.Orderresult) {

        }
        fun onClose(view: View, model: OrderFragment.Orderresult) {
            count = 0
            swipeAdapter!!.notifyDataSetChanged()
        }


    }


    inner private class UIModel : ViewModel() {

        var uiData = MutableLiveData<UI_HomeFragment>()

        fun init() {
            val ui_homefragment = UI_HomeFragment("HomeFragment", false)
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

