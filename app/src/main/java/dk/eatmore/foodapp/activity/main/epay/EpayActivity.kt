package dk.eatmore.foodapp.activity.main.epay

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.transition.*
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.activity.main.epay.fragment.AddCart
import dk.eatmore.foodapp.utils.BaseActivity
import kotlinx.android.synthetic.main.activity_epay.*
import kotlinx.android.synthetic.main.toolbar.*

class EpayActivity : BaseActivity() {

    var transition : Transition?=null
    private lateinit var addcart_fragment: AddCart


    companion object {
        val TAG="EpayActivity"
        fun newInstance() : EpayActivity {
            return EpayActivity()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epay)
        fullScreen()
        initView(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition = buildEnterTransition()
            window.enterTransition = transition
            window.returnTransition=buildReturnTransition()
        }

    }


    private fun initView(savedInstanceState: Bundle?) {

        if(savedInstanceState==null){
            // if you not take in this condition than if you change orientation then fragment added again and again.
        }
        else{
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) toolbar.elevation =0.0f
        DrawableCompat.setTint(ContextCompat.getDrawable(this,R.drawable.close)!!, ContextCompat.getColor(this, R.color.theme_color));
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.close))
        txt_toolbar.text="Basket"
        toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        epay_continue_btn.setOnClickListener{
            addcart_fragment = AddCart.newInstance()
            addFragment(R.id.epay_container, addcart_fragment, AddCart.TAG, true)

        }


    }

    override fun onBackPressed() {
        DrawableCompat.setTint(ContextCompat.getDrawable(this,R.drawable.close)!!, ContextCompat.getColor(this, R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            finishAfterTransition()
        else
            finish()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildEnterTransition(): Transition {
        val enterTransition = Explode()
        enterTransition.setDuration(300)
        return enterTransition
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildReturnTransition(): Transition {
        val enterTransition = Fade()
        enterTransition.setDuration(300)
        return enterTransition
    }

}
