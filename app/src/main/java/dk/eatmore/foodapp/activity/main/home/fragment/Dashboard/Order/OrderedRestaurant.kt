package dk.eatmore.foodapp.fragment.Dashboard.Order

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.location.Address
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.transition.ChangeBounds
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.RateOrder
import dk.eatmore.foodapp.databinding.FragmentOrderedRestaurantBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.CommanAPI
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.fragment_ordered_restaurant.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class OrderedRestaurant : CommanAPI() {



    lateinit var binding: FragmentOrderedRestaurantBinding
    private lateinit var myclickhandler: MyClickHandler
    private lateinit var model: OrderFragment.Orderresult



    companion object {

        val TAG = "OrderedRestaurant"
        var ui_model: UIModel? = null

        fun newInstance(orderresult : OrderFragment.Orderresult): OrderedRestaurant {
            val fragment = OrderedRestaurant()
            val bundle = Bundle()
            bundle.putString(Constants.RESTAURANT, orderresult.restaurant_name)
            bundle.putString(Constants.APP_ICON, orderresult.app_icon)
            bundle.putString(Constants.ORDER_DATE, orderresult.order_date)
            bundle.putString(Constants.ORDER_NO, orderresult.order_no)
            bundle.putBoolean(Constants.ENABLE_RATING, orderresult.enable_rating)
            bundle.putSerializable(Constants.ORDERRESULT, orderresult)
            fragment.arguments=bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_ordered_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        loge(TAG,"saveInstance "+savedInstanceState)
        if(savedInstanceState == null){
            setToolbar()
            binding.isProgress=true
            Handler().postDelayed({
                model = arguments!!.getSerializable(Constants.ORDERRESULT) as OrderFragment.Orderresult
                myclickhandler=  MyClickHandler(this)
                ui_model = createViewModel()
                fetchRestaurant_info()

            },300)
        }
    }


    private fun setToolbar(){

        txt_toolbar.text=getString(R.string.orders)
        txt_toolbar_right.text=Constants.REORDER
        txt_toolbar_right.setOnClickListener{
            loge(TAG,"reorder---")
                //  (parentFragment as OrderFragment).fetchReorder_info(model)
                //  (activity as HomeActivity).onBackPressed()
                fetchReorder_info(model,orderedrestaurant_container)

        }
        img_toolbar_back.setOnClickListener {backpress()}
    }


    class UIModel : ViewModel() {
        var ordered_details = MutableLiveData<OrderedDetails>()  // this is list to show pre order
    }


    fun createViewModel(): OrderedRestaurant.UIModel =

            ViewModelProviders.of(this).get(OrderedRestaurant.UIModel::class.java).apply {
                ordered_details.removeObservers(this@OrderedRestaurant)

                ordered_details.observe(this@OrderedRestaurant, Observer<OrderedDetails> {
                    refreshview()

                })

            }



    private fun generateBillDetails(){
        val list =ui_model!!.ordered_details.value!!.data[0]

        if(list.shipping != "Delivery"){
            // pick up:
            subtotal_layout.visibility=View.VISIBLE
            restuptominimum_layout.visibility=if(list.upto_min_shipping.toDouble() <= 0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility=View.GONE
            additional_charge_layout.visibility=if(list.additional_charge.toDouble() <= 0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility=View.VISIBLE
            subtotal_txt.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(list.order_total)
            restuptominimum_txt.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(list.upto_min_shipping)
            additional_charge_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(list.additional_charge)
            discountcoupan_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(list.discount_amount.toString()))
            discountgift_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(list.discount_amount.toString()))

            if((list.discount_type !=null ) && (list.discount_type ==Constants.GIFTCARD )){
                discountgift_layout.visibility=if(list.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountcoupan_layout.visibility=View.GONE

            }else if((list.discount_type !=null ) && (list.discount_type ==Constants.COUPON )){
                discountcoupan_layout.visibility=if(list.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountgift_layout.visibility=View.GONE

            }else{
                discountgift_layout.visibility=View.GONE
                discountcoupan_layout.visibility=View.GONE


            }

            total_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(list.total_to_pay)

        }

        //--------------------------------------//---------------------------------------------//


        else{
            // delivery :
            subtotal_layout.visibility=View.VISIBLE
            restuptominimum_layout.visibility=if(list.upto_min_shipping.toDouble() <= 0) View.GONE else View.VISIBLE
            shipping_layout.visibility=if(list.shipping_costs <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility=if(list.additional_charge.toDouble() <= 0) View.GONE else View.VISIBLE
            total_layout.visibility=View.VISIBLE
            subtotal_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(list.order_total)
            restuptominimum_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(list.upto_min_shipping)
            shipping_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(list.shipping_costs.toString())
            additional_charge_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(list.additional_charge)
            discountcoupan_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(list.discount_amount.toString()))
            discountgift_txt.text=String.format(getString(R.string.discount),BindDataUtils.convertCurrencyToDanishWithoutLabel(list.discount_amount.toString()))


            if((list.discount_type !=null ) && (list.discount_type ==Constants.GIFTCARD )){
                discountgift_layout.visibility=if(list.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountcoupan_layout.visibility=View.GONE


            }else if((list.discount_type !=null ) && (list.discount_type ==Constants.COUPON )){
                discountcoupan_layout.visibility=if(list.discount_amount <= 0 )View.GONE else View.VISIBLE
                discountgift_layout.visibility=View.GONE

            }else{
                discountgift_layout.visibility=View.GONE
                discountcoupan_layout.visibility=View.GONE

            }

            total_txt.text=BindDataUtils.convertCurrencyToDanishWithoutLabel(list.total_to_pay)
        }
    }


    private fun refreshview() {

        loge(TAG,"refresh view...")
        binding.data= ui_model!!.ordered_details.value!!.data[0]
        binding.myclickhandler=myclickhandler
        binding.util=BindDataUtils
        binding.enableRating=arguments!!.getBoolean(Constants.ENABLE_RATING)
        binding.isProgress=false
        binding.executePendingBindings()

        val data =ui_model!!.ordered_details.value!!.data[0]
        showOrderstatus(payment_status = data.payment_status,enable_rating = data.enable_rating,order_status = data.order_status)

        Glide.with(imageview.context)
                .load(ui_model!!.ordered_details.value!!.data[0].app_icon)
                .apply(RequestOptions().placeholder(BindDataUtils.getRandomDrawbleColor()).error(BindDataUtils.getRandomDrawbleColor()))
                .into(imageview)

        add_parentitem_view.removeAllViewsInLayout()
        val list =ui_model!!.ordered_details.value!!.data[0].order_products_details
        for (i in 0 until list.size){
            var inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view= inflater.inflate(R.layout.dynamic_raw_item,null)
            view.item_name.text=list[i].products.p_name
            view.item_price.text=if(list[i].products.p_price !=null) BindDataUtils.convertCurrencyToDanish(list[i].products.p_price!!) else "null"
            view.remove_item.visibility=View.GONE
            view.add_subitem_view.removeAllViewsInLayout()

            // fill first ingredients size if not null
            for (j in 0 until list.get(i).removed_ingredients.size){
                inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val ingredientview= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                ingredientview.subitem_name.text=String.format(getString(R.string.minues),list.get(i).removed_ingredients[j].ingredient_name)
                ingredientview.subitem_name.setTextColor(ContextCompat.getColor(context!!,R.color.red))
                ingredientview.subitem_price.visibility=View.INVISIBLE
                ingredientview.dummy_image.visibility=View.GONE
                view.add_subitem_view.addView(ingredientview)
            }

            // if attribute is present then fetch extratoppings only from attribute list
            if(list[i].products.is_attributes !=null && list[i].products.is_attributes.equals("1")){
                if(list[i].ordered_product_attributes !=null){
                    for (k in 0 until list[i].ordered_product_attributes!!.size){
                        for (l in 0 until (list[i].ordered_product_attributes!!.get(k).order_product_extra_topping_group?.size ?: 0)){
                            inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                            extratoppings.subitem_name.text=String.format(getString(R.string.plus),list[i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                            extratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanish(list[i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
                            extratoppings.dummy_image.visibility=View.GONE
                            view.add_subitem_view.addView(extratoppings)
                        }
                    }
                }
            }
            else
            {
                // if extratopping group only present then add only extratoppings in the list.
                for (k in 0 until (list[i].order_product_extra_topping_group?.size ?:0)){
                    inflater= context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val onlyextratoppings= inflater.inflate(R.layout.dynamic_raw_subitem,null)
                    onlyextratoppings.subitem_name.text=String.format(getString(R.string.plus),list[i].order_product_extra_topping_group!!.get(k).ingredient_name)
                    // view.subitem_price.visibility=View.VISIBLE
                    onlyextratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanish(list[i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                    onlyextratoppings.dummy_image.visibility=View.GONE
                    view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
            //   subtotal.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
            //   total.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
        }
        generateBillDetails()
    }


    fun fetchRestaurant_info() {

        binding.isProgress=true
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP,true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_NO,arguments!!.getString(Constants.ORDER_NO))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)

        callAPI(ApiCall.orderdetails(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val orderedDetails= body as OrderedDetails
                if (orderedDetails.status) {
                    orderedDetails.data.get(0).restaurant_name=arguments!!.get(Constants.RESTAURANT).toString()
                    orderedDetails.data.get(0).app_icon=arguments!!.get(Constants.APP_ICON).toString()
                    orderedDetails.data.get(0).order_date=arguments!!.get(Constants.ORDER_DATE).toString()
                    ui_model!!.ordered_details.value=orderedDetails
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(orderedrestaurant_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(orderedrestaurant_container, getString(R.string.internet_not_available))
                    }
                }
            }
        })
    }




    private fun on_rating() {
        loge(TAG,"on rating...")
        if(ui_model!!.ordered_details.value!!.data[0].order_status.toLowerCase() == Constants.ACCEPTED){
            val fragment = RateOrder.newInstance(order_no = arguments!!.getString(Constants.ORDER_NO),orderresult = model)
            var enter : Slide?=null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                enter = Slide()
                enter.setDuration(Constants.BOTTOM_TO_TOP_ANIM.toLong())
                enter.slideEdge = Gravity.BOTTOM
                val changeBoundsTransition : ChangeBounds = ChangeBounds()
                changeBoundsTransition.duration = Constants.BOTTOM_TO_TOP_ANIM.toLong()
                //fragment!!.sharedElementEnterTransition=changeBoundsTransition
                fragment.sharedElementEnterTransition=changeBoundsTransition
                fragment.sharedElementReturnTransition=changeBoundsTransition
                fragment.enterTransition=enter
            }
            addFragment(R.id.orderedrestaurant_container,fragment, RateOrder.TAG,false)
        }
    }

    fun showOrderstatus(payment_status : String, order_status : String, enable_rating : Boolean)  {

        loge(TAG,"showOrderstatus--"+payment_status+" "+order_status +" "+enable_rating)
        if(payment_status.toLowerCase() == Constants.REFUNDED){

            order_status_view.visibility=View.VISIBLE
            order_status_txt.text="Ordre er refunderet"
            rating_view.visibility=View.GONE
            rated_view.visibility=View.GONE


        }else if(enable_rating == true && order_status.toLowerCase() == Constants.ACCEPTED){

            order_status_view.visibility=View.VISIBLE
            order_status_txt.text="Order accepteret til"
            rating_view.visibility=View.VISIBLE
            rated_view.visibility=View.GONE

        } else if(enable_rating == false && order_status.toLowerCase() == Constants.ACCEPTED){

            order_status_view.visibility=View.VISIBLE
            order_status_txt.text="Order accepteret til"
            rating_view.visibility=View.GONE
            rated_view.visibility=View.VISIBLE
            showuser_rate()

        } else if(order_status.toLowerCase() == Constants.REJECTED){

            order_status_view.visibility=View.VISIBLE
            order_status_txt.text="Ordre er anulleret"
            rating_view.visibility=View.GONE
            rated_view.visibility=View.GONE

        } else if(enable_rating == true && order_status.toLowerCase() != Constants.ACCEPTED && order_status.toLowerCase() != Constants.REJECTED){

            order_status_view.visibility=View.VISIBLE
            order_status_txt.text="Ordre under behandling"
            rating_view.visibility=View.GONE
            rated_view.visibility=View.GONE

        } else if(order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT){

            order_status_view.visibility=View.VISIBLE
            order_status_txt.text="Ordre under behandling"
            rating_view.visibility=View.GONE
            rated_view.visibility=View.GONE

        }else{

            order_status_view.visibility=View.GONE
            rating_view.visibility=View.GONE
            rated_view.visibility=View.GONE
        }


    }

    fun showuser_rate(){
        val label = arrayOf("","Elendigt","Dårligt","Fint","Godt","Fremragende","Fantastisk")
        val data =ui_model!!.ordered_details.value!!.data[0]
        Log.e(TAG,"rate: "+data.quality_of_food_rating+" "+data.total_rating)

        total_rating.rating= data.total_rating

        qty_rating.text= String.format(getString(R.string.qty_rate),data.quality_of_food_rating.toInt())
        qty_remark.text= if(data.quality_of_food_rating.toInt() > 0) String.format(getString(R.string.rate_label),label[data.quality_of_food_rating.toInt()]) else ""

        customer_rating.text=String.format(getString(R.string.customer_rate),data.customer_service_rating.toInt())
        customer_remark.text=if(data.customer_service_rating.toInt() > 0) String.format(getString(R.string.rate_label), label[data.customer_service_rating.toInt()]) else ""

        deliver_rating.text=String.format(getString(R.string.deliver_rate),data.delivery_time_rating.toInt())
        deliver_remark.text=if(data.delivery_time_rating.toInt() > 0) String.format(getString(R.string.rate_label), label[data.delivery_time_rating.toInt()]) else ""

        if(data.review.length > 0 ){
            comment_txt.text=data.review
            comment_txt.visibility=View.VISIBLE
            your_rating_label.visibility=View.VISIBLE
        }else{
            comment_txt.visibility=View.GONE
            your_rating_label.visibility=View.GONE
        }
    }



    override fun comman_apisuccess(status: String) {
        moveon_reOrder("")
    }

    override fun comman_apifailed(error: String) {
    }

    fun backpress() {

        // First back from rate...
        if(childFragmentManager.backStackEntryCount > 0){
            childFragmentManager.popBackStack()  // if rate fragment is open apply only backpress (rate is child of ordered)
            return
        }

        // second back from this.
        if(parentFragment is OrderFragment){
            // If user is from Order fragment
            if(OrderFragment.ui_model?.reloadfragment !=null) OrderFragment.ui_model!!.reloadfragment.value=true   // every time refresh :  order fragment
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().childFragmentManager.popBackStack()

        }else{
            // from Home fragment
            if(HomeFragment.ui_model?.reloadfragment !=null && HomeFragment.count ==1) HomeFragment.ui_model!!.reloadfragment.value=true  // reload last order from homefragment.
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().childFragmentManager.popBackStack()
        }

    }

    fun updateRate(){

        if(childFragmentManager.backStackEntryCount > 0){
            childFragmentManager.popBackStack()  // if rate fragment is open
        }

        if(parentFragment is OrderFragment){
            // If user is from Order fragment
            fetchRestaurant_info() // refresh current fragment to update view
            if(HomeFragment.ui_model?.reloadfragment !=null && HomeFragment.count ==1) HomeFragment.ui_model!!.reloadfragment.value=true  // reload last order from homefragment.

        }else{
            // from Home fragment
            val orderfragment =  ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment()
            if(orderfragment.childFragmentManager.backStackEntryCount > 0){
                orderfragment.childFragmentManager.popBackStack()  // pop all fragment upon order fragment
            }
            fetchRestaurant_info() // refresh current fragment to update view
            if(OrderFragment.ui_model?.reloadfragment !=null) OrderFragment.ui_model!!.reloadfragment.value=true   // every time refresh :  order fragment


        }

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


    class MyClickHandler(val orderedRestaurant: OrderedRestaurant) {

        fun on_rating (view: View ) {
            orderedRestaurant.on_rating()
        }
        fun re_Order (view: View ) {
                //  (parentFragment as OrderFragment).fetchReorder_info(model)
                //  (activity as HomeActivity).onBackPressed()
                orderedRestaurant.fetchReorder_info(orderedRestaurant.model,orderedRestaurant.orderedrestaurant_container)

        }


    }

}

/*@BindingAdapter("android:layout_setImage")
fun setImage(view : AppCompatImageView, model : String) {
    // i set 100 fixed dp in rating page thats why i am using 100
    Log.e("set image","----"+model)
//    Glide.with(view.context).load(model.app_icon).into(view);

}*/

//-----------Model class---------------//

data class OrderedDetails(
        val msg: String = "",
        val status: Boolean = false,
        val data: ArrayList<Data> // you can always fetch this, using get(0)
)

data class Data(
        val order_no : String ="",
        var order_date : String ="",
        val pickup_delivery_time : String ="",
        val address : String ="",
        var total_to_pay: String = "",
        var shipping : String = "",
        var order_status : String = "",
        var payment_status : String = "",
        var upto_min_shipping :String ="0.0",
        var additional_charge :String ="0.0",
        val total_rating: Float = 0.0f,
        val quality_of_food_rating: Float = 0.0f,
        val customer_service_rating: Float = 0.0f,
        val delivery_time_rating: Float = 0.0f,
        var discount_amount :Double =0.0,
        var discount_type :String? =null,
        var shipping_costs :Double =0.0,
        var accept_reject_time :String? =null,
        var expected_time :String ="",
        var review :String ="",
        var paymethod :String ="0",
        var order_total :String ="0", // this is same as subtotal + excluded Tax
        var restaurant_name: String = "",
        var app_icon: String = "",
        var enable_rating: Boolean = false,
        val order_products_details : ArrayList<Orderproducts_Details> = arrayListOf()  // list of product.
)



data class Orderproducts_Details(
        val products : Products,
        val removed_ingredients : ArrayList<RemovedIngredientsItem>,
        val ordered_product_attributes : ArrayList<OrderedProductAttributesItem>?=null,
        val order_product_extra_topping_group : ArrayList<OrderProductExtraToppingGroupItem>?=null
)

data class Products(
        val is_attributes : String? =null,
        val p_price: String?= null,
        val p_desc: String = "",
        val p_name: String = ""

)

data class RemovedIngredientsItem(
        val i_id: String = "",
        val restaurantId: String = "",
        val ingredient_name: String = "",
        val opi_id: String = "",
        val op_id: String = "",
        val customer_id: String = "")

data class OrderedProductAttributesItem(
        val order_product_extra_topping_group: List<OrderProductExtraToppingGroupItem>?,
        val pad_id: String = "",
        val a_price: String = "",
        val tm_id: String = "",
        val restaurant_id: String = "",
        val opId: String = "",
        val attributeName: String = "",
        val opaId: String = "",
        val customerId: String = "",
        val attribute_value_name: String = "")


data class OrderProductExtraToppingGroupItem(
        val t_price: String = "",
        val tsgd_id: String = "",
        val restaurant_id: String = "",
        val ingredient_name: String = "",
        val opId: String = "",
        val op_id: String = "",
        val customer_id: String = "",
        val opt_id: String = "")