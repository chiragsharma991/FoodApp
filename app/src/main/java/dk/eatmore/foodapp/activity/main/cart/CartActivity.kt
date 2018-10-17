package dk.eatmore.foodapp.activity.main.cart

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.transition.Slide
import android.transition.Transition
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
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
        setContentView(R.layout.activity_cart)
        initView(savedInstanceState)
    }


    private fun fetch_ProductDetailList() {

        callAPI(ApiCall.getProductDetails(
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN,"")!!,
                r_key = PreferenceUtil.getString(PreferenceUtil.R_KEY,"")!!,
                p_id = item_p_id
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
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

            loge(TAG, "boolean " + ui_model!!.product_ingredients.value!![2].selected_ingredient)

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
                            addFragment(R.id.cart_container, fragment, Extratoppings.TAG, false)
                        }
                    } else {
                        if (ui_model!!.product_attribute_list.value!![parentPosition].product_attribute_value!!.get(chilPosition).extra_topping_group_deatils.topping_subgroup_list.size > 0) {
                            val fragment = Extratoppings.newInstance(parentPosition, chilPosition, ui_model!!, ui_model!!.calculateAttribute.value!!.get(parentPosition).calculateExtratoppings)
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
        val title = intent.extras.getString("TITLE", "")
        item_p_id = intent.extras.getString("PID", "")
        p_price = intent.extras.getString("p_price", "")
        txt_toolbar.text = title
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.close))
        toolbar.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                finishAfterTransition()
            else
                finish()
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
            val postParam = JsonObject()
            postParam.addProperty("r_token", Constants.R_TOKEN)
            postParam.addProperty("r_key", Constants.R_KEY)
            postParam.addProperty("is_login", "1")
            postParam.addProperty("p_id", item_p_id)
            postParam.addProperty("p_price", CartListFunction.calculateValuesofAddtocart(ui_model!!.product_attribute_list, productdetails).toString())
            postParam.addProperty("p_quantity", "1")
            postParam.addProperty("ingredients", getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 0).toString())
            postParam.addProperty("attrubutes", getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 1).toString())
            postParam.addProperty("extratoppings", getjsonparmsofAddtocart(item_p_id, ui_model!!.product_ingredients, ui_model!!.product_attribute_list, productdetails, 2).toString())

            callAPI(ApiCall.addtocart(
                    jsonObject = postParam
            ), object : BaseFragment.OnApiCallInteraction {

                override fun <T> onSuccess(body: T?) {
                    val jsonObject = body as JsonObject
                    if (jsonObject.get("status").asBoolean) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            finishAfterTransition()
                        else
                            finish()
                        //  showSnackBar(clayout_crt, jsonObject.get("msg").asString)
                    } else {
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
                    //showProgressDialog()


                }
            })


        }


        //  fillData()


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
