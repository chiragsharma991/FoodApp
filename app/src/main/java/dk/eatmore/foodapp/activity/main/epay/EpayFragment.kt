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
import dk.eatmore.foodapp.databinding.ActivityEpayBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.Address
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.model.epay.ResultItem
import dk.eatmore.foodapp.model.epay.ViewcardModel
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.toolbar.*
import dk.eatmore.foodapp.model.home.Restaurant
import kotlinx.android.synthetic.main.activity_epay.*
import kotlinx.android.synthetic.main.fragment_home_container.*
import retrofit2.Call
import kotlin.collections.ArrayList


class EpayFragment : CommanAPI() {


    var transition : Transition?=null
    private lateinit var addcart_fragment: AddCart
    private lateinit var binding: ActivityEpayBinding
    private lateinit var restaurant : Restaurant
    private lateinit var menu : ArrayList<MenuListItem>
    private var call_viewcartlist  : Call<ViewcardModel>? =null
    private var call_deleteitem  : Call<JsonObject>? =null
    private var tablistner: TabLayout.OnTabSelectedListener? =null






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


        fun newInstance(restaurant: Restaurant, menu: ArrayList<MenuListItem>): EpayFragment {
            val fragment = EpayFragment()
            val bundle =Bundle()
            bundle.putSerializable(Constants.RESTAURANT,restaurant)
            bundle.putSerializable(Constants.MENULISTITEM,menu)
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

        if(savedInstanceState == null){
            amIFinish=true
            accessOnetime=true
            restaurant=arguments!!.getSerializable(Constants.RESTAURANT) as Restaurant
            menu=arguments!!.getSerializable(Constants.MENULISTITEM) as ArrayList<MenuListItem>
            loge(TAG,"menu list --"+menu.size)
            setToolbarforThis()
            epay_continue_btn.text=if(PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS,false)) getString(R.string.continue_) else getString(R.string.login_to_continue)
            epay_continue_btn.setOnClickListener{
                continuefromviewcart()
            }
            ui_model = createViewModel()
            reloadScreen()
        }
    }

    fun reloadScreen(){
        progress_bar.visibility=View.VISIBLE
        empty_view.visibility= View.GONE
        view_container.visibility= View.GONE
        checkinfo_restaurant_closed()
    }

    override fun comman_apisuccess(jsonObject: JsonObject, api_tag: String) {

        when(api_tag ){

            Constants.COM_INFO_RESTAURANT_CLOSED->{

                val msg= if(jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else ""
                if(jsonObject.has(Constants.IS_DELIVERY_PRESENT) && jsonObject.has(Constants.IS_PICKUP_PRESENT)){
                    DetailsFragment.delivery_present=jsonObject.get(Constants.IS_DELIVERY_PRESENT).asBoolean
                    DetailsFragment.pickup_present=jsonObject.get(Constants.IS_PICKUP_PRESENT).asBoolean
                }
                when(getrestaurantstatus(is_restaurant_closed =jsonObject.get(Constants.IS_RESTAURANT_CLOSED)?.asBoolean, pre_order =jsonObject.get(Constants.PRE_ORDER)?.asBoolean )){

                    RestaurantState.CLOSED ->{
                        any_preorder_closedRestaurant(is_restaurant_closed = true ,pre_order = false,msg =msg ) // set hard code to close restaurant.
                    }

                    else ->{
                        // if both tab is not present then:
                        val message=getdeliverymsg_error(jsonObject)
                        if(!DetailsFragment.delivery_present && !DetailsFragment.pickup_present){
                            DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.black),msg = message,title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                                override fun onPositiveButtonClick(position: Int) {
                                    (activity as HomeActivity).onBackPressed()
                                }
                                override fun onNegativeButtonClick() {
                                }
                            })

                        }else{
                            // normal flow
                           // tabfunction()
                            fetch_viewCardList()
                        }

                    }
                }

            }

        }
    }

    override fun comman_apifailed(error: String, api_tag: String) {
        when(api_tag ){
            Constants.COM_INFO_RESTAURANT_CLOSED->{
                if(error == getString(R.string.error_404)){
                    showSnackBarIndefinite(epay_container, getString(R.string.error_404))
                }else if(error == getString(R.string.internet_not_available)){
                    showSnackBarIndefinite(epay_container, getString(R.string.internet_not_available))
                }
            }
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
        postParam.addProperty(Constants.SHIPPING, if (DetailsFragment.isPickup) context!!.getString(R.string.pickup_) else context!!.getString(R.string.delivery_))
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

                    submitActualPrice(viewcardmodel)

                } else {

                    // restaurant is not closed then:
                    ui_model!!.viewcard_list.value=null
                    empty_view.visibility= View.VISIBLE
                    view_container.visibility= View.GONE
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

    private fun submitActualPrice(viewcardmodel : ViewcardModel){

        // selected product list
      loop@ for (resultitem in viewcardmodel.result!! ){

            // check from privious list.
            for (i in 0.until(menu.size)){

                for (j in 0.until(menu[i].product_list!!.size)){

                    if(menu[i].product_list!![j].p_id.trim() == resultitem.p_id.trim()){
                     resultitem.actual_price=menu[i].product_list!![j].actual_price!!.trim()
                     continue@loop
                    }
                }

            }

        }
        ui_model!!.viewcard_list.value=viewcardmodel
        view_container.visibility= View.VISIBLE
        progress_bar.visibility=View.GONE




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

        val viewcardmodel = ui_model!!.viewcard_list.value
        if(viewcardmodel ==null){
            // this condition will null if all item has been deleted : so just clear view and inflate empty view on screen.
            add_parentitem_view.removeAllViewsInLayout()
            add_parentitem_view.invalidate()
            return
        }
        paymentattributes=PaymentAttributes()
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
        restaurant.giftcard_details?.eatmore?.let { paymentattributes.giftcard_details[Constants.EATMORE]=it.toString() }
        restaurant.giftcard_details?.restaurant?.let { paymentattributes.giftcard_details[Constants.RESTAURANT]=it.toString() }
        if(viewcardmodel.offer_details !=null && viewcardmodel.offer_details!!.offer_type == Constants.ORDER_DISCOUNT && viewcardmodel.order_total >= viewcardmodel.offer_details!!.minimum_order_price!!.toDouble()){
            loge(TAG,"Order discount--")
            val discountPrice=((viewcardmodel.offer_details!!.discount!!.toDouble() * viewcardmodel.order_total)/100)
            val actual_price_afterDiscount=viewcardmodel.order_total - discountPrice
            epay_total_txt.text= String.format(getString(R.string.dkk_price),BindDataUtils.convertCurrencyToDanishWithoutLabel(actual_price_afterDiscount.toString()))
            binding.offerDiscounted= true
            subtotal_txt.text = String.format(getString(R.string.dkk_price),BindDataUtils.convertCurrencyToDanishWithoutLabel(viewcardmodel.order_total.toString()))
            discount_txt.text = String.format(getString(R.string.minues),BindDataUtils.convertCurrencyToDanishWithoutLabel(discountPrice.toString()))
            paymentattributes.subtotal=actual_price_afterDiscount.toString()
            paymentattributes.discount_type=Constants.ORDER_DISCOUNT
            paymentattributes.discount_amount=discountPrice
        }else{
            loge(TAG,"No discount--")
            binding.offerDiscounted= false
            epay_total_txt.text= String.format(getString(R.string.dkk_price),BindDataUtils.convertCurrencyToDanish(viewcardmodel.order_total.toString()))
            paymentattributes.subtotal=viewcardmodel.order_total.toString()
            paymentattributes.discount_type=""
            paymentattributes.discount_amount=0.0
        }
        epay_total_lbl.text=String.format(getString(R.string.total_goods),ui_model!!.viewcard_list.value!!.cartcnt)
        add_parentitem_view.removeAllViewsInLayout()
        for (i in 0 until ui_model!!.viewcard_list.value!!.result!!.size){

            loge(TAG,"actual price-"+ui_model!!.viewcard_list.value!!.result!![i].actual_price)
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
          //  subtotal.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
          //  total.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
        }
        binding.executePendingBindings()
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
            var subtotal :String ="0", // this is same as subtotal + excluded Tax
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
            var giftcard_details: HashMap<String, String> = HashMap(),
            var final_amount :Double =0.0  // this is final amount + included Tax

    )


}


