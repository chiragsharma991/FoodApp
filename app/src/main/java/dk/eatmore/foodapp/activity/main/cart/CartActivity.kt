package dk.eatmore.foodapp.activity.main.cart

import android.app.Activity
import android.app.Instrumentation
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.transition.Slide
import android.transition.Transition
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.R.id.addtocart_view
import dk.eatmore.foodapp.activity.main.cart.fragment.Extratoppings
import dk.eatmore.foodapp.activity.main.cart.fragment.OnlyExtratoppings
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Menu
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.databinding.ActivityCartBinding
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
import dk.eatmore.foodapp.model.home.ProductListItem
import java.util.*
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import dk.eatmore.foodapp.utils.CartListFunction.getjsonparmsofAddtocart
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.toolbar_plus.*
import java.io.Serializable
import kotlin.collections.ArrayList


class CartActivity : BaseActivity() {

    var transition: Transition? = null
    private val userList = ArrayList<User>()
    private var mAdapter: CartViewAdapter? = null
    private var tagadapter: TagAdapter<String>? = null
    private lateinit var productdetails: ProductDetails
    private lateinit var binding: ActivityCartBinding
    var item_p_id = ""
    var actual_price = "0"
    var actual_price_afterDiscount = "0"
    var discountType = 0
    var discount = "0"
    var minimum_order_price = "0"
    var offerDiscounted :Boolean = false
    // private var can_i_do_addtocart : Boolean = false

