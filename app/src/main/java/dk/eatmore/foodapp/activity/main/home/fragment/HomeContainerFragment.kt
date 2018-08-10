package dk.eatmore.foodapp.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Account.AccountFragment
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Order.OrderFragment
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_home_container.*
import kotlinx.android.synthetic.main.layout_bottom_menu.*


class HomeContainerFragment : BaseFragment() {

    private lateinit var mAdapter: HomeFragmentViewPagerAdapter


    companion object {

        val TAG = "HomeContainerFragment"
        fun newInstance(): HomeContainerFragment {
            return HomeContainerFragment()
        }


    }

    override fun getLayout(): Int {
        return R.layout.fragment_home_container
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(getLayout(), container, false)

    }

    fun getContainerFragment(): BaseFragment {
        return mAdapter.getContainerFragment(view_pager.currentItem) as BaseFragment
    }

    fun getHomeFragment(): HomeFragment {
        return mAdapter.getHomeFragment()
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {

        if (savedInstanceState == null) {
            logd(TAG, "saveInstance NULL")

            view_pager.apply {
                mAdapter = HomeFragmentViewPagerAdapter(childFragmentManager)
                adapter = mAdapter
                offscreenPageLimit = mAdapter.count
            }

            /**
             * First Time Initialize Menu
             */
           // changeMenu(0)
           /* view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    changeMenu(position)
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }
            })
            rel_home.setOnClickListener { view_pager.setCurrentItem(0, true) }
            rel_order.setOnClickListener { view_pager.setCurrentItem(1, true) }
            rel_profile.setOnClickListener { view_pager.setCurrentItem(2, true) }*/

            setupBottomNavStyle()
          //  createFakeNotification()
            addBottomNavigationItems()
            bottom_navigationbar.setCurrentItem(0)


        } else {
            logd(TAG, "saveInstance NOT NULL")

        }


        bottom_navigationbar.setOnTabSelectedListener(
                object : AHBottomNavigation.OnTabSelectedListener {
                    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
                        //                fragment.updateColor(ContextCompat.getColor(MainActivity.this, colors[position]));

                        view_pager.setCurrentItem(position, true)
                        return true
                    }
                })


    }

    private fun createFakeNotification() {
        Handler().postDelayed({
            val notification = AHNotification.Builder()
                    .setText("1")
                    .setBackgroundColor(Color.YELLOW)
                    .setTextColor(Color.BLACK)
                    .build()
            // Adding notification to last item.

            bottom_navigationbar.setNotification(notification, bottom_navigationbar.getItemsCount() - 1)

          //  notificationVisible = true
        }, 1000)
    }



    private fun setupBottomNavStyle() {
        /*
        Set Bottom Navigation colors. Accent color for active item,
        Inactive color when its view is disabled.

        Will not be visible if setColored(true) and default current item is set.
         */
        bottom_navigationbar.setDefaultBackgroundColor(Color.WHITE)
        bottom_navigationbar.setAccentColor(ContextCompat.getColor(context!!, R.color.red))
        bottom_navigationbar.setInactiveColor(ContextCompat.getColor(context!!, R.color.bottomtab_item_resting))

        // Colors for selected (active) and non-selected items.
        bottom_navigationbar.setColoredModeColors(Color.WHITE, ContextCompat.getColor(context!!, R.color.bottomtab_item_resting))

        //  Enables Reveal effect
       // bottom_navigationbar.setColored(true)

        //  Displays item Title always (for selected and non-selected items)
        bottom_navigationbar.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE)
        
    }


    private fun addBottomNavigationItems() {
        val item1 = AHBottomNavigationItem("Home", R.drawable.ic_bottom_home)
        val item2 = AHBottomNavigationItem("Ordered", R.drawable.ic_bottom_fav)
        val item3 = AHBottomNavigationItem("Profile", R.drawable.ic_bottom_profile)

        bottom_navigationbar.addItem(item1)
        bottom_navigationbar.addItem(item2)
        bottom_navigationbar.addItem(item3)
    }


    private fun changeMenu(position: Int) {
  /*      if (position == 0) {
            img_home.setColorFilter(ContextCompat.getColor(getActivityBase(), R.color.theme_color), android.graphics.PorterDuff.Mode.SRC_IN);
            view_divider_home.visibility = View.VISIBLE
        } else {
            img_home.setColorFilter(ContextCompat.getColor(getActivityBase(), R.color.colorLanguageUnselectedGrey), android.graphics.PorterDuff.Mode.SRC_IN);
            view_divider_home.visibility = View.INVISIBLE
        }

        if (position == 1) {
            img_fav.setColorFilter(ContextCompat.getColor(getActivityBase(), R.color.theme_color), android.graphics.PorterDuff.Mode.SRC_IN);
            view_divider_fav.visibility = View.VISIBLE
        } else {
            img_fav.setColorFilter(ContextCompat.getColor(getActivityBase(), R.color.colorLanguageUnselectedGrey), android.graphics.PorterDuff.Mode.SRC_IN);
            view_divider_fav.visibility = View.INVISIBLE
        }

        if (position == 2) {
            img_profile.setColorFilter(ContextCompat.getColor(getActivityBase(), R.color.theme_color), android.graphics.PorterDuff.Mode.SRC_IN);
            view_divider_profile.visibility = View.VISIBLE
        } else {
            img_profile.setColorFilter(ContextCompat.getColor(getActivityBase(), R.color.colorLanguageUnselectedGrey), android.graphics.PorterDuff.Mode.SRC_IN);
            view_divider_profile.visibility = View.INVISIBLE
        }*/


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


    internal class HomeFragmentViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        private val mFragment: Array<Fragment> = arrayOf(HomeFragment(), OrderFragment(), AccountFragment())

        override fun getItem(position: Int): Fragment {
            return mFragment[position]
        }

        override fun getCount(): Int {
            return mFragment.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return ""
        }

        fun getContainerFragment(position: Int): Fragment {
            return mFragment[position]
        }

        fun getHomeFragment(): HomeFragment = mFragment[0] as HomeFragment

        fun getOrderFragment(): OrderFragment = mFragment[1] as OrderFragment

        fun getAccountFragment(): AccountFragment = mFragment[2] as AccountFragment

    }


}
