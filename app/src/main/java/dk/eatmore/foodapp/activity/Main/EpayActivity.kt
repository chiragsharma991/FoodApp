package dk.eatmore.foodapp.activity.Main

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.transition.*
import android.view.Gravity
import android.view.animation.BounceInterpolator
import dk.eatmore.foodapp.R
import dk.eatmore.foodapp.utils.BaseActivity
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*
import kotlinx.android.synthetic.main.toolbar.*

class EpayActivity : BaseActivity() {

    var transition : Transition?=null


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
        }  }

    override fun onBackPressed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            finishAfterTransition()
        else
            finish()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildEnterTransition(): Transition {
        val enterTransition = Explode()
        enterTransition.setDuration(500)
        return enterTransition
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buildReturnTransition(): Transition {
        val enterTransition = Fade()
        enterTransition.setDuration(500)
        return enterTransition
    }

}
