package dk.eatmore.foodapp.activity.main.home.intro

import android.databinding.DataBindingUtil
import android.os.Bundle
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.databinding.IntroSliderBinding
import dk.eatmore.foodapp.utils.BaseActivity
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.os.Build
import android.view.View
import kotlinx.android.synthetic.main.intro_slider.*
import android.databinding.adapters.TextViewBindingAdapter.setTextSize
import android.text.Html
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.widget.TextView
import eu.epay.library.MainActivity
import android.content.Intent
import android.databinding.adapters.TextViewBindingAdapter.setText
import android.graphics.Color
import android.support.v4.view.ViewPager
import android.view.WindowManager
import android.view.ViewGroup
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.support.v4.view.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dk.eatmore.foodapp.activity.main.home.HomeActivity
import dk.eatmore.foodapp.activity.main.home.intro.Intro_slider.MyViewPagerAdapter
import dk.eatmore.foodapp.storage.PreferenceUtil
import dk.eatmore.foodapp.utils.Constants
import dk.eatmore.foodapp.utils.DialogUtils
import kotlinx.android.synthetic.main.intro_slide1.*
import kotlinx.android.synthetic.main.intro_slide1.view.*
import java.util.ArrayList


class Intro_slider : BaseActivity(){

    private lateinit var binding : IntroSliderBinding
    private lateinit var slider_layouts: IntArray
    private lateinit var slider_list_img: IntArray
    private lateinit var slider_list_text: ArrayList<Intro_text>


    companion object {
        val TAG = "Intro_slider"
        fun newInstance(): Intro_slider {
            return Intro_slider()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.intro_slider)
        initView(savedInstanceState)
    }


    private fun initView(savedInstanceState: Bundle?) {

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        // layouts of all welcome sliders
        // add few more layouts if you want
        slider_layouts = intArrayOf(R.layout.intro_slide1, R.layout.intro_slide2, R.layout.intro_slide3)
        slider_list_img = intArrayOf(R.raw.slider_one, R.raw.slider_two, R.raw.slider_three)
        slider_list_text = ArrayList()
        slider_list_text.add(Intro_text(title = getString(R.string.slider_one_title) , desc = getString(R.string.slider_one_desc),btntitle = getString(R.string.slider_one_btntxt)))
        slider_list_text.add(Intro_text(title = getString(R.string.slider_two_title) , desc = getString(R.string.slider_two_desc),btntitle = getString(R.string.slider_two_btntxt)))
        slider_list_text.add(Intro_text(title = getString(R.string.slider_three_title) , desc = getString(R.string.slider_three_desc),btntitle = getString(R.string.slider_three_btntxt)))
        nxt_btn.setText(slider_list_text[0].btntitle)

        // adding bottom dots
        addBottomDots(0)

        // making notification bar transparent
        changeStatusBarColor()

        view_pager.apply {
            val myViewPagerAdapter = MyViewPagerAdapter()
            setAdapter(myViewPagerAdapter)
            addOnPageChangeListener(viewPagerPageChangeListener)
            offscreenPageLimit = myViewPagerAdapter.count
        }


/*
        btn_skip.setOnClickListener( { launchHomeScreen() })
*/

        nxt_btn.setOnClickListener( {
            // checking for last page
            // if last page home screen will be launched
            val current = getItem(+1)
            // 3<3
            if (current < slider_layouts.size) {
                // move to next screen
                view_pager.setCurrentItem(current)
            } else {
                launchHomeScreen()
            }
        })


    }

    //  viewpager change listener
    val viewPagerPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            if(position== 1){
                is_location_PermissionGranted()
            }else if(position ==2){
                DialogUtils.openDialogDefault(context = this@Intro_slider,btnNegative = "DENY",btnPositive = "ALLOW",
                        color = ContextCompat.getColor(this@Intro_slider, R.color.default_permission),msg ="Notification may include alerts,sound and icon badges.This can be configured in settings",
                        title = "EatMore Would Like To Send You Notifications",onDialogClickListener = object : DialogUtils.OnDialogClickListener{
                    override fun onPositiveButtonClick(position: Int) {

                    }
                    override fun onNegativeButtonClick() {
                    }
                })
            }

            // changing the next button text 'NEXT' / 'GOT IT' (2=2)
            if (position == slider_layouts.size - 1) {
                // last page. make button text to GOT IT
                nxt_btn.setText(getString(R.string.gotit))
             //   btn_skip.setVisibility(View.GONE)
            } else {
                // still pages are left
                nxt_btn.setText(slider_list_text[position].btntitle)
              //  btn_skip.setVisibility(View.VISIBLE)
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

        }

        override fun onPageScrollStateChanged(arg0: Int) {

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // result from permission only
        loge(TAG, "permission result---"+requestCode)
        when (requestCode) {
            1 -> {

                return
            }
        }
    }

    private fun getItem(i: Int): Int {
        return view_pager.getCurrentItem() + i
    }

    private fun launchHomeScreen() {
        PreferenceUtil.putValue(PreferenceUtil.CLOSE_INTRO_SLIDE,true)
        PreferenceUtil.save()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    override fun setVisible(visible: Boolean) {
        super.setVisible(visible)
    }

    private fun addBottomDots(currentPage: Int) {
        val dots = arrayOfNulls<TextView>(slider_layouts.size)

        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        layoutDots.removeAllViews()
        for (i in 0 until dots.size) {
            dots[i] = TextView(this)
            dots[i]!!.setText(Html.fromHtml("&#8226;"))
            dots[i]!!.setTextSize(35f)
            dots[i]!!.setTextColor(colorsInactive[1])
            layoutDots.addView(dots[i])
        }

        if (dots.size > 0)
            dots[currentPage]!!.setTextColor(colorsActive[1])
    }

    data class Intro_text (var title : String ,var btntitle : String , var desc : String )


    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(slider_layouts[position], container, false)
            Glide.with(this@Intro_slider).asGif()
                    .load(slider_list_img[position])
                    .into(view.imageview)
            view.text_header.text=slider_list_text[position].title
            view.text_desc.text=slider_list_text[position].desc

            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return slider_layouts.size
        }



        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }


        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

}