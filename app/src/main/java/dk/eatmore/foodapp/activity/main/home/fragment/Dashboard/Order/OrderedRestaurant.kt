package dk.eatmore.foodapp.fragment.Dashboard.Order

import android.app.AlertDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.transition.ChangeBounds
import android.transition.Slide
import android.transition.Visibility
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.dynamic_raw_item.view.*
import kotlinx.android.synthetic.main.dynamic_raw_subitem.view.*
import kotlinx.android.synthetic.main.fragment_ordered_restaurant.*
import kotlinx.android.synthetic.main.include_orderedrest_info.*
import kotlinx.android.synthetic.main.include_orderedrest_pricecalculation.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import java.util.*

class OrderedRestaurant : CommanAPI() {


    lateinit var binding: FragmentOrderedRestaurantBinding
    private lateinit var myclickhandler: MyClickHandler
    private lateinit var model: OrderFragment.Orderresult
    private var call_check_order: Call<JsonObject>? = null
    private val timeoutHandler = Handler()
    private var finalizer: Runnable? = null
    private var call_favorite: Call<JsonObject>? = null


    companion object {

        val TAG = "OrderedRestaurant"
        var ui_model: UIModel? = null

        fun newInstance(orderresult: OrderFragment.Orderresult): OrderedRestaurant {
            val fragment = OrderedRestaurant()
            val bundle = Bundle()
            bundle.putString(Constants.RESTAURANT, orderresult.restaurant_name)
            bundle.putString(Constants.APP_ICON, orderresult.app_icon)
            bundle.putString(Constants.ORDER_DATE, orderresult.order_date)
            bundle.putString(Constants.ORDER_NO, orderresult.order_no)
            bundle.putBoolean(Constants.ENABLE_RATING, orderresult.enable_rating)
            bundle.putSerializable(Constants.ORDERRESULT, orderresult)
            fragment.arguments = bundle
            return fragment
        }

    }


    override fun getLayout(): Int {
        return R.layout.fragment_ordered_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        loge(TAG, "saveInstance " + savedInstanceState)
        if (savedInstanceState == null) {
            setToolbar()
            binding.isProgress = true
            model = arguments!!.getSerializable(Constants.ORDERRESULT) as OrderFragment.Orderresult
            myclickhandler = MyClickHandler(this)
            ui_model = createViewModel()
            fetchRestaurant_info()
        }
    }


