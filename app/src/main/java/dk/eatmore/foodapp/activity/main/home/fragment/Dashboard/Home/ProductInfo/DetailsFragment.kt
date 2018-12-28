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
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.graphics.Palette
import android.transition.ChangeBounds
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
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
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.ModelUtility
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.BaseFragment
import dk.eatmore.foodapp.utils.BindDataUtils
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.toolbar_plusone.*
import retrofit2.Call
import java.io.Serializable


class DetailsFragment : BaseFragment() {

    lateinit var clickEvent: HomeFragment.MyClickHandler
    private var mAdapter: OrderListAdapter? = null
    var adapter: ViewPagerAdapter? = null
    private lateinit var binding: FragmentDetailBinding
    private var canIrefreshpre_Function : Boolean =false
    private lateinit var mYourBroadcastReceiver: BroadcastReceiver
   // private lateinit var restaurant : Restaurant
    private var call_category_menu  : Call<JsonObject>? =null




    companion object {

        //delivery_present & pickup_present are two condition to show / hide .
        var delivery_present : Boolean=true
        var pickup_present : Boolean=true
        val TAG = "DetailsFragment"
        var total_cartcnt : Int = 0
        var total_cartamt : String = ""
        var delivery_charge_title : String = ""
        var delivery_charge : String = ""
        var ui_model: UIModel? = null
        fun newInstance( status: String): DetailsFragment {

            val fragment = DetailsFragment()
            val bundle = Bundle()
            bundle.putString(Constants.STATUS, status)
           // bundle.putSerializable(Constants.RESTAURANT, restaurant)
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
        loge(TAG,"getUserVisibleHint--")
        return super.getUserVisibleHint()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        loge(TAG,"setUserVisibleHint--"+isVisibleToUser)

        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun getLayout(): Int {
        return R.layout.fragment_detail
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {


        if (savedInstanceState == null) {
          //  restaurant = arguments?.getSerializable(Constants.RESTAURANT) as Restaurant
            binding.isUiprogress=true  // you are also comming back so no loader is required.
            toolbar_badge_view.visibility= View.GONE  // By default viewcart should be gone.
            logd(DetailsFragment.TAG, "saveInstance NULL")
            img_toolbar_back.setImageResource(R.drawable.close)
            img_toolbar_back.setOnClickListener {
                onBackpress()
            }
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
                    refreshview(category_menulist.value!!.restaurant_info!!)

                })
            }


    private fun refreshview(restaurant_info: Restaurant) {

        loge(TAG,"refresh---")
        binding.isUiprogress=false
        broadcastEvent(restaurant_info)
        delivery_present=restaurant_info.delivery_present
        pickup_present=restaurant_info.pickup_present
        delivery_charge= restaurant_info.delivery_charge?:"0"
        delivery_charge_title=restaurant_info.delivery_charge_title?:"null"
        total_cartcnt =if(restaurant_info.cartcnt ==null || restaurant_info.cartcnt =="0") 0 else restaurant_info.cartcnt!!.toInt()
        total_cartamt =if(restaurant_info.cartamt ==null || restaurant_info.cartamt =="0") "00.00" else restaurant_info.cartamt.toString()
        updatebatchcount(0)
        val myclickhandler = MyClickHandler(this)
        binding.restaurant = restaurant_info
        binding.handler = myclickhandler

        Glide.with(context!!).load(restaurant_info.app_icon).into(imageview);
            adapter = ViewPagerAdapter(childFragmentManager)

        if((ui_model!!.category_menulist.value!!.is_restaurant_closed !=null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true) &&
           (ui_model!!.category_menulist.value!!.pre_order !=null && ui_model!!.category_menulist.value!!.pre_order == false)){
            // closed restaurant---



            if(arguments!!.getString(Constants.STATUS) == getString(R.string.open_now)){
                /*TODO  if user is coming from open restaurant and then restaurent suddenly closed then:*/
                val msg =ui_model!!.category_menulist.value!!.msg ?: ""
                DialogUtils.openDialogDefault(context = context!!,btnNegative = "",btnPositive = getString(R.string.ok),color = ContextCompat.getColor(context!!, R.color.black),msg = msg,title = "",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                    override fun onPositiveButtonClick(position: Int) {
                       // viewpager.setCurrentItem(1,true)
                        canIrefreshpre_Function=true
                    }
                    override fun onNegativeButtonClick() {
                    }
                })
            }

            adapter!!.addFragment(Rating.newInstance(restaurant_info), getString(R.string.rating))
            adapter!!.addFragment(Info.newInstance(restaurant_info), getString(R.string.info))
            viewpager.offscreenPageLimit = 2
            viewpager.setAdapter(adapter)
            viewpager.setCurrentItem(1,true)

        }else{
            // open and preorder Restaurant---
            adapter!!.addFragment(Menu.newInstance(ui_model!!.category_menulist.value!!.menu!!,restaurant_info), getString(R.string.menu))
            adapter!!.addFragment(Rating.newInstance(restaurant_info), getString(R.string.rating))
            adapter!!.addFragment(Info.newInstance(restaurant_info), getString(R.string.info))
            viewpager.offscreenPageLimit = 3
            viewpager.setAdapter(adapter)

        }

            tabs.setupWithViewPager(viewpager)
            //  setPalette()
            viewcart.setOnClickListener {
             //   if(total_cartcnt==0) return@setOnClickListener

                val fragment = EpayFragment.newInstance(restaurant_info)
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
                    ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment().addFragment(R.id.home_fragment_container,fragment,EpayFragment.TAG,false)
            }

    }


     fun fetch_category_menu() {

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
         postParam.addProperty(Constants.APP, Constants.RESTAURANT_FOOD_ANDROID)      // if restaurant is closed then
         postParam.addProperty(Constants.LANGUAGE, Constants.EN)

       //  progress_bar_layout.visibility=View.VISIBLE
         call_category_menu=ApiCall.category_menu(postParam)
        callAPI(call_category_menu!!, object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val response= body as JsonObject
                val restaurantInfoModel = GsonBuilder().create().fromJson(response.toString(), RestaurantInfoModel::class.java)
                if (restaurantInfoModel.status) {
                    ui_model!!.category_menulist.value=restaurantInfoModel
                }
            }
            override fun onFail(error: Int) {
                binding.isUiprogress=false

                if(call_category_menu!!.isCanceled){
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
        mYourBroadcastReceiver = object  : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                loge(TAG,"broadcast receive...")
                if(intent!!.action == Constants.CARTCOUNT_BROADCAST){
                    restaurant_info.cartcnt=intent.extras.getInt(Constants.CARTCNT).toString()
                    restaurant_info.cartamt=intent.extras.getString(Constants.CARTAMT).toString()
                    loge(TAG,"new model cartcnt is..."+restaurant_info.cartcnt)
                    total_cartcnt = intent.extras.getInt(Constants.CARTCNT)
                    total_cartamt = intent.extras.getString(Constants.CARTAMT)
                    updatebatchcount(0)

                    // check if category screen is present then update therir batch count as well as this screen.
                    if((activity as HomeActivity).fragmentTab_is() == 1){
                        val fragment = ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getOrderFragment().childFragmentManager.findFragmentByTag(CategoryList.TAG)
                        if(fragment !=null){
                            (fragment as CategoryList).updatebatchcount(0)
                        }
                    }else{
                        val homefragment : HomeFragment = ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getHomeFragment()
                        val fragment =homefragment.childFragmentManager.findFragmentByTag(CategoryList.TAG)
                        if(fragment !=null){
                            (fragment as CategoryList).updatebatchcount(0)
                        }
                    }



                }
            }

        }
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(mYourBroadcastReceiver, IntentFilter(Constants.CARTCOUNT_BROADCAST))

    }

    fun updatebatchcount(count : Int){
        // this is update method will call in both category and details.
        try{
            total_cartcnt= total_cartcnt + count
            badge_notification_txt.visibility = if (total_cartcnt == 0) View.GONE else View.VISIBLE
            if((ui_model!!.category_menulist.value!!.is_restaurant_closed !=null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true) &&
                    (ui_model!!.category_menulist.value!!.pre_order !=null && ui_model!!.category_menulist.value!!.pre_order == false)){
                toolbar_badge_view.visibility= View.GONE

            }else{
                toolbar_badge_view.visibility= if (total_cartcnt == 0) View.GONE else View.VISIBLE
            }
            badge_notification_txt.text= total_cartcnt.toString()
            badge_countprice.text= BindDataUtils.convertCurrencyToDanish(total_cartamt)
        }catch (e : Exception){
            loge(TAG,"exception: - "+e.message)
        }

    }


    fun onBackpress() {
        // parentFragment!!.childFragmentManager.popBackStack()
        showTabBar(true)
        if(parentFragment is HomeFragment){
            val restaurantList= (parentFragment as HomeFragment).childFragmentManager.findFragmentByTag(RestaurantList.TAG)
            if(restaurantList !=null && canIrefreshpre_Function) (restaurantList as RestaurantList).fetch_ProductDetailList()
            canIrefreshpre_Function=false
            (parentFragment as HomeFragment).childFragmentManager.popBackStack()
        }else{
            (parentFragment as OrderFragment).childFragmentManager.popBackStack()
        }
    }

    /*   fun onFragmentResult(requestCode: Int, resultCode: Int) {
          loge("onActivityResult Detail fragment---",""+resultCode+" "+requestCode)
          // request : send code with request
          // result :  get code from target activity.
          val fragment= ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).getContainerFragment()
          if(fragment is OrderFragment){
              // If user is coming from reorder>>>>
              if(requestCode ==1 && resultCode == 1 && TransactionStatus.moveonsearch){
                  // back press from transaction success and continue.
                  TransactionStatus.moveonsearch=false
                  fragment.popAllFragment()
              }
          }else{
              // If user is coming from Homecontainer >>>>
              if(requestCode ==1 && resultCode == 1 && TransactionStatus.moveonsearch){
                  // back press from transaction success and continue.
                  TransactionStatus.moveonsearch=false
                  val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
                  (fragmentof as HomeContainerFragment).getHomeFragment().popAllFragment()
              }

              else if(requestCode ==1 && resultCode == 2 && EpayFragment.moveonEpay ){
                  ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(2)
              }
          }
      }*/

    fun setPalette() {

        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.banner)
        Palette.from(bitmap).generate(object : Palette.PaletteAsyncListener {
            override fun onGenerated(palette: Palette) {

                val vibrant = palette.vibrantSwatch

                if (vibrant != null) {
                    val mutedColor = palette.vibrantSwatch!!.getRgb()
                    collapse_toolbar.setBackgroundColor(mutedColor);
                    collapse_toolbar.setStatusBarScrimColor(palette.getDarkMutedColor(mutedColor));
                    collapse_toolbar.setContentScrimColor(palette.getMutedColor(mutedColor));

                } else {

                    collapse_toolbar.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white));
                    collapse_toolbar.setStatusBarScrimColor(ContextCompat.getColor(context!!, R.color.white))
                    collapse_toolbar.setContentScrimColor(ContextCompat.getColor(context!!, R.color.white))
                }

            }
        })


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

        ui_model?.let {
            ViewModelProviders.of(this).get(DetailsFragment.UIModel::class.java).category_menulist.removeObservers(this@DetailsFragment)
        }

        call_category_menu?.let {
            it.cancel()
        }

        if(::mYourBroadcastReceiver.isInitialized){
            LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(mYourBroadcastReceiver);
        }

        super.onDestroyView()

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

     data class RestaurantInfoModel (val msg: String? = null,
                                    val cartcnt: String = "",
                                    val is_user_deleted: Boolean = false,
                                    val is_restaurant_closed: Boolean? = null,
                                    val pre_order: Boolean? = null,
                                    val image_path: String = "",
                                    val product_image_thumbnail_path: String = "",
                                    val time: String = "",
                                    val menu: ArrayList<MenuListItem>? = null,
                                    val restaurant_info: Restaurant? =null ,
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
            Log.e(TAG,"click ---")
            detailsfragment.tapOnRating()
        }


    }

    private fun tapOnRating() {

        if(ui_model!!.category_menulist.value!!.is_restaurant_closed !=null && ui_model!!.category_menulist.value!!.is_restaurant_closed == true ){
            viewpager.setCurrentItem(0,true)
        }else{
            viewpager.setCurrentItem(1,true)
        }

    }




}





