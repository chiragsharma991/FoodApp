package dk.eatmore.foodapp.activity.main.epay


import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.transition.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.fragment.*
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.databinding.ActivityEpayBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.ArrayList
import dk.eatmore.foodapp.model.home.Restaurant
import kotlinx.android.synthetic.main.activity_epay.*
import kotlinx.android.synthetic.main.fragment_home_container.*
import retrofit2.Call


class EpayFragment : BaseFragment() {

    var transition : Transition?=null
    private lateinit var addcart_fragment: AddCart
    private lateinit var binding: ActivityEpayBinding
    private lateinit var restaurant : Restaurant
    private var call_viewcartlist  : Call<ViewcardModel>? =null
    private var call_deleteitem  : Call<JsonObject>? =null





    /**TODO : Payment flow
     *  all information regarding payment has been added in this compainon object and PaymentAttributes data class.
     *
     *  Make sure if you add other parms and variable please be add in dataclass or may be in compainion so you can accesss all this into payment activity.
     *
     *
     *
     */



    companion object {

        // Make sure if you are assigning variable in this section then keep mind to assign null on destroy or set default values always time.
        val TAG="EpayFragment"
        lateinit var paymentattributes: PaymentAttributes
        lateinit var selected_op_id: ArrayList<String>
        var amIFinish :Boolean=true
        var accessOnetime :Boolean=true
        var moveonEpay:Boolean=false
        var ui_model: UIModel? = null
        var isPickup:Boolean = false  // just for check pickup/delivery selection on tabview.
        var first_time : String =""


        fun newInstance(restaurant: Restaurant): EpayFragment {
            val fragment = EpayFragment()
            val bundle =Bundle()
            bundle.putSerializable(Constants.RESTAURANT,restaurant)
            fragment.arguments=bundle
            return fragment
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_epay
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        amIFinish=true
        accessOnetime=true
        restaurant=arguments!!.getSerializable(Constants.RESTAURANT) as Restaurant
        progress_bar.visibility=View.VISIBLE
        empty_view.visibility= View.GONE
        view_container.visibility= View.GONE
        tabfunction()
        setToolbarforThis()
        epay_continue_btn.text=if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)) getString(R.string.continue_) else getString(R.string.login_to_continue)
        epay_continue_btn.setOnClickListener{
            continuefromviewcart()
        }
        ui_model = createViewModel()
        if (ui_model!!.viewcard_list.value == null) {
            fetch_viewCardList()
        } else {

        }
    }

    fun setToolbarforThis(){
        img_toolbar_back.setImageResource(R.drawable.close)
        txt_toolbar.text=getString(R.string.basket)
        img_toolbar_back.setOnClickListener{
            loge(TAG,"eapay finishing...")
            //   onBackPressed()
            (activity as HomeActivity).onBackPressed()
        }
    }

    fun continuefromviewcart(){

        if(epay_continue_btn.text == getString(R.string.continue_)){
            //collect op_id to proceed in payment.
            selected_op_id= ArrayList()
            for (i in 0.until(ui_model!!.viewcard_list.value!!.result!!.size)){
                selected_op_id.add(ui_model!!.viewcard_list.value!!.result!!.get(i).op_id)
            }
            val fragment = Address.newInstance(restaurant)
            addFragment(R.id.epay_container,fragment,Address.TAG,true)

        }else{
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).view_pager.setCurrentItem(2)
            moveonEpay=true
            loge(TAG,"move on eapy--"+EpayFragment.moveonEpay)


            //setResult(RESULT_OK);
            // onBackPressed()
        }
    }

    fun fetch_viewCardList() {

        //{"customer_id":"1766","r_token":"w5oRqFiAXTBB3hwpixAORbg_BwUj0EMQ07042017114812","is_login":"1","r_key":"fcARlrbZFXYee1W6eYEIA0VRlw7MgV4o07042017114812"}
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN,""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY,""))
        if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))
        }else{
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }
        postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        call_viewcartlist=ApiCall.viewcart(jsonObject = postParam)
        callAPI(call_viewcartlist!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val viewcardmodel = body as ViewcardModel
                if (viewcardmodel.status) {

                    if(!any_preorder_closedRestaurant(viewcardmodel.is_restaurant_closed,viewcardmodel.pre_order,viewcardmodel.msg)){
                        // restaurant is not closed then:
                        ui_model!!.viewcard_list.value=viewcardmodel
                        view_container.visibility= View.VISIBLE
                    }
                    progress_bar.visibility=View.GONE


                } else {

                    if(!any_preorder_closedRestaurant(viewcardmodel.is_restaurant_closed,viewcardmodel.pre_order,viewcardmodel.msg)){
                        // restaurant is not closed then:
                        ui_model!!.viewcard_list.value=null
                        empty_view.visibility= View.VISIBLE
                        view_container.visibility= View.GONE
                    }
                    progress_bar.visibility=View.GONE

                }
            }

            override fun onFail(error: Int) {

                if( call_viewcartlist!!.isCanceled){
                    return
                }
                when (error) {
                    404 -> {
                        showSnackBar(epay_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(epay_container, getString(R.string.internet_not_available))
                    }
                }
                progress_bar.visibility=View.GONE
            }
        })

    }

    private fun deleteitemFromcart(op_id: String) {

        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN,""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY,""))
        if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)){
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))
        }else{
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }
        postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN,""))
        postParam.addProperty(Constants.OP_ID, op_id)
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        call_deleteitem=ApiCall.deleteitemFromcart(jsonObject = postParam)
        callAPI(call_deleteitem!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val json = body as JsonObject
                progress_bar.visibility=View.GONE
                val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                intent.putExtra(Constants.CARTCNT,if(json.get(Constants.CARTCNT).isJsonNull() || json.get(Constants.CARTCNT).toString() == "0") 0 else (json.get(Constants.CARTCNT).asString).toInt())
                intent.putExtra(Constants.CARTAMT,if(json.get(Constants.CARTAMT).isJsonNull() || json.get(Constants.CARTAMT).toString() == "0") "00.00" else json.get(Constants.CARTAMT).asString)
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
                fetch_viewCardList()
            }

            override fun onFail(error: Int) {

                if(call_deleteitem!!.isCanceled){
                    return
                }

                when (error) {
                    404 -> {
                        showSnackBar(epay_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(epay_container, getString(R.string.internet_not_available))
                    }
                }
                //showProgressDialog()
                progress_bar.visibility=View.GONE

            }
        })

    }

    fun backpress(): Boolean {
        if(childFragmentManager.backStackEntryCount > 0){
            val fragment =childFragmentManager.findFragmentByTag(childFragmentManager.getBackStackEntryAt(childFragmentManager.backStackEntryCount-1).name)
            if(fragment !=null && fragment.isVisible){
                when (fragment) {

                    is BamboraWebfunction -> {
                        fragment.onBackpress()
                    }
                    is TransactionStatus -> {
                        fragment.onBackpress()
                    }
                    else ->{
                        childFragmentManager.popBackStack()
                    }
                }
            }

            return true
        }
        return false
    }


    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {

                viewcard_list.removeObservers(this@EpayFragment)
                viewcard_list.observe(this@EpayFragment, Observer<ViewcardModel> {
                    refresh_viewCard()
                })
            }


    class UIModel : ViewModel() {

        var viewcard_list = MutableLiveData<ViewcardModel>()

    }


    private fun refresh_viewCard(){
        if(ui_model!!.viewcard_list.value ==null){
            // this condition will null if all item has been deleted : so just clear view and inflate empty view on screen.
            add_parentitem_view.removeAllViewsInLayout()
            add_parentitem_view.invalidate()
            return
        }
        paymentattributes=PaymentAttributes()
        paymentattributes.order_total=ui_model!!.viewcard_list.value!!.order_total.toString()
        for (i in 0.until(restaurant.restpaymentmethods.size)){
            if(restaurant.restpaymentmethods[i].pm_id =="1"){
             // online
             paymentattributes.online_logo=restaurant.restpaymentmethods[i].logo
            }else{
             //cash
             paymentattributes.cash_logo=restaurant.restpaymentmethods[i].logo
            }
        }
        paymentattributes.restaurant_name=restaurant.restaurant_name
        paymentattributes.restaurant_address=restaurant.address
        paymentattributes.restaurant_phone=restaurant.phone
        paymentattributes.restaurant_appicon=restaurant.app_icon
        paymentattributes.is_fav=restaurant.is_fav
        paymentattributes.restaurant_id=restaurant.restaurant_id

        epay_total_txt.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
        epay_total_lbl.text=String.format(getString(R.string.total_goods),ui_model!!.viewcard_list.value!!.cartcnt)
        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until ui_model!!.viewcard_list.value!!.result!!.size){
            var inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view= inflater.inflate(R.layout.dynamic_raw_item,null)
            view.remove_item.tag=i
            view.remove_item.setOnClickListener { v->
                val position  = v.tag as Int
                if(accessOnetime){
                    DialogUtils.openDialog(context!!, getString(R.string.are_you_sure_to_delete), "",
                            getString(R.string.yes), getString(R.string.no), ContextCompat.getColor(context!!, R.color.theme_color), object : DialogUtils.OnDialogClickListener {
                        override fun onPositiveButtonClick(p: Int) {
                            progress_bar.visibility=View.VISIBLE
                            deleteitemFromcart(ui_model!!.viewcard_list.value!!.result!![position].op_id)
                            accessOnetime=true  // if you want to allowed only one time then pass: false
                        }

                        override fun onNegativeButtonClick() {
                        }
                    })
                }else{
                    progress_bar.visibility=View.VISIBLE
                    deleteitemFromcart(ui_model!!.viewcard_list.value!!.result!![position].op_id)
                }
            }
            view.item_name.text=ui_model!!.viewcard_list.value!!.result!![i].product_name
            view.item_price.text=if(ui_model!!.viewcard_list.value!!.result!![i].p_price !=null) BindDataUtils.convertCurrencyToDanishWithoutLabel(ui_model!!.viewcard_list.value!!.result!![i].p_price!!) else "null"
            view.add_subitem_view.removeAllViewsInLayout()

            // fill first ingredients size if not null
            for (j in 0 until (ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients?.size ?: 0)){
                inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val ingredientview= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                ingredientview.subitem_name.text=String.format(getString(R.string.minues),ui_model!!.viewcard_list.value!!.result!![i].removed_ingredients!!.get(j).ingredient_name)
                ingredientview.subitem_name.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                ingredientview.subitem_price.visibility= View.INVISIBLE
                view.add_subitem_view.addView(ingredientview)
            }

            // if attribute is present then fetch extratoppings only from attribute list
            if(ui_model!!.viewcard_list.value!!.result!![i].is_attributes !=null && ui_model!!.viewcard_list.value!!.result!![i].is_attributes.equals("1")){
                if(ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes !=null){
                    for (k in 0 until ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.size){
                        // attribute_value_name = AB
                        inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val attribute_value_name= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                        attribute_value_name.subitem_name.text=ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).attribute_value_name
                        attribute_value_name.subitem_price.visibility= View.INVISIBLE
                        view.add_subitem_view.addView(attribute_value_name)

                        for (l in 0 until (ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!![k].order_product_extra_topping_group?.size ?: 0)){
                            // attribute's ingredient_name +a +b
                            inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                            extratoppings.subitem_name.text=String.format(getString(R.string.plus),ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                           // extratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(ui_model!!.viewcard_list.value!!.result!![i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
                            extratoppings.subitem_price.visibility=View.INVISIBLE
                            view.add_subitem_view.addView(extratoppings)
                        }
                    }
                }
            }
            else
            {
                // if extratopping group only present then add only extratoppings in the list.
                for (k in 0 until (ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group?.size ?:0)){
                    inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val onlyextratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                    onlyextratoppings.subitem_name.text=String.format(getString(R.string.plus),ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).ingredient_name)
                    // view.subitem_price.visibility=View.VISIBLE
                  //  onlyextratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(ui_model!!.viewcard_list.value!!.result!![i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                    onlyextratoppings.subitem_price.visibility=View.INVISIBLE
                    view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
            subtotal.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
            total.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
        }
    }


