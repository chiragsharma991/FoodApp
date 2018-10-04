package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home


import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateAttribute
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.cart.fragment.Extratoppings
import dk.eatmore.foodapp.activity.main.cart.fragment.OnlyExtratoppings
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.adapter.restaurantList.RestaurantListParentAdapter
import dk.eatmore.foodapp.databinding.FragmentFbSignupBinding
import dk.eatmore.foodapp.databinding.RestaurantlistBinding
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
import dk.eatmore.foodapp.model.home.AreaDetails
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.model.home.RestaurantListModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.CartListFunction
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.fragment_account_container.*
import kotlinx.android.synthetic.main.restaurantlist.*
import java.util.HashMap


class RestaurantList : BaseFragment() {


    private lateinit var binding: RestaurantlistBinding
    private lateinit var clickEvent: MyClickHandler
    private lateinit  var list : ArrayList<StatusWiseRestaurant>
    private lateinit var mAdapter: RestaurantListParentAdapter


    companion object {

        val TAG = "RestaurantList"
        var ui_model: RestaurantList.UIModel? = null
        fun newInstance(): RestaurantList {
            return RestaurantList()
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

    private fun fetch_ProductDetailList() {

        val jsonobject= JsonObject()
        jsonobject.addProperty(Constants.AUTH_KEY,Constants.AUTH_VALUE)
        jsonobject.addProperty(Constants.EATMORE_APP,true)
        jsonobject.addProperty(Constants.POSTAL_CODE,"6400")

        callAPI(ApiCall.restaurantList(jsonobject), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val restaurantlistmodel = body as RestaurantListModel
                if (restaurantlistmodel.status) {
                    ui_model!!.restaurantList.value=restaurantlistmodel

                }
            }

            override fun onFail(error: Int) {
           /*     when (error) {
                    404 -> {
                        showSnackBar(clayout_crt, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout_crt, getString(R.string.internet_not_available))
                    }
                }*/
            }
        })


    }


    private fun refreshview() {
        loge(TAG,"refresh view...")
        list= ArrayList()
        var statuswiserestaurant= StatusWiseRestaurant("Open Now",ui_model!!.restaurantList.value!!.restaurant_list.open_now)
        list.add(statuswiserestaurant)
        statuswiserestaurant= StatusWiseRestaurant("Pre Order",ui_model!!.restaurantList.value!!.restaurant_list.pre_order)
        list.add(statuswiserestaurant)
        statuswiserestaurant= StatusWiseRestaurant("Closed",ui_model!!.restaurantList.value!!.restaurant_list.closed)
        list.add(statuswiserestaurant)
        recycler_view_parent.apply {

            mAdapter = RestaurantListParentAdapter(context!!,list, object : RestaurantListParentAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                    loge(TAG,"clicked---")
                }
            })
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }


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
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG, "on pause...")

    }

    data class StatusWiseRestaurant(
            val status: String = "",
            val restaurant: ArrayList<Restaurant>
    )


    class MyClickHandler(val restaurantlist: RestaurantList) {


        fun signupFunction(view: View) {

        }


    }


}
