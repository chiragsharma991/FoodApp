package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.transition.ChangeBounds
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentOrderContainerBinding
import dk.eatmore.foodapp.databinding.RowOrderedPizzaBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.fragment_order_container.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.Serializable
import java.util.*

class OrderFragment : CommanAPI(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: FragmentOrderContainerBinding
    private val myclickhandler = MyClickHandler(this@OrderFragment)
    public var mAdapter: UniversalAdapter<Orderresult, RowOrderedPizzaBinding>? = null


    companion object {
        val TAG = "OrderFragment"
        var ui_model: UIModel? = null
        fun newInstance(): OrderFragment {
            return OrderFragment()
        }
    }

    override fun onRefresh() {
        // swipe to refresh>>>
        swipetorefresh_view.visibility = View.GONE
        fetchmyOrder()
        if(HomeFragment.ui_model?.reloadfragment !=null && HomeFragment.count ==1) HomeFragment.ui_model!!.reloadfragment.value=true  // reload last order from homefragment.
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // Event when you change tab...
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_order_container
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        loge(TAG, "saveInstance " + savedInstanceState)
        if (savedInstanceState == null) {
            loge(TAG,"check visible-"+getUserVisibleHint())
            empty_view.visibility = View.GONE
            //progress_bar.visibility=View.GONE
            txt_toolbar.text = getString(R.string.orders)
            img_toolbar_back.visibility = View.GONE
            swipeRefresh.setOnRefreshListener(this)
            ui_model = createViewModel()
            if (!PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
                empty_view.visibility = View.VISIBLE
                error_txt.text = getString(R.string.please_login_to_see)
                return
            }
            fetchmyOrder()
        }

/*

         ui_model = ViewModelProviders.of(this).get(UIModel::class.java)
        ui_model!!.getUIModel().observe(this, Observer<UI_OrderFragment>{
            loge(TAG,"observer success---")
            binding.uiOrder=ui_model!!.getUIModel().value
        })
        ui_model!!.init()
*/
    }


    class UIModel : ViewModel() {
        var myorder_List = MutableLiveData<Myorder_Model>()  // this is list to show pre order
        var restaurant_info = MutableLiveData<Myorder_Model>() // this is list for fetch restaurant info
        var reloadfragment = MutableLiveData<Boolean>()

    }

    fun createViewModel(): OrderFragment.UIModel =

            ViewModelProviders.of(this).get(OrderFragment.UIModel::class.java).apply {
                myorder_List.removeObservers(this@OrderFragment)
                restaurant_info.removeObservers(this@OrderFragment)
                reloadfragment.removeObservers(this@OrderFragment)

                myorder_List.observe(this@OrderFragment, Observer<Myorder_Model> {
                    val list = ArrayList<Orderresult>()
                    list.addAll(ui_model!!.myorder_List.value!!.orderresult)
                    refreshview(list)
                })
                restaurant_info.observe(this@OrderFragment, Observer<Myorder_Model> {
                    loge(TAG, "move next refresh---")
                    moveon_next()
                })
                reloadfragment.observe(this@OrderFragment, Observer<Boolean> {
                    // reload fragment from here.
                    loge(TAG, "reload refresh---")
                    fetchmyOrder()

                })
            }


    fun refreshview(list: ArrayList<Orderresult>) {
        if (mAdapter != null) {
            mAdapter!!.updatedata(list)
            mAdapter!!.notifyDataSetChanged()
            return
        }
        swipetorefresh_view.visibility = if (list.size > 3) View.GONE else (if (swipetorefresh_view.visibility == View.VISIBLE) View.VISIBLE else View.GONE)
        recycler_view.apply {
            mAdapter = UniversalAdapter(context!!, list, R.layout.row_ordered_pizza, object : RecyclerCallback<RowOrderedPizzaBinding, Orderresult> {
                override fun bindData(binder: RowOrderedPizzaBinding, model: Orderresult) {
                    binder.orderresult = model
                    binder.util = BindDataUtils
                    binder.myclickhandler = myclickhandler

                    //   binder.handler=this@OrderFragment
                }
            })
            layoutManager = LinearLayoutManager(getActivityBase())
            adapter = mAdapter
        }
    }


    private fun moveon_next() {

        val fragment = DetailsFragment.newInstance(
                restaurant = ui_model!!.restaurant_info.value!!.restaurant_info,
                status = ""
        )
        var enter: Slide? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(300)
            enter.slideEdge = Gravity.BOTTOM
            fragment.enterTransition = enter
        }
        // pop all fragment on homecontainer to open reorder framgment.
        val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
        (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
        (activity as HomeActivity).supportFragmentManager.beginTransaction().add(R.id.home_container, fragment, DetailsFragment.TAG).addToBackStack(DetailsFragment.TAG).commit()

    }


    private fun onDetails(model: Orderresult) {
        val fragment = OrderedRestaurant.newInstance(model)
        addFragment(R.id.home_order_container, fragment, OrderedRestaurant.TAG, true)

    }

    private fun onRate(model: Orderresult) {

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
        addFragment(R.id.home_order_container, fragment, RateOrder.TAG, false)
    }

    fun fetchmyOrder() {
        empty_view.visibility = View.GONE
        //  progress_bar.visibility=View.VISIBLE
        swipeRefresh.isRefreshing = true
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))

        callAPI(ApiCall.myorders(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val myorder_Model = body as Myorder_Model
                if (myorder_Model.status) {
                    loge(TAG, "status--" + myorder_Model.orderresult.get(0).order_no)
                    ui_model!!.myorder_List.value = myorder_Model
                } else {
                    //  if(ui_model!!.myorder_List.value != null) ui_model!!.myorder_List.value!!.orderresult.clear()
                    //  val list = ArrayList<Orderresult>()
                    //  refreshview(list)
                    if (mAdapter != null) {
                        mAdapter!!.getdata().clear()
                        mAdapter!!.notifyDataSetChanged()
                    }
                    empty_view.visibility = View.VISIBLE
                    error_txt.text = getString(R.string.no_order)
                }
                //progress_bar.visibility=View.GONE
                swipeRefresh.isRefreshing = false


            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(home_order_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(home_order_container, getString(R.string.internet_not_available))
                    }
                }
                swipeRefresh.isRefreshing = false
                //progress_bar.visibility=View.GONE
            }
        })
    }