/*
    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount > 0){
            val fragment =supportFragmentManager.findFragmentByTag(supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount-1).name)
            if(fragment !=null && fragment.isVisible){
                when (fragment) {

                    is TransactionStatus -> {
                        //  popFragment()
                        when(fragment.currentView){
                            Constants.PROGRESSDIALOG ->{ }
                            Constants.PAYMENTSTATUS -> {
                                fragment.onBackpress()
                            }
                        }
                    }

                    is Address ->{
                        img_toolbar_back.setImageResource(R.drawable.close)
                        txt_toolbar_right_img.visibility= View.GONE
                        txt_toolbar.text=getString(R.string.basket)
                        popFragment()
                    }
                    is SelectAddress ->{
                        txt_toolbar_right_img.apply { visibility= View.VISIBLE ; setImageResource(R.drawable.info_outline) }
                        popFragment()
                    }
                    is DeliveryTimeslot ->{
                        txt_toolbar.text=getString(R.string.address)
                        txt_toolbar_right_img.apply { if(EpayActivity.isPickup) visibility= View.GONE else visibility= View.VISIBLE ; setImageResource(R.drawable.info_outline) }
                        popFragment()
                    }
                    is Paymentmethod ->{
                        txt_toolbar.text=getString(R.string.confirm_delivery_time)
                        popFragment()
                    }

                    is BamboraWebfunction ->{
                        fragment.onBackpress()
                        */