    private fun setToolbar() {

        txt_toolbar.text = getString(R.string.orderhistorik)
        txt_toolbar_right.visibility=View.GONE
        txt_toolbar_right.text = getString(R.string.genbestil)
        txt_toolbar_right.setOnClickListener {
            timeoutHandler.removeCallbacks(finalizer)
            if (call_check_order != null) {
                call_check_order!!.cancel()
            }
            fetchReorder_info(model, orderedrestaurant_container)
        }
        img_toolbar_back.setOnClickListener { backpress() }
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


    private fun generateBillDetails() {
        val list = ui_model!!.ordered_details.value!!.data!![0]

        if (list.shipping != "Delivery" && list.shipping != "Udbringning") {
            // pick up:
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (list.upto_min_shipping == null || list.upto_min_shipping!!.toDouble() <= 0) View.GONE else View.VISIBLE  // product price - mincharge
            shipping_layout.visibility = View.GONE
            additional_charge_layout.visibility = if (list.additional_charge == null || list.additional_charge!!.toDouble() <= 0) View.GONE else View.VISIBLE    // online/cash tax
            total_layout.visibility = View.VISIBLE
            eatmoregift_layout.visibility= if(list.eatmore_giftcard.trim() != "" && list.eatmore_giftcard.trim().toDouble() > 0) View.VISIBLE else View.GONE
            restaurantgift_layout.visibility= if(list.restaurant_giftcard.trim() != "" && list.restaurant_giftcard.trim().toDouble() > 0) View.VISIBLE else View.GONE
            discount_layout.visibility= if(list.discount_amount > 0) View.VISIBLE else View.GONE

            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(list.order_total)
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(list.upto_min_shipping?:"")
            additional_charge_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(list.additional_charge?:"")
            discount_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.discount_amount.toString()))
            eatmoregift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.eatmore_giftcard))
            restaurantgift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.restaurant_giftcard))

            total_txt.text = String.format(getString(R.string.dkk_price), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.total_to_pay))

        }

        //--------------------------------------//---------------------------------------------//


        else {
            // delivery :
            subtotal_layout.visibility = View.VISIBLE
            restuptominimum_layout.visibility = if (list.upto_min_shipping == null || list.upto_min_shipping!!.toDouble() <= 0) View.GONE else View.VISIBLE
            shipping_layout.visibility = if (list.shipping_costs == null || list.shipping_costs!! <= 0.0) View.GONE else View.VISIBLE
            additional_charge_layout.visibility = if (list.additional_charge == null || list.additional_charge!!.toDouble() <= 0) View.GONE else View.VISIBLE
            total_layout.visibility = View.VISIBLE
            eatmoregift_layout.visibility= if(list.eatmore_giftcard.trim() != "" && list.eatmore_giftcard.trim().toDouble() > 0) View.VISIBLE else View.GONE
            restaurantgift_layout.visibility= if(list.restaurant_giftcard.trim() != "" && list.restaurant_giftcard.trim().toDouble() > 0) View.VISIBLE else View.GONE
            discount_layout.visibility= if(list.discount_amount > 0) View.VISIBLE else View.GONE


            subtotal_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(list.order_total)
            restuptominimum_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(list.upto_min_shipping ?: "")
            shipping_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(list.shipping_costs?.toString() ?: "")
            additional_charge_txt.text = BindDataUtils.convertCurrencyToDanishWithoutLabel(list.additional_charge ?: "")
            discount_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.discount_amount.toString()))
            eatmoregift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.eatmore_giftcard))
            restaurantgift_txt.text = String.format(getString(R.string.discount), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.restaurant_giftcard))

            total_txt.text = String.format(getString(R.string.dkk_price), BindDataUtils.convertCurrencyToDanishWithoutLabel(list.total_to_pay))

        }
        addspantext()
        check_order()
    }


    private fun refreshview() {

        loge(TAG, "refresh view...")
        favorite_btn.setColorFilter(if (ui_model!!.ordered_details.value!!.data!![0].is_fav) ContextCompat.getColor(context!!, R.color.theme_color) else ContextCompat.getColor(context!!, R.color.gray))
        transaction_progress_bar.loadUrl("file:///android_asset/sandclock.svg")
        binding.data = ui_model!!.ordered_details.value!!.data!![0]
        binding.myclickhandler = myclickhandler
        binding.util = BindDataUtils
        binding.enableRating = arguments!!.getBoolean(Constants.ENABLE_RATING)
        binding.executePendingBindings()

        val data = ui_model!!.ordered_details.value!!.data!![0]
        showOrderstatus(payment_status = data.payment_status, enable_rating = data.enable_rating, order_status = data.order_status)

        ImageLoader.loadImageRoundCornerFromUrl(context = imageview.context,cornerSize = 32,fromFile = ui_model!!.ordered_details.value!!.data!![0].app_icon,imageView = imageview)


        add_parentitem_view.removeAllViewsInLayout()
        val list = ui_model!!.ordered_details.value!!.data!![0].order_products_details
        for (i in 0 until list.size) {
            var inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dynamic_raw_item, null)
            view.item_name.text = String.format(getString(R.string.qty_n_price),list[i].quantity,list[i].products.p_name)
            view.item_price.text = if (list[i].p_price != null) BindDataUtils.convertCurrencyToDanishWithoutLabel(list[i].p_price!!) else "null"
            view.remove_item.visibility = View.GONE
            view.add_subitem_view.removeAllViewsInLayout()

            // fill first ingredients size if not null
            for (j in 0 until list.get(i).removed_ingredients.size) {
                inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val ingredientview = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                ingredientview.subitem_name.text = String.format(getString(R.string.minues), list.get(i).removed_ingredients[j].ingredient_name)
                ingredientview.subitem_name.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                ingredientview.subitem_price.visibility = View.INVISIBLE
                ingredientview.dummy_image.visibility = View.GONE
                view.add_subitem_view.addView(ingredientview)
            }

            // if attribute is present then fetch extratoppings only from attribute list
            if (list[i].products.is_attributes != null && list[i].products.is_attributes.equals("1")) {
                if (list[i].ordered_product_attributes != null) {
                    for (k in 0 until list[i].ordered_product_attributes!!.size) {
                        // attribute_value_name = AB
                        inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val attribute_value_name = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                        attribute_value_name.subitem_name.text = list[i].ordered_product_attributes!!.get(k).attribute_value_name
                        // view.subitem_price.visibility=View.VISIBLE
                        attribute_value_name.subitem_price.visibility = View.INVISIBLE
                        attribute_value_name.dummy_image.visibility = View.GONE
                        view.add_subitem_view.addView(attribute_value_name)


                        for (l in 0 until (list[i].ordered_product_attributes!!.get(k).order_product_extra_topping_group?.size
                                ?: 0)) {
                            inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val extratoppings = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                            extratoppings.subitem_name.text = String.format(getString(R.string.plus), list[i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].ingredient_name)
                            // view.subitem_price.visibility=View.VISIBLE
                            extratoppings.subitem_price.visibility = View.INVISIBLE
                            //   extratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(list[i].ordered_product_attributes!!.get(k).order_product_extra_topping_group!![l].t_price) ?: "null"
                            extratoppings.dummy_image.visibility = View.GONE
                            view.add_subitem_view.addView(extratoppings)
                        }
                    }
                }
            } else {
                // if extratopping group only present then add only extratoppings in the list.
                for (k in 0 until (list[i].order_product_extra_topping_group?.size ?: 0)) {
                    inflater = context!!.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val onlyextratoppings = inflater.inflate(R.layout.dynamic_raw_subitem, null)
                    onlyextratoppings.subitem_name.text = String.format(getString(R.string.plus), list[i].order_product_extra_topping_group!!.get(k).ingredient_name)
                    // view.subitem_price.visibility=View.VISIBLE
                    onlyextratoppings.subitem_price.visibility = View.INVISIBLE
                    //  onlyextratoppings.subitem_price.text= BindDataUtils.convertCurrencyToDanishWithoutLabel(list[i].order_product_extra_topping_group!!.get(k).t_price) ?: "null"
                    onlyextratoppings.dummy_image.visibility = View.GONE
                    view.add_subitem_view.addView(onlyextratoppings)
                }
            }
            add_parentitem_view.addView(view)
            //   subtotal.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
            //   total.text= BindDataUtils.convertCurrencyToDanish(ui_model!!.viewcard_list.value!!.order_total.toString()) ?: "null"
        }
        generateBillDetails()
        Handler().postDelayed({
            binding.isProgress = false
        }, 1000)

    }


    fun fetchRestaurant_info() {
        binding.isProgress = true
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_NO, arguments!!.getString(Constants.ORDER_NO))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)

        callAPI(ApiCall.orderdetails(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val orderedDetails = body as OrderedDetails
                if (orderedDetails.data!!.get(0) == null) {
                    // data is null
                    DialogUtils.openDialogDefault(context = context!!, btnNegative = "", btnPositive = getString(R.string.ok), color = ContextCompat.getColor(context!!, R.color.black), msg = "We're Sorry.\nDetails for this order could not be fetched at this moment.", title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                        override fun onPositiveButtonClick(position: Int) {
                            backpress()
                        }

                        override fun onNegativeButtonClick() {
                        }
                    })
                } else {
                    if (orderedDetails.status) {
                        orderedDetails.data.get(0).restaurant_name = arguments!!.get(Constants.RESTAURANT).toString()
                        orderedDetails.data.get(0).app_icon = arguments!!.get(Constants.APP_ICON).toString()
                        orderedDetails.data.get(0).order_date = arguments!!.get(Constants.ORDER_DATE).toString()
                        ui_model!!.ordered_details.value = orderedDetails
                    }
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


    fun favourite() {
        val data = ui_model!!.ordered_details.value!!.data!![0]
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))      // if restaurant is closed then
        postParam.addProperty(Constants.RESTAURANT_ID, data.restaurant_id)
        if (data.is_fav) {
            // unfavourite--
            DialogUtils.openDialog(context = context!!, btnNegative = getString(R.string.no), btnPositive = getString(R.string.yes), color = ContextCompat.getColor(context!!, R.color.theme_color), msg = getString(R.string.vil_du_fjerne), title = "", onDialogClickListener = object : DialogUtils.OnDialogClickListener {
                override fun onPositiveButtonClick(position: Int) {
                    call_favorite = ApiCall.remove_favorite_restaurant(jsonObject = postParam)
                    remove_favorite_restaurant(call_favorite!!, data)
                }

                override fun onNegativeButtonClick() {
                }
            })
        } else {
            // favourite---
            call_favorite = ApiCall.add_favorite_restaurant(jsonObject = postParam)
            setfavorite(call_favorite!!, data)
        }
    }

    override fun comman_apisuccess(jsonObject: JsonObject, api_tag: String) {
        when (api_tag) {
            Constants.COM_ADD_FAVORITE_RESTAURANT -> {

                val data = ui_model!!.ordered_details.value!!.data!![0]
                favorite_btn.setColorFilter(if (data.is_fav) ContextCompat.getColor(context!!, R.color.theme_color) else ContextCompat.getColor(context!!, R.color.gray))
            }
            else -> {
                moveon_reOrder("")
            }
        }
    }

    override fun comman_apifailed(error: String, api_tag: String) {

        when (api_tag) {
            Constants.COM_ADD_FAVORITE_RESTAURANT -> {

                val data = ui_model!!.ordered_details.value!!.data!![0]
                favorite_btn.setColorFilter(if (data.is_fav) ContextCompat.getColor(context!!, R.color.theme_color) else ContextCompat.getColor(context!!, R.color.gray))
            }
        }
    }


    private fun on_rating() {
        loge(TAG, "on rating...")
        if (ui_model!!.ordered_details.value!!.data!![0].order_status.toLowerCase() == Constants.ACCEPTED) {
            val fragment = RateOrder.newInstance(order_no = arguments!!.getString(Constants.ORDER_NO), orderresult = model)
            var enter: Slide? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                enter = Slide()
                enter.setDuration(Constants.BOTTOM_TO_TOP_ANIM.toLong())
                enter.slideEdge = Gravity.BOTTOM
                val changeBoundsTransition: ChangeBounds = ChangeBounds()
                changeBoundsTransition.duration = Constants.BOTTOM_TO_TOP_ANIM.toLong()
                //fragment!!.sharedElementEnterTransition=changeBoundsTransition
                fragment.sharedElementEnterTransition = changeBoundsTransition
                fragment.sharedElementReturnTransition = changeBoundsTransition
                fragment.enterTransition = enter
            }
            addFragment(R.id.orderedrestaurant_container, fragment, RateOrder.TAG, false)
        }
    }

    fun showOrderstatus(payment_status: String, order_status: String, enable_rating: Boolean) {

        loge(TAG, "showOrderstatus--" + payment_status + " " + order_status + " " + enable_rating)
        if (payment_status.toLowerCase() == Constants.REFUNDED) {

            order_status_view.visibility = View.VISIBLE
            order_status_txt.text = "Ordre er refunderet"
            rating_view.visibility = View.GONE
            rated_view.visibility = View.GONE


        } else if (enable_rating == true && order_status.toLowerCase() == Constants.ACCEPTED) {

            order_status_view.visibility = View.VISIBLE
            order_status_txt.text = "Order accepteret til"
            rating_view.visibility = View.VISIBLE
            rated_view.visibility = View.GONE

        } else if (enable_rating == false && order_status.toLowerCase() == Constants.ACCEPTED) {

            order_status_view.visibility = View.VISIBLE
            order_status_txt.text = "Order accepteret til"
            rating_view.visibility = View.GONE
            rated_view.visibility = View.VISIBLE
            showuser_rate()

        } else if (order_status.toLowerCase() == Constants.REJECTED) {

            order_status_view.visibility = View.VISIBLE
            order_status_txt.text = "Ordre er anulleret"
            rating_view.visibility = View.GONE
            rated_view.visibility = View.GONE

        } else if (enable_rating == true && order_status.toLowerCase() != Constants.ACCEPTED && order_status.toLowerCase() != Constants.REJECTED) {

            order_status_view.visibility = View.VISIBLE
            order_status_txt.text = "Ordre under behandling"
            rating_view.visibility = View.GONE
            rated_view.visibility = View.GONE

        } else if (order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT) {

            order_status_view.visibility = View.VISIBLE
            order_status_txt.text = "Ordre under behandling"
            rating_view.visibility = View.GONE
            rated_view.visibility = View.GONE

        } else {

            order_status_view.visibility = View.GONE
            rating_view.visibility = View.GONE
            rated_view.visibility = View.GONE
        }


    }

    fun showuser_rate() {
        val label = arrayOf("", "Elendigt", "Dårligt", "Fint", "Godt", "Fremragende", "Fantastisk")
        val data = ui_model!!.ordered_details.value!!.data!![0]
        Log.e(TAG, "rate: " + data.quality_of_food_rating + " " + data.total_rating)

        total_rating.rating = data.total_rating

        qty_rating.text = String.format(getString(R.string.qty_rate), data.quality_of_food_rating.toInt())
        qty_remark.text = if (data.quality_of_food_rating.toInt() > 0) String.format(getString(R.string.rate_label), label[data.quality_of_food_rating.toInt()]) else ""

        customer_rating.text = String.format(getString(R.string.customer_rate), data.customer_service_rating.toInt())
        customer_remark.text = if (data.customer_service_rating.toInt() > 0) String.format(getString(R.string.rate_label), label[data.customer_service_rating.toInt()]) else ""

        deliver_rating.text = String.format(getString(R.string.deliver_rate), data.delivery_time_rating.toInt())
        deliver_remark.text = if (data.delivery_time_rating.toInt() > 0) String.format(getString(R.string.rate_label), label[data.delivery_time_rating.toInt()]) else ""

        if (data.review.length > 0) {
            comment_txt.text = data.review
            comment_txt.visibility = View.VISIBLE
            your_rating_label.visibility = View.VISIBLE
        } else {
            comment_txt.visibility = View.GONE
            your_rating_label.visibility = View.GONE
        }
    }


    fun backpress() {

        // First back from rate...
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()  // if rate fragment is open apply only backpress (rate is child of ordered)
            return
        }

        // second back from this.
        if (parentFragment is OrderFragment) {
            // If user is from Order fragment
            if (OrderFragment.ui_model?.reloadfragment != null) OrderFragment.ui_model!!.reloadfragment.value = true   // every time refresh :  order fragment
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().childFragmentManager.popBackStack()

        } else {
            // from Home fragment
            if (HomeFragment.ui_model?.reloadfragment != null && HomeFragment.count == 1) HomeFragment.ui_model!!.reloadfragment.value = true  // reload last order from homefragment.
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().childFragmentManager.popBackStack()
        }

    }

    fun updateRate() {

        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()  // if rate fragment is open
        }

        if (parentFragment is OrderFragment) {
            // If user is from Order fragment
            fetchRestaurant_info() // refresh current fragment to update view
            if (HomeFragment.ui_model?.reloadfragment != null && HomeFragment.count == 1) HomeFragment.ui_model!!.reloadfragment.value = true  // reload last order from homefragment.

        } else {
            // from Home fragment
            val orderfragment = ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment()
            if (orderfragment.childFragmentManager.backStackEntryCount > 0) {
                orderfragment.childFragmentManager.popBackStack()  // pop all fragment upon order fragment
            }
            fetchRestaurant_info() // refresh current fragment to update view
            if (OrderFragment.ui_model?.reloadfragment != null) OrderFragment.ui_model!!.reloadfragment.value = true   // every time refresh :  order fragment


        }

    }

    private fun check_order() {

        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.ORDER_NO, ui_model!!.ordered_details.value!!.data!![0].order_no)
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)
        call_check_order = ApiCall.check_order(jsonObject = postParam)

        callAPI(call_check_order!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                // check_order api responce
                var reject_reason = ""
                var accept_reject_time = ""
                val order_status = jsonObject.get(Constants.ORDER_STATUS).asString
                val payment_status = jsonObject.get(Constants.PAYMENT_STATUS).asString
                if (jsonObject.has(Constants.REJECT_REASON)) {
                    reject_reason = if (jsonObject.get(Constants.REJECT_REASON).isJsonNull) "" else jsonObject.get(Constants.REJECT_REASON).asString
                }
                if (jsonObject.has(Constants.ACCEPT_REJECT_TIME)) {
                    accept_reject_time = if (jsonObject.get(Constants.ACCEPT_REJECT_TIME).isJsonNull) "" else jsonObject.get(Constants.ACCEPT_REJECT_TIME).asString
                }

                if (call_check_order != null) {

                    order_status_txt.setTextColor(ContextCompat.getColor(context!!, R.color.black_txt_regular))

                    if (order_status.toLowerCase() == Constants.PENDING_RESTAURANT || order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT) {
                        // Processing view
                        order_status_txt.text = "Ordre under behandling"
                        transaction_progress_bar.visibility = View.VISIBLE

                    } else {
                        // status view
                        transaction_progress_bar.visibility = View.GONE
                        if (payment_status.toLowerCase() == Constants.REFUNDED) {
                            order_status_txt.text = "Ordre er refunderet"
                            order_accepted_time.text = "Bare rolig, har du benyttet et betalingskort, så er betalingen allerede refunderet."

                        } else {
                            if (order_status.toLowerCase() == Constants.REJECTED) {
                                order_status_txt.text = "Ordre annulleret af restauranten, årsag: ${reject_reason}"
                                order_status_txt.setTextColor(ContextCompat.getColor(context!!, R.color.theme_color))
                                order_accepted_time.text = "Bare rolig, har du benyttet et betalingskort, så er betalingen allerede annulleret. Du kan nu lave en ny bestilling."

                            } else if (order_status.toLowerCase() == Constants.ACCEPTED) {
                                order_status_txt.text = "Ordre accepteret til"
                                if (accept_reject_time.length > 0) {
                                    order_accepted_time.visibility = View.VISIBLE
                                    order_accepted_time.text = accept_reject_time
                                    order_accepted_time.text = String.format(getString(R.string.order_accept_date), BindDataUtils.parsewithoutTimeToddMMyyyy(accept_reject_time), BindDataUtils.parseTimeToHHmm(accept_reject_time))
                                } else {
                                    order_accepted_time.visibility = View.GONE
                                }
                            }

                        }
                    }
                }

                // can i call again?

                if ((order_status.toLowerCase() == Constants.PENDING_OPENING_RESTAURANT) || (order_status.toLowerCase() == Constants.PENDING_RESTAURANT) || (order_status.toLowerCase() == Constants.ACCEPTED && payment_status.toLowerCase() != Constants.REFUNDED)) {
                    // call

                    // we are continuous running because if manager wrong press--- he can change status.
                    finalizer = object : Runnable {
                        override fun run() {
                            loge(TAG, "Handler-----")
                            check_order()

                        }
                    }
                    timeoutHandler.postDelayed(finalizer, 5 * 1000)
                } else {
                    // stop calling api
                    // payment has been refunded---

                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        // showSnackBar(containerview, getString(R.string.error_404))
                        loge(TAG, getString(R.string.error_404))

                    }
                    100 -> {
                        // showSnackBar(containerview, getString(R.string.internet_not_available))
                        loge(TAG, getString(R.string.internet_not_available))
                    }
                }
            }
        })

    }


    fun addspantext() {

        val clickableSpan = object : ClickableSpan() {
            val data = ui_model!!.ordered_details.value!!.data!![0]
            var dialog: AlertDialog? = null
            override fun onClick(textView: View) {
                Log.e(TAG, "onClick:--- ")
                dialog = AlertDialog.Builder(activity).setMessage("Do you want to call ${data.restaurant_phone.trim()}").setCancelable(true).setPositiveButton("yes") { dialogInterface, i ->
                    if (is_callphn_PermissionGranted()) {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + data.restaurant_phone.trim()))
                        startActivity(intent)
                    }
                }.setNegativeButton("no") { dialogInterface, i -> dialog!!.dismiss() }.show()
            }

            override fun updateDrawState(ds: TextPaint) {
                //
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                //                ds.setColor(getResources().getColor(R.color.orange));
                try {
                    val colour = ContextCompat.getColor(context!!, R.color.dark_blue)
                    ds.color = colour
                } catch (e: Exception) {
                    Log.e(TAG, "updateDrawState: error " + e.message)
                }

            }
        }
        val data = ui_model!!.ordered_details.value!!.data!![0]
        val span = SpannableString(String.format(getString(R.string.har_du_brug), data.restaurant_phone.trim()))
        span.setSpan(clickableSpan, (String.format(getString(R.string.har_du_brug), data.restaurant_phone).trim().length - (data.restaurant_phone.trim().length + 1)),
                String.format(getString(R.string.har_du_brug), data.restaurant_phone).trim().length - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        contact_txt.text = span
        contact_txt.movementMethod = LinkMovementMethod.getInstance()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        loge(TAG, "permission result---")
        when (requestCode) {
            0 -> {

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "88826543"))
                    startActivity(intent)
                    //    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    override fun onDestroyView() {

        super.onDestroyView()

        loge(OrderedRestaurant.TAG, "onDestroyView...")

        timeoutHandler.removeCallbacks(finalizer)

        if (call_check_order != null) {
            call_check_order!!.cancel()
        }

        if (call_favorite != null) {
            call_favorite!!.cancel()
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

        fun on_rating(view: View) {
            orderedRestaurant.timeoutHandler.removeCallbacks(orderedRestaurant.finalizer)
            if (orderedRestaurant.call_check_order != null) {
                orderedRestaurant.call_check_order!!.cancel()
            }
            orderedRestaurant.on_rating()
        }

        fun on_favorite(view: View) {
            orderedRestaurant.favourite()
        }

        fun re_Order(view: View) {
            orderedRestaurant.timeoutHandler.removeCallbacks(orderedRestaurant.finalizer)
            if (orderedRestaurant.call_check_order != null) {
                orderedRestaurant.call_check_order!!.cancel()
            }
            orderedRestaurant.fetchReorder_info(orderedRestaurant.model, orderedRestaurant.orderedrestaurant_container)

        }

        fun showmenu (view: View){
            val r_key=(orderedRestaurant.arguments!!.getSerializable(Constants.ORDERRESULT) as OrderFragment.Orderresult).r_key
            val r_token= (orderedRestaurant.arguments!!.getSerializable(Constants.ORDERRESULT) as OrderFragment.Orderresult).r_token
            PreferenceUtil.putValue(PreferenceUtil.R_KEY,r_key)
            PreferenceUtil.putValue(PreferenceUtil.R_TOKEN,r_token)
            PreferenceUtil.save()
            // orderedRestaurant.showProgressDialog()

            if(orderedRestaurant.parentFragment is HomeFragment){
                // home
                ((orderedRestaurant.activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().reorderfuntion()

            }else{
                // order fragment
                val fragmentof = (orderedRestaurant.activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
                (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
                ((orderedRestaurant.activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(0,0) // if user is login and press only back then move->Home
                Handler().postDelayed({
                    //orderedRestaurant.showProgressDialog()
                    HomeFragment.is_from_reorder=true
                    ((orderedRestaurant.activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().reorderfuntion()
                }, 800)
            }




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
        val data: ArrayList<Data>? = null
        // you can always fetch this, using get(0)
)

data class Data(
        val order_no: String = "",
        var order_date: String = "",
        var restaurant_id: String = "",
        val pickup_delivery_time: String? = null,
        val address: String = "",
        var is_fav: Boolean = false,
        var total_to_pay: String = "",
        var shipping: String = "",
        var order_status: String = "",
        var restaurant_phone: String = "",
        var payment_status: String = "",
        var upto_min_shipping: String? = null,
        var additional_charge: String? = null,
        val total_rating: Float = 0.0f,
        val quality_of_food_rating: Float = 0.0f,
        val customer_service_rating: Float = 0.0f,
        val delivery_time_rating: Float = 0.0f,
        var discount_amount: Double = 0.0,
        var discount_type: String? = null,
        var discount_id: String? = null,
        var eatmore_giftcard: String = "",
        var restaurant_giftcard: String = "",
        var shipping_costs: Double? = null,
        var accept_reject_time: String? = null,
        var expected_time: String = "",
        var review: String = "",
        var paymethod: String = "0",
        var order_total: String = "0", // this is same as subtotal + excluded Tax
        var restaurant_name: String = "",
        var app_icon: String = "",
        var enable_rating: Boolean = false,
        val order_products_details: ArrayList<Orderproducts_Details> = arrayListOf()  // list of product.
)


data class Orderproducts_Details(
        val quantity: String? =null,
        val p_price: String? = null,
        val products: Products,
        val removed_ingredients: ArrayList<RemovedIngredientsItem>,
        val ordered_product_attributes: ArrayList<OrderedProductAttributesItem>? = null,
        val order_product_extra_topping_group: ArrayList<OrderProductExtraToppingGroupItem>? = null
)

data class Products(
        val is_attributes: String? = null,
        val p_price: String? = null,
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