package dk.eatmore.foodapp.fragment.ProductInfo


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.cart.CartActivity
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickListner
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.databinding.RowCategoryListBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.ProductListItem
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.TransitionHelper
import kotlinx.android.synthetic.main.category_list.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import java.util.*

class CategoryList : BaseFragment(), RecyclerClickListner {



    private lateinit var binding: FragmentAccountContainerBinding
    private var mAdapter: UniversalAdapter<ProductListItem, RowCategoryListBinding>? = null
    private val userList = ArrayList<User>()
    private lateinit var productpricecalculation: ProductPriceCalculation


    companion object {

        val TAG = "CategoryList"
        fun newInstance(): CategoryList {
            return CategoryList()
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
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            productpricecalculation=ProductPriceCalculation(this)
            val menuListItem = arguments?.getSerializable("MenuListItem") as MenuListItem
            val bundle=arguments

            subtxt_toolbar.text=bundle?.getString("TITLE","") ?:""
            setanim_toolbartitle(appbar,txt_toolbar,bundle?.getString("TITLE","") ?:"")
            loge(TAG,menuListItem.c_desc+" "+menuListItem.product_list!!.size)
            mAdapter = UniversalAdapter(context!!, menuListItem.product_list, R.layout.row_category_list, object : RecyclerCallback<RowCategoryListBinding, ProductListItem> {
                override fun bindData(binder: RowCategoryListBinding, model: ProductListItem) {
                    setRecyclerData(binder, model)
                }
            })
            recycler_view_category.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view_category.adapter = mAdapter
        }else{
            logd(TAG,"saveInstance NOT NULL")
        }
    }




    override fun <T> onClick(model: T?) {
        //                    android:text="@{data.product_attribute == null ? util.convertCurrencyToDanish(data.p_price) : productpricecalculation.getprice(data)}"


        val data= model as ProductListItem
        val intent=Intent(activity, CartActivity::class.java)
        intent.putExtra("TITLE",data.p_name)
        intent.putExtra("PID",data.p_id)
        intent.putExtra("p_price",if(data.product_attribute ==null) BindDataUtils.convertCurrencyToDanish(data.p_price) else productpricecalculation.getprice(data))
        val pairs: Array<Pair<View,String>> = TransitionHelper.createSafeTransitionParticipants(activity!!, true)
        val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, *pairs)
        startActivity(intent, transitionActivityOptions.toBundle())


    }


    private fun setRecyclerData(binder: RowCategoryListBinding, model: ProductListItem) {
        binder.data=model
        binder.productpricecalculation = productpricecalculation
        binder.util=BindDataUtils
        binder.handler=this
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
        val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
        val homeFragment : HomeFragment =(fragmentof as HomeContainerFragment).getHomeFragment()
        (homeFragment.fragment as DetailsFragment).setPalette()
        (homeFragment.fragment as DetailsFragment).appbar.setExpanded(true,true)
        return true
    }


    class  ProductPriceCalculation(val categorylist : CategoryList) {

        var attribute_cost :Double=0.0
        fun getprice( productListItem: ProductListItem):String {
            attribute_cost=0.0
            for(i in 0..productListItem.product_attribute.size -1){
                attribute_cost =attribute_cost + productListItem.product_attribute.get(i).default_attribute_value.a_price.toDouble()
            }
            return BindDataUtils.convertCurrencyToDanish(attribute_cost.toString()) ?: "null"
        }

    }




}