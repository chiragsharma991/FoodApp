package dk.eatmore.foodapp.activity.main.cart

import android.app.Activity
import android.app.Instrumentation
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
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
import dk.eatmore.foodapp.adapter.cart.CartViewAdapter
import dk.eatmore.foodapp.databinding.ActivityCartBinding
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.cart.Data
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
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
    private  lateinit var binding :ActivityCartBinding


    companion object {
        val TAG = "CartActivity"
        var item_p_id = ""
        var p_price = ""
        var ui_model: UIModel? = null
        fun newInstance(): CartActivity {
            return CartActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        loge(TAG, "on create...")
        super.onCreate(savedInstanceState)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_cart)
        initView(savedInstanceState)
    }


    private fun fetch_ProductDetailList() {
        progress_bar.visibility=View.VISIBLE
        callAPI(ApiCall.getProductDetails(
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN,"")!!,
                r_key = PreferenceUtil.getString(PreferenceUtil.R_KEY,"")!!,
                p_id = item_p_id
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                progress_bar.visibility=View.GONE
                productdetails = body as ProductDetails
                if (productdetails.status) {
                    // if you get only extratoppings then condition will true anotherwise false:
                    if (productdetails.data.is_attributes.equals("0")) {
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
                progress_bar.visibility=View.GONE
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

                    val text = String.format(getString(R.string.add_to_cart), BindDataUtils.convertCurrencyToDanish(CartListFunction.calculateValuesofAddtocart(ui_model!!.product_attribute_list, productdetails).toString()))
                    addtocart_txt.text = text
                })
            }


    private fun refreshIngredients() {
        val mVals = arrayOfNulls<String>(ui_model!!.product_ingredients.value!!.size)
        if(mVals.size <= 0) binding.isIngradientsVisible=false else binding.isIngradientsVisible=true
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

    private fun refreshAttributes() {


        recycler_view_cart.apply {

            mAdapter = CartViewAdapter(context!!, ui_model!!.product_attribute_list.value!!, ui_model!!.calculateAttribute.value!!, object : CartViewAdapter.AdapterListener {
                override fun itemClicked(parentView: Boolean, parentPosition: Int, chilPosition: Int) {


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

                        if (ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.size > 0) {
                            val fragment = Extratoppings.newInstance(parentPosition, chilPosition, ui_model!!, ui_model!!.calculateAttribute.value!!.get(parentPosition).calculateExtratoppings)
                            toolbar.setNavigationIcon(ContextCompat.getDrawable(context, R.drawable.back))
                            addFragment(R.id.cart_container, fragment, Extratoppings.TAG, false)
                        }
                    } else {
                        if (ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.size > 0) {
                            val fragment = Extratoppings.newInstance(parentPosition, chilPosition, ui_model!!, ui_model!!.calculateAttribute.value!!.get(parentPosition).calculateExtratoppings)
                            toolbar.setNavigationIcon(ContextCompat.getDrawable(context, R.drawable.back))
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
        binding.isIngradientsVisible=false
        progress_bar.visibility=View.GONE
        val title = intent.extras.getString("TITLE", "")
        item_p_id = intent.extras.getString("PID", "")
        p_price = intent.extras.getString("p_price", "")
        txt_toolbar.text = title
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.close))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition = buildEnterTransition()
            window.enterTransition = transition
        }
        val text = String.format(getString(R.string.add_to_cart), p_price)
        addtocart_txt.text = text
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
            if(progress_bar.visibility == View.VISIBLE){
                return@setOnClickListener
            }
            showProgressDialog()
            val postParam = JsonObject()
            postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN,""))
            postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY,""))
            if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
                postParam.addProperty(Constants.IS_LOGIN, "1")
                postParam.addProperty(Constants.CUSTOMER_ID,PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))
            }else{
                postParam.addProperty(Constants.IS_LOGIN, "0")
            }
            postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))
            postParam.addProperty(Constants.P_ID, item_p_id)
            postParam.addProperty(Constants.P_PRICE, CartListFunction.calculateValuesofAddtocart(ui_model!!.product_attribute_list, productdetails).toString())
            postParam.addProperty(Constants.P_QUANTITY, "1")
            // pass 0,1,2 to get different INGREDIENTS/ATTRUBUTES/EXTRATOPPINGS
            postParam.add(Constants.INGREDIENTS, getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 0))
            postParam.add(Constants.ATTRUBUTES, getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 1))
            postParam.add(Constants.EXTRATOPPINGS, getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 2))
            postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
            postParam.addProperty(Constants.LANGUAGE, Constants.EN)
            callAPI(ApiCall.addtocart(
                    jsonObject = postParam
            ), object : BaseFragment.OnApiCallInteraction {

                override fun <T> onSuccess(body: T?) {
                    showProgressDialog()
                    val jsonObject = body as JsonObject
                    if (jsonObject.get(Constants.STATUS).asBoolean) {


                        /*       2018-12-05 18:16:16.495 25542-26139/dk.eatmore.foodapp D/OkHttp:     "status": true,
                               2018-12-05 18:16:16.496 25542-26139/dk.eatmore.foodapp D/OkHttp:     "is_user_deleted": false,
                               2018-12-05 18:16:16.496 25542-26139/dk.eatmore.foodapp D/OkHttp:     "is_restaurant_closed": false,
                               2018-12-05 18:16:16.496 25542-26139/dk.eatmore.foodapp D/OkHttp:     "order_total": 65,
                               2018-12-05 18:16:16.496 25542-26139/dk.eatmore.foodapp D/OkHttp:     "msg": "all records.",
                               2018-12-05 18:16:16.496 25542-26139/dk.eatmore.foodapp D/OkHttp:     "cartcnt": "1",
                               2018-12-05 18:31:59.001 26334-26386/dk.eatmore.foodapp D/OkHttp:     "pre_order": true
                               2018-12-05 18:16:16.496 25542-26139/dk.eatmore.foodapp D/OkHttp:     "cartamt": "65.00"*/
                        if((jsonObject.has(Constants.IS_RESTAURANT_CLOSED) && jsonObject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean == true) &&
                           (jsonObject.has(Constants.PRE_ORDER) && jsonObject.get(Constants.PRE_ORDER).asBoolean == false) ){
                            // restaurant is closed / preorder
                            val msg= if(jsonObject.has(Constants.MSG))jsonObject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                            val intent= Intent()
                            intent.putExtra(Constants.IS_RESTAURANT_CLOSED,jsonObject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean)
                            intent.putExtra(Constants.PRE_ORDER,jsonObject.get(Constants.PRE_ORDER).asBoolean)
                            intent.putExtra(Constants.MSG,msg)
                            setResult(Activity.RESULT_OK,intent)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                finishAfterTransition()
                            else
                                finish()
                        }else{
                            val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                            intent.putExtra(Constants.CARTCNT,if(jsonObject.get(Constants.CARTCNT).isJsonNull || jsonObject.get(Constants.CARTCNT).asString == "0") 0 else (jsonObject.get(Constants.CARTCNT).asString).toInt())
                            intent.putExtra(Constants.CARTAMT,if(jsonObject.get(Constants.CARTAMT).isJsonNull || jsonObject.get(Constants.CARTAMT).asString =="0") "00.00" else jsonObject.get(Constants.CARTAMT).asString)
                            LocalBroadcastManager.getInstance(this@CartActivity).sendBroadcast(intent)
                            Toast.makeText(this@CartActivity,getString(R.string.item_has_been), Toast.LENGTH_SHORT).show()
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


    fun any_preorder_closedRestaurant(is_restaurant_closed : Boolean?, pre_order : Boolean?,msg : String?) : Boolean{

        if((is_restaurant_closed !=null && is_restaurant_closed == true) &&
                (pre_order !=null && pre_order == false) ){
            // Test if restaurant is closed.
            val status= msg?:"Sorry Restaurant has been closed."
            DialogUtils.openDialogDefault(context = this,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(this, R.color.black),msg = status,title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                override fun onPositiveButtonClick(position: Int) {

                }
                override fun onNegativeButtonClick() {
                }
            })
            return true
        }else{
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


    override fun onBackPressed() {


        if (supportFragmentManager.backStackEntryCount > 0) {
            var fragment = supportFragmentManager.findFragmentByTag(supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name)
            if (fragment != null) {
                when (fragment) {

                    is OnlyExtratoppings -> {
                        finishThisActivity()
                    }
                    is Extratoppings -> {
                        supportFragmentManager.popBackStack()
                    }

                }
                toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.close))
            }
        }else{
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
