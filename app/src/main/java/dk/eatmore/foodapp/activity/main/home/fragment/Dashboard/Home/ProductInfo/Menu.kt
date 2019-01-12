package dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseFragment
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickListner
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.MenuRestaurantBinding
import dk.eatmore.foodapp.databinding.RowMenuRestaurantBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.fragment.ProductInfo.CategoryList
import dk.eatmore.foodapp.model.home.*
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.menu_restaurant.*
import java.io.Serializable
import java.util.ArrayList
import android.text.Layout




class Menu : BaseFragment(), RecyclerClickListner {


    private lateinit var binding: MenuRestaurantBinding
    private var mAdapter: UniversalAdapter<MenuListItem, RowMenuRestaurantBinding>? = null
    private lateinit var homeFragment: HomeFragment
    private val list = ArrayList<User>()
   // private  var ui_model: UIModel?=null
    private lateinit var restaurant : Restaurant
    private lateinit var menuListItem : ArrayList<MenuListItem>





    companion object {

        val TAG = "Menu"

        fun newInstance(menulistitem : ArrayList<MenuListItem>,restaurant : Restaurant): Menu {
            val fragment = Menu()
            val bundle = Bundle()
            bundle.putSerializable(Constants.MENULISTITEM,menulistitem)
            bundle.putSerializable(Constants.RESTAURANT,restaurant)
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun getLayout(): Int {
        return R.layout.menu_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return inflater.inflate(getLayout(), container, false)

        binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            logd(TAG,"saveInstance NULL")
            progress_bar.visibility=View.GONE
            restaurant= arguments!!.getSerializable(Constants.RESTAURANT) as Restaurant
            menuListItem= arguments!!.getSerializable(Constants.MENULISTITEM) as ArrayList<MenuListItem>
            binding.restaurant=restaurant
            binding.executePendingBindings()
            freetext_function()
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            homeFragment=(fragmentof as HomeContainerFragment).getHomeFragment()
            menu_search.setOnClickListener{


                logd(TAG,"click---")
                val fragment = SearchMenu.newInstance(menuListItem)
                /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                     *//*             // 2. Shared Elements Transition
                                     val enterTransitionSet = TransitionSet()
                                     enterTransitionSet.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.move))
                                     enterTransitionSet.duration = 1000
                                     enterTransitionSet.startDelay = 300
                                     fragment.setSharedElementEnterTransition(enterTransitionSet)*//*

                        // 3. Enter Transition for New Fragment
//                        val enterFade = Fade()
//                        enterFade.setStartDelay(0)
//                        enterFade.setDuration(300)
//                        fragment.setEnterTransition(enterFade)


                        homeFragment.childFragmentManager.beginTransaction().add(R.id.home_fragment_container,fragment,SearchMenu.TAG).addToBackStack(SearchMenu.TAG)
                                //      .addSharedElement(search_card, getString(R.string.transition_string))
                                .commit()
                    }
                    else {
                        homeFragment.addFragment(R.id.home_fragment_container,fragment, SearchMenu.TAG,false)
                    }*/
                if((activity as HomeActivity).fragmentTab_is() == 1)
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().addFragment(R.id.home_order_container,fragment, SearchMenu.TAG,false)
                else
                    homeFragment.addFragment(R.id.home_fragment_container,fragment, SearchMenu.TAG,false)




            }
         //   fetch_ProductList()
            refreshUI()

        }else{
            logd(TAG,"saveInstance NOT NULL")

        }

    }

    private fun freetext_function() {

        expandtxt_btn.visibility= View.GONE

        Handler().postDelayed({
            loge(TAG, "addOnGlobalLayoutListener--")

            val layout = free_txt.layout
            if (layout != null) {
                val lines = free_txt.getLineCount()
                if (lines > 0) {
                    val ellipsisCount = layout.getEllipsisCount(lines - 1)
                    if (ellipsisCount > 0) {
                        expandtxt_btn.visibility= View.VISIBLE
                    }else{
                        expandtxt_btn.visibility= View.GONE
                    }
                }
            }

        },100)

        var isTextViewClicked = false
        free_txt_view.setOnClickListener{

            if(isTextViewClicked){
                //This will shrink textview to 2 lines if it is expanded.
                free_txt.maxLines =1;
                expandtxt_btn.setImageResource(R.drawable.down_arrow)
                expandtxt_btn.setColorFilter(ContextCompat.getColor(context!!,R.color.black_default),PorterDuff.Mode.SRC_IN)
                isTextViewClicked = false;
            } else {
                //This will expand the textview if it is of 2 lines
                free_txt.setMaxLines(Integer.MAX_VALUE);
                expandtxt_btn.setImageResource(R.drawable.up_arrow)
                expandtxt_btn.setColorFilter(ContextCompat.getColor(context!!,R.color.black_default),PorterDuff.Mode.SRC_IN)
                isTextViewClicked = true;
            }
        }

    }

