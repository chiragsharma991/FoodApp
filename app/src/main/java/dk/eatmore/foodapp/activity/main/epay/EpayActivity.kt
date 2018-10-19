package dk.eatmore.foodapp.activity.main.epay

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.transition.*
import android.view.LayoutInflater
import android.view.View
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.fragment.AddCart
import dk.eatmore.foodapp.activity.main.epay.fragment.DeliveryTimeslot
import dk.eatmore.foodapp.activity.main.epay.fragment.Paymentmethod
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.AddressForm
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.activity_epay.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.toolbar.*

class EpayActivity : BaseActivity() {

    var transition : Transition?=null
    private lateinit var addcart_fragment: AddCart


    companion object {
        val TAG="EpayActivity"
        var amIFinish :Boolean=true
        var accessOnetime :Boolean=true
        var moveonEpay:Boolean=false
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
        accessOnetime=true
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        menu_tabs.addTab(menu_tabs.newTab().setText("Delivery"))
        menu_tabs.addTab(menu_tabs.newTab().setText("PickUp"))
        menu_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
                logd(TAG, menu_tabs.selectedTabPosition.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })
        setToolbarforThis()
        epay_continue_btn.text=if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)) getString(R.string.continue_) else getString(R.string.login_to_continue)
        epay_continue_btn.setOnClickListener{
            if(epay_continue_btn.text == getString(R.string.continue_)){
                val fragment = Address.newInstance()
                addFragment(R.id.epay_container,fragment, Address.TAG,true)
            }else{
                moveonEpay=true
                setResult(RESULT_OK);
                onBackPressed()
            }
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
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN,""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY,""))
        if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID,PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))
        }else{
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }
        postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))

        callAPI(ApiCall.viewcart(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val viewcardmodel = body as ViewcardModel
                if (viewcardmodel.status) {
                    ui_model!!.viewcard_list.value=viewcardmodel

                } else {
                    ui_model!!.viewcard_list.value=null
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

    private fun deleteitemFromcart(op_id: String) {

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
        postParam.addProperty(Constants.OP_ID, op_id)

        callAPI(ApiCall.deleteitemFromcart(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject
                if(json.get("status").asBoolean){
                    fetch_viewCardList()
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
       // epay_total_txt.text=BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
       // epay_total_lbl.text=String.format(getString(R.string.total_goods),ui_model!!.viewcard_list.value!!.cartcnt)
        if(ui_model!!.viewcard_list.value ==null){

            add_parentitem_view.removeAllViewsInLayout()
            add_parentitem_view.invalidate()
            return
        }

        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until ui_model!!.viewcard_list.value!!.result!!.size){
            var inflater= this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view= inflater.inflate(R.layout.dynamic_raw_item,null)
            view.remove_item.tag=i
            view.remove_item.setOnClickListener { v->
                val position  = v.tag as Int
                if(accessOnetime){
                    DialogUtils.openDialog(this, getString(R.string.are_you_sure_to_delete), "",
                            getString(R.string.yes), getString(R.string.no), ContextCompat.getColor(this, R.color.black), object : DialogUtils.OnDialogClickListener {
                        override fun onPositiveButtonClick(position: Int) {
                            deleteitemFromcart(ui_model!!.viewcard_list.value!!.result!![position].op_id)
                            accessOnetime=false
                        }

                        override fun onNegativeButtonClick() {
                        }
                    })
                }else{
                    deleteitemFromcart(ui_model!!.viewcard_list.value!!.result!![position].op_id)
                }
            }
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
            if(ui_model!!.viewcard_list.value!!.result!![i].is_attributes !=null && ui_model!!.viewcard_list.value!!.result!![i].is_attributes.equals("1")){
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
        if(supportFragmentManager.backStackEntryCount > 0){
            val fragment =supportFragmentManager.findFragmentByTag(supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount-1).name)
            if(fragment !=null && fragment.isVisible){
                  when (fragment) {

                        is Paymentmethod -> {
                          when(fragment.currentView){
                              Constants.PAYMENTMETHOD -> {fragment.onBackpress() }
                              Constants.PROGRESSDIALOG ->{ }
                              Constants.PAYMENTSTATUS -> {finishActivity()}
                          }
                        }
                        is DeliveryTimeslot -> fragment.onBackpress()

                        is Address ->{
                            // test this condition if fragment have child fragment then:
                            if(fragment.childFragmentManager.backStackEntryCount > 0){

                                val anychild_fragment =fragment.childFragmentManager.findFragmentByTag(fragment.childFragmentManager.getBackStackEntryAt(fragment.childFragmentManager.backStackEntryCount-1).name)

                                if(anychild_fragment is AddressForm) anychild_fragment.onBackpress()

                            }else{
                                fragment.onBackpress()
                            }

                        }
                        else -> finishActivity()

                  }
            }
        }
        else {
            finishActivity()
        }
    }


    fun finishActivity(){
       // DrawableCompat.setTint(ContextCompat.getDrawable(this,R.drawable.close)!!, ContextCompat.getColor(this, R.color.white));
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
