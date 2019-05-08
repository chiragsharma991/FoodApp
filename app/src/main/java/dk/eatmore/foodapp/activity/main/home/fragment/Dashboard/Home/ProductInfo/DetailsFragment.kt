package dk.eatmore.foodapp.fragment.ProductInfo

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.transition.ChangeBounds
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayFragment
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Info
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Menu
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Rating
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.RestaurantList
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentDetailBinding
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.fragment.Dashboard.Order.OrderedRestaurant
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.ModelUtility
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.*
import kotlinx.android.synthetic.main.fragment_detail.*
import retrofit2.Call
import java.io.Serializable


class DetailsFragment : CommanAPI() {


    lateinit var clickEvent: HomeFragment.MyClickHandler
    private var mAdapter: OrderListAdapter? = null
    var adapter: ViewPagerAdapter? = null
    private lateinit var binding: FragmentDetailBinding
    private lateinit var mYourBroadcastReceiver: BroadcastReceiver
    // private lateinit var restaurant : Restaurant
    private var call_category_menu: Call<JsonObject>? = null
    private var call_favorite: Call<JsonObject>? = null
    private var restaurant : Restaurant ? =null
    private var ordertype : String =""
    private var isShow = false
    private val myclickhandler = MyClickHandler(this)





    companion object {

        //delivery_present & pickup_present are two condition to show / hide .
        var delivery_present: Boolean = true
        var pickup_present: Boolean = true
        val TAG = "DetailsFragment"
        var total_cartcnt: Int = 0
        var canIrefreshpre_Function: Boolean = false
        var total_cartamt: String = "0"
        var delivery_charge_title: String = ""
        var delivery_charge: String = ""
        var isPickup:Boolean = false
        var delivery_text : String =""
        var pickup_text : String=""
        var is_restaurant_closed : Boolean =false
        var ui_model: UIModel? = null
        fun newInstance(status: String,ordertype : String,restaurant : Restaurant?): DetailsFragment {

            val fragment = DetailsFragment()
            val bundle = Bundle()
            bundle.putString(Constants.STATUS, status)
            bundle.putString(Constants.ORDERTYPE, ordertype)
            bundle.putSerializable(Constants.RESTAURANT, restaurant)
            fragment.arguments = bundle
            return fragment
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //  return inflater.inflate(getLayout(), container, false)

        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return binding.root

    }

    override fun getUserVisibleHint(): Boolean {
        loge(TAG, "getUserVisibleHint--")
        return super.getUserVisibleHint()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        loge(TAG, "setUserVisibleHint--" + isVisibleToUser)
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun getLayout(): Int {
        return R.layout.fragment_detail
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {


        if (savedInstanceState == null) {





          //  restaurant = arguments?.getSerializable(Constants.RESTAURANT) as Restaurant
            logd(DetailsFragment.TAG, "saveInstance NULL")
            binding.isUiprogress = true  // you are also comming back so no loader is required.
            binding.kstatus=PreferenceUtil.getBoolean(PreferenceUtil.KSTATUS, false)
            restaurant=arguments?.getSerializable(Constants.RESTAURANT) as Restaurant?
            ordertype=arguments?.getString(Constants.ORDERTYPE) as String
            viewcart.visibility = View.GONE  // By default viewcart should be gone.



            img_toolbar_back.setOnClickListener {
                onBackpress()
            }

           // App bar default text and background color.
           // badge_countprice.setTextColor(ContextCompat.getColor(context!!,R.color.theme_color))
           // addtocart_bascket.setColorFilter(ContextCompat.getColor(context!!,R.color.theme_color))
          //  viewcart.background=ContextCompat.getDrawable(context!!,R.drawable.rectangle_curvewhite_shape)
            appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {

                    //  Vertical offset == 0 indicates appBar is fully expanded.
                    if (Math.abs(verticalOffset) > 200) {
                        if(!isShow){
                            loge(TAG,"expanded >200---"+Math.abs(verticalOffset))
                         //   badge_countprice.setTextColor(ContextCompat.getColor(context!!,R.color.white))
                         //   addtocart_bascket.setColorFilter(ContextCompat.getColor(context!!,R.color.white))
                         //   viewcart.background=ContextCompat.getDrawable(context!!,R.drawable.rectangle_curve_shape)
                          //  txt_toolbar.text= ui_model?.category_menulist?.value?.restaurant_info?.restaurant_name ?: ""
                         //   img_toolbar_back.setColorFilter(ContextCompat.getColor(context!!,R.color.black_default))

                            isShow=true
                        }

                    } else {
                        if(isShow){
                            loge(TAG,"expanded true---"+Math.abs(verticalOffset))
                          //  badge_countprice.setTextColor(ContextCompat.getColor(context!!,R.color.theme_color))
                          //  addtocart_bascket.setColorFilter(ContextCompat.getColor(context!!,R.color.theme_color))
                          //  viewcart.background=ContextCompat.getDrawable(context!!,R.drawable.rectangle_curvewhite_shape)
                          //  txt_toolbar.text=""
                         //   img_toolbar_back.setColorFilter(ContextCompat.getColor(context!!,R.color.white))
                            isShow=false
                        }

                    }
                }
            })
            ui_model = createViewModel()
            fetch_category_menu()

        } else {
            logd(DetailsFragment.TAG, "saveInstance NOT NULL")
        }
    }





    class UIModel : ViewModel() {
        var category_menulist = MutableLiveData<RestaurantInfoModel>()  // this is list to show pre order
    }


    fun createViewModel(): DetailsFragment.UIModel =

            ViewModelProviders.of(this).get(DetailsFragment.UIModel::class.java).apply {
                category_menulist.removeObservers(this@DetailsFragment)

                category_menulist.observe(this@DetailsFragment, Observer<RestaurantInfoModel> {
                    CartListFunction.submitAllDiscount(category_menulist.value!!.menu,category_menulist.value!!.restaurant_info)
                    refreshview(category_menulist.value!!.restaurant_info!!)
                })
            }



    private fun refreshview(restaurant_info: Restaurant) {

        loge(TAG, "refresh---")
        favorite_btn.setImageResource(if(restaurant_info.is_fav) R.mipmap.heartfilled_white else R.mipmap.heart_white)
        binding.isUiprogress = false
        broadcastEvent(restaurant_info)
        delivery_present = restaurant_info.delivery_present
        pickup_present = restaurant_info.pickup_present
        isPickup= if(!delivery_present) true else false
        delivery_charge = restaurant_info.delivery_charge ?: "0"
        delivery_charge_title = restaurant_info.delivery_charge_title ?: "null"
        total_cartcnt = if (restaurant_info.cartcnt == null || restaurant_info.cartcnt == "0") 0 else restaurant_info.cartcnt!!.toInt()
        total_cartamt = if (restaurant_info.cartamt == null || restaurant_info.cartamt == "0") "00.00" else restaurant_info.cartamt.toString()
        delivery_text="Se leveringspriser"
        pickup_text=""
        updatebatchcount(0)
        binding.restaurant = restaurant_info
        binding.handler = myclickhandler

        val stars : LayerDrawable = rating.getProgressDrawable() as LayerDrawable
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        rating.rating=restaurant_info.total_rating

        binding.executePendingBindings()

        ImageLoader.loadImageRoundCornerFromUrl(context = context!!,cornerSize = 32,fromFile = restaurant_info.app_icon,imageView = imageview)

            // initialise all layouts and tab
            adapter = ViewPagerAdapter(childFragmentManager)
            if (((ui_model!!.category_menulist.value!!.is_restaurant_closed != null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true) &&
                            (ui_model!!.category_menulist.value!!.pre_order != null && ui_model!!.category_menulist.value!!.pre_order == false))
                    ||
                    (ui_model!!.category_menulist.value!!.is_restaurant_closed != null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true) && ui_model!!.category_menulist.value!!.pre_order == null
            ) {
                // closed restaurant---
                is_restaurant_closed=true
                adapter!!.addFragment(Menu.newInstance(ui_model!!.category_menulist.value!!.menu!!, restaurant_info), getString(R.string.menu))
                adapter!!.addFragment(Rating.newInstance(restaurant_info), getString(R.string.rating))
                adapter!!.addFragment(Info.newInstance(restaurant_info), getString(R.string.info))
                viewpager.offscreenPageLimit = 2
                viewpager.setAdapter(adapter)
                //viewpager.setCurrentItem(1, true)
                DialogUtils.openDialogDefault(context = context!!,btnNegative = "Se menukort",btnPositive = String.format(getString(R.string.find_andet_take_away),restaurant_info.city),
                        color = ContextCompat.getColor(context!!, R.color.theme_color),msg ="",
                        title ="We are closed today. Please check opening hours",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                    override fun onPositiveButtonClick(position: Int) {
                        //back press
                        // canIrefreshpre_Function=true
                        onBackpress()
                    }
                    override fun onNegativeButtonClick() {
                        //  canIrefreshpre_Function=true
                        // dismiss
                    }
                })


            } else if((ui_model!!.category_menulist.value!!.is_restaurant_closed != null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true) &&
                    (ui_model!!.category_menulist.value!!.pre_order != null && ui_model!!.category_menulist.value!!.pre_order == true)) {
                // preorder Restaurant---
                is_restaurant_closed=false
                adapter!!.addFragment(Menu.newInstance(ui_model!!.category_menulist.value!!.menu!!, restaurant_info), getString(R.string.menu))
                adapter!!.addFragment(Rating.newInstance(restaurant_info), getString(R.string.rating))
                adapter!!.addFragment(Info.newInstance(restaurant_info), getString(R.string.info))
                viewpager.offscreenPageLimit = 3
                viewpager.setAdapter(adapter)
                DialogUtils.openDialogDefault(context = context!!,btnNegative = "FORUDBESTIL NU",btnPositive = String.format(getString(R.string.find_andet_take_away),restaurant_info.city),
                        color = ContextCompat.getColor(context!!, R.color.theme_color),msg ="\nRestauranten ${restaurant_info.opening_title} ${restaurant_info.time}\n",
                        title = "Denne restaurant har desværre lukket lige nu",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                    override fun onPositiveButtonClick(position: Int) {
                        //back press
                        //    canIrefreshpre_Function=true
                        onBackpress()
                    }
                    override fun onNegativeButtonClick() {
                        //    canIrefreshpre_Function=true
                        // dismiss
                    }
                })

            }else{
                // Open Restaurant---
                is_restaurant_closed=false
                adapter!!.addFragment(Menu.newInstance(ui_model!!.category_menulist.value!!.menu!!, restaurant_info), getString(R.string.menu))
                adapter!!.addFragment(Rating.newInstance(restaurant_info), getString(R.string.rating))
                adapter!!.addFragment(Info.newInstance(restaurant_info), getString(R.string.info))
                viewpager.offscreenPageLimit = 3
                viewpager.setAdapter(adapter)
            }
            tabs.setupWithViewPager(viewpager)
            viewcart.setOnClickListener {
                loge(TAG,"view pager on click--")
                //   if(total_cartcnt==0) return@setOnClickListener

                val fragment = EpayFragment.newInstance(restaurant_info,ui_model!!.category_menulist.value!!.menu!!)
                var enter: Slide? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    enter = Slide()
                    enter.setDuration(300)
                    enter.slideEdge = Gravity.BOTTOM
                    val changeBoundsTransition: ChangeBounds = ChangeBounds()
                    changeBoundsTransition.duration = 300
                   // fragment.sharedElementEnterTransition = changeBoundsTransition
                 //   fragment.enterTransition = enter
                }

