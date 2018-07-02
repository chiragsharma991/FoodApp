package dk.eatmore.foodapp.fragment

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.FindrestaurantBinding
import dk.eatmore.foodapp.utils.BaseFragment
import android.widget.Toast
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.model.LastOrder
import dk.eatmore.foodapp.model.User
import dk.eatmore.foodapp.rest.ApiCall
import kotlinx.android.synthetic.main.findrestaurant.*
import kotlinx.android.synthetic.main.fragment_home_container.*
import kotlinx.android.synthetic.main.layout_bottom_menu.*


class HomeContainerFragment : BaseFragment() {

    private lateinit var mAdapter: HomeFragmentViewPagerAdapter


    companion object {

        val TAG= "HomeContainerFragment"
        fun newInstance() : HomeContainerFragment {
            return HomeContainerFragment()
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_home_container
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)

    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {

        view_pager.apply {
            mAdapter = HomeFragmentViewPagerAdapter(childFragmentManager)
            adapter = mAdapter
            offscreenPageLimit = mAdapter.count
        }

        /**
         * First Time Initialize Menu
         */
        changeMenu(0)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
        rel_profile.setOnClickListener { view_pager.setCurrentItem(2, true) }



    }






    private fun changeMenu(position: Int) {
        if (position == 0) {
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
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        logd(TAG,"on destroy...")
    }

    override fun onDetach() {
        super.onDetach()
        logd(TAG,"on detech...")

    }

    override fun onPause() {
        super.onPause()
        logd(TAG,"on pause...")

    }



  internal  class HomeFragmentViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

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

        fun getHomeFragment(): HomeFragment =  mFragment[0] as HomeFragment

        fun getOrderFragment() : OrderFragment = mFragment[1] as OrderFragment

        fun getAccountFragment() : AccountFragment = mFragment[2] as AccountFragment

    }






}
