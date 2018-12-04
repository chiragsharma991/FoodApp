package dk.eatmore.foodapp.fragment.ProductInfo


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
        fun newInstance(restaurant: Restaurant,data: MenuListItem): CategoryList {
            val fragment = CategoryList()
            val bundle = Bundle()
            bundle.putSerializable(Constants.RESTAURANT, restaurant)
            bundle.putString(Constants.TITLE,data.c_name)
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
            loge(TAG, "saveInstance NULL"+(arguments?.getSerializable(Constants.RESTAURANT) as Restaurant).toString())
            productpricecalculation = ProductPriceCalculation(this)
            updatebatchcount(0)
            restaurant=arguments?.getSerializable(Constants.RESTAURANT) as Restaurant
            val menuListItem = arguments?.getSerializable(Constants.PRODUCTLIST) as MenuListItem
            val bundle = arguments
            subtxt_toolbar.text = bundle?.getString(Constants.TITLE, "") ?: ""
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
                if(DetailsFragment.total_cartcnt == 0) return@setOnClickListener

                val fragment = EpayFragment.newInstance(restaurant)
                var enter : Slide?=null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    enter = Slide()
                    enter.setDuration(300)
                    enter.slideEdge = Gravity.BOTTOM
                    val changeBoundsTransition : ChangeBounds = ChangeBounds()
                    changeBoundsTransition.duration = 300
                    fragment.sharedElementEnterTransition=changeBoundsTransition
                    fragment.enterTransition=enter
                }
                if((activity as HomeActivity).fragmentTab_is() == 1)
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().addFragment(R.id.home_order_container,fragment,EpayFragment.TAG,false)
                else
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().addFragment(R.id.home_fragment_container,fragment, EpayFragment.TAG,false)


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
        badge_notification_txt.visibility = if (DetailsFragment.total_cartcnt == 0) View.GONE else View.VISIBLE
        viewcart.alpha= if (DetailsFragment.total_cartcnt == 0) 0.3f else 0.9f
        badge_notification_txt.text = DetailsFragment.total_cartcnt.toString()
        badge_countprice.text = BindDataUtils.convertCurrencyToDanish(DetailsFragment.total_cartamt)
    }


    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         loge("onActivityResult categorylist---", "" + resultCode + " " + requestCode)
         // request : send code with request
         // result :  get code from target activity

         if ((activity as HomeActivity).is_reorderprocess()) {
             // If user is coming from reorder>>>>
             if (requestCode == 1 && resultCode == AppCompatActivity.DEFAULT_KEYS_SHORTCUT && TransactionStatus.moveonsearch) {
                 // back press from transaction success and continue.
                 TransactionStatus.moveonsearch = false
                 (activity as HomeActivity).popAllReorderFragment()
                 //  ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getContainerFragment().popAllFragment()
             }
         } else {
             // If user is coming from Homecontainer >>>>
             if (requestCode == 1 && resultCode == AppCompatActivity.DEFAULT_KEYS_SHORTCUT && TransactionStatus.moveonsearch) {
                 // back press from transaction success and continue.
                 TransactionStatus.moveonsearch = false
                 val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
                 (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()

                 //  ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getContainerFragment().popAllFragment()
             } else if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK && EpayActivity.moveonEpay) {
                 ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(2)
             }
         }



     }*/


    override fun <T> onClick(model: T?) {

        //Direct add to cart process condition
        val data = model as ProductListItem
        if (data.is_attributes == "0" && data.extra_topping_group == null) {
            addToCard(data)
        } else {
            val intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("TITLE", data.p_name)
            intent.putExtra("PID", data.p_id)
            intent.putExtra("p_price", if (data.product_attribute == null) BindDataUtils.convertCurrencyToDanish(data.p_price
                    ?: "0") else productpricecalculation.getprice(data))
            val pairs: Array<Pair<View, String>> = TransitionHelper.createSafeTransitionParticipants(activity!!, true)
            val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, *pairs)
            startActivity(intent, transitionActivityOptions.toBundle())
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

        callAPI(ApiCall.addtocart(
                jsonObject = postParam
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val jsonObject = body as JsonObject
                if (jsonObject.get(Constants.STATUS).asBoolean) {
                    showProgressDialog()
                    Toast.makeText(context, getString(R.string.item_has_been), Toast.LENGTH_SHORT).show()
                    val intent = Intent(Constants.CARTCOUNT_BROADCAST)
                    intent.putExtra(Constants.CARTCNT, if (jsonObject.get(Constants.CARTCNT).isJsonNull || jsonObject.get(Constants.CARTCNT).asString == "0") 0 else (jsonObject.get(Constants.CARTCNT).asString).toInt())
                    intent.putExtra(Constants.CARTAMT, if (jsonObject.get(Constants.CARTAMT).isJsonNull || jsonObject.get(Constants.CARTAMT).asString == "0") "00.00" else jsonObject.get(Constants.CARTAMT).asString)
                    LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

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

        var attribute_cost: Double = 0.0
        fun getprice(productListItem: ProductListItem): String {
            attribute_cost = 0.0
            for (i in 0..productListItem.product_attribute.size - 1) {
                attribute_cost = attribute_cost + productListItem.product_attribute.get(i).default_attribute_value.a_price.toDouble()
            }
            return BindDataUtils.convertCurrencyToDanish(attribute_cost.toString()) ?: "null"
        }

    }


}