package dk.eatmore.foodapp.fragment.ProductInfo


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.airbnb.lottie.utils.Utils
import com.bumptech.glide.util.Util
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.epay.fragment.TransactionStatus
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickListner
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.databinding.RowCategoryListBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.ProductListItem
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.category_list.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import java.util.*

class CategoryList : BaseFragment(), RecyclerClickListner {


    private lateinit var binding: FragmentAccountContainerBinding
    private var mAdapter: UniversalAdapter<ProductListItem, RowCategoryListBinding>? = null
    private val userList = ArrayList<User>()
    private lateinit var productpricecalculation: ProductPriceCalculation
    private lateinit var restaurant: Restaurant


    companion object {

        val TAG = "CategoryList"
        fun newInstance(restaurant: Restaurant, data: MenuListItem): CategoryList {
            val fragment = CategoryList()
            val bundle = Bundle()
            bundle.putSerializable(Constants.RESTAURANT, restaurant)
            bundle.putString(Constants.TITLE, data.c_name)
            bundle.putString(Constants.C_DESC, data.c_desc)
            bundle.putSerializable(Constants.PRODUCTLIST, data)
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun getLayout(): Int {
        return R.layout.category_list
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

        //   binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
        // return binding.root

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            loge(TAG, "saveInstance NULL" + (arguments?.getSerializable(Constants.RESTAURANT) as Restaurant).toString())
            productpricecalculation = ProductPriceCalculation(this)
            updatebatchcount(0)
            restaurant = arguments?.getSerializable(Constants.RESTAURANT) as Restaurant
            val menuListItem = arguments?.getSerializable(Constants.PRODUCTLIST) as MenuListItem
            val bundle = arguments
            subtxt_toolbar.text = bundle?.getString(Constants.TITLE, "") ?: ""
            subtxt_desc.text = bundle?.getString(Constants.C_DESC, "") ?: ""
            subtxt_desc.visibility = if (subtxt_desc.text.toString().trim().length > 0) View.VISIBLE else View.GONE
            setanim_toolbartitle(appbar, txt_toolbar, bundle?.getString(Constants.TITLE, "") ?: "")
            img_toolbar_back.setOnClickListener {
                (activity as HomeActivity).onBackPressed()
            }
            // loge(TAG,"product_attribute --- "+menuListItem.product_list!!.get(0).product_attribute)
            mAdapter = UniversalAdapter(context!!, menuListItem.product_list, R.layout.row_category_list, object : RecyclerCallback<RowCategoryListBinding, ProductListItem> {
                override fun bindData(binder: RowCategoryListBinding, model: ProductListItem) {
                    setRecyclerData(binder, model)
                }
            })
            recycler_view_category.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view_category.adapter = mAdapter
            viewcart.setOnClickListener {
                if (DetailsFragment.total_cartcnt == 0) return@setOnClickListener

                val fragment = EpayFragment.newInstance(restaurant)
                var enter: Slide? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    enter = Slide()
                    enter.setDuration(300)
                    enter.slideEdge = Gravity.BOTTOM
                    val changeBoundsTransition: ChangeBounds = ChangeBounds()
                    changeBoundsTransition.duration = 300
                    fragment.sharedElementEnterTransition = changeBoundsTransition
                    fragment.enterTransition = enter
                }
                if ((activity as HomeActivity).fragmentTab_is() == 1)
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().addFragment(R.id.home_order_container, fragment, EpayFragment.TAG, false)
                else
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().addFragment(R.id.home_fragment_container, fragment, EpayFragment.TAG, false)


                /*   val intent = Intent(activity, EpayActivity::class.java)
                   val bundle= Bundle()
                   bundle.putSerializable(Constants.RESTAURANT,restaurant)
                   intent.putExtras(bundle)
                   startActivityForResult(intent,1)
   */
            }


        } else {
            logd(TAG, "saveInstance NOT NULL")
        }
    }

