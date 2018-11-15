package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home


import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.restaurantList.RestaurantListParentAdapter
import dk.eatmore.foodapp.databinding.RestaurantlistBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.restaurantlist.*
import kotlinx.android.synthetic.main.toolbar.*


class RestaurantList : BaseFragment() {


    private lateinit var binding: RestaurantlistBinding
    private lateinit var clickEvent: MyClickHandler
    private lateinit  var list : ArrayList<StatusWiseRestaurant>
    private lateinit var mAdapter: RestaurantListParentAdapter


    companion object {

        fun getuimodel() : UIModel? {
            return ui_model
        }

        val TAG = "RestaurantList"
        var ui_model: RestaurantList.UIModel? = null
        fun newInstance(postal_code : String): RestaurantList {
            val fragment = RestaurantList()
            val bundle = Bundle()
            bundle.putString(Constants.POSTAL_CODE ,postal_code)
            fragment.arguments=bundle
            return fragment
        }
    }


    override fun getLayout(): Int {
        return R.layout.restaurantlist
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            progresswheel(progresswheel,true)
            setToolbarforThis()
            search_again_btn.setOnClickListener{ onBackpress() }
            ui_model = createViewModel()
            if (ui_model!!.restaurantList.value == null) {
                fetch_ProductDetailList()
            } else {
                refreshview()
            }
        } else {
            logd(TAG, "saveInstance NOT NULL")

        }

    }

    fun setToolbarforThis(){
        txt_toolbar.text=getString(R.string.restaurants)
        img_toolbar_back.setImageResource(R.drawable.back)
        img_toolbar_back.setOnClickListener{
            onBackpress()
        }
    }


    private fun fetch_ProductDetailList() {
        val bundle=arguments

        val jsonobject= JsonObject()
        jsonobject.addProperty(Constants.AUTH_KEY,Constants.AUTH_VALUE)
        jsonobject.addProperty(Constants.EATMORE_APP,true)
        jsonobject.addProperty(Constants.POSTAL_CODE,bundle!!.getString(Constants.POSTAL_CODE,"0"))
        if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
            jsonobject.addProperty(Constants.IS_LOGIN, "1")
            jsonobject.addProperty(Constants.CUSTOMER_ID,PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))
        }else{
            jsonobject.addProperty(Constants.IS_LOGIN, "0")
        }
        jsonobject.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))

        callAPI(ApiCall.restaurantList(jsonobject), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val restaurantlistmodel = body as RestaurantListModel
                if (restaurantlistmodel.status) {
                    ui_model!!.restaurantList.value=restaurantlistmodel

                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(clayout_crt, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout_crt, getString(R.string.internet_not_available))
                    }
                }
                progresswheel(progresswheel,false)
            }
        })


    }


    private fun refreshview() {
        var statuswiserestaurant: StatusWiseRestaurant
        list= ArrayList()
        if(ui_model!!.restaurantList.value!!.restaurant_list.open_now.size > 0){
            statuswiserestaurant= StatusWiseRestaurant(getString(R.string.open_now),getString(R.string.ordernow),ui_model!!.restaurantList.value!!.restaurant_list.open_now)
            list.add(statuswiserestaurant)
        }
        if(ui_model!!.restaurantList.value!!.restaurant_list.pre_order.size > 0){
            statuswiserestaurant= StatusWiseRestaurant(getString(R.string.pre_order),getString(R.string.preorder),ui_model!!.restaurantList.value!!.restaurant_list.pre_order)
            list.add(statuswiserestaurant)
        }
        if(ui_model!!.restaurantList.value!!.restaurant_list.closed.size > 0){
            statuswiserestaurant= StatusWiseRestaurant(getString(R.string.closed),getString(R.string.notavailable),ui_model!!.restaurantList.value!!.restaurant_list.closed)
            list.add(statuswiserestaurant)
        }
        if(list.size <= 0 ){
            error_view.visibility=View.VISIBLE
        }

        recycler_view_parent.apply {

            mAdapter = RestaurantListParentAdapter(context!!,list, object : RestaurantListParentAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                    loge(TAG,"clicked---")
                    val fragment = DetailsFragment.newInstance(
                            restaurant = list.get(parentPosition).restaurant.get(chilPosition),
                            status =     list.get(parentPosition).status
                    )
                    var enter : Slide?=null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        enter = Slide()
                        enter.setDuration(300)
                        enter.slideEdge = Gravity.BOTTOM
                        val changeBoundsTransition : ChangeBounds = ChangeBounds()
                        changeBoundsTransition.duration = 300
                        //fragment!!.sharedElementEnterTransition=changeBoundsTransition
                        fragment.sharedElementEnterTransition=changeBoundsTransition
                        fragment.sharedElementReturnTransition=changeBoundsTransition
                        fragment.enterTransition=enter
                    }
                    PreferenceUtil.putValue(PreferenceUtil.R_KEY,list.get(parentPosition).restaurant.get(chilPosition).r_key)
                    PreferenceUtil.putValue(PreferenceUtil.R_TOKEN,list.get(parentPosition).restaurant.get(chilPosition).r_token)
                    PreferenceUtil.save()
                    (parentFragment as HomeFragment).addFragment(R.id.home_fragment_container,fragment, DetailsFragment.TAG,false)
                }
            })
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        progresswheel(progresswheel,false)

    }






    private fun createViewModel(): UIModel =

            ViewModelProviders.of(this).get(RestaurantList.UIModel::class.java).apply {
                restaurantList.observe(this@RestaurantList,Observer<RestaurantListModel>{
                    refreshview()
                })
            }


    class UIModel : ViewModel() {

        var restaurantList = MutableLiveData<RestaurantListModel>()


    }


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
        ui_model=null
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

    fun onBackpress(){
        (activity as HomeActivity).onBackPressed()
    }

    data class StatusWiseRestaurant(
            // status : order type like : pre order, new order
            // order type : Mainly use to show on button

            val status: String = "",
            val ordertype: String = "",
            val restaurant: ArrayList<Restaurant>
    )


    class MyClickHandler(val restaurantlist: RestaurantList) {


        fun signupFunction(view: View) {

        }


    }


}
