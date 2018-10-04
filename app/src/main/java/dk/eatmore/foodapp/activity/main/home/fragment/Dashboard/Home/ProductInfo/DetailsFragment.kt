package dk.eatmore.foodapp.fragment.ProductInfo

import android.content.Intent
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
import dk.eatmore.foodapp.activity.main.home.fragment.ProductInfo.Info
import dk.eatmore.foodapp.activity.main.home.fragment.ProductInfo.Rating
import dk.eatmore.foodapp.utils.TransitionHelper
import android.support.design.widget.AppBarLayout
import android.view.animation.AlphaAnimation
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*
import kotlinx.android.synthetic.main.toolbar_plusone.*


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


    fun getParent() : Fragment? {
        return parentFragment
    }


    override fun initView(view: View?, savedInstanceState: Bundle?) {




        if(savedInstanceState == null){
         //   Glide.with(this).load(ContextCompat.getDrawable(context!!,R.drawable.food_slash)).into(details_back_img);
            detail_fab_btn.startAnimation(translateAnim(800f, 0f, 0f, 0f,700,true))
            detail_item_info.startAnimation(translateAnim(-800f, 0f, 0f, 0f,700,true))
           // DrawableCompat.setTint(ContextCompat.getDrawable(context!!,R.drawable.close)!!, ContextCompat.getColor(context!!, R.color.white));
            logd(DetailsFragment.TAG,"saveInstance NULL")
            img_toolbar_back.setImageResource(R.drawable.close)
            img_toolbar_back.setOnClickListener{
                  onBackpress()
            }
            adapter = ViewPagerAdapter(childFragmentManager)
            adapter!!.addFragment(Menu(), getString(R.string.menu))
            adapter!!.addFragment(Rating(), getString(R.string.rating))
            adapter!!.addFragment(Info(), getString(R.string.info))
            viewpager.offscreenPageLimit=3
            viewpager.setAdapter(adapter)
            tabs.setupWithViewPager(viewpager)
          //  setPalette()
            viewcart.setOnClickListener{
                val animation = TranslateAnimation(0f, 0f, 0f, 5f)
                animation.duration = 100
                animation.fillAfter = false
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation) {

                    }
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        val intent= Intent(activity, EpayActivity::class.java)
                        val pairs: Array<Pair<View, String>> = TransitionHelper.createSafeTransitionParticipants(activity!!, true)
                        val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, *pairs)
                        startActivity(intent, transitionActivityOptions.toBundle())
                    }
                })
                detail_fab_btn.startAnimation(animation)


            }

        }else{
            logd(DetailsFragment.TAG,"saveInstance NOT NULL")

        }
    }


fun onBackpress(){
    parentFragment!!.childFragmentManager.popBackStack()

}


    fun setPalette() {

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

                    collapse_toolbar.setBackgroundColor(ContextCompat.getColor(context!!,R.color.white));
                    collapse_toolbar.setStatusBarScrimColor(ContextCompat.getColor(context!!,R.color.white))
                    collapse_toolbar.setContentScrimColor(ContextCompat.getColor(context!!,R.color.white))
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