/*
    fun fetchRestaurant_info(model: Orderresult, cartcnt: String, cartamt: String, show_msg: Boolean, msg: String) {


        callAPI(ApiCall.restaurant_info(
                r_token = model.r_token,
                r_key = model.r_key,
                customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, "")!!

        ), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val myorder_Model = body as Myorder_Model
                if (myorder_Model.status) {
                    PreferenceUtil.putValue(PreferenceUtil.R_KEY, model.r_key)
                    PreferenceUtil.putValue(PreferenceUtil.R_TOKEN, model.r_token)
                    myorder_Model.restaurant_info.cartamt = cartamt
                    myorder_Model.restaurant_info.cartcnt = cartcnt
                    PreferenceUtil.save()
                    // if restaurant is closed then block all next process.
                    if (model.is_restaurant_closed) {
                        DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(context!!, R.color.black), msg = getString(R.string.we_are_sorry), title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                            override fun onPositiveButtonClick(position: Int) {}
                            override fun onNegativeButtonClick() {}
                        })
                    } else {
                        // if restaurant wants to show some info about then.
                        if (show_msg) {
                            DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(context!!, R.color.black), msg = msg, title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                                override fun onPositiveButtonClick(position: Int) {
                                    ui_model!!.restaurant_info.value = myorder_Model // move this response to another list to reorder perpose.
                                }

                                override fun onNegativeButtonClick() {}
                            })
                        } else {
                            ui_model!!.restaurant_info.value = myorder_Model // move this response to another list to reorder perpose.
                        }

                    }

                }
                loge(TAG, "ending----")
                showProgressDialog()
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(home_order_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(home_order_container, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
    }
*/


/*
    fun fetchReorder_info(model: Orderresult) {

        empty_view.visibility = View.GONE
        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_NO, model.order_no)

        callAPI(ApiCall.reorder(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    fetchRestaurant_info(
                            model = model,
                            msg = jsonObject.get(Constants.MSG).asString,
                            cartamt = jsonObject.get(Constants.CARTAMT).asString,
                            cartcnt = jsonObject.get(Constants.CARTCNT).asString,
                            show_msg = jsonObject.get(Constants.SHOW_MSG).asBoolean
                    )

                } else {
                    showProgressDialog()
                    showSnackBar(home_order_container, getString(R.string.error_404))

                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(home_order_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(home_order_container, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
    }
*/


    /*
        override fun onClick(user: User) {
            val fragment = OrderedRestaurant.newInstance()
            addFragment(R.id.home_order_container,fragment, OrderedRestaurant.TAG,true)
        }

    */
    override fun comman_apisuccess(msg: String, model: Myorder_Model) {
        moveon_reOrder(model)
    }

    override fun comman_apifailed(error: String) {
    }


    class MyClickHandler(val orderFragment: OrderFragment) {

        fun reOrder(view: View, model: Orderresult) {
            if (orderFragment.swipeRefresh.isRefreshing == false){
                orderFragment.empty_view.visibility = View.GONE
                // orderFragment.fetchReorder_info(model)
                orderFragment.fetchReorder_info(model,orderFragment.home_order_container)
            }
        }

        fun onDetails(view: View, model: Orderresult) {
            if (orderFragment.swipeRefresh.isRefreshing == false)
                orderFragment.onDetails(model)
        }

        fun onRate(view: View, model: Orderresult) {
            if (model.order_status.toLowerCase() == Constants.ACCEPTED) {
                if (orderFragment.swipeRefresh.isRefreshing == false)
                    orderFragment.onRate(model)
            }
        }

    }

    // we set one model for all API call.

    data class Myorder_Model(
            val status: Boolean = false,
            val msg: String = "",
            val orderresult: ArrayList<Orderresult> = arrayListOf(),
            val last_order_details: Orderresult,
            val restaurant_info: Restaurant

    ) : Serializable

    data class Orderresult(
            var customer_id: String = "",
            var restaurant_id: String = "",
            var order_status: String = "",
            var order_no: String = "",
            var expected_time: String = "",
            var total_to_pay: String = "",
            var order_date: String = "",
            var discount_amount: String? = null,
            var restaurant_name: String = "",
            var postal_code: String = "",
            var app_icon: String = "",
            var is_restaurant_closed: Boolean = false,
            var r_key: String = "",
            var r_token: String = "",
            var enable_rating: Boolean = false

    ) : Serializable {

        init {


        }
    }


    override fun onDestroy() {
        super.onDestroy()
        loge(TAG, "on destroy...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loge(TAG, "on destroyView...")
        // ui_model=null
    }

    override fun onDetach() {
        super.onDetach()
        loge(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        loge(TAG, "on pause...")

    }

}


@BindingAdapter("android:layout_setImage")
fun setImage(view: AppCompatImageView, model: OrderFragment.Orderresult) {
    // i set 100 fixed dp in rating page thats why i am using 100
    Log.e("set image", "----" + model.toString())
    Glide.with(view.context).load(model.app_icon).into(view);

}