/*      popFragment()
                              txt_toolbar.text=getString(R.string.payment)
                              val fragment= supportFragmentManager.findFragmentByTag(Paymentmethod.TAG)
                              (fragment as Paymentmethod).onlineTransactionFailed()*//*


                    }
                    else -> popFragment()

                }
            }
        }
        else {
            finishActivity()
        }
    }
*/


    fun tabfunction() {


        if(!DetailsFragment.delivery_present && DetailsFragment.pickup_present){
            menu_tabs.addTab(menu_tabs.newTab().setText(getString(R.string.pickup)))
            isPickup=true
        } else if(DetailsFragment.delivery_present && !DetailsFragment.pickup_present){
            menu_tabs.addTab(menu_tabs.newTab().setText((if(DetailsFragment.delivery_charge_title=="") getString(R.string.delivery) else getString(R.string.delivery)+"\n"+ DetailsFragment.delivery_charge_title)+" "+ BindDataUtils.convertCurrencyToDanish(DetailsFragment.delivery_charge)))
            isPickup=false
        }else{
            menu_tabs.addTab(menu_tabs.newTab().setText((if(DetailsFragment.delivery_charge_title=="") getString(R.string.delivery) else getString(R.string.delivery)+"\n"+ DetailsFragment.delivery_charge_title)+" "+ BindDataUtils.convertCurrencyToDanish(DetailsFragment.delivery_charge)))
            menu_tabs.addTab(menu_tabs.newTab().setText(getString(R.string.pickup)))
            isPickup=false
        }

        if(isPickup){
            info_outline_img.visibility=View.GONE
            binding.pickupDeliveryTxt = DetailsFragment.pickup_text
        } else {
            info_outline_img.visibility=View.VISIBLE
            binding.pickupDeliveryTxt = DetailsFragment.delivery_text
        }

        info_outline_img.setOnClickListener{ CartListFunction.showDialog(restaurant = restaurant,context = context!!)}

        binding.executePendingBindings()


        menu_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                logd(TAG, menu_tabs.selectedTabPosition.toString())
                when(menu_tabs.selectedTabPosition){
                    1->{
                        isPickup=true
                        info_outline_img.visibility=View.GONE
                        binding.pickupDeliveryTxt = DetailsFragment.pickup_text.also { binding.executePendingBindings() }
                        DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.black),msg = getString(R.string.you_are_ordering_pickup),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                            override fun onPositiveButtonClick(position: Int) {
                            }
                            override fun onNegativeButtonClick() {
                            }
                        })
                    }
                    0->{
                        isPickup=false
                        info_outline_img.visibility=View.VISIBLE
                        binding.pickupDeliveryTxt = DetailsFragment.delivery_text.also { binding.executePendingBindings() }
                    }
                }

            }
        })
    }



    /*  fun finishActivity(){
          // DrawableCompat.setTint(ContextCompat.getDrawable(this,R.drawable.close)!!, ContextCompat.getColor(this, R.color.white));
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
              finishAfterTransition()
          else
              finish()
      }*/

    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "on destroy...")
    }

    override fun onDestroyView() {
        logd(TAG, "onDestroyView...")

        ui_model?.let {
            ViewModelProviders.of(this).get(UIModel::class.java).viewcard_list.removeObservers(this@EpayFragment)
        }

        call_deleteitem?.let {
            progress_bar.visibility=View.GONE
            it.cancel()
        }
        call_viewcartlist?.let {
            progress_bar.visibility=View.GONE
            it.cancel()
        }

        super.onDestroyView()
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



    data class PaymentAttributes(
            var first_name :String ="",
            var address :String ="",
            var telephone_no :String ="",
            var postal_code :String ="",
            var comments :String ="",
            var restaurant_id: String = "",
            var payment_address :String ="",
            var is_fav : Boolean = false,
            var payment_time :String ="",
            var expected_time :String ="",
            var expected_time_display :String ="",
            var restaurant_name :String ="",
            var restaurant_address :String ="",
            var restaurant_phone :String ="",
            var restaurant_appicon :String ="",
            var first_time :String ="",
            var online_logo :String ="",
            var cash_logo :String ="",
            var discount_id :Int =0,
            var discount_type :String ="",
            var discount_amount :Double =0.0,
            var shipping_charge :String ="0",
            var upto_min_shipping :String ="0",
            var minimum_order_price :String ="0",
            var order_total :String ="0", // this is same as subtotal + excluded Tax
            var additional_charge :String ="0",
            var additional_charges_online :String ="0",
            var additional_charges_cash :String ="0",
            var distance :String ="",
            var cardno :String ="",
            var txnid :String ="",
            var epay_merchant :String ="",
            var paymenttype :String ="",
            var txnfee :String ="",
            var order_no :Int =0,
            var final_amount :Double =0.0  // this is final amount + included Tax

    )


}


