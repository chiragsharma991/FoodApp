package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.searchmenu.SearchlistParentAdapter
import dk.eatmore.foodapp.databinding.SearchMenuBinding
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.utils.DrawableClickListener.DrawablePosition
import kotlinx.android.synthetic.main.search_menu.*
import java.util.ArrayList
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.home.DefaultAttributeValue
import dk.eatmore.foodapp.model.home.ProductAttributeItem
import dk.eatmore.foodapp.model.home.ProductListItem
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*


class SearchMenu : BaseFragment() {

    private lateinit var binding: SearchMenuBinding
    private lateinit var mAdapter: SearchlistParentAdapter
    private lateinit var menu_list : ArrayList<MenuListItem>
    private lateinit var menu_list_filtered : ArrayList<MenuListItem>
    private var filtertask: AsyncTask<String, Void, String>?=null
    private lateinit var productpricecalculation: ProductPriceCalculation





    companion object {

        val TAG = "SearchMenu"
        var searchString: String=""
        fun newInstance(menuList: ArrayList<MenuListItem>?): SearchMenu {
            val bundle=Bundle()
            val fragment = SearchMenu()
            bundle.putSerializable(Constants.MENULIST,menuList)
            fragment.arguments=bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.search_menu
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
           //return inflater.inflate(getLayout(), container, false)

           binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
           return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            searchString=""
            productpricecalculation = ProductPriceCalculation(this)
            menu_list = arguments!!.getSerializable(Constants.MENULIST) as ArrayList<MenuListItem>
            menu_list_filtered= menu_list
            search_edt.requestFocus()
            showKeyboard()
            keylistner()
            refreshview()


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }



    }

