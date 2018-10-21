package dk.eatmore.foodapp.fragment.ProductInfo

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.eatmore.foodapp.adapter.OrderListAdapter
import dk.eatmore.foodapp.fragment.Dashboard.Home.HomeFragment
import dk.eatmore.foodapp.utils.BaseFragment
import android.support.v4.content.ContextCompat
import android.graphics.BitmapFactory
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.graphics.Palette
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.EpayActivity
import dk.eatmore.foodapp.utils.TransitionHelper
import android.support.design.widget.AppBarLayout
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.animation.AlphaAnimation
import android.widget.TextView
import com.bumptech.glide.Glide
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Info
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Menu
import dk.eatmore.foodapp.activity.main.home.fragment.Dashboard.Home.ProductInfo.Rating
import dk.eatmore.foodapp.databinding.FragmentDetailBinding
import dk.eatmore.foodapp.databinding.InfoRestaurantBinding
import dk.eatmore.foodapp.fragment.HomeContainerFragment
import dk.eatmore.foodapp.model.home.Restaurant
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.toolbar_plusone.*


class DetailsFragment : BaseFragment() {

    lateinit var clickEvent: HomeFragment.MyClickHandler
    private var mAdapter: OrderListAdapter? = null
    var adapter: ViewPagerAdapter? = null
    private lateinit var binding: FragmentDetailBinding
    private  var mYourBroadcastReceiver: BroadcastReceiver?=null
    private lateinit var restaurant : Restaurant



    companion object {

        val TAG = "DetailsFragment"
        var totalcartcount :Int = 0
        fun newInstance(restaurant: Restaurant, status: String): DetailsFragment {


            val fragment = DetailsFragment()
            val bundle = Bundle()
            bundle.putString(Constants.STATUS, status)
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

    override fun getLayout(): Int {
        return R.layout.fragment_detail
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {


        if (savedInstanceState == null) {
            //   Glide.with(this).load(ContextCompat.getDrawable(context!!,R.drawable.food_slash)).into(details_back_img);
            restaurant = arguments?.getSerializable(Constants.RESTAURANT) as Restaurant
            totalcartcount = if(restaurant.cartcnt ==null || restaurant.cartcnt =="0") 0 else restaurant.cartcnt!!.toInt()
            broadcastEvent()
            setnotifybatch(0)
            loge(TAG,"card cnt is "+restaurant.cartcnt)
            val myclickhandler = MyClickHandler(this)
            binding.restaurant = restaurant
            binding.handler = myclickhandler
            //detail_fab_btn.startAnimation(translateAnim(800f, 0f, 0f, 0f, 700, true))
            //detail_item_info.startAnimation(translateAnim(-800f, 0f, 0f, 0f, 700, true))
            // DrawableCompat.setTint(ContextCompat.getDrawable(context!!,R.drawable.close)!!, ContextCompat.getColor(context!!, R.color.white));
            logd(DetailsFragment.TAG, "saveInstance NULL")
            Glide.with(context!!).load(restaurant.app_icon).into(imageview);
            img_toolbar_back.setImageResource(R.drawable.close)
            img_toolbar_back.setOnClickListener {
                onBackpress()
            }

            adapter = ViewPagerAdapter(childFragmentManager)
            when (arguments!!.getString(Constants.STATUS)) {
                getString(R.string.closed) -> {
                   // adapter!!.addFragment(Menu.newInstance(restaurant), getString(R.string.menu))
                    adapter!!.addFragment(Rating.newInstance(restaurant), getString(R.string.rating))
                    adapter!!.addFragment(Info.newInstance(restaurant), getString(R.string.info))
                    viewpager.offscreenPageLimit = 2
                }
                else -> {
                    adapter!!.addFragment(Menu.newInstance(restaurant), getString(R.string.menu))
                    adapter!!.addFragment(Rating.newInstance(restaurant), getString(R.string.rating))
                    adapter!!.addFragment(Info.newInstance(restaurant), getString(R.string.info))
                    viewpager.offscreenPageLimit = 3
                }
            }
            viewpager.setAdapter(adapter)
            tabs.setupWithViewPager(viewpager)
            //  setPalette()
            viewcart.setOnClickListener {
                val animation = TranslateAnimation(0f, 0f, 0f, 5f)
                animation.duration = 100
                animation.fillAfter = false
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation) {

                    }

                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        val intent = Intent(activity, EpayActivity::class.java)
                        val pairs: Array<Pair<View, String>> = TransitionHelper.createSafeTransitionParticipants(activity!!, true)
                        val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, *pairs)
                   //     startActivity(intent, transitionActivityOptions.toBundle())
                        startActivityForResult(intent,1)
                    }
                })
                detail_fab_btn.startAnimation(animation)


            }

        } else {
            logd(DetailsFragment.TAG, "saveInstance NOT NULL")

        }
    }

    fun setnotifybatch(count:Int){
        badge_notification_txt.text= (totalcartcount+count).toString()
        badge_notification_txt.visibility = if(totalcartcount == 0) View.GONE else View.VISIBLE

    }

    override fun onStart() {
        super.onStart()
        loge(TAG,"on start---")
        mYourBroadcastReceiver =  object  : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                loge(TAG,"broad cast event fire")

            }

        }

        LocalBroadcastManager.getInstance(activity!!).registerReceiver(mYourBroadcastReceiver!!,
                 IntentFilter("thisIsForMyFragment"));
    }

    override fun onStop() {
        super.onStop()
        loge(TAG,"on onStop---")
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(mYourBroadcastReceiver!!);

    }


    private fun broadcastEvent() {


    }


    fun onBackpress() {
        parentFragment!!.childFragmentManager.popBackStack()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loge("onActivityResult Detail fragment---",""+resultCode+" "+requestCode)
        // request : send code with request
        // result :  get code from target activity.
        if(requestCode ==1 && resultCode == AppCompatActivity.RESULT_OK){
            ((activity as HomeActivity).getHomeContainerFragment() as HomeContainerFragment).changeHomeview_page(2)
        }
    }





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
        when (arguments!!.getString(Constants.STATUS)) {
            getString(R.string.closed) -> {
                viewpager.setCurrentItem(0,true)
            }
            else -> {
                viewpager.setCurrentItem(1,true)
            }
        }

    }


}





