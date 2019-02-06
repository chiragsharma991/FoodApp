package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.databinding.FragmentOrderContainerBinding
import dk.eatmore.foodapp.databinding.RateOrderBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.rate_order.*
import kotlinx.android.synthetic.main.toolbar.*

class RateOrder : BaseFragment(), RatingBar.OnRatingBarChangeListener {


    private lateinit var binding: RateOrderBinding
    private lateinit var model: OrderFragment.Orderresult


    companion object {

        val TAG = "RateOrder"
        fun newInstance(order_no: String, orderresult : OrderFragment.Orderresult): RateOrder {
            val fragment = RateOrder()
            val bundle = Bundle()
            bundle.putString(Constants.ORDER_NO, order_no)
            bundle.putSerializable(Constants.ORDERRESULT, orderresult)
            fragment.arguments = bundle
            return fragment
        }


    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.rate_order
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        loge(TAG, "saveInstance " + savedInstanceState)
        if (savedInstanceState == null) {
            model = arguments!!.getSerializable(Constants.ORDERRESULT) as OrderFragment.Orderresult
            img_toolbar_back.setImageResource(R.drawable.close)
            img_toolbar_back.setOnClickListener {(activity as HomeActivity).onBackPressed()}
            rate_btn.alpha = 0.6f
            rate_btn.isEnabled = false
            quality_of_food_rating.onRatingBarChangeListener = this
            delivery_time_rating.onRatingBarChangeListener = this
            customer_service_rating.onRatingBarChangeListener = this
            rate_btn.setOnClickListener {
                submit_rate()
            }
            setanim_toolbartitle(appbar, txt_toolbar, getString(R.string.rate_order))

        }

    }

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {

        val label = arrayOf("","Elendigt","Dårligt","Fint","Godt","Fremragende","Fantastisk")

        if(ratingBar!!.id == quality_of_food_rating.id){
            qty_remark.text= if(rating.toInt() > 0) String.format(getString(R.string.rate_label),label[rating.toInt()]) else ""
        }else if(ratingBar.id == customer_service_rating.id){
            customer_service_remark.text= if(rating.toInt() > 0) String.format(getString(R.string.rate_label),label[rating.toInt()]) else ""
        }else if(ratingBar.id == delivery_time_rating.id){
            delivery_time_remark.text= if(rating.toInt() > 0) String.format(getString(R.string.rate_label),label[rating.toInt()]) else ""
        }

        if ((quality_of_food_rating.rating.toInt() > 0) && (delivery_time_rating.rating.toInt() > 0) && (customer_service_rating.rating.toInt() > 0)) {
            rate_btn.alpha = 1.0f
            rate_btn.isEnabled = true
        } else {
            rate_btn.alpha = 0.6f
            rate_btn.isEnabled = false
        }
    }


    fun submit_rate() {

        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        postParam.addProperty(Constants.ORDER_NO, arguments!!.getString(Constants.ORDER_NO))
        postParam.addProperty(Constants.QUALITY_OF_FOOD, quality_of_food_rating.rating.toDouble())
        postParam.addProperty(Constants.DELIVERY_TIME, delivery_time_rating.rating.toDouble())
        postParam.addProperty(Constants.CUSTOMER_SERVICE, customer_service_rating.rating.toDouble())
        postParam.addProperty(Constants.REVIEW, comment_edt.text.toString())
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)

        callAPI(ApiCall.rating(postParam), object : BaseFragment.OnApiCallInteraction {
            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    model.enable_rating =false
                    val fragment=((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getContainerFragment()
                    when(fragment){

                        is HomeFragment ->{
                            if(parentFragment is OrderedRestaurant){
                                (parentFragment as OrderedRestaurant).updateRate() // refresh ordered restaurant.
                            }else{
                                fragment.updateRate() // refresh order fragment
                            }
                        }
                        is OrderFragment ->{

                            if(parentFragment is OrderedRestaurant){
                                (parentFragment as OrderedRestaurant).updateRate() // refresh ordered restaurant.
                            }else{
                                fragment.updateRate() // refresh order fragment
                            }
                        }
                    }
                    Toast.makeText(context, jsonObject.get(Constants.MSG).asString, Toast.LENGTH_SHORT).show()

                } else {
                    showSnackBar(rate_container, jsonObject.get(Constants.MSG).asString)
                }
                showProgressDialog()

            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(rate_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(rate_container, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        loge(TAG, "on destroy...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loge(TAG, "on destroyView...")
        // ui_model=null
    }

    override fun onDetach() {
        super.onDetach()
        loge(TAG, "on detech...")

    }

    override fun onPause() {
        super.onPause()
        loge(TAG, "on pause...")

    }

}












