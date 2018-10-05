package dk.eatmore.foodapp.fragment.ProductInfo

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.menu_restaurant.*
import android.support.design.widget.TabLayout
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.model.User
import android.support.transition.*
import android.support.v4.content.ContextCompat
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.ProductInfo.SearchMenu
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.universalAdapter.RecyclerClickListner
import dk.eatmore.foodapp.adapter.universalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.RowMenuRestaurantBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.home.MenuListItem
import dk.eatmore.foodapp.model.home.ProductListModel
import dk.eatmore.foodapp.rest.ApiCall
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_details.*
import java.util.ArrayList


class Menu : BaseFragment(), RecyclerClickListner {


    private lateinit var binding: FragmentAccountContainerBinding
    private var mAdapter: UniversalAdapter<MenuListItem, RowMenuRestaurantBinding>? = null
    private lateinit var homeFragment: HomeFragment
    private val list = ArrayList<User>()
    private  var ui_model: UIModel?=null




    companion object {

        val TAG = "Menu"
        fun newInstance(): Menu {
            return Menu()
        }

    }


    override fun getLayout(): Int {
        return R.layout.menu_restaurant
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

        //   binding=DataBindingUtil.inflate(inflater,getLayout(),container,false)
       // return binding.root

    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState == null){
            if(ui_model == null)
            ui_model=createViewModel()
            logd(TAG,"saveInstance NULL")
            val fragmentof = (activity as HomeActivity).supportFragmentManager.findFragmentByTag(HomeContainerFragment.TAG)
            homeFragment=(fragmentof as HomeContainerFragment).getHomeFragment()
            menu_tabs.addTab(menu_tabs.newTab().setText("Delivery"))
            menu_tabs.addTab(menu_tabs.newTab().setText("PickUp"))
            menu_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    logd(TAG, menu_tabs.selectedTabPosition.toString())
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }
                override fun onTabSelected(tab: TabLayout.Tab?) {
                }
            })
            menu_search.setOnClickListener{


                    logd(TAG,"")
                    val fragment = SearchMenu.newInstance()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        /*             // 2. Shared Elements Transition
                                     val enterTransitionSet = TransitionSet()
                                     enterTransitionSet.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.move))
                                     enterTransitionSet.duration = 1000
                                     enterTransitionSet.startDelay = 300
                                     fragment.setSharedElementEnterTransition(enterTransitionSet)*/

                        // 3. Enter Transition for New Fragment
                        val enterFade = Fade()
                        enterFade.setStartDelay(0)
                        enterFade.setDuration(300)
                        fragment.setEnterTransition(enterFade)


                        homeFragment.childFragmentManager.beginTransaction().add(R.id.home_fragment_container,fragment,SearchMenu.TAG).addToBackStack(SearchMenu.TAG)
                                //      .addSharedElement(search_card, getString(R.string.transition_string))
                                .commit()
                    }
                    else {
                        homeFragment.addFragment(R.id.home_fragment_container,fragment, SearchMenu.TAG,false)
                    }


            }
            fetch_ProductList()

        }else{
            logd(TAG,"saveInstance NOT NULL")

        }

    }

    private fun createViewModel(): UIModel =
            ViewModelProviders.of(this).get(UIModel::class.java).apply {
                productList.observe(this@Menu, Observer<ArrayList<MenuListItem>> {
                    refreshUI()
                })
            }



    private fun refreshUI() {
        //       val myclickhandler = Profile.MyClickHandler(this)
//        val xml_profile = ui_model.getUIModel().value
//        binding.xmlProfile = xml_profile
//        binding.handlers=myclickhandler



        mAdapter = UniversalAdapter(context!!, ui_model!!.productList.value, R.layout.row_menu_restaurant, object : RecyclerCallback<RowMenuRestaurantBinding, MenuListItem> {
            override fun bindData(binder: RowMenuRestaurantBinding, model: MenuListItem) {
                setRecyclerData(binder, model)
            }
        })
        recycler_view_menu.layoutManager = LinearLayoutManager(getActivityBase())
        recycler_view_menu.adapter = mAdapter


    }



    private fun fetch_ProductList() {

        callAPI(ApiCall.getProductList(
                r_token = Constants.R_TOKEN,
                r_key = Constants.R_KEY,
                customer_id = "1706"
        ), object : BaseFragment.OnApiCallInteraction {

            override fun <T> onSuccess(body: T?) {
                val productlistmodel= body as ProductListModel
                if (productlistmodel.status) {
                    loge(TAG," menu list size"+""+productlistmodel.menu!!.get(1).c_name+" "+productlistmodel.menu.get(1).product_list!!.size)
                    ui_model!!.productList.value=productlistmodel.menu
                }
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
                //showProgressDialog()
            }
        })


    }

    override fun<T> onClick(model: T?) {

        val data= model as MenuListItem
        (parentFragment as DetailsFragment).appbar.setExpanded(false,true)
        (parentFragment as DetailsFragment).collapse_toolbar.setBackgroundColor(ContextCompat.getColor(context!!,R.color.white));
        (parentFragment as DetailsFragment).collapse_toolbar.setStatusBarScrimColor(ContextCompat.getColor(context!!,R.color.white))
        (parentFragment as DetailsFragment).collapse_toolbar.setContentScrimColor(ContextCompat.getColor(context!!,R.color.white))


        val fragment = CategoryList.newInstance()
        var enter :Slide?=null
        val bundle = Bundle()
        bundle.putString("TITLE",data.c_name)
        bundle.putSerializable("MenuListItem", data)
        fragment.arguments=bundle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(300)
            enter.slideEdge = Gravity.RIGHT
            val changeBoundsTransition :ChangeBounds = ChangeBounds()
            changeBoundsTransition.duration = 300
            fragment.sharedElementEnterTransition=changeBoundsTransition
            fragment.enterTransition=enter
        }
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

     class UIModel : ViewModel() {

        var productList = MutableLiveData<ArrayList<MenuListItem>>()


    }



}