/*    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                productList.observe(this@Menu, Observer<ArrayList<MenuListItem>> {
                    refreshUI()
                })
            }*/



    private fun refreshUI() {
        //       val myclickhandler = Profile.MyClickHandler(this)
//        val xml_profile = ui_model.getUIModel().value
//        binding.xmlProfile = xml_profile
//        binding.handlers=myclickhandler



        mAdapter = UniversalAdapter(context!!, menuListItem, R.layout.row_menu_restaurant, object : RecyclerCallback<RowMenuRestaurantBinding, MenuListItem> {
            override fun bindData(binder: RowMenuRestaurantBinding, model: MenuListItem) {
                setRecyclerData(binder, model)
            }
        })
        recycler_view_menu.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view_menu.adapter = mAdapter


    }



    /*private fun fetch_ProductList() {
        progress_bar.visibility=View.VISIBLE
        callAPI(ApiCall.getProductList(
                r_token = PreferenceUtil.getString(PreferenceUtil.R_TOKEN,"")!!,
                r_key = PreferenceUtil.getString(PreferenceUtil.R_KEY,"")!!,
                customer_id = PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,"")!!
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val productlistmodel= body as ProductListModel
                if (productlistmodel.status) {
                loge(TAG," menu list size"+""+productlistmodel.menu!!.get(1).c_name+" "+productlistmodel.menu.get(1).product_list!!.size)
                    ui_model!!.productList.value=productlistmodel.menu
                }
                progress_bar.visibility=View.GONE
            }
            override fun onFail(error: Int) {
                when (error) {
                    404 -> {
                        showSnackBar(clayout_menu, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout_menu, getString(R.string.internet_not_available))
                    }
                }
                progress_bar.visibility=View.GONE
                //showProgressDialog()
            }
        })


    }*/

    override fun<T> onClick(model: T?) {

        val data= model as MenuListItem
        /*    (parentFragment as DetailsFragment).appbar.setExpanded(false,true)
            (parentFragment as DetailsFragment).collapse_toolbar.setBackgroundColor(ContextCompat.getColor(context!!,R.color.white));
            (parentFragment as DetailsFragment).collapse_toolbar.setStatusBarScrimColor(ContextCompat.getColor(context!!,R.color.white))
            (parentFragment as DetailsFragment).collapse_toolbar.setContentScrimColor(ContextCompat.getColor(context!!,R.color.white))*/

        val fragment = CategoryList.newInstance(restaurant,data)
        var enter :Slide?=null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(300)
            enter.slideEdge = Gravity.RIGHT
            val changeBoundsTransition :ChangeBounds = ChangeBounds()
            changeBoundsTransition.duration = 300
            fragment.sharedElementEnterTransition=changeBoundsTransition
            fragment.enterTransition=enter
        }
        if((activity as HomeActivity).fragmentTab_is() == 1)
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().addFragment(R.id.home_order_container,fragment, CategoryList.TAG,false)
        else
            homeFragment.addFragment(R.id.home_fragment_container,fragment,CategoryList.TAG,false)

    }


    private fun setRecyclerData(binder: RowMenuRestaurantBinding, model: MenuListItem) {
        binder.data=model
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

  /*  class UIModel : ViewModel() {

        var productList = MutableLiveData<ArrayList<MenuListItem>>()


    }*/



    data class FilterCategoryList(
            val c_name: String = "",
            val product_list: ArrayList<ProductListItem> = arrayListOf()) : Serializable


    data class ProductListItem(
            val p_desc: String = "",
            val p_price: String = "",
            val p_name: String = "") :Serializable

}








