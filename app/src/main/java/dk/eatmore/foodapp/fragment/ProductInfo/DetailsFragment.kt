package dk.eatmore.foodapp.fragment.ProductInfo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.fragment.Dashboard.HomeFragment
import dk.eatmore.foodapp.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_details.*
import android.support.v4.content.ContextCompat
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v7.graphics.Palette
import android.util.Log
import kotlinx.android.synthetic.main.test_two.*


class DetailsFragment : BaseFragment() {

    lateinit var clickEvent : HomeFragment.MyClickHandler
    private  var mAdapter: OrderListAdapter?=null
    var adapter: ViewPagerAdapter? = null


    companion object {

        val TAG= "DetailsFragment"
        fun newInstance() : DetailsFragment {
            return DetailsFragment()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         return inflater.inflate(getLayout(), container, false)

        //binding= DataBindingUtil.inflate(inflater,getLayout(),container,false)
        //return binding.root

    }

    override fun getLayout(): Int {
        return R.layout.fragment_details
    }



    override fun initView(view: View?, savedInstanceState: Bundle?) {
        toolbar.setNavigationIcon(ContextCompat.getDrawable(context!!,R.drawable.close))
        toolbar.setNavigationOnClickListener{ loge(TAG,"on back press") }
        adapter = ViewPagerAdapter(childFragmentManager)
        adapter!!.addFragment(Menu(), getString(R.string.menu))
        adapter!!.addFragment(Rating(), getString(R.string.rating))
        adapter!!.addFragment(Info(), getString(R.string.info))
        viewpager.offscreenPageLimit=3
        viewpager.setAdapter(adapter)
        tabs.setupWithViewPager(viewpager)
        setPalette()






    }

    private fun setPalette() {

        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.banner)
        Palette.from(bitmap).generate(object : Palette.PaletteAsyncListener {
            override  fun onGenerated(palette: Palette) {

                val vibrant = palette.vibrantSwatch

                if (vibrant != null) {
                    val mutedColor = palette.vibrantSwatch!!.getRgb()
                    collapse_toolbar.setBackgroundColor(mutedColor);
                    collapse_toolbar.setStatusBarScrimColor(palette.getDarkMutedColor(mutedColor));
                    collapse_toolbar.setContentScrimColor(palette.getMutedColor(mutedColor));

                }else{

                    collapse_toolbar.setBackgroundColor(ContextCompat.getColor(context!!,R.color.divider_color));
                    collapse_toolbar.setStatusBarScrimColor(ContextCompat.getColor(context!!,R.color.divider_color))
                    collapse_toolbar.setContentScrimColor(ContextCompat.getColor(context!!,R.color.divider_color))
                }

            }
        })


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





}