    private fun keylistner() {

        img_toolbar_back.setOnClickListener{
            (activity as HomeActivity).onBackPressed()
        }
        cancel_button.setOnClickListener{
            search_edt.text.clear()

        }

    /*    search_edt.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawableClickListener.DrawablePosition.LEFT -> {
                  //     (activity as HomeActivity).onBackPressed()
                    }
                    else -> {
                        search_edt.text.clear()
                    }
                }
            }
        })*/

        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                if(filtertask ==null){
                    filtertask = FilterTask().execute(search_edt.text.toString())

                }else{
                    filtertask!!.cancel(true)
                    filtertask = FilterTask().execute(search_edt.text.toString())
                }
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int,
                                           arg2: Int, arg3: Int) {
            }

            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int,
                                       arg3: Int) {
            }
        })


    }




         inner class FilterTask() : AsyncTask<String, Void, String>() {

            override fun onPreExecute() {
            }

            override fun doInBackground(vararg params: String): String {
                var count=0
                val charString = params[0]
                loge(TAG,"do in ---"+charString)
                SearchMenu.searchString=charString.toLowerCase()
                if (charString.isEmpty()) {
                    menu_list_filtered = menu_list

                } else {

                    val filteredList = ArrayList<MenuListItem>() // This is main list to add
                    var i =0

                    while (i < menu_list.size  && !isCancelled()) {
                        loge(TAG,"is p canceled "+isCancelled)

                        val productlistitem = ArrayList<ProductListItem>() // we can add filter list in this varg
                        var j =0

                        // Product name

                        while (j < menu_list.get(i).product_list!!.size && !isCancelled()) {

                            // category

                            if(menu_list.get(i).product_list!![j].p_name.toLowerCase().contains(charString.toLowerCase()) || menu_list.get(i).product_list!![j].p_desc.toLowerCase().contains(charString.toLowerCase())){
                                count++
                                loge(TAG,"count ---"+count)

                                val productattributeitem = ArrayList<ProductAttributeItem>()
                                val item =menu_list.get(i).product_list!![j]
                                if(menu_list.get(i).product_list!!.get(j).product_attribute ==null){


                                    productlistitem.add(ProductListItem(
                                            productIngredients = "",
                                            restaurantId = "",
                                            product_attribute = arrayListOf(),
                                            p_desc =item.p_desc,
                                            p_price = if(item.p_price == null) "" else item.p_price,
                                            productNo = item.productNo ,
                                            p_name = item.p_name,
                                            extra_topping_group =item.extra_topping_group ,
                                            c_id = item.c_id,
                                            is_attributes =item.is_attributes ,
                                            pImage = item.pImage,
                                            p_id = item.p_id,

                                            actual_price = item.actual_price,
                                            actual_price_afterDiscount = item.actual_price_afterDiscount,
                                            discountType = item.discountType,
                                            discount = item.discount,
                                            minimum_order_price = item.minimum_order_price,
                                            offerDiscounted = item.offerDiscounted
                                    )
                                    )

                                }else{
                                    for (k in 0.until(menu_list.get(i).product_list!!.get(j).product_attribute!!.size) ){

                                        productattributeitem.add(ProductAttributeItem(
                                                menu_list.get(i).product_list!!.get(j).product_attribute!!.get(k).pam_id,
                                                menu_list.get(i).product_list!!.get(j).product_attribute!!.get(k).a_name,
                                                DefaultAttributeValue(
                                                        menu_list.get(i).product_list!!.get(j).product_attribute!!.get(k).default_attribute_value.pad_id,
                                                        menu_list.get(i).product_list!!.get(j).product_attribute!!.get(k).default_attribute_value.a_price,
                                                        menu_list.get(i).product_list!!.get(j).product_attribute!!.get(k).default_attribute_value.a_value
                                                )
                                        )
                                        )
                                    }

                                    productlistitem.add(ProductListItem(
                                            productIngredients = "",
                                            restaurantId = "",
                                            product_attribute = productattributeitem,
                                            p_desc =item.p_desc,
                                            p_price = if(item.p_price == null) "" else item.p_price,
                                            productNo = item.productNo ,
                                            p_name = item.p_name,
                                            extra_topping_group =item.extra_topping_group ,
                                            c_id = item.c_id,
                                            is_attributes =item.is_attributes ,
                                            pImage = item.pImage,
                                            p_id = item.p_id,

                                            actual_price = item.actual_price,
                                            actual_price_afterDiscount = item.actual_price_afterDiscount,
                                            discountType = item.discountType,
                                            discount = item.discount,
                                            minimum_order_price = item.minimum_order_price,
                                            offerDiscounted = item.offerDiscounted

                                    )
                                    )

                                }

                            }
                            ++j

                        }
                        // add if condition here
                        if(productlistitem.size > 0)
                        filteredList.add(MenuListItem("","","","", menu_list.get(i).c_name,productlistitem))




                        ++i
                    }
                    menu_list_filtered = filteredList
                }
                return count.toString()
            }
            override fun onPostExecute(result: String) {
                //mAdapter.notifyDataSetChanged()
                mAdapter.refreshlist(menu_list_filtered)


            }
        }


    private fun refreshview(){

        recycler_view_parent.apply {

            mAdapter = SearchlistParentAdapter(context!!,menu_list,menu_list_filtered, object : SearchlistParentAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {
                    loge(TAG,"clicked---"+parentPosition+"-"+chilPosition)

                    //Direct add to cart process condition
                    if(DetailsFragment.is_restaurant_closed) return // if restaurant is closed then nothing will happen
                    val data = menu_list_filtered.get(parentPosition).product_list!!.get(chilPosition)
                    if (data.is_attributes == "0" && data.extra_topping_group == null) {
                        addToCard(data)
                    } else {
                        hideKeyboard()
                        val intent = Intent(activity, CartActivity::class.java)
                        intent.putExtra("TITLE", data.p_name)
                        intent.putExtra("PID", data.p_id)
                        intent.putExtra("actual_price", data.actual_price?:"")
                        intent.putExtra("actual_price_afterDiscount", data.actual_price_afterDiscount?:"")
                        intent.putExtra("discountType", data.discountType)
                        intent.putExtra("discount", data.discount?:"")
                        intent.putExtra("offerDiscounted", data.offerDiscounted)
                        intent.putExtra("minimum_order_price", data.minimum_order_price?:"")
                        val pairs: Array<Pair<View, String>> = TransitionHelper.createSafeTransitionParticipants(activity!!, true)
                        val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, *pairs)
                        //startActivityForResult(intent,Constants.REQ_SEA_RESAURANT_CLOSED, transitionActivityOptions.toBundle())
                        startActivityForResult(intent,Constants.REQ_SEA_RESAURANT_CLOSED)
                    }

                }
            })
            mAdapter.setHasStableIds(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge("onActivityResult searchmenu---","---")
        if(requestCode == Constants.REQ_SEA_RESAURANT_CLOSED){
            if(resultCode == Activity.RESULT_OK){
                any_preorder_closedRestaurant(data!!.extras.get(Constants.IS_RESTAURANT_CLOSED) as Boolean,data.extras.get(Constants.PRE_ORDER) as Boolean,data.extras.get(Constants.MSG) as String )
            }
        }
    }

    private fun addToCard(data: ProductListItem) {

        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.SHIPPING, if (DetailsFragment.isPickup) context!!.getString(R.string.pickup_) else context!!.getString(R.string.delivery_))
        if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        } else {
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }
        postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
        postParam.addProperty(Constants.P_ID, data.p_id)
        when(data.discountType){
            0 ->{
                // no discount
                postParam.addProperty(Constants.P_PRICE,data.actual_price)
                postParam.addProperty(Constants.DISCOUNT_APPLIED,  0 )
                postParam.addProperty(Constants.PRODUCT_DISCOUNT_,"0.00")
            }
            1->{
                // product discount
                postParam.addProperty(Constants.P_PRICE, data.actual_price)
                postParam.addProperty(Constants.DISCOUNT_APPLIED,if(data.offerDiscounted) 1 else 0 )
                postParam.addProperty(Constants.PRODUCT_DISCOUNT_,if(data.offerDiscounted) data.discount else "0.00")  // add discount amount from API
            }
            2->{
                // order discount
                postParam.addProperty(Constants.P_PRICE, data.actual_price)
                if(DetailsFragment.total_cartamt.toDouble() + data.actual_price!!.toDouble() >= data.minimum_order_price!!.toDouble())
                    postParam.addProperty(Constants.DISCOUNT_APPLIED,2)
                else
                    postParam.addProperty(Constants.DISCOUNT_APPLIED,0)
                val discountprice_only = ((data.discount!!.toDouble() * data.actual_price!!.toDouble())/100)
                postParam.addProperty(Constants.PRODUCT_DISCOUNT_,discountprice_only)  // add only discount amount on actual price

            }
        }
        postParam.addProperty(Constants.P_QUANTITY, "1")
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)

        callAPI(ApiCall.addtocart(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get("status").asBoolean) {
                    showProgressDialog()

                    when(getrestaurantstatus(is_restaurant_closed =jsonObject.get(Constants.IS_RESTAURANT_CLOSED)?.asBoolean, pre_order =jsonObject.get(Constants.PRE_ORDER)?.asBoolean )){

                        RestaurantState.CLOSED ->{
                            val msg = if (jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                            any_preorder_closedRestaurant(is_restaurant_closed = true ,pre_order = false,msg =msg ) // set hard code to close restaurant.
                        }
                        else ->{
                            val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                            intent.putExtra(Constants.CARTCNT,if(jsonObject.get(Constants.CARTCNT).isJsonNull  || jsonObject.get(Constants.CARTCNT).asString =="0") 0 else (jsonObject.get(Constants.CARTCNT).asString).toInt())
                            intent.putExtra(Constants.CARTAMT,if(jsonObject.get(Constants.CARTAMT).isJsonNull || jsonObject.get(Constants.CARTAMT).asString =="0") "00.00" else jsonObject.get(Constants.CARTAMT).asString)
                            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
                            Toast.makeText(context, getString(R.string.item_has_been), Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    showProgressDialog()
                    showSnackBar(clayout_crt, getString(R.string.error_404))
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
                showProgressDialog()
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


    class ProductPriceCalculation(val searchmenu: SearchMenu) {

        var attribute_cost: Double = 0.0
        fun getprice(productListItem: ProductListItem): String {
            attribute_cost = 0.0
            for (i in 0..productListItem.product_attribute!!.size - 1) {
                attribute_cost = attribute_cost + productListItem.product_attribute.get(i).default_attribute_value.a_price.toDouble()
            }
            return BindDataUtils.convertCurrencyToDanish(attribute_cost.toString()) ?: "null"
        }

    }

}