                if ((activity as HomeActivity).fragmentTab_is() == 1)
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().addFragment(R.id.home_order_container, fragment, EpayFragment.TAG, true)
                else
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().addFragment(R.id.home_fragment_container, fragment, EpayFragment.TAG, true)
            }







    }


    fun fetch_category_menu() {

        binding.isUiprogress = true  // you are also comming back so no loader is required.
        val postParam = JsonObject()
        postParam.addProperty(Constants.R_TOKEN_N, PreferenceUtil.getString(PreferenceUtil.R_TOKEN, ""))
        postParam.addProperty(Constants.R_KEY_N, PreferenceUtil.getString(PreferenceUtil.R_KEY, ""))
        postParam.addProperty(Constants.SHIPPING, if (isPickup) context!!.getString(R.string.pickup_) else context!!.getString(R.string.delivery_)) // set delivery default method
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
                val restaurantInfoModel = GsonBuilder().create().fromJson(response.toString(), RestaurantInfoModel::class.java)
                if (restaurantInfoModel.status) {
                    ui_model!!.category_menulist.value = restaurantInfoModel
                    loge(TAG," result --"+ui_model!!.category_menulist.value!!.restaurant_info!!.offer_details.toString())
                }
            }

            override fun onFail(error: Int) {
                binding.isUiprogress = false

                if (call_category_menu!!.isCanceled) {
                    return
                }

                when (error) {
                    404 -> {
                        showSnackBar(viewpager, getString(R.string.error_404))
                    }
                    100 -> {

                        showSnackBar(viewpager, getString(R.string.internet_not_available))
                    }
                }
            }
        })


    }




    private fun broadcastEvent(restaurant_info: Restaurant) {

        if (!::mYourBroadcastReceiver.isInitialized) {
            loge(TAG,"broadcastEvent--")
            mYourBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    loge(TAG, "broadcast receive..."+ui_model?.category_menulist?.value?.restaurant_info!!.test)
                    if (intent!!.action == Constants.CARTCOUNT_BROADCAST) {
                        restaurant_info.cartcnt = intent.extras.getInt(Constants.CARTCNT).toString()
                        restaurant_info.cartamt = intent.extras.getString(Constants.CARTAMT).toString()
                        loge(TAG, "new model cartcnt is..." + restaurant_info.cartcnt)
                        total_cartcnt = intent.extras.getInt(Constants.CARTCNT)
                        total_cartamt = intent.extras.getString(Constants.CARTAMT)
                        updatebatchcount(0)

                        // check if category screen is present then update therir batch count as well as this screen.
                        if ((activity as HomeActivity).fragmentTab_is() == 1) {
                            val fragment = ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().childFragmentManager.findFragmentByTag(CategoryList.TAG)
                            if (fragment != null) {
                                (fragment as CategoryList).updatebatchcount(0)
                            }
                        } else {
                            val homefragment: HomeFragment = ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment()
                            val fragment = homefragment.childFragmentManager.findFragmentByTag(CategoryList.TAG)
                            if (fragment != null) {
                                (fragment as CategoryList).updatebatchcount(0)
                            }
                        }


                    }
                }

            }
            LocalBroadcastManager.getInstance(activity!!).registerReceiver(mYourBroadcastReceiver, IntentFilter(Constants.CARTCOUNT_BROADCAST))
        }
    }

    fun updatebatchcount(count: Int) {
        // this is update method will call in both category and details.
        try {
            total_cartcnt = total_cartcnt + count
            //badge_notification_txt.visibility = View.GONE
            if ((ui_model!!.category_menulist.value!!.is_restaurant_closed != null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true) &&
                    (ui_model!!.category_menulist.value!!.pre_order != null && ui_model!!.category_menulist.value!!.pre_order == false)) {
                viewcart.visibility = View.GONE

            } else {
                viewcart.visibility = if (total_cartcnt == 0) View.GONE else View.VISIBLE
            }
            //  badge_notification_txt.text= total_cartcnt.toString()
            if (total_cartcnt == 0 || total_cartcnt == 1)
                badge_countprice.text = BindDataUtils.convertCurrencyToDanish(total_cartamt)
            else
                badge_countprice.text = String.format(getString(R.string.count_ammount), total_cartcnt, BindDataUtils.convertCurrencyToDanish(total_cartamt))
        } catch (e: Exception) {
            loge(TAG, "exception: - " + e.message)
        }

    }


    fun onBackpress() {
        // parentFragment!!.childFragmentManager.popBackStack()
        showTabBar(true)
        //-- Home fragment--
        if (parentFragment is HomeFragment) {
            val restaurantList = (parentFragment as HomeFragment).childFragmentManager.findFragmentByTag(RestaurantList.TAG)
            if (restaurantList != null && canIrefreshpre_Function) (restaurantList as RestaurantList).fetch_ProductDetailList() // if some one restaurant immediate closed then refresh current.
            canIrefreshpre_Function = false
            // this for re-order
            if(HomeFragment.isFrom == HomeFragment.IsFrom.ORDER || HomeFragment.isFrom == HomeFragment.IsFrom.PROFILE ){
                if(OrderFragment.ui_model?.reloadfragment !=null) OrderFragment.ui_model!!.reloadfragment.value=true   // every time refresh :  order fragment
                ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(if(HomeFragment.isFrom == HomeFragment.IsFrom.ORDER) 1 else 2 , 800) // if you are from reorder then go there.
                HomeFragment.isFrom = HomeFragment.IsFrom.HOME
                (parentFragment as HomeFragment).childFragmentManager.popBackStack()

            }else{
                (parentFragment as HomeFragment).childFragmentManager.popBackStack()
            }

            //--order fragmnt--
        } else {
            (parentFragment as OrderFragment).childFragmentManager.popBackStack()
            // call order and ordered screeen again
            val orderedfragment = (parentFragment as OrderFragment).childFragmentManager.findFragmentByTag(OrderedRestaurant.TAG)
            if(orderedfragment !=null){
              //  (orderedfragment as OrderedRestaurant).fetchRestaurant_info()
            }else{
                if(OrderFragment.ui_model?.reloadfragment !=null) OrderFragment.ui_model!!.reloadfragment.value=true   // every time refresh :  order fragment
            }
        }
    }



    fun favourite(){
        val restaurant_info= ui_model!!.category_menulist.value!!.restaurant_info
        val postParam = JsonObject()
        postParam.addProperty(Constants.AUTH_KEY, Constants.AUTH_VALUE)
        postParam.addProperty(Constants.EATMORE_APP, true)
        postParam.addProperty(Constants.CUSTOMER_ID, PreferenceUtil.getString(PreferenceUtil.CUSTOMER_ID,""))      // if restaurant is closed then
        postParam.addProperty(Constants.RESTAURANT_ID,restaurant_info!!.restaurant_id)
        if(restaurant_info.is_fav){
            // unfavourite--
            DialogUtils.openDialog(context = context!!,btnNegative = getString(R.string.no) , btnPositive = getString(R.string.yes),color = ContextCompat.getColor(context!!, R.color.theme_color),msg = getString(R.string.vil_du_fjerne),title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                override fun onPositiveButtonClick(position: Int) {
                    call_favorite = ApiCall.remove_favorite_restaurant(jsonObject = postParam)
                    remove_favorite_restaurant(call_favorite!!,restaurant_info)
                }
                override fun onNegativeButtonClick() {

                }
            })
        }else{
            // favourite---
            call_favorite = ApiCall.add_favorite_restaurant(jsonObject = postParam)
            setfavorite(call_favorite!!,restaurant_info)
        }
    }

    override fun comman_apisuccess(jsonObject: JsonObject, api_tag: String) {
        updatefavourite()
    }

    override fun comman_apifailed(error: String, api_tag: String) {
        updatefavourite()
    }


    fun updatefavourite(){

        val restaurant_info= ui_model!!.category_menulist.value!!.restaurant_info
        if(restaurant_info!!.is_fav){
            favorite_btn.setImageResource(R.mipmap.heartfilled_white)
        }else{
            favorite_btn.setImageResource(R.mipmap.heart_white)
        }
        // update in previous restaurant list.
        if(restaurant != null && parentFragment is HomeFragment ){
            loge(TAG,"restaurant != null---")
            restaurant!!.is_fav=restaurant_info.is_fav
            val homefragment=((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment()
            val restaurantlist = homefragment.childFragmentManager.findFragmentByTag(RestaurantList.TAG) as RestaurantList
            if(restaurantlist !=null){
                restaurantlist.updatefavourite()
            }
        }else{
            loge(TAG,"restaurant == null---")

        }


    }



    override fun onDestroy() {
        super.onDestroy()

        loge(TAG, "on destroy...")


    }

    override fun onDetach() {
        super.onDetach()
        loge(TAG, "on detech...")

    }

    override fun onDestroyView() {
        loge(TAG, "onDestroyView...")
        super.onDestroyView()

        ui_model?.let {
            ViewModelProviders.of(this).get(DetailsFragment.UIModel::class.java).category_menulist.removeObservers(this@DetailsFragment)
        }

        call_category_menu?.let {
            it.cancel()
        }

        if (::mYourBroadcastReceiver.isInitialized) {
            LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(mYourBroadcastReceiver);
        }

        if (call_favorite != null) {
            call_favorite!!.cancel()
        }


    }


    override fun onPause() {
        super.onPause()
        loge(TAG, "on pause...")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loge(TAG, "on create...")
        //  broadcastEvent()

    }

    override fun onStart() {
        super.onStart()
        logd(TAG, "on start...")

    }

    override fun onResume() {
        super.onResume()
        loge(TAG, "on resume...")

    }

    override fun onStop() {
        super.onStop()
        loge(TAG, "on stop...")
    }

    data class RestaurantInfoModel(val msg: String? = null,
                                   val cartcnt: String = "",
                                   val is_user_deleted: Boolean = false,
                                   val is_restaurant_closed: Boolean? = null,
                                   val pre_order: Boolean? = null,
                                   val image_path: String = "",
                                   val product_image_thumbnail_path: String = "",
                                   val time: String = "",
                                   val menu: ArrayList<MenuListItem>? = null,
                                   val restaurant_info: Restaurant? = null,
                                   val status: Boolean = false) : ModelUtility(), Serializable


    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList.get(position)
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList.get(position)
        }

    }



    class MyClickHandler(val detailsfragment: DetailsFragment) {


        fun tapOnRating(view: View) {
            Log.e(TAG, "click ---")
            detailsfragment.tapOnRating()
        }
        fun tapOnfavourite(view: View) {
            Log.e(TAG, "taponfavourite ---")
            detailsfragment.favourite()
        }


    }

    private fun tapOnRating() {

        if ((ui_model!!.category_menulist.value!!.is_restaurant_closed != null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true) &&
                (ui_model!!.category_menulist.value!!.pre_order != null && ui_model!!.category_menulist.value!!.pre_order == false)) {
            // closed restaurant---
            viewpager.setCurrentItem(0, true)
        } else {
            viewpager.setCurrentItem(1, true)
        }

    }




}





