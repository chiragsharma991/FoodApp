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
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
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
import kotlinx.android.synthetic.main.menu_restaurant.*
import java.io.Serializable
import android.text.Layout
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dk.eatmore.foodapp.fragment.ProductInfo.DetailsFragment
import dk.eatmore.foodapp.utils.*
import retrofit2.Call


class Menu : BaseFragment(), RecyclerClickListner {


    private lateinit var binding: MenuRestaurantBinding
    private var mAdapter: UniversalAdapter<MenuListItem, RowMenuRestaurantBinding>? = null
    private lateinit var homeFragment: HomeFragment
    private val list = ArrayList<User>()
   // private  var ui_model: UIModel?=null
    private lateinit var restaurant : Restaurant
    private lateinit var menuListItem : ArrayList<MenuListItem>
    private var tablistner: TabLayout.OnTabSelectedListener? =null
    private var call_category_menu: Call<JsonObject>? = null







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
        loge(TAG, "create view...")
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
            freetext_function()
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            homeFragment=(fragmentof as HomeContainerFragment).getHomeFragment()
            menu_search.setOnClickListener{
                if(progress_bar.visibility==View.GONE){
                    val fragment = SearchMenu.newInstance(menuListItem)
                    if((activity as HomeActivity).fragmentTab_is() == 1)
                        ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().addFragment(R.id.home_order_container,fragment, SearchMenu.TAG,false)
                    else
                        homeFragment.addFragment(R.id.home_fragment_container,fragment, SearchMenu.TAG,false)
                }
            }
            tabfunction()
            refreshUI()

        }else{
            logd(TAG,"saveInstance NOT NULL")

        }

    }




    fun tabfunction() {

        menu_tabs.removeAllTabs()
        info_outline_img.setOnClickListener(null)
        tablistner?.let { menu_tabs.removeOnTabSelectedListener(it)  }


        if(!DetailsFragment.delivery_present && DetailsFragment.pickup_present){
            menu_tabs.addTab(menu_tabs.newTab().setText(getString(R.string.pickup)))
            DetailsFragment.isPickup=true
        } else if(DetailsFragment.delivery_present && !DetailsFragment.pickup_present){
            menu_tabs.addTab(menu_tabs.newTab().setText(if(DetailsFragment.delivery_charge_title=="") getString(R.string.delivery)+" "+(if(DetailsFragment.delivery_charge.trim() == "") "" else "\n"+BindDataUtils.convertCurrencyToDanish(DetailsFragment.delivery_charge))
                                                        else getString(R.string.delivery)+"\n"+ DetailsFragment.delivery_charge_title+" "+BindDataUtils.convertCurrencyToDanish(DetailsFragment.delivery_charge) ))
            DetailsFragment.isPickup=false
        }else{
           // menu_tabs.addTab(menu_tabs.newTab().setText((if(DetailsFragment.delivery_charge_title=="") getString(R.string.delivery) else getString(R.string.delivery)+"\n"+ DetailsFragment.delivery_charge_title)+" "+ BindDataUtils.convertCurrencyToDanish(DetailsFragment.delivery_charge)))
            menu_tabs.addTab(menu_tabs.newTab().setText(if(DetailsFragment.delivery_charge_title=="") getString(R.string.delivery)+" "+(if(DetailsFragment.delivery_charge.trim() == "") "" else "\n"+BindDataUtils.convertCurrencyToDanish(DetailsFragment.delivery_charge))
            else getString(R.string.delivery)+"\n"+ DetailsFragment.delivery_charge_title+" "+BindDataUtils.convertCurrencyToDanish(DetailsFragment.delivery_charge) ))
            menu_tabs.addTab(menu_tabs.newTab().setText(getString(R.string.pickup)))
            DetailsFragment.isPickup=false
        }

        if(DetailsFragment.isPickup){
            info_outline_img.visibility=View.GONE
            binding.pickupDeliveryTxt = DetailsFragment.pickup_text
        } else {
            info_outline_img.visibility=View.VISIBLE
            binding.pickupDeliveryTxt = DetailsFragment.delivery_text
        }

        info_outline_img.setOnClickListener{ CartListFunction.showDialog(restaurant = restaurant,context = context!!)}

        var isAnyexecution : Boolean = true // when you set tab programmatically: you can get event multiple time.

        tablistner = object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(menu_tabs.selectedTabPosition){
                    0->{
                        //delivery
                        if(isAnyexecution){

                            if(DetailsFragment.total_cartcnt > 0){
                                // alert for total_cartcnt > 0
                                DialogUtils.openDialogDefault(context = context!!,btnNegative = getString(R.string.cancel),btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.theme_color),msg = getString(R.string.you_are_about_to_order_your_food_for_delivery_one_or_more),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                                    override fun onPositiveButtonClick(position: Int) {
                                        DetailsFragment.isPickup=false
                                        info_outline_img.visibility=View.VISIBLE
                                        binding.pickupDeliveryTxt = DetailsFragment.delivery_text.also { binding.executePendingBindings() }
                                        Refetch_category_menu()
                                    }
                                    override fun onNegativeButtonClick() {
                                        isAnyexecution=false
                                        menu_tabs.getTabAt(1)!!.select()
                                    }
                                })

                            }else{
                                // no alert for total_cartcnt < 0
                                DetailsFragment.isPickup=false
                                info_outline_img.visibility=View.VISIBLE
                                binding.pickupDeliveryTxt = DetailsFragment.delivery_text.also { binding.executePendingBindings() }
                                Refetch_category_menu()
                            }

                        }else{
                            isAnyexecution=true
                        }
                    }

                    1->{
                        // pickup
                        if(isAnyexecution){

                            if(DetailsFragment.total_cartcnt > 0){

                                DialogUtils.openDialogDefault(context = context!!,btnNegative = getString(R.string.cancel),btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.theme_color),msg = getString(R.string.you_are_about_to_order_your_food_for_pickup_one_or_more),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                                    override fun onPositiveButtonClick(position: Int) {
                                        DetailsFragment.isPickup=true
                                        info_outline_img.visibility=View.GONE
                                        binding.pickupDeliveryTxt = DetailsFragment.pickup_text.also { binding.executePendingBindings() }
                                        Refetch_category_menu()
                                    }
                                    override fun onNegativeButtonClick() {
                                        isAnyexecution=false
                                        menu_tabs.getTabAt(0)!!.select()
                                    }
                                })

                            }else{
                                DialogUtils.openDialogDefault(context = context!!,btnNegative = getString(R.string.cancel),btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.theme_color),msg = getString(R.string.you_are_ordering_pickup),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                                    override fun onPositiveButtonClick(position: Int) {
                                        DetailsFragment.isPickup=true
                                        info_outline_img.visibility=View.GONE
                                        binding.pickupDeliveryTxt = DetailsFragment.pickup_text.also { binding.executePendingBindings() }
                                        Refetch_category_menu()
                                    }
                                    override fun onNegativeButtonClick() {
                                        isAnyexecution=false
                                        menu_tabs.getTabAt(0)!!.select()
                                    }
                                })
                            }

                        }else{
                            isAnyexecution=true
                        }
                    }

                }

            }
        }

        menu_tabs.addOnTabSelectedListener(tablistner!!)

        binding.executePendingBindings()

    }


    fun Refetch_category_menu() {
        progress_bar.visibility=View.VISIBLE
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.SHIPPING, if (DetailsFragment.isPickup) context!!.getString(R.string.pickup_) else context!!.getString(R.string.delivery_)) // set delivery default method
        if (PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)) {
            postParam.addProperty(Constants.IS_LOGIN, "1")
            postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID, ""))
        } else {
            postParam.addProperty(Constants.IS_LOGIN, "0")
        }
        postParam.addProperty(Constants.IP, PreferenceUtil.getString(PreferenceUtil.DEVICE_TOKEN, ""))
        postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
        postParam.addProperty(Constants.LANGUAGE, Constants.DA)

        //  progress_bar_layout.visibility=View.VISIBLE
        call_category_menu = ApiCall.category_menu(postParam)
        callAPI(call_category_menu!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val response = body as JsonObject
                val restaurantInfoModel = GsonBuilder().create().fromJson(response.toString(), DetailsFragment.RestaurantInfoModel::class.java)
                if (restaurantInfoModel.status) {
                    menuListItem = restaurantInfoModel.menu!!
                    restaurant = restaurantInfoModel.restaurant_info!!
                    CartListFunction.submitAllDiscount(menuListItem,restaurant)
                    refreshUI()
                }
            }

            override fun onFail(error: Int) {
                progress_bar.visibility=View.GONE

                if (call_category_menu!!.isCanceled) {
                    return
                }

                when (error) {
                    404 -> {
                        showSnackBar(clayout_menu, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(clayout_menu, getString(R.string.internet_not_available))
                    }
                }
            }
        })

    }


    private fun freetext_function() {

        expandtxt_btn.visibility= View.GONE

        Handler().postDelayed({

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




    private fun refreshUI() {
        progress_bar.visibility=View.GONE
        mAdapter = UniversalAdapter(context!!, menuListItem, R.layout.row_menu_restaurant, object : RecyclerCallback<RowMenuRestaurantBinding, MenuListItem> {
            override fun bindData(binder: RowMenuRestaurantBinding, model: MenuListItem) {
                setRecyclerData(binder, model)
            }
        })
        recycler_view_menu.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view_menu.adapter = mAdapter
    }


    override fun<T> onClick(model: T?) {

        if(progress_bar.visibility==View.GONE){
            val data= model as MenuListItem
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
    }


    private fun setRecyclerData(binder: RowMenuRestaurantBinding, model: MenuListItem) {
        binder.data=model
        binder.handler=this
    }


    override fun onDestroyView() {
        super.onDestroyView()
        logd(TAG, "on onDestroyView...")

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





}








