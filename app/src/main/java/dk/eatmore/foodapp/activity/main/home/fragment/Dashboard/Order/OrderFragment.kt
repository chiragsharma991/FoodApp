package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
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
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentOrderContainerBinding
import dk.eatmore.foodapp.databinding.RowOrderedPizzaBinding
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.order.UI_OrderFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_order_container.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.Serializable
import java.util.ArrayList

class OrderFragment : BaseFragment(), RecyclerClickInterface {


    private lateinit var binding: FragmentOrderContainerBinding
    private  val myclickhandler = MyClickHandler(this@OrderFragment)




    companion object {

        val TAG= "OrderFragment"
        var ui_model: UIModel? = null
        fun newInstance() : OrderFragment {
            return OrderFragment()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_order_container
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {

        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            empty_view.visibility=View.GONE
            progress_bar.visibility=View.GONE
            txt_toolbar.text=getString(R.string.orders)
            img_toolbar_back.visibility=View.GONE
            if(!PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
                empty_view.visibility=View.VISIBLE
                error_txt.text=getString(R.string.please_login_to_see)
                return
            }
            if (ui_model == null){
                loge(TAG,"ui model is null")
                ui_model = createViewModel()
                fetchmyOrder()
            }else{
                loge(TAG,"ui model is active")

                // refreshview()
            }
        }else{
            logd(TAG,"saveInstance NOT NULL")
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
        var restaurant_info = MutableLiveData<Testingclass>() // this is list for fetch restaurant info
        var reloadfragment = MutableLiveData<Boolean>()

    }

     fun createViewModel(): OrderFragment.UIModel =

            ViewModelProviders.of(this).get(OrderFragment.UIModel::class.java).apply {
                myorder_List.observe(this@OrderFragment, Observer<Myorder_Model> {
                    refreshview()
                })
                restaurant_info.observe(this@OrderFragment, Observer<Testingclass> {
                    loge(TAG,"move next refresh---")
                   // moveon_next()
                })
                reloadfragment.observe(this@OrderFragment, Observer<Boolean> {
                    // reload fragment from here.
                    loge(TAG,"reload refresh---")
                    fetchmyOrder()


                })
            }



     fun refreshview() {
        loge(TAG,"refresh view--")
        recycler_view.apply {
           val mAdapter = UniversalAdapter(context!!, ui_model!!.myorder_List.value!!.orderresult, R.layout.row_ordered_pizza, object : RecyclerCallback<RowOrderedPizzaBinding, Orderresult> {
                override fun bindData(binder: RowOrderedPizzaBinding, model: Orderresult) {
                    binder.orderresult=model
                    binder.util=BindDataUtils
                    binder.myclickhandler=myclickhandler
                 //   binder.handler=this@OrderFragment
                } })
            layoutManager = LinearLayoutManager(getActivityBase())
            adapter = mAdapter
        }
    }


    private fun moveon_next(){
        loge(TAG,"moveon_next--")

        val fragment = DetailsFragment.newInstance(
                restaurant =  ui_model!!.restaurant_info.value!!.restaurant_info,
                status =     "test"
        )
        var enter : Slide?=null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(300)
            enter.slideEdge = Gravity.BOTTOM
//            val changeBoundsTransition : ChangeBounds = ChangeBounds()
//            changeBoundsTransition.duration = 300
//            //fragment!!.sharedElementEnterTransition=changeBoundsTransition
//            fragment.sharedElementEnterTransition=changeBoundsTransition
//            fragment.sharedElementReturnTransition=changeBoundsTransition
            fragment.enterTransition=enter

        }
        (activity as HomeActivity).supportFragmentManager.beginTransaction().replace(R.id.home_container, fragment, DetailsFragment.TAG).addToBackStack(DetailsFragment.TAG).commit()

    }


    private fun onDetails(model : Orderresult){
        loge(TAG,"onDetails-")
    }

    fun fetchmyOrder() {
        empty_view.visibility=View.GONE
        progress_bar.visibility=View.VISIBLE
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))

        callAPI(ApiCall.myorders(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val myorder_Model = body as Myorder_Model
                if (myorder_Model.status) {
                    loge(TAG,"status--"+myorder_Model.orderresult.size.toString())
                    ui_model!!.myorder_List.value=myorder_Model
                }else{
                    empty_view.visibility=View.VISIBLE
                    error_txt.text=getString(R.string.no_order)
                }
                progress_bar.visibility=View.GONE

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
                progress_bar.visibility=View.GONE
            }
        })
    }


    fun fetchRestaurant_info(model : Orderresult) {

        empty_view.visibility=View.GONE
        showProgressDialog()

        callAPI(ApiCall.restaurant_info(
                r_token = model.r_token,
                r_key = model.r_key,
                customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!

        ), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val myorder_Model= body as Testingclass
                if (myorder_Model.status) {
                    PreferenceUtil.putValue(PreferenceUtil.R_KEY, model.r_key)
                    PreferenceUtil.putValue(PreferenceUtil.R_TOKEN,model.r_token)
                    PreferenceUtil.save()
                    ui_model!!.restaurant_info.value = myorder_Model // move this response to another list to reorder perpose.
                    moveon_next()
                }
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




    override fun onClick(user: User) {
        val fragment = OrderedRestaurant.newInstance()
        addFragment(R.id.home_order_container,fragment, OrderedRestaurant.TAG,true)
    }

    class MyClickHandler(val orderFragment: OrderFragment) {

        fun reOrder(view: View , model: Orderresult) {
            orderFragment.fetchRestaurant_info(model)
        }
        fun onDetails(view: View, model: Orderresult) {
            orderFragment.onDetails(model)
        }

    }

    // we set one model for all API call.

    data class Myorder_Model(
            val status: Boolean = false,
            val msg: String ="",
            val orderresult: ArrayList<Orderresult> = arrayListOf(),
            val restaurant_info : Restaurant

    ) : Serializable

    data class Orderresult(
            var customer_id: String = "",
            var restaurant_id: String = "",
            var order_no: String = "",
            var expected_time: String = "",
            var total_to_pay: String = "",
            var order_date: String = "",
            var discount_amount: String? = null,
            var restaurant_name: String = "",
            var postal_code: String = "",
            var app_icon: String = "",
            var r_key: String = "",
            var r_token: String = ""

    ) : Serializable

    data class Testingclass(
            val status: Boolean = false,
            val restaurant_info : Restaurant
    )











    override fun onDestroy() {
        super.onDestroy()
        loge(TAG,"on destroy...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loge(TAG,"on destroyView...")
        ui_model=null
    }

    override fun onDetach() {
        super.onDetach()
        loge(TAG,"on detech...")

    }

    override fun onPause() {
        super.onPause()
        loge(TAG,"on pause...")

    }

}


@BindingAdapter("android:layout_setImage")
fun setImage(view : AppCompatImageView, model : OrderFragment.Orderresult) {
    // i set 100 fixed dp in rating page thats why i am using 100
  Log.e("set image","----"+model.toString())
    Glide.with(view.context).load(model.app_icon).into(view);

}