    companion object {
        val TAG = "CartActivity"
        var ui_model: UIModel? = null
        fun newInstance(): CartActivity {
            return CartActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        loge(TAG, "on create...")
        super.onCreate(savedInstanceState)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        binding.executePendingBindings()
        initView(savedInstanceState)

    }


    private fun fetch_ProductDetailList() {
        progress_bar.visibility = View.VISIBLE
        callAPI(ApiCall.getProductDetails(
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN, "")!!,
                r_key = PreferenceUtil.getString(PreferenceUtil.R_KEY, "")!!,
                p_id = item_p_id
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                progress_bar.visibility = View.GONE
                productdetails = body as ProductDetails
                if (productdetails.status) {
                    // if you get only extratoppings then condition will true anotherwise false:
                    if (productdetails.data.is_attributes.equals("0")) {
                        addtocart_view.alpha =1.0f
                        val fragment = OnlyExtratoppings.newInstance(productdetails.data.extra_topping_group_deatils)
                        addFragment(R.id.cart_container, fragment, OnlyExtratoppings.TAG, false)
                        ui_model!!.product_ingredients.value = productdetails.data.product_ingredients

                    } else {
                        ui_model!!.product_ingredients.value = productdetails.data.product_ingredients
                        ui_model!!.product_attribute_list.value = productdetails.data.product_attribute_list
                    }


                    // ui_model!!.any_selection.value=true
                }
            }

            override fun onFail(error: Int) {
                progress_bar.visibility = View.GONE
                when (error) {
                    404 -> {
                        showSnackBar(clayout_crt, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout_crt, getString(R.string.internet_not_available))
                    }
                }
                //showProgressDialog()


            }
        })


    }


    private fun createViewModel(): UIModel =

            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                product_ingredients.observe(this@CartActivity, Observer<ArrayList<ProductIngredientsItem>> {
                    refreshIngredients()
                })
                product_attribute_list.observe(this@CartActivity, Observer<ArrayList<ProductAttributeListItem>> {
                    setdefault()
                    refreshAttributes()
                })
                any_selection.observe(this@CartActivity, Observer<Boolean> {

                    loge(TAG,"discount--"+discount)

                    if(discountType == 0 || discountType == 2){
                        // no discount || order discount
                         actual_price = CartListFunction.calculateValuesofAddtocart(ui_model!!.product_attribute_list, productdetails).toString()
                         addtocart_txt.text = Setproductprice(context = this@CartActivity,actual_price = actual_price, actual_price_afterDiscount = actual_price_afterDiscount ,discountType = discountType)
                        // only need actual price
                    }else{
                        // discount is present (product discount)
                        actual_price=CartListFunction.calculateValuesofAddtocart(ui_model!!.product_attribute_list, productdetails).toString()
                        actual_price_afterDiscount = (actual_price.toDouble() - ((discount.toDouble() * actual_price.toDouble()) / 100)).toString()
                        addtocart_txt.text = Setproductprice(context = this@CartActivity,actual_price = actual_price, actual_price_afterDiscount = actual_price_afterDiscount,discountType = discountType)

                    }



                })
            }


    private fun refreshIngredients() {
        addtocart_view.visibility = View.VISIBLE
        val mVals = arrayOfNulls<String>(ui_model!!.product_ingredients.value!!.size)
        if (mVals.size <= 0) binding.isIngradientsVisible = false else binding.isIngradientsVisible = true
        for (i in 0..ui_model!!.product_ingredients.value!!.size - 1) {
            mVals[i] = ui_model!!.product_ingredients.value!![i].i_name
        }

        tagadapter = object : TagAdapter<String>(mVals) {
            override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
                val tv = LayoutInflater.from(this@CartActivity).inflate(R.layout.ingredients_selct_layout,
                        flowlayout, false) as TextView
                tv.text = t
                return tv

            }

        }
        flowlayout.setAdapter(tagadapter)
        val set = HashSet<Int>()
        for (j in mVals.indices) {
            set.add(j)

        }
        (tagadapter as TagAdapter<String>).setSelectedList(set)
        flowlayout.setOnSelectListener(TagFlowLayout.OnSelectListener { selectPosSet ->

            Log.e(TAG, "selected ---" + selectPosSet + " checked list " + set.toString())

            for (i in 0..ui_model!!.product_ingredients.value!!.size - 1) {
                ui_model!!.product_ingredients.value!![i].selected_ingredient = false
            }
            val list = ArrayList(selectPosSet)
            Collections.sort(list)
            for (i in list) {
                ui_model!!.product_ingredients.value!![i].selected_ingredient = true
            }

            //    loge(TAG, "boolean " + ui_model!!.product_ingredients.value!![2].selected_ingredient)

        })


    }

    private fun can_i_do_addtocart(): Boolean {
        if (ui_model != null) {
            for (i in 0..ui_model!!.product_attribute_list.value!!.size - 1) {
                var result: Boolean = false

                for (j in 0..ui_model!!.product_attribute_list.value!![i].product_attribute_value!!.size - 1) {
                    if (ui_model!!.product_attribute_list.value!![i].product_attribute_value!!.get(j).is_copyof_itemselected == true) {
                        result = true
                    }
                }

                if (result == false) {
                    // no one selected in this size
                    return false
                }
            }
            return true
        }
        return false
    }

    private fun refreshAttributes() {


        recycler_view_cart.apply {


            mAdapter = CartViewAdapter(context!!, ui_model!!.product_attribute_list.value!!, ui_model!!.calculateAttribute.value!!, object : CartViewAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {


                    for (i in 0..ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.size - 1) {
                        ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(i).is_copyof_itemselected = false  // just for check box indication

                    }
                    ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).is_copyof_itemselected = true  // just for check box indication

                    addtocart_view.alpha =if(can_i_do_addtocart()) 1.0f else 0.5f

                    //----


                    if (ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).is_itemselected == false) {


                        for (i in 0..ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.size - 1) {
                            ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(i).is_itemselected = false
                        }
                        ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).is_itemselected = true

                        for (i in 0..ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.size - 1) {
                            for (j in 0..ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.get(i).topping_subgroup_details.size - 1) {
                                ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.get(i).topping_subgroup_details.get(j).is_et_itemselected = false
                            }
                        }
                        // if user click to open extratopping from attributest then open fragment.
                        if (ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.size > 0) {
                            val fragment = Extratoppings.newInstance(parentPosition, chilPosition, ui_model!!, ui_model!!.calculateAttribute.value!!.get(parentPosition).calculateExtratoppings)
                            toolbar.setNavigationIcon(ContextCompat.getDrawable(context, R.drawable.back))
                            // continue_btn.visibility = View.VISIBLE
                            //addtocart_txt.visibility = View.GONE
                            addtocart_view.setTag("CONTINUE")
                            addtocart_view.alpha = 1.0f
                            addFragment(R.id.cart_container, fragment, Extratoppings.TAG, false)
                        }

                    } else {
                        if (ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.size > 0) {
                            val fragment = Extratoppings.newInstance(parentPosition, chilPosition, ui_model!!, ui_model!!.calculateAttribute.value!!.get(parentPosition).calculateExtratoppings)
                            toolbar.setNavigationIcon(ContextCompat.getDrawable(context, R.drawable.back))
                            //continue_btn.visibility = View.VISIBLE
                            //addtocart_txt.visibility = View.GONE
                            addtocart_view.setTag("CONTINUE")
                            addtocart_view.alpha = 1.0f
                            addFragment(R.id.cart_container, fragment, Extratoppings.TAG, false)
                        }
                    }
                    mAdapter!!.notifyDataSetChanged()
                    ui_model!!.any_selection.value = true

                }
            })
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

    }


    private fun initView(savedInstanceState: Bundle?) {
        binding.isIngradientsVisible = false
        binding.isRestaurantClosed=DetailsFragment.is_restaurant_closed
        progress_bar.visibility = View.GONE
        addtocart_txt.visibility = View.VISIBLE
        //continue_btn.visibility = View.GONE
        addtocart_view.setTag("ADD_TO_CART")
        addtocart_view.visibility = View.GONE
        addtocart_view.alpha =0.5f

        val title = intent.extras.getString("TITLE", "")
        item_p_id = intent.extras.getString("PID", "")
        actual_price = intent.extras.getString("actual_price", "0")
        actual_price_afterDiscount = intent.extras.getString("actual_price_afterDiscount", "0")
        discountType = intent.extras.getInt("discountType", 0)
        discount = intent.extras.getString("discount", "0")
        minimum_order_price = intent.extras.getString("minimum_order_price", "0")
        offerDiscounted = intent.extras.getBoolean("offerDiscounted", false)


        txt_toolbar.text = title
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.close))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition = buildEnterTransition()
            window.enterTransition = transition
        }
        //val text = String.format(getString(R.string.add_to_cart), p_price)
        addtocart_txt.text = Setproductprice(context = this,actual_price = actual_price, actual_price_afterDiscount = actual_price_afterDiscount ,discountType = discountType)




        ui_model = createViewModel()




        if (ui_model!!.product_attribute_list.value == null) {
            fetch_ProductDetailList()

        } else {
            // orientation change if start from oncreate
            refreshIngredients()
            setdefault()
            refreshAttributes()

        }


        addtocart_view.setOnClickListener {

            if (progress_bar.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            /*   if (continue_btn.visibility == View.VISIBLE) {
                   onBackPressed()
                   return@setOnClickListener
               }*/
            if (addtocart_view.getTag().toString() == "CONTINUE") {
                onBackPressed()
                return@setOnClickListener
            }

            if (addtocart_view.alpha == 1.0f) {

                showProgressDialog()
                val postParam = JsonObject()
                postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
                postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
                postParam.addProperty(Constants.SHIPPING, if (DetailsFragment.isPickup) getString(R.string.pickup_) else getString(R.string.delivery_))
                if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
                    postParam.addProperty(Constants.IS_LOGIN, "1")
                    postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
                } else {
                    postParam.addProperty(Constants.IS_LOGIN, "0")
                }
                postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
                postParam.addProperty(Constants.P_ID, item_p_id)

                when(discountType){
                    0 ->{
                        // no discount
                        postParam.addProperty(Constants.P_PRICE,actual_price)
                        postParam.addProperty(Constants.DISCOUNT_APPLIED,  0 )
                        postParam.addProperty(Constants.PRODUCT_DISCOUNT_,"0.00")
                    }
                    1->{
                        // product discount
                        postParam.addProperty(Constants.P_PRICE, actual_price)
                        postParam.addProperty(Constants.DISCOUNT_APPLIED,if(offerDiscounted) 1 else 0 )
                        postParam.addProperty(Constants.PRODUCT_DISCOUNT_,if(offerDiscounted) discount else "0.00")  // add discount amount from API
                    }
                    2->{
                        // order discount
                        postParam.addProperty(Constants.P_PRICE, actual_price)
                        if(DetailsFragment.total_cartamt.toDouble() + actual_price.toDouble() >= minimum_order_price.toDouble())
                        postParam.addProperty(Constants.DISCOUNT_APPLIED,2)
                        else
                        postParam.addProperty(Constants.DISCOUNT_APPLIED,0)
                        val discountprice_only = ((discount.toDouble() * actual_price.toDouble())/100)
                        postParam.addProperty(Constants.PRODUCT_DISCOUNT_,discountprice_only)  // add only discount amount on actual price

                    }
                }
                postParam.addProperty(Constants.P_QUANTITY, "1")
                // pass 0,1,2 to get different INGREDIENTS/ATTRUBUTES/EXTRATOPPINGS
                postParam.add(Constants.INGREDIENTS, getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 0))
                postParam.add(Constants.ATTRUBUTES, getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 1))
                postParam.add(Constants.EXTRATOPPINGS, getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 2))
                postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
                postParam.addProperty(Constants.LANGUAGE, Constants.DA)
                callAPI(ApiCall.addtocart(
                        jsonObject = postParam
                ), object : BaseFragment.OnApiCallInteraction {

                    override fun <T> onSuccess(body: T?) {
                        showProgressDialog()
                        val jsonObject = body as JsonObject
                        if (jsonObject.get(Constants.STATUS).asBoolean) {

                            if ((jsonObject.has(Constants.IS_RESTAURANT_CLOSED) && jsonObject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean == true) &&
                                (jsonObject.has(Constants.PRE_ORDER) && jsonObject.get(Constants.PRE_ORDER).asBoolean == false)) {
                                // restaurant is closed / preorder
                                val msg = if (jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                                val intent = Intent()
                                intent.putExtra(Constants.IS_RESTAURANT_CLOSED, jsonObject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean)
                                intent.putExtra(Constants.PRE_ORDER, jsonObject.get(Constants.PRE_ORDER).asBoolean)
                                intent.putExtra(Constants.MSG, msg)
                                setResult(Activity.RESULT_OK, intent)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    finishAfterTransition()
                                else
                                    finish()
                            } else {
                                val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                                intent.putExtra(Constants.CARTCNT, if (jsonObject.get(Constants.CARTCNT).isJsonNull || jsonObject.get(Constants.CARTCNT).asString == "0") 0 else (jsonObject.get(Constants.CARTCNT).asString).toInt())
                                intent.putExtra(Constants.CARTAMT, if (jsonObject.get(Constants.CARTAMT).isJsonNull || jsonObject.get(Constants.CARTAMT).asString == "0") "00.00" else jsonObject.get(Constants.CARTAMT).asString)
                                LocalBroadcastManager.getInstance(this@CartActivity).sendBroadcast(intent)
                                Toast.makeText(this@CartActivity, getString(R.string.item_has_been), Toast.LENGTH_SHORT).show()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    finishAfterTransition()
                                else
                                    finish()
                            }


                            //  showSnackBar(clayout_crt, jsonObject.get("msg").asString)
                        } else {
                            showSnackBar(clayout_crt, getString(R.string.error_404))
                        }
                    }

                    override fun onFail(error: Int) {
                        showProgressDialog()
                        when (error) {
                            404 -> {
                                showSnackBar(clayout_crt, getString(R.string.error_404))
                            }
                            100 -> {

                                showSnackBar(clayout_crt, getString(R.string.internet_not_available))
                            }
                        }
                        //showProgressDialog()


                    }
                })


            }

            //  fillData()

        }

    }


    fun any_preorder_closedRestaurant(is_restaurant_closed: Boolean?, pre_order: Boolean?, msg: String?): Boolean {

        if ((is_restaurant_closed != null && is_restaurant_closed == true) &&
                (pre_order != null && pre_order == false)) {
            // Test if restaurant is closed.
            val status = msg ?: "Sorry Restaurant has been closed."
            DialogUtils.openDialogDefault(context = this, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(this, R.color.black), msg = status, title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                override fun onPositiveButtonClick(position: Int) {

                }

                override fun onNegativeButtonClick() {
                }
            })
            return true
        } else {
            return false
        }

    }


    private fun setdefault() {
        for (i in 0..ui_model!!.product_attribute_list.value!!.size - 1) {

            val defaultvalue = ui_model!!.product_attribute_list.value!!.get(i).default_attribute_value.pad_id

            for (j in 0..ui_model!!.product_attribute_list.value!![i].product_attribute_value!!.size - 1) {
                if (ui_model!!.product_attribute_list.value!![i].product_attribute_value!![j].pad_id.equals(defaultvalue))
                    ui_model!!.product_attribute_list.value!![i].product_attribute_value!![j].is_itemselected = true
                else
                    ui_model!!.product_attribute_list.value!![i].product_attribute_value!![j].is_itemselected = false

            }

        }
        val list: ArrayList<CalculateAttribute> = ArrayList()
        for (i in 0..ui_model!!.product_attribute_list.value!!.size - 1) {
            list.add(CalculateAttribute())
        }
        ui_model!!.calculateAttribute.value = list
        loge(TAG, "size is " + ui_model!!.calculateAttribute.value!!.size)


    }

    fun Setproductprice(context : Context, actual_price : String , actual_price_afterDiscount : String , discountType : Int ) : SpannableStringBuilder {

        if(discountType == 0 || discountType == 2){
            // no discount || order discount
            val builder = SpannableStringBuilder()
            val span1 = SpannableString("ADD TO CART ")
            val span2 = SpannableString(BindDataUtils.convertCurrencyToDanish(actual_price))
            builder.append(span1).append(span2)
            return builder

        }else{
            // discount is present (product discount)
            //String.format("%2f", model.actual_price!!.toDouble()) // round decimal
            val priceAfterDiscount = BindDataUtils.convertCurrencyToDanish(actual_price_afterDiscount)!! // add kr and change into danish
            val priceBeforeDiscount = BindDataUtils.convertCurrencyToDanishWithoutLabel(actual_price)!!
            val builder = SpannableStringBuilder()
            val strikethroughSpan = StrikethroughSpan()
            val span1 = SpannableString("ADD TO CART ")
            val span2 = SpannableString(priceBeforeDiscount)
            val span3 = SpannableString(" " + priceAfterDiscount)
            span2.setSpan(strikethroughSpan, 0, priceBeforeDiscount.trim().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            span2.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, priceBeforeDiscount.trim().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(span1).append(span2).append(span3)
            return builder
        }

    }




    override fun onBackPressed() {


        if (supportFragmentManager.backStackEntryCount > 0) {
            var fragment = supportFragmentManager.findFragmentByTag(supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name)
            if (fragment != null) {
                when (fragment) {

                    is OnlyExtratoppings -> {
                        finishThisActivity()
                    }
                    is Extratoppings -> {
                        //continue_btn.visibility = View.GONE
                        //addtocart_txt.visibility = View.VISIBLE
                        addtocart_view.setTag("ADD_TO_CART")
                        addtocart_view.alpha =if(can_i_do_addtocart()) 1.0f else 0.5f
                        supportFragmentManager.popBackStack()
                    }

                }
                toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.close))
            }
        } else {
            finishThisActivity()
        }


    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildEnterTransition(): Transition {
        val enterTransition = Slide()
        enterTransition.setDuration(300)
        enterTransition.slideEdge = Gravity.BOTTOM
        return enterTransition
    }


    private fun fillData() {
        val user1 = User()
        user1.name = "Small"
        userList.add(user1)

        val user2 = User()
        user2.name = "midium"
        userList.add(user2)

        val user3 = User()
        user3.name = "large"
        userList.add(user3)

        val user4 = User()
        user4.name = "extra large"
        userList.add(user4)


    }


    class UIModel : ViewModel() {

        var product_ingredients = MutableLiveData<ArrayList<ProductIngredientsItem>>()
        var product_attribute_list = MutableLiveData<ArrayList<ProductAttributeListItem>>()
        var calculateAttribute = MutableLiveData<ArrayList<CalculateAttribute>>()
        var any_selection = MutableLiveData<Boolean>()


    }


}

data class CalculateAttribute(
        var p_id: String = "",
        var pad_id: String = "",
        var a_price: String = "",
        var calculateExtratoppings: ArrayList<CalculateExtratoppings> = arrayListOf()
) : Serializable

data class CalculateExtratoppings(
// val p_id : String="",
// val pad_id : String="",
        var tsgd_id: String = ""
) : Serializable
