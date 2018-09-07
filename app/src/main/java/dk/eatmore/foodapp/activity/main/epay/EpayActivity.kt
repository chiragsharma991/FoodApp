package dk.eatmore.foodapp.activity.main.epay

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.transition.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CalculateAttribute
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.cart.fragment.OnlyExtratoppings
import dk.eatmore.foodapp.activity.main.epay.fragment.AddCart
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.model.cart.ProductAttributeListItem
import dk.eatmore.foodapp.model.cart.ProductDetails
import dk.eatmore.foodapp.model.cart.ProductIngredientsItem
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_epay.*
import kotlinx.android.synthetic.main.dynamic_raw_item.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.toolbar.*

class EpayActivity : BaseActivity() {

    var transition : Transition?=null
    private lateinit var addcart_fragment: AddCart


    companion object {
        val TAG="EpayActivity"
        var amIFinish :Boolean=true
        var ui_model: UIModel? = null
        fun newInstance() : EpayActivity {
            return EpayActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epay)
        amIFinish=true
        initView(savedInstanceState)

    }


    private fun initView(savedInstanceState: Bundle?) {
        loge(TAG,"count is"+supportFragmentManager.backStackEntryCount)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setToolbarforThis()
        epay_continue_btn.setOnClickListener{
            val fragment = Address.newInstance()
            addFragment(R.id.epay_container,fragment, Address.TAG,true)
        }
        ui_model = createViewModel()
        if (ui_model!!.viewcard_list.value == null) {
            fetch_viewCardList()
        } else {

        }


    }

    fun setToolbarforThis(){
        img_toolbar_back.setImageResource(R.drawable.close)
        txt_toolbar.text="Basket"
        img_toolbar_back.setOnClickListener{
            finishActivity()
        }
    }

    private fun fetch_viewCardList() {

        //{"customer_id":"1766","r_token":"w5oRqFiAXTBB3hwpixAORbg_BwUj0EMQ07042017114812","is_login":"1","r_key":"fcARlrbZFXYee1W6eYEIA0VRlw7MgV4o07042017114812"}
        val postParam = JsonObject()
        postParam.addProperty("r_token", Constants.R_TOKEN)
        postParam.addProperty("r_key", Constants.R_KEY)
        postParam.addProperty("is_login", "1")
        postParam.addProperty("customer_id","1766")

        callAPI(ApiCall.viewcart(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val viewcardmodel = body as ViewcardModel
                if (viewcardmodel.status) {
                    ui_model!!.viewcard_list.value=viewcardmodel

                } else {
                    showSnackBar(epay_container,"Sorry No data found.")
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(epay_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(epay_container, getString(R.string.internet_not_available))
                    }
                }
                //showProgressDialog()


            }
        })

    }


    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                viewcard_list.observe(this@EpayActivity, Observer<ViewcardModel> {
                    refresh_viewCard()
                })
            }


    class UIModel : ViewModel() {

        var viewcard_list = MutableLiveData<ViewcardModel>()

    }

    private fun refresh_viewCard(){
        epay_total_txt.text=BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
        epay_total_lbl.text=String.format(getString(R.string.total_goods),ui_model!!.viewcard_list.value!!.cartcnt)
        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until ui_model!!.viewcard_list.value!!.result!!.size){
            var inflater= this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view= inflater.inflate(R.layout.dynamic_raw_item,null)
            view.item_name.text=ui_model!!.viewcard_list.value!!.result!![i].product_name
            view.item_price.text=BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.result!![i].p_price) ?: "null"
            view.add_subitem_view.removeAllViewsInLayout()

                // fill first ingredients size if not null
                for (j in 0 until (ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients?.size ?: 0)){
                    inflater= this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val ingredientview= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                    ingredientview.subitem_name.text=String.format(getString(R.string.minues),ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients!!.get(j).ingredient_name)
                    ingredientview.subitem_name.setTextColor(ContextCompat.getColor(this,R.color.red))
                    ingredientview.subitem_price.visibility=View.INVISIBLE
                    view.add_subitem_view.addView(ingredientview)
                }

            // if attribute is present then fetch extratoppings only from attribute list
            if(ui_model!!.viewcard_list.value!!.result!![i].is_attributes.equals("1")){
                if(ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes !=null){
                    for (k in 0 until ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.size){
                        for (l in 0 until (ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!![k].order_product_extra_topping_group?.size ?: 0)){
                            inflater= this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                            extratoppings.subitem_name.text=String.format(getString(R.string.plus),ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                            extratoppings.subitem_price.text=BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
                            view.add_subitem_view.addView(extratoppings)
                        }
                    }
                }
            }
            else
            {
                // if extratopping group only present then add only extratoppings in the list.
                for (k in 0 until (ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group?.size ?:0)){
                        inflater= this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val onlyextratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                        onlyextratoppings.subitem_name.text=String.format(getString(R.string.plus),ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).ingredient_name)
                        // view.subitem_price.visibility=View.VISIBLE
                        onlyextratoppings.subitem_price.text=BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                        view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
            subtotal.text=BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
            total.text=BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
        }
    }


    override fun onBackPressed() {
    /*    if(amIFinish){
            if(!popFragment()) {
                finishActivity()
            }
        }*/
    }




    fun finishActivity(){
        DrawableCompat.setTint(ContextCompat.getDrawable(this,R.drawable.close)!!, ContextCompat.getColor(this, R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            finishAfterTransition()
        else
            finish()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildEnterTransition(): Transition {
        val enterTransition = Explode()
        enterTransition.setDuration(300)
        return enterTransition
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildReturnTransition(): Transition {
        val enterTransition = Fade()
        enterTransition.setDuration(300)
        return enterTransition
    }




}
