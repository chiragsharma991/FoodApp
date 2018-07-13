package dk.eatmore.foodapp.fragment.ProductInfo

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.databinding.FragmentAccountContainerBinding
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_order_container.*
import kotlinx.android.synthetic.main.menu_restaurant.*
import android.support.design.widget.TabLayout
import android.support.v7.widget.RecyclerView
import android.transition.ChangeBounds
import android.transition.Slide
import android.view.Gravity
import android.widget.Toast
import dk.eatmore.foodapp.adapter.GenericAdapter
import dk.eatmore.foodapp.adapter.ViewHolderFactory
import dk.eatmore.foodapp.fragment.Dashboard.HomeFragment
import dk.eatmore.foodapp.model.MenuRestaurant
import dk.eatmore.foodapp.model.User
import kotlinx.android.synthetic.main.rating_restaurant.*
import android.annotation.TargetApi
import android.support.transition.*
import android.support.v4.content.ContextCompat
import dk.eatmore.foodapp.activity.Main.HomeActivity
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerCallback
import dk.eatmore.foodapp.adapter.UniversalAdapter.RecyclerClickInterface
import dk.eatmore.foodapp.adapter.UniversalAdapter.UniversalAdapter
import dk.eatmore.foodapp.databinding.RowCategoryListBinding
import dk.eatmore.foodapp.databinding.RowMenuRestaurantBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import kotlinx.android.synthetic.main.category_list.*
import kotlinx.android.synthetic.main.fragment_details.*
import java.util.ArrayList
import kotlin.math.log


class Menu : BaseFragment(), RecyclerClickInterface {

    private lateinit var binding: FragmentAccountContainerBinding
    private var mAdapter: UniversalAdapter<User, RowMenuRestaurantBinding>? = null
    private lateinit var homeFragment: HomeFragment
    private val list = ArrayList<User>()




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

            fillData()
            mAdapter = UniversalAdapter(context!!, list, R.layout.row_menu_restaurant, object : RecyclerCallback<RowMenuRestaurantBinding, User> {
                override fun bindData(binder: RowMenuRestaurantBinding, model: User) {
                    setRecyclerData(binder, model)
                }
            })
            recycler_view_menu.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view_menu.adapter = mAdapter




/*

            val mAdapter=object : GenericAdapter<Any>(list){
                override fun getLayoutId(position: Int, obj: Any): Int {
                    return when(obj){
                        is MenuRestaurant -> R.layout.row_menu_restaurant
                        else -> R.layout.row_rating_list
                    }
                }
                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return ViewHolderFactory.create(view,viewType,object : ViewHolderFactory.Clicklistner{
                        override fun clickOn() {

                            (parentFragment as DetailsFragment).appbar.setExpanded(false,true)
                            (parentFragment as DetailsFragment).collapse_toolbar.setBackgroundColor(ContextCompat.getColor(context!!,R.color.white));
                            (parentFragment as DetailsFragment).collapse_toolbar.setStatusBarScrimColor(ContextCompat.getColor(context!!,R.color.white))
                            (parentFragment as DetailsFragment).collapse_toolbar.setContentScrimColor(ContextCompat.getColor(context!!,R.color.white))


                            val fragment = CategoryList.newInstance()
                            var enter :Slide?=null
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                enter = Slide()
                                enter.setDuration(500)
                                enter.slideEdge = Gravity.RIGHT
                                val changeBoundsTransition :ChangeBounds = ChangeBounds()
                                changeBoundsTransition.duration = 500
                                fragment.sharedElementEnterTransition=changeBoundsTransition
                                fragment.enterTransition=enter


                            }
                            homeFragment.addFragment(R.id.home_fragment_container,fragment,CategoryList.TAG)

                        }
                    })
                }
            }
            recycler_view_menu.layoutManager = LinearLayoutManager(getActivityBase())
            recycler_view_menu.adapter = mAdapter
*/



            menu_search.setOnClickListener{


                if(fragmentof !=null){
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
                        homeFragment.addFragment(R.id.home_fragment_container,fragment,SearchMenu.TAG)
                    }

                }

            }


        }else{
            logd(TAG,"saveInstance NOT NULL")

        }

    }

    override fun onClick(user: User) {

        (parentFragment as DetailsFragment).appbar.setExpanded(false,true)
        (parentFragment as DetailsFragment).collapse_toolbar.setBackgroundColor(ContextCompat.getColor(context!!,R.color.white));
        (parentFragment as DetailsFragment).collapse_toolbar.setStatusBarScrimColor(ContextCompat.getColor(context!!,R.color.white))
        (parentFragment as DetailsFragment).collapse_toolbar.setContentScrimColor(ContextCompat.getColor(context!!,R.color.white))


        val fragment = CategoryList.newInstance()
        var enter :Slide?=null

        val bundle = Bundle()
        bundle.putString("TITLE",user.name)
        fragment.arguments=bundle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enter = Slide()
            enter.setDuration(500)
            enter.slideEdge = Gravity.RIGHT
            val changeBoundsTransition :ChangeBounds = ChangeBounds()
            changeBoundsTransition.duration = 500
            fragment.sharedElementEnterTransition=changeBoundsTransition
            fragment.enterTransition=enter


        }
        homeFragment.addFragment(R.id.home_fragment_container,fragment,CategoryList.TAG)

    }


    private fun setRecyclerData(binder: RowMenuRestaurantBinding, model: User) {
        binder.user=model
        binder.handler=this
    }

    private fun fillData() {
        val user1 = User()
        user1.name="Pizza"
        list.add(user1)

        val user2 = User()
        user2.name="Coca-Cola"
        list.add(user2)

        val user3 = User()
        user3.name="Deep-Pan"
        list.add(user3)

        val user4 = User()
        user4.name="Brown stick"
        list.add(user4)

        val user5 = User()
        user5.name="Choco Moko"
        list.add(user4)


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