    fun updatebatchcount(count: Int) {

        try {
            badge_notification_txt.visibility = View.GONE
            toolbar_badge_view.visibility = if (DetailsFragment.total_cartcnt == 0) View.GONE else View.VISIBLE
            //viewcart.alpha= if(DetailsFragment.total_cartcnt == 0) 0.3f else 0.9f
            //badge_notification_txt.text = DetailsFragment.total_cartcnt.toString()
            if (DetailsFragment.total_cartcnt == 0 || DetailsFragment.total_cartcnt == 1)
                badge_countprice.text = BindDataUtils.convertCurrencyToDanish(DetailsFragment.total_cartamt)
            else
                badge_countprice.text = String.format(getString(R.string.count_ammount), DetailsFragment.total_cartcnt, BindDataUtils.convertCurrencyToDanish(DetailsFragment.total_cartamt))

        } catch (e: Exception) {
            loge(TAG, "exception: - " + e.message)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge("onActivityResult categorylist---", "---")
        // request : send code with request
        // result :  get code from target activity
        if (requestCode == Constants.REQ_CAT_RESAURANT_CLOSED) {
            if (resultCode == Activity.RESULT_OK) {
                any_preorder_closedRestaurant(data!!.extras.get(Constants.IS_RESTAURANT_CLOSED) as Boolean, data.extras.get(Constants.PRE_ORDER) as Boolean, data.extras.get(Constants.MSG) as String)
            }
        }
    }


    override fun <T> onClick(model: T?) {

        //Direct add to cart process condition
        val data = model as ProductListItem
        if (data.is_attributes == "0" && data.extra_topping_group == null) {
            addToCard(data)
        } else {
            val intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("TITLE", data.p_name)
            intent.putExtra("PID", data.p_id)
            intent.putExtra("p_price",productpricecalculation.getprice(data))
            val pairs: Array<Pair<View, String>> = TransitionHelper.createSafeTransitionParticipants(activity!!, true)
            val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, *pairs)
            //startActivityForResult(intent, Constants.REQ_CAT_RESAURANT_CLOSED, transitionActivityOptions.toBundle())
            startActivityForResult(intent, Constants.REQ_CAT_RESAURANT_CLOSED)
        }

    }

    private fun addToCard(data: ProductListItem) {
        showProgressDialog()
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        } else {
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }
        postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
        postParam.addProperty(Constants.P_ID, data.p_id)
        postParam.addProperty(Constants.P_PRICE, data.p_price)
        postParam.addProperty(Constants.P_QUANTITY, "1")
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.EN)

        callAPI(ApiCall.addtocart(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    showProgressDialog()

                    if ((jsonObject.has(Constants.IS_RESTAURANT_CLOSED) && jsonObject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean == true) &&
                            (jsonObject.has(Constants.PRE_ORDER) && jsonObject.get(Constants.PRE_ORDER).asBoolean == false)) {
                        val msg = if (jsonObject.has(Constants.MSG)) jsonObject.get(Constants.MSG).asString else getString(R.string.sorry_restaurant_has_been_closed)
                        any_preorder_closedRestaurant(jsonObject.get(Constants.IS_RESTAURANT_CLOSED).asBoolean, jsonObject.get(Constants.PRE_ORDER).asBoolean, msg)

                    } else {
                        val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                        intent.putExtra(Constants.CARTCNT, if (jsonObject.get(Constants.CARTCNT).isJsonNull || jsonObject.get(Constants.CARTCNT).asString == "0") 0 else (jsonObject.get(Constants.CARTCNT).asString).toInt())
                        intent.putExtra(Constants.CARTAMT, if (jsonObject.get(Constants.CARTAMT).isJsonNull || jsonObject.get(Constants.CARTAMT).asString == "0") "00.00" else jsonObject.get(Constants.CARTAMT).asString)
                        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
                        Toast.makeText(context, getString(R.string.item_has_been), Toast.LENGTH_SHORT).show()
                    }

                } else {
                    showProgressDialog()
                    showSnackBar(category_list_container, getString(R.string.error_404))
                }
            }

            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(category_list_container, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(category_list_container, getString(R.string.internet_not_available))
                    }
                }
                showProgressDialog()
            }
        })
    }


    private fun setRecyclerData(binder: RowCategoryListBinding, model: ProductListItem) {
        binder.data = model
        binder.productpricecalculation = productpricecalculation
        binder.util = BindDataUtils
        binder.handler = this
        binder.executePendingBindings()
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

    fun backpress(): Boolean {
        //  val fragment = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(DetailsFragment.TAG)
        //   val homeFragment : HomeFragment =(fragmentof as HomeContainerFragment).getHomeFragment()
        //   (parentFragment as DetailsFragment).setPalette()
        //   (parentFragment as DetailsFragment).appbar.setExpanded(true,true)
        return true
    }

    class ProductPriceCalculation(val categorylist: CategoryList) {
        // this is calculation of showing p price which will take from array or object.
        var attribute_cost: Double = 0.0

        fun getprice(productListItem: ProductListItem): String {


                if (productListItem.product_attribute == null) {
                    return BindDataUtils.convertCurrencyToDanish(productListItem.p_price!!)!!
                } else {
                    attribute_cost = 0.0
                    for (i in 0..productListItem.product_attribute.size - 1) {
                        attribute_cost = attribute_cost + productListItem.product_attribute.get(i).default_attribute_value.a_price.toDouble()
                    }
                    return BindDataUtils.convertCurrencyToDanish(attribute_cost.toString())
                            ?: "null"
                }




        }

    }


}